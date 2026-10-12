package com.example.onlinequizapp.domain.model

enum class FeedbackMode {
    IMMEDIATE,
    ON_SUBMIT
}

enum class TimerMode {
    COUNTDOWN,
    ELAPSED,
    NONE
}

data class QuizConfig(
    val feedbackMode: FeedbackMode = FeedbackMode.ON_SUBMIT,
    val timerMode: TimerMode = TimerMode.COUNTDOWN,
    val timeLimitSeconds: Int = 180,
    val allowHints: Boolean = true,
    val shuffleQuestions: Boolean = false,
    val shuffleOptions: Boolean = false
)

enum class QuizStatus {
    DRAFT,
    READY_FOR_REVIEW,
    APPROVED,
    PUBLISHED,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

enum class QuizCreationMethod {
    MANUAL,
    AI_GENERATED
}

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val icon: String = "code",
    val questions: List<Question>,
    val config: QuizConfig = QuizConfig(),
    val targetBatchIds: List<String> = emptyList(),
    val quizCode: String = "",
    val status: QuizStatus = QuizStatus.PUBLISHED,
    val creationMethod: QuizCreationMethod = QuizCreationMethod.MANUAL,
    val aiProvider: String? = null,
    val teacherId: String = "",
    val targetStudentIds: List<String> = emptyList(),
    val availabilityStartMillis: Long? = null,
    val availabilityEndMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalQuestions: Int get() = questions.size
    val difficulty: Difficulty get() = questions.firstOrNull()?.difficulty ?: Difficulty.MEDIUM

    val isPublishedOrActive: Boolean
        get() = status == QuizStatus.PUBLISHED || status == QuizStatus.ACTIVE

    /**
     * Verifies if this quiz is currently within its configured availability window.
     */
    fun isAvailableAt(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (!isPublishedOrActive) return false
        val startOk = availabilityStartMillis?.let { currentTimeMillis >= it } ?: true
        val endOk = availabilityEndMillis?.let { currentTimeMillis <= it } ?: true
        return startOk && endOk
    }

    /**
     * Computes the current lifecycle status regarding scheduled availability window.
     */
    fun getScheduleStatus(currentTimeMillis: Long = System.currentTimeMillis()): QuizScheduleStatus {
        if (availabilityStartMillis == null && availabilityEndMillis == null) {
            return QuizScheduleStatus.ALWAYS_AVAILABLE
        }
        if (availabilityStartMillis != null && currentTimeMillis < availabilityStartMillis) {
            return QuizScheduleStatus.UPCOMING
        }
        if (availabilityEndMillis != null && currentTimeMillis > availabilityEndMillis) {
            return QuizScheduleStatus.EXPIRED
        }
        return QuizScheduleStatus.ACTIVE_WITH_DEADLINE
    }

    /**
     * Returns a human-readable display string summarizing the configured window.
     */
    fun formatScheduleWindowSummary(): String? {
        val start = availabilityStartMillis
        val end = availabilityEndMillis
        if (start == null && end == null) return null

        val sdfDate = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        val sdfTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())

        return when {
            start != null && end != null -> {
                val startDate = sdfDate.format(java.util.Date(start))
                val endDate = sdfDate.format(java.util.Date(end))
                val startTime = sdfTime.format(java.util.Date(start))
                val endTime = sdfTime.format(java.util.Date(end))
                if (startDate == endDate) {
                    "$startDate • $startTime – $endTime"
                } else {
                    "$startDate $startTime – $endDate $endTime"
                }
            }
            start != null -> "Opens: ${sdfDate.format(java.util.Date(start))} at ${sdfTime.format(java.util.Date(start))}"
            end != null -> "Deadline: ${sdfDate.format(java.util.Date(end))} at ${sdfTime.format(java.util.Date(end))}"
            else -> null
        }
    }
}

enum class QuizScheduleStatus {
    ALWAYS_AVAILABLE,
    UPCOMING,
    ACTIVE_WITH_DEADLINE,
    EXPIRED
}

