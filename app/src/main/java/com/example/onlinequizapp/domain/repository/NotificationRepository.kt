package com.example.onlinequizapp.domain.repository

import com.example.onlinequizapp.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing in-app notifications for both Students and Teachers.
 */
interface NotificationRepository {
    /**
     * Observes real-time notifications directed to a specific user.
     */
    fun getNotificationsFlow(userId: String): Flow<List<AppNotification>>

    /**
     * One-shot fetch of notifications for a user.
     */
    suspend fun getNotifications(userId: String): List<AppNotification>

    /**
     * Sends an in-app notification to the specified recipient.
     */
    suspend fun sendNotification(notification: AppNotification): Result<Unit>

    /**
     * Marks a specific notification as read.
     */
    suspend fun markAsRead(notificationId: String): Result<Unit>

    /**
     * Marks all notifications for a specific user as read.
     */
    suspend fun markAllAsRead(userId: String): Result<Unit>

    /**
     * Deletes a specific notification by ID.
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit>

    /**
     * Clears all read notifications for a user.
     */
    suspend fun clearReadNotifications(userId: String): Result<Unit>

    /**
     * Observes real-time unread notification count for a specific user.
     */
    fun getUnreadCountFlow(userId: String): Flow<Int>
}

