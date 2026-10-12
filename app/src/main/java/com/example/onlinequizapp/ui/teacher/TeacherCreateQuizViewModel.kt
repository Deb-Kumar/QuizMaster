package com.example.onlinequizapp.ui.teacher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.ai.AIQuizGeneratorFactory
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.ai.AiProvider
import com.example.onlinequizapp.domain.ai.AiQuizRequest
import com.example.onlinequizapp.domain.model.AssignmentType
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.EnrolledStudent
import com.example.onlinequizapp.domain.model.FeedbackMode
import com.example.onlinequizapp.domain.model.Option
import com.example.onlinequizapp.domain.model.Question
import com.example.onlinequizapp.domain.model.QuestionDraft
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizConfig
import com.example.onlinequizapp.domain.model.QuizCreationMethod
import com.example.onlinequizapp.domain.model.QuizStatus
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.repository.QuizRepository
import com.example.onlinequizapp.ui.teacher.create.CreateQuizStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateQuizUiState(
    val currentStep: CreateQuizStep = CreateQuizStep.METHOD_SELECT,
    val creationMethod: QuizCreationMethod = QuizCreationMethod.MANUAL,
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val hasApiKeyForSelectedProvider: Boolean = true,
    val apiKeyInput: String = "",

    // Basic Information
    val title: String = "",
    val description: String = "",
    val categories: List<String> = emptyList(),
    val category: String = "",
    val selectedLogo: String = "",
    val difficulty: Difficulty = Difficulty.EASY,
    val questionCountToGenerate: Int = 5,
    val questionType: QuestionType = QuestionType.SINGLE,

    // Time & Hints Configuration
    val timerMode: TimerMode = TimerMode.NONE,
    val timeLimitMinutes: Int = 5,
    val feedbackMode: FeedbackMode = FeedbackMode.ON_SUBMIT,
    val allowHints: Boolean = false,

    // Scheduled Availability Window
    val hasScheduleWindow: Boolean = false,
    val availabilityStartMillis: Long? = null,
    val availabilityEndMillis: Long? = null,

    // Question & Option Shuffling
    val shuffleQuestionsAndOptions: Boolean = false,

    // Publishing Target & Roster
    val publishTargetType: AssignmentType = AssignmentType.BATCH,
    val targetBatchIds: Set<String> = emptySet(),
    val availableBatches: List<Batch> = emptyList(),
    val targetStudentIds: Set<String> = emptySet(),
    val availableStudents: List<EnrolledStudent> = emptyList(),

    // Questions under review/editing
    val questions: List<QuestionDraft> = emptyList(),

    // State indicators
    val editingQuizId: String? = null,
    val existingQuizCode: String? = null,
    val existingCreatedAt: Long? = null,
    val existingUpdatedAt: Long? = null,
    val isLoading: Boolean = false,
    val isAiGenerating: Boolean = false,
    val aiStatusMessage: String = "",
    val errorMessage: String? = null,
    val publishedQuiz: Quiz? = null,
    val showAiMetadataDialog: Boolean = false,
    val showPublishDialog: Boolean = false,
    val showPublishSuccessDialog: Boolean = false
)

class TeacherCreateQuizViewModel(
    private val quizRepository: QuizRepository = QuizRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateQuizUiState())
    val uiState: StateFlow<CreateQuizUiState> = _uiState.asStateFlow()

    fun loadTeacherData(teacherId: String) {
        viewModelScope.launch {
            BatchRepositoryImpl.instance.getBatchesByTeacher(teacherId).collect { batches ->
                _uiState.update { it.copy(availableBatches = batches) }

                // Gather unique enrolled students across all teacher's batches
                val allStudents = mutableListOf<EnrolledStudent>()
                val seenIds = mutableSetOf<String>()
                batches.forEach { batch ->
                    val result = BatchRepositoryImpl.instance.getStudentsInBatch(batch.id, teacherId)
                    result.getOrNull()?.forEach { student ->
                        if (!seenIds.contains(student.studentId)) {
                            seenIds.add(student.studentId)
                            allStudents.add(student)
                        }
                    }
                }
                _uiState.update { it.copy(availableStudents = allStudents) }
            }
        }
    }

    fun loadQuizForEditing(quizId: String, startAtReview: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val quiz = quizRepository.getQuizById(quizId)
            if (quiz == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Quiz not found.") }
                return@launch
            }

            val questionDrafts = quiz.questions.map { q ->
                QuestionDraft(
                    id = q.id,
                    prompt = q.prompt,
                    type = q.type,
                    options = q.options.map { it.text },
                    correctAnswers = q.correctAnswers,
                    explanation = q.explanation,
                    hint = q.hint ?: ""
                )
            }

            val initialBatchIds = quiz.targetBatchIds.toSet()
            val initialStudentIds = quiz.targetStudentIds.toSet()
            val publishTarget = if (initialBatchIds.isNotEmpty()) {
                AssignmentType.BATCH
            } else if (initialStudentIds.isNotEmpty()) {
                AssignmentType.INDIVIDUAL_STUDENT
            } else {
                AssignmentType.PUBLIC
            }

            val parsedCategories = if (quiz.category.isNotBlank()) {
                quiz.category.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct().take(5)
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    editingQuizId = quiz.id,
                    existingQuizCode = quiz.quizCode,
                    existingCreatedAt = quiz.createdAt,
                    existingUpdatedAt = quiz.updatedAt,
                    currentStep = if (startAtReview && questionDrafts.isNotEmpty()) CreateQuizStep.QUESTION_REVIEW else CreateQuizStep.BASIC_INFO,
                    creationMethod = quiz.creationMethod ?: QuizCreationMethod.MANUAL,
                    title = quiz.title,
                    description = quiz.description,
                    category = parsedCategories.joinToString(", "),
                    categories = parsedCategories,
                    selectedLogo = quiz.icon,
                    difficulty = quiz.difficulty,
                    timerMode = quiz.config.timerMode,
                    timeLimitMinutes = if (quiz.config.timeLimitSeconds > 0) quiz.config.timeLimitSeconds / 60 else 5,
                    feedbackMode = quiz.config.feedbackMode,
                    allowHints = quiz.config.allowHints,
                    hasScheduleWindow = quiz.availabilityStartMillis != null || quiz.availabilityEndMillis != null,
                    availabilityStartMillis = quiz.availabilityStartMillis,
                    availabilityEndMillis = quiz.availabilityEndMillis,
                    shuffleQuestionsAndOptions = quiz.config.shuffleQuestions || quiz.config.shuffleOptions,
                    publishTargetType = publishTarget,
                    targetBatchIds = initialBatchIds,
                    targetStudentIds = initialStudentIds,
                    questions = questionDrafts,
                    isLoading = false
                )
            }
        }
    }

    // Step 1: Choose Creation Method
    fun selectCreationMethod(method: QuizCreationMethod) {
        val initialQuestions = if (method == QuizCreationMethod.MANUAL && _uiState.value.questions.isEmpty()) {
            listOf(QuestionDraft())
        } else {
            _uiState.value.questions
        }

        _uiState.update {
            it.copy(
                creationMethod = method,
                currentStep = CreateQuizStep.BASIC_INFO,
                questions = initialQuestions,
                errorMessage = null
            )
        }
    }

    // Step 2 (AI): Proceed from Configure AI Quiz to Model Selection
    fun proceedToProviderSelect() {
        val s = _uiState.value
        val promptText = s.description.trim()
        if (promptText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter what quiz you want to generate in the prompt field.") }
            return
        }
        val effectiveTitle = if (s.title.trim().isNotBlank()) {
            s.title.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            val words = promptText.split("\\s+".toRegex()).take(6).joinToString(" ")
            if (words.length > 50) words.take(50) else words
        }
        _uiState.update { it.copy(title = effectiveTitle, currentStep = CreateQuizStep.PROVIDER_SELECT, errorMessage = null) }
    }

    // Choose AI Provider
    fun selectAiProvider(provider: AiProvider) {
        val hasKey = AIQuizGeneratorFactory.hasApiKey(provider)
        _uiState.update {
            it.copy(
                selectedAiProvider = provider,
                hasApiKeyForSelectedProvider = hasKey,
                errorMessage = null
            )
        }
    }

    fun proceedToBasicInfo() {
        _uiState.update { it.copy(currentStep = CreateQuizStep.BASIC_INFO, errorMessage = null) }
    }

    fun navigateBackStep() {
        val current = _uiState.value.currentStep
        val method = _uiState.value.creationMethod

        val prevStep = when (current) {
            CreateQuizStep.METHOD_SELECT -> CreateQuizStep.METHOD_SELECT
            CreateQuizStep.BASIC_INFO -> {
                if (_uiState.value.editingQuizId != null) CreateQuizStep.BASIC_INFO
                else CreateQuizStep.METHOD_SELECT
            }
            CreateQuizStep.PROVIDER_SELECT -> CreateQuizStep.BASIC_INFO
            CreateQuizStep.QUESTION_REVIEW -> {
                if (method == QuizCreationMethod.AI_GENERATED && _uiState.value.editingQuizId == null) CreateQuizStep.PROVIDER_SELECT
                else CreateQuizStep.BASIC_INFO
            }
        }
        _uiState.update { it.copy(currentStep = prevStep, errorMessage = null) }
    }

fun capitalizeEachWord(text: String): String {
    if (text.isEmpty()) return text
    val sb = StringBuilder()
    var capitalizeNext = true
    for (char in text) {
        if (char.isWhitespace()) {
            sb.append(char)
            capitalizeNext = true
        } else if (capitalizeNext && char.isLetter()) {
            sb.append(char.uppercaseChar())
            capitalizeNext = false
        } else {
            sb.append(char)
            capitalizeNext = false
        }
    }
    return sb.toString()
}

    // Step 3: Basic Info Modifiers
    fun onTitleChanged(title: String) {
        val capitalized = capitalizeEachWord(title)
        _uiState.update { it.copy(title = capitalized, errorMessage = null) }
    }

    fun onDescriptionChanged(description: String) {
        _uiState.update { it.copy(description = description, errorMessage = null) }
    }

    fun onCategoriesChanged(categories: List<String>) {
        val limited = categories
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
        _uiState.update {
            it.copy(
                categories = limited,
                category = limited.joinToString(", ")
            )
        }
    }

    fun onCategoryChanged(category: String) {
        val list = if (category.isBlank()) emptyList() else {
            category.split(",").map { it.trim() }.filter { it.isNotBlank() }.distinct().take(5)
        }
        _uiState.update {
            it.copy(
                categories = list,
                category = list.joinToString(", ")
            )
        }
    }

    fun onDifficultyChanged(difficulty: Difficulty) {
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    fun onQuestionCountToGenerateChanged(count: Int) {
        _uiState.update { it.copy(questionCountToGenerate = count.coerceIn(1, 50)) }
    }

    fun onQuestionTypeChanged(type: QuestionType) {
        _uiState.update { it.copy(questionType = type) }
    }

    fun onTimerModeChanged(mode: TimerMode) {
        _uiState.update { it.copy(timerMode = mode) }
    }

    fun onTimeLimitMinutesChanged(minutes: Int) {
        _uiState.update { it.copy(timeLimitMinutes = minutes.coerceIn(1, 180)) }
    }

    fun onFeedbackModeChanged(mode: FeedbackMode) {
        _uiState.update { it.copy(feedbackMode = mode) }
    }

    fun onAllowHintsChanged(allow: Boolean) {
        _uiState.update { it.copy(allowHints = allow) }
    }

    fun onToggleScheduleWindow(enabled: Boolean) {
        _uiState.update { current ->
            val now = System.currentTimeMillis()
            val start = if (enabled && current.availabilityStartMillis == null) now else current.availabilityStartMillis
            // Default deadline: 24 hours from start if not set
            val end = if (enabled && current.availabilityEndMillis == null) (start ?: now) + (24 * 3600 * 1000L) else current.availabilityEndMillis
            current.copy(
                hasScheduleWindow = enabled,
                availabilityStartMillis = if (enabled) start else null,
                availabilityEndMillis = if (enabled) end else null,
                errorMessage = null
            )
        }
    }

    fun onAvailabilityStartMillisChanged(millis: Long?) {
        _uiState.update { it.copy(availabilityStartMillis = millis, errorMessage = null) }
    }

    fun onAvailabilityEndMillisChanged(millis: Long?) {
        _uiState.update { it.copy(availabilityEndMillis = millis, errorMessage = null) }
    }

    fun onToggleShuffleQuestionsAndOptions(enabled: Boolean) {
        _uiState.update { it.copy(shuffleQuestionsAndOptions = enabled) }
    }

    fun onLogoSelected(logoId: String) {
        _uiState.update { it.copy(selectedLogo = logoId) }
    }

    fun onApiKeyInputChanged(key: String) {
        _uiState.update { it.copy(apiKeyInput = key) }
    }

    // AI Generation Trigger
    fun startAiQuestionGeneration() {
        val s = _uiState.value
        val promptText = s.description.trim()
        if (promptText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter what quiz you want to generate in the prompt field.") }
            return
        }

        val effectiveTitle = if (s.title.trim().isNotBlank()) {
            s.title.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            val words = promptText.split("\\s+".toRegex()).take(6).joinToString(" ")
            if (words.length > 50) words.take(50) else words
        }

        val provider = s.selectedAiProvider
        val apiKey = if (s.apiKeyInput.isNotBlank()) {
            s.apiKeyInput.trim()
        } else {
            AIQuizGeneratorFactory.getApiKeyForProvider(provider)
        }

        if (apiKey.isBlank()) {
            _uiState.update {
                it.copy(
                    hasApiKeyForSelectedProvider = false,
                    errorMessage = "Please provide an API key for ${provider.displayName} to generate questions."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiGenerating = true,
                    title = effectiveTitle,
                    aiStatusMessage = "Connecting to ${provider.displayName}...",
                    errorMessage = null
                )
            }

            val generator = AIQuizGeneratorFactory.getGenerator(provider)
            val request = AiQuizRequest(
                topicOrTitle = promptText,
                category = if (s.category.isNotBlank()) s.category.trim() else "General",
                difficulty = s.difficulty,
                questionCount = s.questionCountToGenerate,
                questionType = s.questionType,
                additionalInstructions = promptText
            )

            _uiState.update { it.copy(aiStatusMessage = "Generating questions with ${provider.displayName}...") }

            val result = generator.generateQuestions(request, apiKey)

            result.fold(
                onSuccess = { generatedDrafts ->
                    _uiState.update {
                        it.copy(
                            isAiGenerating = false,
                            title = effectiveTitle,
                            questions = generatedDrafts,
                            currentStep = CreateQuizStep.QUESTION_REVIEW,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isAiGenerating = false,
                            errorMessage = "Generation failed: ${error.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            )
        }
    }

    fun proceedToManualQuestionEditor() {
        val s = _uiState.value
        val capitalizedTitle = capitalizeEachWord(s.title.trim())
        if (capitalizedTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a quiz title.") }
            return
        }
        if (s.description.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a quiz description.") }
            return
        }
        if (s.categories.isEmpty() && s.category.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one quiz category (up to 5).") }
            return
        }
        _uiState.update { it.copy(title = capitalizedTitle, currentStep = CreateQuizStep.QUESTION_REVIEW, errorMessage = null) }
    }

    // Step 4: Question Review & Editing Powers
    fun addQuestion() {
        val defaultType = if (_uiState.value.questionType == QuestionType.MULTIPLE) QuestionType.MULTIPLE else QuestionType.SINGLE
        _uiState.update {
            it.copy(
                questions = it.questions + QuestionDraft(type = defaultType),
                errorMessage = null
            )
        }
    }

    fun importBulkQuestions(newQuestions: List<QuestionDraft>, replaceAt: Int? = null) {
        if (newQuestions.isEmpty()) return
        _uiState.update { current ->
            val updated = current.questions.toMutableList()
            if (replaceAt != null && replaceAt in updated.indices) {
                updated[replaceAt] = newQuestions.first()
                if (newQuestions.size > 1) {
                    updated.addAll(replaceAt + 1, newQuestions.drop(1))
                }
            } else {
                // Remove any blank dummy questions so only genuine questions remain
                updated.removeAll { it.prompt.trim().isBlank() && it.options.all { opt -> opt.isBlank() } }
                updated.addAll(newQuestions)
            }
            current.copy(questions = updated, errorMessage = null)
        }
    }

    fun removeQuestion(index: Int) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            it.copy(questions = updated)
        }
    }

    fun clearAllQuestions() {
        _uiState.update {
            it.copy(questions = emptyList())
        }
    }

    fun moveQuestion(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        _uiState.update {
            val updated = it.questions.toMutableList()
            if (fromIndex in updated.indices && toIndex in updated.indices) {
                val item = updated.removeAt(fromIndex)
                updated.add(toIndex, item)
            }
            it.copy(questions = updated)
        }
    }

    fun updateQuestionPrompt(index: Int, prompt: String) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            updated[index] = updated[index].copy(prompt = prompt)
            it.copy(questions = updated, errorMessage = null)
        }
    }

    fun updateQuestionType(index: Int, type: QuestionType) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            val current = updated[index]
            val defaultCorrect = if (type == QuestionType.SINGLE) setOf("A") else current.correctAnswers
            updated[index] = current.copy(type = type, correctAnswers = defaultCorrect)
            it.copy(questions = updated)
        }
    }

    fun updateOptionText(qIndex: Int, optIndex: Int, text: String) {
        _uiState.update {
            val updatedQuestions = it.questions.toMutableList()
            val question = updatedQuestions[qIndex]
            val updatedOptions = question.options.toMutableList()
            updatedOptions[optIndex] = text
            updatedQuestions[qIndex] = question.copy(options = updatedOptions)
            it.copy(questions = updatedQuestions, errorMessage = null)
        }
    }

    fun addOption(qIndex: Int) {
        _uiState.update {
            val updatedQuestions = it.questions.toMutableList()
            val question = updatedQuestions[qIndex]
            if (question.options.size < 6) {
                updatedQuestions[qIndex] = question.copy(options = question.options + "")
                it.copy(questions = updatedQuestions)
            } else {
                it
            }
        }
    }

    fun removeOption(qIndex: Int, optIndex: Int) {
        _uiState.update {
            val updatedQuestions = it.questions.toMutableList()
            val question = updatedQuestions[qIndex]
            if (question.options.size > 2) {
                val updatedOptions = question.options.toMutableList()
                updatedOptions.removeAt(optIndex)
                val letters = listOf("A", "B", "C", "D", "E", "F")
                val removedLetter = letters.getOrElse(optIndex) { "" }
                val newCorrect = question.correctAnswers - removedLetter
                updatedQuestions[qIndex] = question.copy(
                    options = updatedOptions,
                    correctAnswers = if (newCorrect.isEmpty()) setOf("A") else newCorrect
                )
                it.copy(questions = updatedQuestions)
            } else {
                it
            }
        }
    }

    fun duplicateQuestion(index: Int) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            val target = updated[index]
            val duplicate = target.copy(id = UUID.randomUUID().toString().take(8))
            updated.add(index + 1, duplicate)
            it.copy(questions = updated)
        }
    }

    fun toggleCorrectAnswer(qIndex: Int, letter: String) {
        _uiState.update {
            val updatedQuestions = it.questions.toMutableList()
            val question = updatedQuestions[qIndex]

            val newCorrect = if (question.type == QuestionType.SINGLE) {
                setOf(letter)
            } else {
                if (question.correctAnswers.contains(letter)) {
                    if (question.correctAnswers.size > 1) {
                        question.correctAnswers - letter
                    } else {
                        question.correctAnswers
                    }
                } else {
                    question.correctAnswers + letter
                }
            }
            updatedQuestions[qIndex] = question.copy(correctAnswers = newCorrect)
            it.copy(questions = updatedQuestions)
        }
    }

    fun updateQuestionHint(index: Int, hint: String) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            updated[index] = updated[index].copy(hint = hint)
            it.copy(questions = updated)
        }
    }

    fun updateQuestionExplanation(index: Int, explanation: String) {
        _uiState.update {
            val updated = it.questions.toMutableList()
            updated[index] = updated[index].copy(explanation = explanation)
            it.copy(questions = updated)
        }
    }

    // Step 5: Publishing & Draft Logic
    fun openPublishDialog() {
        val s = _uiState.value

        if (s.questions.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add at least one question before publishing.") }
            return
        }

        // Validate questions
        for ((idx, q) in s.questions.withIndex()) {
            if (q.prompt.trim().isBlank()) {
                _uiState.update { it.copy(errorMessage = "Question #${idx + 1} prompt cannot be empty.") }
                return
            }
            if (q.options.any { it.trim().isBlank() }) {
                _uiState.update { it.copy(errorMessage = "Question #${idx + 1} has empty options.") }
                return
            }
            if (q.correctAnswers.isEmpty()) {
                _uiState.update { it.copy(errorMessage = "Question #${idx + 1} must have at least one correct option.") }
                return
            }
        }

        // For AI generated quiz, open the AI metadata dialog first to choose title, logo, and categories
        if (s.creationMethod == QuizCreationMethod.AI_GENERATED) {
            _uiState.update { it.copy(showAiMetadataDialog = true, errorMessage = null) }
            return
        }

        // For manual quiz, validate title was provided
        if (s.title.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a quiz title.") }
            return
        }

        _uiState.update { it.copy(showPublishDialog = true, errorMessage = null) }
    }

    fun dismissAiMetadataDialog() {
        _uiState.update { it.copy(showAiMetadataDialog = false) }
    }

    fun confirmAiMetadata(newTitle: String, newLogo: String, newCategories: List<String>) {
        val limited = newCategories
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(5)
        _uiState.update {
            it.copy(
                title = newTitle.trim(),
                selectedLogo = newLogo,
                categories = limited,
                category = limited.joinToString(", "),
                showAiMetadataDialog = false,
                showPublishDialog = true,
                errorMessage = null
            )
        }
    }

    fun dismissPublishDialog() {
        _uiState.update { it.copy(showPublishDialog = false) }
    }

    fun dismissPublishSuccessDialog() {
        _uiState.update { it.copy(showPublishSuccessDialog = false) }
    }

    fun setPublishTargetType(type: AssignmentType) {
        _uiState.update { it.copy(publishTargetType = type) }
    }

    fun toggleBatchSelection(batchId: String) {
        _uiState.update {
            val current = it.targetBatchIds.toMutableSet()
            if (current.contains(batchId)) current.remove(batchId) else current.add(batchId)
            it.copy(targetBatchIds = current)
        }
    }

    fun toggleStudentSelection(studentId: String) {
        _uiState.update {
            val current = it.targetStudentIds.toMutableSet()
            if (current.contains(studentId)) current.remove(studentId) else current.add(studentId)
            it.copy(targetStudentIds = current)
        }
    }

    private fun buildDomainQuiz(status: QuizStatus): Quiz {
        val s = _uiState.value
        val letters = listOf("A", "B", "C", "D", "E", "F")
        val quizId = s.editingQuizId ?: "quiz_${UUID.randomUUID().toString().take(8)}"
        val teacherId = AuthSessionManager.getSavedUser()?.id ?: "teacher_default"

        val finalQuestions = s.questions.mapIndexed { qIdx, qDraft ->
            Question(
                id = "${quizId}_q${qIdx + 1}",
                type = qDraft.type,
                prompt = qDraft.prompt.trim(),
                options = qDraft.options.mapIndexed { optIdx, text ->
                    Option(id = letters.getOrElse(optIdx) { "opt$optIdx" }, text = text.trim())
                },
                correctAnswers = qDraft.correctAnswers,
                explanation = qDraft.explanation.trim().ifEmpty {
                    "Correct answer(s): ${qDraft.correctAnswers.sorted().joinToString(", ")}"
                },
                hint = qDraft.hint.trim().ifEmpty { null },
                difficulty = s.difficulty
            )
        }

        return Quiz(
            id = quizId,
            quizCode = s.existingQuizCode ?: "",
            title = s.title.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
            description = s.description.trim().ifBlank { "${s.category} Quiz" },
            category = s.category.trim(),
            icon = s.selectedLogo.ifBlank { s.category.take(2).uppercase() },
            config = QuizConfig(
                feedbackMode = s.feedbackMode,
                timerMode = s.timerMode,
                timeLimitSeconds = if (s.timerMode == TimerMode.COUNTDOWN) s.timeLimitMinutes * 60 else 0,
                allowHints = s.allowHints,
                shuffleQuestions = s.shuffleQuestionsAndOptions,
                shuffleOptions = s.shuffleQuestionsAndOptions
            ),
            questions = finalQuestions,
            targetBatchIds = if (status == QuizStatus.DRAFT) emptyList() else if (s.publishTargetType == AssignmentType.BATCH) s.targetBatchIds.toList() else emptyList(),
            targetStudentIds = if (status == QuizStatus.DRAFT) emptyList() else if (s.publishTargetType == AssignmentType.INDIVIDUAL_STUDENT) s.targetStudentIds.toList() else emptyList(),
            status = status,
            creationMethod = s.creationMethod,
            aiProvider = if (s.creationMethod == QuizCreationMethod.AI_GENERATED) s.selectedAiProvider.displayName else null,
            teacherId = teacherId,
            availabilityStartMillis = if (s.hasScheduleWindow) s.availabilityStartMillis else null,
            availabilityEndMillis = if (s.hasScheduleWindow) s.availabilityEndMillis else null,
            createdAt = s.existingCreatedAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    fun saveDraft(onSaved: (Quiz) -> Unit) {
        val s = _uiState.value
        val domainQuiz = buildDomainQuiz(QuizStatus.DRAFT)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (s.editingQuizId != null) {
                quizRepository.updateQuiz(domainQuiz, domainQuiz.teacherId)
                Result.success(domainQuiz)
            } else {
                quizRepository.saveQuizDraft(domainQuiz, domainQuiz.teacherId)
            }
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { savedQuiz ->
                    _uiState.update { it.copy(publishedQuiz = savedQuiz) }
                    onSaved(savedQuiz)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = "Failed to save draft: ${err.message}") }
                }
            )
        }
    }

    fun publishQuizConfirmed(onSuccess: (Quiz) -> Unit) {
        val s = _uiState.value
        val batchIds = if (s.publishTargetType == AssignmentType.BATCH) s.targetBatchIds.toList() else emptyList()
        val studentIds = if (s.publishTargetType == AssignmentType.INDIVIDUAL_STUDENT) s.targetStudentIds.toList() else emptyList()

        if (s.publishTargetType == AssignmentType.BATCH && batchIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one batch to publish.") }
            return
        }

        if (s.publishTargetType == AssignmentType.INDIVIDUAL_STUDENT && studentIds.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one student to assign.") }
            return
        }

        if (s.hasScheduleWindow) {
            val start = s.availabilityStartMillis
            val end = s.availabilityEndMillis
            if (start != null && end != null && end <= start) {
                _uiState.update { it.copy(errorMessage = "Quiz deadline must be set to a time after the opening start time.") }
                return
            }
        }

        val baseQuiz = buildDomainQuiz(QuizStatus.PUBLISHED)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, showPublishDialog = false) }

            // 1. Create or update base quiz
            if (s.editingQuizId != null) {
                quizRepository.updateQuiz(baseQuiz, baseQuiz.teacherId)
            } else {
                val created = quizRepository.createQuiz(baseQuiz, baseQuiz.teacherId)
                if (!created) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "A quiz with this ID already exists.") }
                    return@launch
                }
            }

            // 2. Publish & generate 6-char access code
            val publishResult = quizRepository.publishQuiz(
                quizId = baseQuiz.id,
                batchIds = batchIds,
                studentIds = studentIds,
                teacherId = baseQuiz.teacherId
            )

            _uiState.update { it.copy(isLoading = false) }

            publishResult.fold(
                onSuccess = { publishedQuiz ->
                    _uiState.update {
                        it.copy(
                            publishedQuiz = publishedQuiz,
                            showPublishSuccessDialog = true
                        )
                    }
                    onSuccess(publishedQuiz)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(errorMessage = "Failed to publish quiz: ${err.message}") }
                }
            )
        }
    }
}
