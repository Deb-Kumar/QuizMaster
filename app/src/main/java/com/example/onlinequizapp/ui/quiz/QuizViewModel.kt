package com.example.onlinequizapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.data.session.QuizDraftManager
import com.example.onlinequizapp.domain.model.FeedbackMode
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.model.UserQuestionAnswer
import com.example.onlinequizapp.domain.repository.QuizRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(
    private val quizId: String,
    private val repository: QuizRepository = QuizRepositoryImpl.instance
) : ViewModel() {

    private val _sessionState = MutableStateFlow<QuizSessionState?>(null)
    val sessionState: StateFlow<QuizSessionState?> = _sessionState.asStateFlow()

    private val _isRestoredSession = MutableStateFlow(false)
    val isRestoredSession: StateFlow<Boolean> = _isRestoredSession.asStateFlow()

    private val _unavailableReason = MutableStateFlow<String?>(null)
    val unavailableReason: StateFlow<String?> = _unavailableReason.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuiz()
    }

    private fun getUserId(): String {
        return AuthSessionManager.getSavedUser()?.id ?: "anonymous_student"
    }

    fun loadQuiz(forceRestart: Boolean = false) {
        viewModelScope.launch {
            val quiz = repository.getQuizById(quizId)
            if (quiz != null) {
                val now = System.currentTimeMillis()
                val scheduleStatus = quiz.getScheduleStatus(now)
                if (scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.UPCOMING) {
                    val summary = quiz.formatScheduleWindowSummary() ?: "at scheduled time"
                    _unavailableReason.value = "This quiz is not open yet.\nScheduled window: $summary"
                    return@launch
                } else if (scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED) {
                    val summary = quiz.formatScheduleWindowSummary() ?: "the deadline"
                    _unavailableReason.value = "The submission deadline for this quiz has passed.\nDeadline was: $summary"
                    return@launch
                }
                initSession(quiz, forceRestart)
            }
        }
    }

    private fun initSession(quiz: Quiz, forceRestart: Boolean = false) {
        val uid = getUserId()
        val draft = if (!forceRestart) QuizDraftManager.getDraftForQuiz(uid, quiz.id) else null
        val now = System.currentTimeMillis()

        if (draft != null) {
            // Restore question and option order if saved in draft
            val restoredQuestions = if (draft.questionIdsOrder.isNotEmpty()) {
                val qMap = quiz.questions.associateBy { it.id }
                val ordered = draft.questionIdsOrder.mapNotNull { qId ->
                    val q = qMap[qId] ?: return@mapNotNull null
                    val optOrder = draft.optionIdsOrderByQuestion[qId]
                    if (!optOrder.isNullOrEmpty()) {
                        val optMap = q.options.associateBy { it.id }
                        val reorderedOpts = optOrder.mapNotNull { optMap[it] }
                        if (reorderedOpts.size == q.options.size) q.copy(options = reorderedOpts) else q
                    } else q
                }
                if (ordered.size == quiz.questions.size) ordered else quiz.questions
            } else {
                quiz.questions
            }
            val effectiveQuiz = quiz.copy(questions = restoredQuestions)

            // Restore answers from saved draft
            val restoredAnswers = effectiveQuiz.questions.associate { q ->
                val selected = draft.answers[q.id] ?: emptySet()
                val isAnswered = selected.isNotEmpty()
                val isCorrect = selected == q.correctAnswers
                q.id to UserQuestionAnswer(
                    questionId = q.id,
                    selectedOptionIds = selected,
                    isAnswered = isAnswered,
                    isCorrect = isCorrect
                )
            }
            val restoredIndex = if (draft.currentIndex in 0 until effectiveQuiz.questions.size) {
                draft.currentIndex
            } else 0

            // Wall-clock calculation
            val startTime = if (draft.startTimeTimestamp > 0L) draft.startTimeTimestamp else (draft.savedAtTimestamp - (draft.elapsedTimeSeconds * 1000L))
            val deadline = if (draft.deadlineTimestamp > 0L) {
                draft.deadlineTimestamp
            } else if (effectiveQuiz.config.timerMode == TimerMode.COUNTDOWN) {
                // Backward compatibility: compute deadline from remaining time when saved
                draft.savedAtTimestamp + (draft.remainingTimeSeconds * 1000L)
            } else 0L

            val remainingTime: Int
            val isExpired: Boolean

            if (effectiveQuiz.config.timerMode == TimerMode.COUNTDOWN) {
                val wallClockRemaining = ((deadline - now) / 1000L).toInt()
                if (wallClockRemaining <= 0) {
                    remainingTime = 0
                    isExpired = true
                } else {
                    remainingTime = wallClockRemaining
                    isExpired = false
                }
            } else {
                remainingTime = 0
                isExpired = false
            }

            val elapsedSeconds = if (startTime > 0L) {
                ((now - startTime) / 1000L).toInt().coerceAtLeast(draft.elapsedTimeSeconds)
            } else draft.elapsedTimeSeconds

            val state = QuizSessionState(
                quiz = effectiveQuiz,
                currentIndex = restoredIndex,
                answers = restoredAnswers,
                remainingTimeSeconds = remainingTime,
                elapsedTimeSeconds = elapsedSeconds,
                isHintRevealed = false,
                showImmediateFeedback = false,
                status = if (isExpired) SessionStatus.TIMED_OUT else SessionStatus.IN_PROGRESS,
                startTimeTimestamp = startTime,
                deadlineTimestamp = deadline
            )
            _sessionState.value = state
            _isRestoredSession.value = true

            if (isExpired) {
                submitQuiz()
            } else {
                startTimer(effectiveQuiz)
                saveCurrentDraft(state)
            }
        } else {
            // Fresh attempt: shuffle questions and/or options if configured
            val preparedQuestions = quiz.questions
                .let { list -> if (quiz.config.shuffleQuestions) list.shuffled() else list }
                .map { q -> if (quiz.config.shuffleOptions) q.copy(options = q.options.shuffled()) else q }
            val effectiveQuiz = quiz.copy(questions = preparedQuestions)

            val initialAnswers = effectiveQuiz.questions.associate { q ->
                q.id to UserQuestionAnswer(questionId = q.id)
            }

            val isCountDown = effectiveQuiz.config.timerMode == TimerMode.COUNTDOWN && effectiveQuiz.config.timeLimitSeconds > 0
            val startTime = now
            val deadline = if (isCountDown) {
                now + (effectiveQuiz.config.timeLimitSeconds * 1000L)
            } else 0L

            val state = QuizSessionState(
                quiz = effectiveQuiz,
                currentIndex = 0,
                answers = initialAnswers,
                remainingTimeSeconds = if (isCountDown) effectiveQuiz.config.timeLimitSeconds else 0,
                elapsedTimeSeconds = 0,
                isHintRevealed = false,
                showImmediateFeedback = false,
                status = SessionStatus.IN_PROGRESS,
                startTimeTimestamp = startTime,
                deadlineTimestamp = deadline
            )
            _sessionState.value = state
            _isRestoredSession.value = false
            startTimer(effectiveQuiz)
            saveCurrentDraft(state)
        }
    }

    private fun saveCurrentDraft(state: QuizSessionState) {
        val uid = getUserId()
        QuizDraftManager.saveDraft(uid, state)
    }

    private fun startTimer(quiz: Quiz) {
        timerJob?.cancel()
        val isCountDown = quiz.config.timerMode == TimerMode.COUNTDOWN && quiz.config.timeLimitSeconds > 0

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _sessionState.value ?: break
                if (current.status != SessionStatus.IN_PROGRESS) break

                val now = System.currentTimeMillis()

                if (isCountDown) {
                    // Compute against authoritative wall-clock deadline if present
                    val calculatedRemaining = if (current.deadlineTimestamp > 0L) {
                        ((current.deadlineTimestamp - now) / 1000L).toInt()
                    } else {
                        current.remainingTimeSeconds - 1
                    }

                    val calculatedElapsed = if (current.startTimeTimestamp > 0L) {
                        ((now - current.startTimeTimestamp) / 1000L).toInt()
                    } else {
                        current.elapsedTimeSeconds + 1
                    }

                    if (calculatedRemaining <= 0) {
                        val timedOutState = current.copy(
                            remainingTimeSeconds = 0,
                            elapsedTimeSeconds = calculatedElapsed,
                            status = SessionStatus.TIMED_OUT
                        )
                        _sessionState.value = timedOutState
                        submitQuiz()
                        break
                    } else {
                        val updatedState = current.copy(
                            remainingTimeSeconds = calculatedRemaining,
                            elapsedTimeSeconds = calculatedElapsed
                        )
                        _sessionState.value = updatedState
                        // Save timer periodically every 5s to avoid excessive disk writes
                        if (calculatedRemaining % 5 == 0) {
                            saveCurrentDraft(updatedState)
                        }
                    }
                } else {
                    // Unlimited/untimed mode: track elapsed time consecutively
                    val calculatedElapsed = if (current.startTimeTimestamp > 0L) {
                        ((now - current.startTimeTimestamp) / 1000L).toInt()
                    } else {
                        current.elapsedTimeSeconds + 1
                    }
                    val updatedState = current.copy(elapsedTimeSeconds = calculatedElapsed)
                    _sessionState.value = updatedState
                    if (calculatedElapsed % 5 == 0) {
                        saveCurrentDraft(updatedState)
                    }
                }
            }
        }
    }

    fun selectOption(optionId: String) {
        val current = _sessionState.value ?: return
        if (current.status != SessionStatus.IN_PROGRESS) return

        val question = current.currentQuestion
        val existingAnswer = current.answers[question.id] ?: UserQuestionAnswer(questionId = question.id)

        val updatedSelectedIds = when (question.type) {
            QuestionType.SINGLE -> setOf(optionId)
            QuestionType.MULTIPLE -> {
                if (existingAnswer.selectedOptionIds.contains(optionId)) {
                    existingAnswer.selectedOptionIds - optionId
                } else {
                    existingAnswer.selectedOptionIds + optionId
                }
            }
            QuestionType.MIXED -> {
                if (question.correctAnswers.size > 1) {
                    if (existingAnswer.selectedOptionIds.contains(optionId)) {
                        existingAnswer.selectedOptionIds - optionId
                    } else {
                        existingAnswer.selectedOptionIds + optionId
                    }
                } else {
                    setOf(optionId)
                }
            }
        }

        val isAnswered = updatedSelectedIds.isNotEmpty()
        val isCorrect = updatedSelectedIds == question.correctAnswers

        val updatedAnswer = existingAnswer.copy(
            selectedOptionIds = updatedSelectedIds,
            isAnswered = isAnswered,
            isCorrect = isCorrect
        )

        val updatedAnswers = current.answers + (question.id to updatedAnswer)
        val updatedState = current.copy(answers = updatedAnswers)

        _sessionState.value = updatedState
        // Auto-save immediately upon every answer selection!
        saveCurrentDraft(updatedState)
    }

    fun submitCurrentImmediateFeedback() {
        // No-op: Immediate feedback handled during review
    }

    fun toggleHint() {
        _sessionState.update { it?.copy(isHintRevealed = !(it.isHintRevealed)) }
    }

    fun navigateNext() {
        val current = _sessionState.value ?: return
        if (current.isLastQuestion) {
            submitQuiz()
        } else {
            val updatedState = current.copy(
                currentIndex = current.currentIndex + 1,
                isHintRevealed = false,
                showImmediateFeedback = false
            )
            _sessionState.value = updatedState
            saveCurrentDraft(updatedState)
        }
    }

    fun navigatePrevious() {
        val current = _sessionState.value ?: return
        if (current.currentIndex > 0) {
            val updatedState = current.copy(
                currentIndex = current.currentIndex - 1,
                isHintRevealed = false,
                showImmediateFeedback = false
            )
            _sessionState.value = updatedState
            saveCurrentDraft(updatedState)
        }
    }

    fun jumpToQuestion(index: Int) {
        val current = _sessionState.value ?: return
        if (index in 0 until current.totalQuestions) {
            val updatedState = current.copy(
                currentIndex = index,
                isHintRevealed = false,
                showImmediateFeedback = false
            )
            _sessionState.value = updatedState
            saveCurrentDraft(updatedState)
        }
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val current = _sessionState.value ?: return

        // Clear saved draft since quiz is now completed
        val uid = getUserId()
        QuizDraftManager.clearDraft(uid, quizId)

        // Evaluate all answers
        val evaluatedAnswers = current.quiz.questions.associate { q ->
            val existing = current.answers[q.id] ?: UserQuestionAnswer(questionId = q.id)
            val isAnswered = existing.isAnswered || existing.selectedOptionIds.isNotEmpty()
            val isCorrect = isAnswered && existing.selectedOptionIds == q.correctAnswers
            q.id to existing.copy(isAnswered = isAnswered, isCorrect = isCorrect)
        }

        val targetStatus = if (current.status == SessionStatus.TIMED_OUT) {
            SessionStatus.TIMED_OUT
        } else {
            SessionStatus.COMPLETED
        }

        val completedElapsed = if (current.startTimeTimestamp > 0L) {
            ((System.currentTimeMillis() - current.startTimeTimestamp) / 1000L).toInt()
        } else current.elapsedTimeSeconds

        val completedState = current.copy(
            answers = evaluatedAnswers,
            elapsedTimeSeconds = completedElapsed,
            status = targetStatus
        )

        _sessionState.value = completedState

        viewModelScope.launch {
            repository.saveCompletedQuizResult(completedState)
        }
    }

    fun restartQuiz() {
        timerJob?.cancel()
        val uid = getUserId()
        QuizDraftManager.clearDraft(uid, quizId)
        val current = _sessionState.value ?: return
        initSession(current.quiz, forceRestart = true)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(
            quizId: String,
            repository: QuizRepository = QuizRepositoryImpl.instance
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuizViewModel(quizId, repository) as T
            }
        }
    }
}
