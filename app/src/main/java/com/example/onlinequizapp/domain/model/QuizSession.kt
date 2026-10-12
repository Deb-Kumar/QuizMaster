package com.example.onlinequizapp.domain.model

data class UserQuestionAnswer(
    val questionId: String,
    val selectedOptionIds: Set<String> = emptySet(),
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val timeSpentSeconds: Int = 0
)

enum class SessionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    TIMED_OUT
}

data class QuizSessionState(
    val quiz: Quiz,
    val currentIndex: Int = 0,
    val answers: Map<String, UserQuestionAnswer> = emptyMap(),
    val remainingTimeSeconds: Int = 0,
    val elapsedTimeSeconds: Int = 0,
    val isHintRevealed: Boolean = false,
    val showImmediateFeedback: Boolean = false,
    val status: SessionStatus = SessionStatus.NOT_STARTED,
    val startTimeTimestamp: Long = 0L,
    val deadlineTimestamp: Long = 0L
) {
    val currentQuestion: Question
        get() = quiz.questions[currentIndex]

    val totalQuestions: Int
        get() = quiz.questions.size

    val isFirstQuestion: Boolean
        get() = currentIndex == 0

    val isLastQuestion: Boolean
        get() = currentIndex == totalQuestions - 1

    val currentAnswer: UserQuestionAnswer?
        get() = answers[currentQuestion.id]

    val currentSelectedOptionIds: Set<String>
        get() = currentAnswer?.selectedOptionIds ?: emptySet()

    val progressPercent: Float
        get() = if (totalQuestions == 0) 0f else (currentIndex + 1).toFloat() / totalQuestions

    val correctCount: Int
        get() = answers.values.count { it.isCorrect }

    val incorrectCount: Int
        get() = answers.values.count { it.isAnswered && !it.isCorrect }

    val unansweredCount: Int
        get() = totalQuestions - answers.values.count { it.isAnswered }

    val scorePercentage: Int
        get() = if (totalQuestions == 0) 0 else ((correctCount * 100) / totalQuestions)

    val elapsedTimeFormatted: String
        get() {
            val minutes = elapsedTimeSeconds / 60
            val seconds = elapsedTimeSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}
