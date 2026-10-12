package com.example.onlinequizapp.domain.model

/**
 * Represents an earned certificate by a student for scoring >= 80% on a quiz.
 */
data class EarnedCertificateItem(
    val certificateId: String,
    val quizId: String,
    val quizTitle: String,
    val quizCategory: String,
    val quizIcon: String = "code",
    val scorePercentage: Int,
    val submittedAtMillis: Long,
    val studentName: String
) {
    val isDistinction: Boolean get() = scorePercentage >= 90
    val gradeText: String get() = if (isDistinction) "Distinction (90%+)" else "Merit (80-89%)"
}

/**
 * Represents a quiz that the student can attempt or retake to unlock a certificate.
 */
data class QuizToUnlockCertificateItem(
    val quizId: String,
    val quizTitle: String,
    val quizCategory: String,
    val quizIcon: String = "code",
    val previousScore: Int? = null,
    val questionsCount: Int = 0,
    val targetBatchName: String? = null
) {
    val hasAttempted: Boolean get() = previousScore != null
    val gapPercentage: Int get() = if (previousScore != null) (80 - previousScore).coerceAtLeast(1) else 80
}

/**
 * Aggregated summary statistics for the student's certificates portfolio.
 */
data class CertificateSummaryStats(
    val totalEarned: Int = 0,
    val distinctionCount: Int = 0,
    val meritCount: Int = 0,
    val averageScore: Int = 0
)
