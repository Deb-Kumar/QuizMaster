package com.example.onlinequizapp.ui.teacher.create

import com.example.onlinequizapp.domain.model.QuestionDraft
import com.example.onlinequizapp.domain.model.QuestionType

/**
 * Utility parser that converts raw copied/pasted quiz text into structured [QuestionDraft] items.
 *
 * Fully supports all answer formats and question types:
 *
 * Format 1: Inline correct answer markers on option lines:
 *     1.
 *     What is the chemical symbol for the element gold?
 *     A) Ag
 *     B) Fe
 *     C) Au (Correct Answer)
 *     D) Pb
 *
 * Format 2: Consolidated answers list at the bottom:
 *     Correct Answer: 1.Au(C), 2.Canada(A), 3.1989(D)
 *     OR on separate lines:
 *     Correct Answer:
 *     1.Au(C)
 *     2.Canada(A)
 *     3.1989(D)
 *
 * Format 3: Per-question answer line right after the options:
 *     A) Ag
 *     B) Fe
 *     C) Au
 *     D) Pb
 *     Correct Answer:Au(C) or Au(c) or Canada(A) or 1989(D)
 *
 * Format 4: Per-question answer line with option letter prefix:
 *     A) Ag
 *     B) Fe
 *     C) Au
 *     D) Pb
 *     Correct Answer:A)Au or Correct Answer: A) Canada or Correct answer: d)1989
 * Supports option formats in:
 * - Capital letters: A) B) C) D) or A. B. C. D.
 * - Small letters: a) b) c) d) or a. b. c. d. or (a) (b) (c) (d)
 * - Roman numerals (capital): I) II) III) IV) or I. II. III. IV. or (I) (II) (III) (IV)
 * - Roman numerals (small): i) ii) iii) iv) or i. ii. iii. iv. or (i) (ii) (iii) (iv)
 *
 * Automatically marks correct answer(s) and classifies questions into
 * [QuestionType.SINGLE] or [QuestionType.MULTIPLE] accordingly.
 */
object BulkQuestionParser {

    private const val OPTION_LABEL_PATTERN = """(?:[A-Fa-f]|VI|IV|III|II|I|V)"""

    private val ROMAN_MAP = mapOf(
        "I" to "A",
        "II" to "B",
        "III" to "C",
        "IV" to "D",
        "V" to "E",
        "VI" to "F"
    )

    fun normalizeOptionKey(raw: String): String {
        val upper = raw.trim().uppercase()
        return ROMAN_MAP[upper] ?: upper
    }

    private val QUESTION_NUM_ONLY_REGEX = Regex(
        """^\s*(?:\*\*)?(?:(?:Q|Question)\s*)?(\d+)\\?[\.:\)]?(?:\*\*)?\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val QUESTION_WITH_TEXT_REGEX = Regex(
        """^\s*(?:\*\*)?(?:(?:Q|Question)\s*\d+\\?[\.:\)]?|\(?\d+\)?\\?[\.:]?|\d+\\?[\.:\)])(?:\*\*)?\s+(.+)$""",
        RegexOption.IGNORE_CASE
    )

    private val OPTION_REGEX = Regex(
        """^\s*(?:[\*\-•]\s*)?(?:\*\*)?(?:\(?($OPTION_LABEL_PATTERN)[\)\.\:\-]|\[($OPTION_LABEL_PATTERN)\])(?:\*\*)?\s*(.*)$""",
        RegexOption.IGNORE_CASE
    )

    private val MULTI_OPTION_REGEX = Regex(
        """(?:\b|^)(?:[\*\-•]\s*)?(?:\*\*)?(?:\(?($OPTION_LABEL_PATTERN)[\)\.\:\-]|\[($OPTION_LABEL_PATTERN)\])(?:\*\*)?\s*(.*?)(?=(?:\s+(?:[\*\-•]\s*)?(?:\*\*)?(?:\(?$OPTION_LABEL_PATTERN[\)\.\:\-]|\[$OPTION_LABEL_PATTERN\]))|$)""",
        RegexOption.IGNORE_CASE
    )

    private val INLINE_CORRECT_REGEX = Regex(
        """(?:\s*(?:[\(\[]\s*(?:Correct(?:\s*Answer)?|Answer|Ans|Right)\s*[\)\]]|[-–—]\s*(?:Correct(?:\s*Answer)?|Answer)|✓|\*correct\*))\s*$""",
        RegexOption.IGNORE_CASE
    )

    private val PER_QUESTION_ANSWER_REGEX = Regex(
        """^\s*(?:[\*\-•]\s*)?(?:\*\*)?(?:(?:Correct\s*)?Ans(?:wer)?|Correct|Key)\s*[:\-]\s*(.+)$""",
        RegexOption.IGNORE_CASE
    )

    private val CONSOLIDATED_HEADER_START_REGEX = Regex(
        """^\s*(?:#{1,6}\s*)?(?:(?:Correct\s*)?Answers?(?:\s*Key)?|Answer\s*Key)\s*[:\-]?(?:\s*.*)?$""",
        RegexOption.IGNORE_CASE
    )

    private val CONSOLIDATED_ENTRY_REGEX = Regex(
        """(?:\b|^|[\s,;\n])(\d+)\s*[\.:\)\-]\s*([A-Za-z0-9_\-\.\s\(\)]+?)(?=(?:,\s*\d+\s*[\.:\)\-]|\n\s*\d+\s*[\.:\)\-]|,|\n|$))"""
    )

    private val TABLE_ROW_REGEX = Regex(
        """^\s*\|?\s*(\d+)\s*\|\s*([^|]+?)\s*(?:\|\s*([^|]*?)\s*)?\|?\s*$"""
    )

    private val HINT_REGEX = Regex(
        """^\s*Hint\s*[:\-]\s*(.+)$""",
        RegexOption.IGNORE_CASE
    )

    private val EXPLANATION_REGEX = Regex(
        """^\s*Explanation\s*[:\-]\s*(.+)$""",
        RegexOption.IGNORE_CASE
    )

    /**
     * Checks if the given text looks like bulk questions (either > 1 question,
     * or 1 question with 2+ detected options).
     */
    fun isBulkText(text: String): Boolean {
        if (text.isBlank() || text.length < 10) return false
        val parsed = parse(text)
        return parsed.size > 1 || (parsed.size == 1 && parsed[0].options.count { it.isNotBlank() } >= 2)
    }

    /**
     * Parses the raw pasted text into a list of [QuestionDraft] objects.
     */
    fun parse(text: String): List<QuestionDraft> {
        if (text.isBlank()) return emptyList()

        // 1. Pre-process text to extract any consolidated answer section (Format 2)
        val (cleanedText, consolidatedAnswers) = extractConsolidatedAnswers(text)

        val lines = cleanedText.lines()
        val hasNumberedQuestions = lines.any {
            val t = it.trim()
            QUESTION_NUM_ONLY_REGEX.matchEntire(t) != null || QUESTION_WITH_TEXT_REGEX.matchEntire(t) != null
        }

        val rawQuestions = mutableListOf<RawQuestion>()
        var current: RawQuestion? = null
        var expectingPromptNextLine = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isBlank()) continue

            // If the document has numbered questions, skip all preamble/headers before question 1
            if (hasNumberedQuestions && current == null) {
                val isQNumOnly = QUESTION_NUM_ONLY_REGEX.matchEntire(line) != null
                val isQWithText = QUESTION_WITH_TEXT_REGEX.matchEntire(line) != null
                if (!isQNumOnly && !isQWithText) {
                    continue
                }
            } else if (current == null && (line.startsWith("#") || line.startsWith(">") || line.startsWith("---") || line.startsWith("***"))) {
                continue
            }

            // 1. Line is just a question number (e.g. "1.", "2.", "Q1")
            val numOnlyMatch = QUESTION_NUM_ONLY_REGEX.matchEntire(line)
            if (numOnlyMatch != null) {
                current = RawQuestion()
                rawQuestions.add(current)
                expectingPromptNextLine = true
                continue
            }

            // 2. Line is a question number WITH text (e.g. "1. What is gold?")
            val numWithTextMatch = QUESTION_WITH_TEXT_REGEX.matchEntire(line)
            if (numWithTextMatch != null) {
                current = RawQuestion()
                rawQuestions.add(current)
                val promptText = numWithTextMatch.groupValues[1].trim()
                    .removeSuffix("**").removeSuffix("__").trim()
                if (promptText.isNotBlank()) {
                    current.promptLines.add(promptText)
                }
                expectingPromptNextLine = false
                continue
            }

            // 3. Line is per-question Answer indicator (Format 3 & Format 4: "Correct Answer:Au(C)" or "Correct Answer: A) Gold, C) Silver")
            val ansMatch = PER_QUESTION_ANSWER_REGEX.matchEntire(line)
            if (ansMatch != null) {
                val fullAns = ansMatch.groupValues[1].trim()
                val noteRegex = Regex("""\s*(?:\*\s*)?\((.+?)\)(?:\s*\*)*\s*$""")
                val noteMatch = noteRegex.find(fullAns)
                if (noteMatch != null && current?.explanation.isNullOrBlank()) {
                    current?.explanation = noteMatch.groupValues[1].trim()
                }
                val baseAns = (if (noteMatch != null) fullAns.substring(0, noteMatch.range.first).trim() else fullAns)
                    .removeSuffix("**").removeSuffix("__").trim()
                current?.answerText = baseAns
                continue
            }

            // 4. Line is Hint indicator
            val hintMatch = HINT_REGEX.matchEntire(line)
            if (hintMatch != null) {
                current?.hint = hintMatch.groupValues[1].trim()
                continue
            }

            // 5. Line is Explanation indicator
            val expMatch = EXPLANATION_REGEX.matchEntire(line)
            if (expMatch != null) {
                current?.explanation = expMatch.groupValues[1].trim()
                continue
            }

            // 6. Line is an Option or multiple Options (e.g. "A) Mumbai B) New Delhi C) Kolkata D) Chennai")
            val multiMatches = MULTI_OPTION_REGEX.findAll(line).toList()
            if (multiMatches.isNotEmpty() && (multiMatches.size > 1 || OPTION_REGEX.matchEntire(line) != null)) {
                for (optMatch in multiMatches) {
                    val rawLabel = optMatch.groupValues[1].ifEmpty { optMatch.groupValues[2] }
                    val letter = normalizeOptionKey(rawLabel)
                    val rawOptText = optMatch.groupValues[3].trim()

                    // Check for inline correct answer marker (Format 1: "Au (Correct Answer)")
                    val isInlineCorrect = INLINE_CORRECT_REGEX.containsMatchIn(rawOptText)
                    val cleanOptText = rawOptText.replace(INLINE_CORRECT_REGEX, "")
                        .removeSurrounding("**").removeSurrounding("__").trim()

                    // If letter is "A" and current question already has options, a new unnumbered question started
                    if (letter == "A" && current != null && current.options.isNotEmpty()) {
                        current = RawQuestion()
                        rawQuestions.add(current)
                    }

                    if (current == null) {
                        current = RawQuestion()
                        rawQuestions.add(current)
                    }

                    current.options[letter] = cleanOptText
                    if (isInlineCorrect) {
                        current.inlineCorrectAnswers.add(letter)
                    }
                }
                expectingPromptNextLine = false
                continue
            }

            // 7. Plain text line (Prompt or multi-line continuation)
            if (current == null) {
                current = RawQuestion()
                rawQuestions.add(current)
                current.promptLines.add(line)
                expectingPromptNextLine = false
            } else if (expectingPromptNextLine) {
                current.promptLines.add(line)
                expectingPromptNextLine = false
            } else if (current.options.isEmpty()) {
                current.promptLines.add(line)
            } else {
                // Already has options, so this is the prompt of a subsequent unnumbered question
                current = RawQuestion()
                rawQuestions.add(current)
                current.promptLines.add(line)
            }
        }

        // Apply consolidated answers (Format 2) if present
        if (consolidatedAnswers.isNotEmpty()) {
            rawQuestions.forEachIndexed { idx, q ->
                val qNumber = idx + 1
                val ans = consolidatedAnswers[qNumber]
                if (!ans.isNullOrBlank()) {
                    q.answerText = ans
                }
            }
        }

        return rawQuestions.mapNotNull { it.toQuestionDraft() }
    }

    /**
     * Resolves the set of correct answer letters from raw answer text.
     * Handles formats like "Au(C)", "Au(c)", "Canada(A)", "1989(D)", "C", "A, C", "Au", etc.
     */
    fun resolveCorrectAnswers(rawAnswer: String, options: Map<String, String>): Set<String> {
        val trimmed = rawAnswer.trim()
            .removeSurrounding("**")
            .removeSurrounding("__")
            .trimEnd('*', ' ', '\t')
            .trim()
        if (trimmed.isBlank()) return emptySet()

        val correctLetters = mutableSetOf<String>()

        // 1. Letters / Roman numerals in parentheses or brackets, e.g. "Au(C)", "Au(c)", "Au(iii)", "(III)"
        val parenTokenRegex = Regex(
            """[\(\[]\s*($OPTION_LABEL_PATTERN)\s*[\)\]]""",
            RegexOption.IGNORE_CASE
        )
        val parenMatches = parenTokenRegex.findAll(trimmed)
        for (m in parenMatches) {
            val letter = normalizeOptionKey(m.groupValues[1])
            if (options.isEmpty() || options.containsKey(letter)) {
                correctLetters.add(letter)
            }
        }
        if (correctLetters.isNotEmpty()) {
            return correctLetters
        }

        // 2. Letter / Roman prefix tokens, e.g. "A)Au", "c) Au", "iii) Au", "IV. 1989", "i) Canada, iii) Russia"
        val prefixTokenRegex = Regex(
            """(?:\b|^)(?:\(?($OPTION_LABEL_PATTERN)[\)\.\:\-]|\[($OPTION_LABEL_PATTERN)\])\s*(.*?)(?=(?:,\s*|\s+(?:and|or)\s+)(?:\(?$OPTION_LABEL_PATTERN[\)\.\:\-]|\[$OPTION_LABEL_PATTERN\])|$)""",
            RegexOption.IGNORE_CASE
        )
        val prefixMatches = prefixTokenRegex.findAll(trimmed).toList()
        if (prefixMatches.isNotEmpty()) {
            for (pm in prefixMatches) {
                val rawLabel = pm.groupValues[1].ifEmpty { pm.groupValues[2] }
                val letter = normalizeOptionKey(rawLabel)
                val textAfter = pm.groupValues[3].trim()

                // Check if textAfter matches an option text (e.g. "Au" matches option C, "Canada" matches option A)
                val matchingOpt = if (textAfter.isNotBlank()) {
                    options.entries.firstOrNull { (_, optVal) ->
                        optVal.isNotBlank() && (
                            textAfter.equals(optVal, ignoreCase = true) ||
                            Regex("""\b${Regex.escape(optVal)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(textAfter)
                        )
                    }
                } else null

                if (matchingOpt != null) {
                    correctLetters.add(matchingOpt.key)
                } else if (options.isEmpty() || options.containsKey(letter)) {
                    correctLetters.add(letter)
                }
            }
            if (correctLetters.isNotEmpty()) {
                return correctLetters
            }
        }

        // 3. Standalone letters / Roman numerals, e.g. "C", "c", "iii", "III", "A, C", "i, iii"
        val standaloneTokenRegex = Regex("""\b($OPTION_LABEL_PATTERN)\b""", RegexOption.IGNORE_CASE)
        val standaloneMatches = standaloneTokenRegex.findAll(trimmed)
        for (m in standaloneMatches) {
            val letter = normalizeOptionKey(m.groupValues[1])
            if (options.isEmpty() || options.containsKey(letter)) {
                correctLetters.add(letter)
            }
        }
        if (correctLetters.isNotEmpty()) {
            return correctLetters
        }

        // 4. Match against option texts, e.g. "Au", "Canada", "1989"
        for ((letter, optText) in options) {
            if (optText.isNotBlank()) {
                if (trimmed.equals(optText, ignoreCase = true) ||
                    Regex("""\b${Regex.escape(optText)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(trimmed) ||
                    trimmed.contains(optText, ignoreCase = true)
                ) {
                    correctLetters.add(letter)
                }
            }
        }

        return correctLetters
    }

    /**
     * Extracts consolidated answer list from text (Format 2: "Correct Answer: 1.Au(C), 2.Canada(A), 3.1989(D)").
     * Returns the text without the answer section and a map of (1-based question number -> raw answer string).
     */
    private fun extractConsolidatedAnswers(text: String): Pair<String, Map<Int, String>> {
        val lines = text.lines()
        var headerLineIndex = -1

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (CONSOLIDATED_HEADER_START_REGEX.containsMatchIn(line)) {
                val remainingLines = lines.subList(i + 1, lines.size)
                // A consolidated answer section cannot contain question options (A, B, C...) below it
                val hasOptionsBelow = remainingLines.any { OPTION_REGEX.containsMatchIn(it) }
                if (hasOptionsBelow) {
                    continue
                }

                val remainingText = lines.subList(i, lines.size).joinToString("\n")
                val standardMatches = CONSOLIDATED_ENTRY_REGEX.findAll(remainingText).toList()
                val tableMatches = remainingLines.mapNotNull { TABLE_ROW_REGEX.matchEntire(it.trim()) }

                // Check if this line or the immediate next lines contain question #1 indicator
                val hasQuestion1 = Regex("""\b1\s*[\.:\)\-]""").containsMatchIn(line) ||
                    (remainingLines.isNotEmpty() && Regex("""^\s*1\s*[\.:\)\-]""").containsMatchIn(remainingLines[0])) ||
                    tableMatches.any { it.groupValues[1] == "1" }

                if (hasQuestion1 && (standardMatches.isNotEmpty() || tableMatches.isNotEmpty())) {
                    headerLineIndex = i
                    break
                }
            }
        }

        if (headerLineIndex == -1) {
            return Pair(text, emptyMap())
        }

        val questionsLines = lines.subList(0, headerLineIndex)
        val answerSectionText = lines.subList(headerLineIndex, lines.size).joinToString("\n")

        val answersMap = mutableMapOf<Int, String>()
        val tableMatches = lines.subList(headerLineIndex + 1, lines.size).mapNotNull { TABLE_ROW_REGEX.matchEntire(it.trim()) }

        if (tableMatches.isNotEmpty()) {
            for (m in tableMatches) {
                val qNum = m.groupValues[1].toIntOrNull() ?: continue
                val col2 = m.groupValues[2].trim()
                val col3 = m.groupValues[3].trim()

                val isCol2Option = Regex("""^$OPTION_LABEL_PATTERN$""", RegexOption.IGNORE_CASE).matches(col2)
                val ansVal = if (isCol2Option && col3.isNotBlank()) {
                    "$col2) $col3"
                } else if (col3.isNotBlank()) {
                    "$col3) $col2"
                } else {
                    col2
                }
                answersMap[qNum] = ansVal
            }
        } else {
            val matches = CONSOLIDATED_ENTRY_REGEX.findAll(answerSectionText)
            for (m in matches) {
                val qNum = m.groupValues[1].toIntOrNull() ?: continue
                var ansVal = m.groupValues[2].trim()
                // Clean up trailing separators or connective "or"
                ansVal = ansVal.replace(Regex("""\s+or\s*$""", RegexOption.IGNORE_CASE), "")
                    .trimEnd(',', ';', '.', ' ')
                if (ansVal.isNotBlank()) {
                    answersMap[qNum] = if (answersMap.containsKey(qNum)) {
                        "${answersMap[qNum]}, $ansVal"
                    } else {
                        ansVal
                    }
                }
            }
        }

        return Pair(questionsLines.joinToString("\n"), answersMap)
    }

    private class RawQuestion {
        val promptLines = mutableListOf<String>()
        val options = mutableMapOf<String, String>()
        val inlineCorrectAnswers = mutableSetOf<String>()
        var answerText: String? = null
        var hint: String? = null
        var explanation: String? = null

        fun toQuestionDraft(): QuestionDraft? {
            val prompt = promptLines.joinToString("\n").trim()
            if (prompt.isBlank() || options.size < 2) return null

            val letters = listOf("A", "B", "C", "D", "E", "F")
            val optionList = mutableListOf<String>()
            val maxLetterIdx = letters.indexOfLast { options.containsKey(it) }
            val count = maxOf(4, if (maxLetterIdx >= 0) maxLetterIdx + 1 else 0)

            for (i in 0 until minOf(count, 6)) {
                val letter = letters[i]
                optionList.add(options[letter] ?: "")
            }

            while (optionList.size < 2) {
                optionList.add("")
            }

            // Determine correct answers
            val resolvedCorrect = mutableSetOf<String>()

            // Priority 1: Answer text from Format 2 (consolidated) or Format 3 (per-question)
            if (!answerText.isNullOrBlank()) {
                val answers = resolveCorrectAnswers(answerText!!, options)
                resolvedCorrect.addAll(answers)
            }

            // Priority 2: Inline markers from Format 1 ("Au (Correct Answer)")
            if (resolvedCorrect.isEmpty() && inlineCorrectAnswers.isNotEmpty()) {
                resolvedCorrect.addAll(inlineCorrectAnswers)
            }

            // Fallback default
            if (resolvedCorrect.isEmpty()) {
                resolvedCorrect.add("A")
            }

            // Automatic single vs multiple choice classification
            val questionType = if (resolvedCorrect.size > 1) {
                QuestionType.MULTIPLE
            } else {
                QuestionType.SINGLE
            }

            return QuestionDraft(
                prompt = prompt,
                type = questionType,
                options = optionList,
                correctAnswers = resolvedCorrect,
                hint = hint ?: "",
                explanation = explanation ?: ""
            )
        }
    }
}
