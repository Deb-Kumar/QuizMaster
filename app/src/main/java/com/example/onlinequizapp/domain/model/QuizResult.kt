package com.example.onlinequizapp.domain.model

/**
 * Represents a completed quiz result that is persisted to both
 * Firebase Firestore (cloud) and local SharedPreferences (offline cache).
 *
 * This is the serialization-friendly representation of a completed [QuizSessionState].
 */
data class QuizResult(
    val resultId: String = "",
    val userId: String = "",
    val userName: String = "",
    val quizId: String = "",
    val quizTitle: String = "",
    val quizCategory: String = "",
    val quizIcon: String = "code",
    val teacherId: String = "",
    val totalQuestions: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val unansweredCount: Int = 0,
    val scorePercentage: Int = 0,
    val elapsedTimeSeconds: Int = 0,
    val submittedAtMillis: Long = 0L,
    val status: String = "COMPLETED",
    /** Per-question breakdown stored as questionId → [QuestionResultEntry] */
    val questionResults: List<QuestionResultEntry> = emptyList()
) {
    val isPassed: Boolean get() = scorePercentage >= 60
}

/**
 * Individual question result entry within a [QuizResult].
 */
data class QuestionResultEntry(
    val questionId: String = "",
    val selectedOptionIds: List<String> = emptyList(),
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val timeSpentSeconds: Int = 0
)
