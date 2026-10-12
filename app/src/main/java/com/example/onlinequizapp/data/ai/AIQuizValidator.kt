package com.example.onlinequizapp.data.ai

import com.example.onlinequizapp.domain.model.QuestionDraft
import com.example.onlinequizapp.domain.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object AIQuizValidator {

    private val VALID_LETTERS = listOf("A", "B", "C", "D", "E", "F")

    /**
     * Extracts and validates an array of question drafts from raw JSON text returned by any LLM.
     * Sanitizes markdown fences, validates bounds, ensures unique questions, and converts
     * to the strict internal [QuestionDraft] schema.
     */
    fun parseAndValidate(rawResponse: String, expectedType: QuestionType): List<QuestionDraft> {
        val jsonArray = parseToJsonArray(rawResponse) ?: return emptyList()

        val validDrafts = mutableListOf<QuestionDraft>()
        val seenPrompts = mutableSetOf<String>()

        for (i in 0 until jsonArray.length()) {
            val qObj = jsonArray.optJSONObject(i) ?: continue

            val prompt = qObj.optString("prompt")
                .ifBlank { qObj.optString("question") }
                .trim()

            // 1. Validate prompt presence & uniqueness
            if (prompt.isBlank() || seenPrompts.contains(prompt.lowercase())) {
                continue
            }

            // 2. Extract options
            val optionsList = mutableListOf<String>()
            val optArray = qObj.optJSONArray("options")
            if (optArray != null) {
                for (j in 0 until optArray.length()) {
                    val opt = optArray.optString(j).trim()
                    if (opt.isNotBlank()) {
                        optionsList.add(opt)
                    }
                }
            }

            // Must have between 2 and 6 valid non-empty options
            if (optionsList.size < 2 || optionsList.size > 6) {
                continue
            }

            val maxOptionLetter = VALID_LETTERS[optionsList.size - 1]
            val allowedLetters = VALID_LETTERS.take(optionsList.size).toSet()

            // 3. Extract and validate correct answers
            val correctSet = mutableSetOf<String>()
            val rawCorrectAnswers = qObj.optJSONArray("correctAnswers")
            if (rawCorrectAnswers != null) {
                for (k in 0 until rawCorrectAnswers.length()) {
                    val ans = rawCorrectAnswers.optString(k).trim().uppercase()
                    if (allowedLetters.contains(ans)) {
                        correctSet.add(ans)
                    }
                }
            } else {
                val singleCorrect = qObj.optString("correctAnswer")
                    .ifBlank { qObj.optString("answer") }
                    .trim()

                // Check for comma, semicolon, or space-separated letters (e.g. "A, C" or "A,C")
                val tokens = singleCorrect.split(",", ";", " ", "/", "&")
                    .map { it.trim().uppercase() }
                    .filter { allowedLetters.contains(it) }

                if (tokens.isNotEmpty()) {
                    correctSet.addAll(tokens)
                } else if (allowedLetters.contains(singleCorrect.uppercase())) {
                    correctSet.add(singleCorrect.uppercase())
                } else {
                    // Try to match option text if AI returned full answer text instead of letter
                    val matchingIndex = optionsList.indexOfFirst {
                        it.equals(singleCorrect, ignoreCase = true)
                    }
                    if (matchingIndex in optionsList.indices) {
                        correctSet.add(VALID_LETTERS[matchingIndex])
                    }
                }
            }

            // Must have at least 1 correct answer that belongs to the options
            if (correctSet.isEmpty()) {
                // Default fallback to first option if strictly needed
                correctSet.add("A")
            }

            // Determine individual question draft type and final correct answers
            val (draftType, finalCorrect) = when (expectedType) {
                QuestionType.SINGLE -> {
                    QuestionType.SINGLE to setOf(correctSet.first())
                }
                QuestionType.MULTIPLE -> {
                    QuestionType.MULTIPLE to correctSet
                }
                QuestionType.MIXED -> {
                    if (correctSet.size > 1) {
                        QuestionType.MULTIPLE to correctSet
                    } else {
                        QuestionType.SINGLE to correctSet
                    }
                }
            }

            // 4. Extract explanation and hint
            val explanation = qObj.optString("explanation").trim().ifBlank {
                "Correct option: ${finalCorrect.sorted().joinToString(", ")}"
            }
            val hint = qObj.optString("hint").trim()

            seenPrompts.add(prompt.lowercase())
            validDrafts.add(
                QuestionDraft(
                    id = UUID.randomUUID().toString().take(8),
                    prompt = prompt,
                    type = draftType,
                    options = optionsList,
                    correctAnswers = finalCorrect,
                    hint = hint,
                    explanation = explanation
                )
            )
        }

        return validDrafts
    }

    private fun parseToJsonArray(text: String): JSONArray? {
        // 1. Direct JSON array attempt
        try {
            return JSONArray(text.trim())
        } catch (_: Exception) { }

        // 2. Bracket slice: find substring between first '[' and last ']'
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        if (start != -1 && end != -1 && start < end) {
            try {
                return JSONArray(text.substring(start, end + 1))
            } catch (_: Exception) { }
        }

        // 3. Object wrapper: e.g. { "questions": [...] }
        val objStart = text.indexOf('{')
        val objEnd = text.lastIndexOf('}')
        if (objStart != -1 && objEnd != -1 && objStart < objEnd) {
            try {
                val obj = JSONObject(text.substring(objStart, objEnd + 1))
                val array = obj.optJSONArray("questions") ?: obj.optJSONArray("data")
                if (array != null) return array
            } catch (_: Exception) { }
        }

        return null
    }
}
