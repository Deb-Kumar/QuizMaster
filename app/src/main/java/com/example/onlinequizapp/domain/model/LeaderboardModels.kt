package com.example.onlinequizapp.domain.model

/**
 * Represents a student's standing within a specific batch leaderboard.
 *
 * Scoring rule:
 * If a batch has [totalAssigned] quizzes and a student has completed [completedCount] quizzes,
 * their [averageScore] is the mean of all completed quiz score percentages ((score1 + score2 + ... + scoreN) / N).
 * If [completedCount] == 0, [averageScore] is 0.
 *
 * Example:
 * 3 quizzes assigned:
 * Student 1: 84%, 50%, 100% -> avg = 78%, completed "3 of 3"
 * Student 2: 78%            -> avg = 78%, completed "1 of 3"
 * Student 3: 0 completed    -> avg = 0%, completed "0 of 3"
 */
data class BatchLeaderboardStudent(
    val studentId: String,
    val studentName: String,
    val studentEmail: String = "",
    val avatarUrl: String? = null,
    val completedCount: Int = 0,
    val totalAssigned: Int = 0,
    val averageScore: Int = 0,
    val quizBreakdown: List<BatchStudentQuizScore> = emptyList(),
    val rank: Int = 0,
    val isCurrentStudent: Boolean = false
) {
    val hasRank: Boolean get() = rank > 0 && completedCount > 0
    val rankText: String get() = if (hasRank) "#$rank" else "—"

    val completionRatioText: String
        get() = "$completedCount of $totalAssigned"

    val isFullCompletion: Boolean
        get() = totalAssigned > 0 && completedCount >= totalAssigned
}

/**
 * Score detail for an individual quiz completed by a student in a batch.
 */
data class BatchStudentQuizScore(
    val quizId: String,
    val quizTitle: String,
    val scorePercentage: Int,
    val submittedAtMillis: Long = 0L
)

/**
 * Aggregated leaderboard data for a single Batch.
 */
data class BatchLeaderboardData(
    val batch: Batch,
    val assignedQuizzes: List<Quiz> = emptyList(),
    val students: List<BatchLeaderboardStudent> = emptyList(),
    val classAverageScore: Int = 0,
    val currentStudentRank: Int? = null
)

/**
 * Represents an individual quiz's leaderboard entry, showing all student completions.
 */
data class IndividualQuizLeaderboardItem(
    val quiz: Quiz,
    val assignmentSource: String = "Public Quiz",
    val totalCompletionsCount: Int = 0,
    val studentAttempts: List<QuizLeaderboardStudentAttempt> = emptyList(),
    val currentStudentAttempt: QuizLeaderboardStudentAttempt? = null
)

/**
 * A student's score attempt on an individual quiz.
 */
data class QuizLeaderboardStudentAttempt(
    val studentId: String,
    val studentName: String,
    val avatarUrl: String? = null,
    val scorePercentage: Int = 0,
    val elapsedTimeSeconds: Int = 0,
    val submittedAtMillis: Long = 0L,
    val rank: Int = 1,
    val isCurrentStudent: Boolean = false
)
