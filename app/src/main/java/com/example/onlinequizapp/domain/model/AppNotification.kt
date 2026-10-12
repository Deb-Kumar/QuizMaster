package com.example.onlinequizapp.domain.model

import java.util.UUID

/**
 * Domain model representing an in-app notification for either Students or Teachers.
 *
 * Implements strict user isolation via [recipientId] and [recipientRole].
 */
data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val recipientId: String,
    val recipientRole: UserRole,
    val title: String,
    val description: String,
    val category: String, // "QUIZ", "SUBMISSION", "STUDENT_ENROLLMENT", "RESULT", "ACHIEVEMENT", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetQuizId: String? = null,
    val targetBatchId: String? = null,
    val senderId: String? = null,
    val senderName: String? = null
) {
    /**
     * Formats [timestamp] relative to current time (e.g. "Just now", "15m ago", "2h ago", "Yesterday").
     */
    fun formatTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diffMs = (now - timestamp).coerceAtLeast(0L)
        val diffSec = diffMs / 1000
        val diffMin = diffSec / 60
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24

        return when {
            diffMin < 1 -> "Just now"
            diffMin < 60 -> "${diffMin}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            diffDays == 1L -> "Yesterday"
            diffDays < 7 -> "${diffDays}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}

