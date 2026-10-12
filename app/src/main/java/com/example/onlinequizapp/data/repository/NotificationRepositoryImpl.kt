package com.example.onlinequizapp.data.repository

import android.util.Log
import com.example.onlinequizapp.domain.model.AppNotification
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.domain.repository.NotificationRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Concrete implementation of [NotificationRepository].
 *
 * Implements a dual-layer architecture:
 * 1. High-speed in-memory StateFlow cache for zero-latency UI observation.
 * 2. Background Firestore synchronization with fallback for offline mode.
 */
class NotificationRepositoryImpl private constructor() : NotificationRepository {

    companion object {
        private const val TAG = "NotificationRepoImpl"
        private const val COLLECTION_NOTIFICATIONS = "notifications"

        val instance: NotificationRepositoryImpl by lazy { NotificationRepositoryImpl() }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mutex = Mutex()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not available: ${e.message}")
            null
        }
    }

    // In-memory cache of notificationId -> AppNotification
    private val _notifications = MutableStateFlow<Map<String, AppNotification>>(emptyMap())

    init {
        syncFromFirestore()
    }

    private fun syncFromFirestore() {
        val fs = firestore ?: return
        scope.launch {
            try {
                fs.collection(COLLECTION_NOTIFICATIONS)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w(TAG, "Notification snapshot listener error: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val map = mutableMapOf<String, AppNotification>()
                            for (doc in snapshot.documents) {
                                parseNotification(doc)?.let { map[it.id] = it }
                            }
                            scope.launch {
                                mutex.withLock {
                                    val current = _notifications.value.toMutableMap()
                                    current.putAll(map)
                                    _notifications.value = current
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to attach notification listener: ${e.message}")
            }
        }
    }

    private fun parseNotification(doc: DocumentSnapshot): AppNotification? {
        return try {
            val id = doc.getString("id") ?: doc.id
            val recipientId = doc.getString("recipientId") ?: return null
            val roleStr = doc.getString("recipientRole") ?: "STUDENT"
            val role = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.STUDENT }
            val title = doc.getString("title") ?: "Notification"
            val description = doc.getString("description") ?: ""
            val category = doc.getString("category") ?: "SYSTEM"
            val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
            val isRead = doc.getBoolean("isRead") ?: false
            val targetQuizId = doc.getString("targetQuizId")
            val targetBatchId = doc.getString("targetBatchId")
            val senderId = doc.getString("senderId")
            val senderName = doc.getString("senderName")

            AppNotification(
                id = id,
                recipientId = recipientId,
                recipientRole = role,
                title = title,
                description = description,
                category = category,
                timestamp = timestamp,
                isRead = isRead,
                targetQuizId = targetQuizId,
                targetBatchId = targetBatchId,
                senderId = senderId,
                senderName = senderName
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse notification ${doc.id}", e)
            null
        }
    }

    override fun getNotificationsFlow(userId: String): Flow<List<AppNotification>> {
        return _notifications.map { map ->
            map.values
                .filter { it.recipientId == userId }
                .sortedByDescending { it.timestamp }
        }
    }

    override suspend fun getNotifications(userId: String): List<AppNotification> {
        return mutex.withLock {
            _notifications.value.values
                .filter { it.recipientId == userId }
                .sortedByDescending { it.timestamp }
        }
    }

    override suspend fun sendNotification(notification: AppNotification): Result<Unit> {
        val finalNotification = if (notification.id.isBlank()) {
            notification.copy(id = UUID.randomUUID().toString())
        } else {
            notification
        }

        mutex.withLock {
            val current = _notifications.value.toMutableMap()
            current[finalNotification.id] = finalNotification
            _notifications.value = current
        }

        firestore?.let { fs ->
            try {
                val data = mapOf(
                    "id" to finalNotification.id,
                    "recipientId" to finalNotification.recipientId,
                    "recipientRole" to finalNotification.recipientRole.name,
                    "title" to finalNotification.title,
                    "description" to finalNotification.description,
                    "category" to finalNotification.category,
                    "timestamp" to finalNotification.timestamp,
                    "isRead" to finalNotification.isRead,
                    "targetQuizId" to finalNotification.targetQuizId,
                    "targetBatchId" to finalNotification.targetBatchId,
                    "senderId" to finalNotification.senderId,
                    "senderName" to finalNotification.senderName
                )
                fs.collection(COLLECTION_NOTIFICATIONS)
                    .document(finalNotification.id)
                    .set(data, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore write warning for sendNotification: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        mutex.withLock {
            val current = _notifications.value.toMutableMap()
            val existing = current[notificationId]
            if (existing != null) {
                current[notificationId] = existing.copy(isRead = true)
                _notifications.value = current
            }
        }

        firestore?.let { fs ->
            try {
                fs.collection(COLLECTION_NOTIFICATIONS)
                    .document(notificationId)
                    .update("isRead", true)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore update warning for markAsRead: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override suspend fun markAllAsRead(userId: String): Result<Unit> {
        val affectedIds = mutableListOf<String>()
        mutex.withLock {
            val current = _notifications.value.toMutableMap()
            for ((id, item) in current) {
                if (item.recipientId == userId && !item.isRead) {
                    current[id] = item.copy(isRead = true)
                    affectedIds.add(id)
                }
            }
            _notifications.value = current
        }

        firestore?.let { fs ->
            try {
                val batch = fs.batch()
                for (id in affectedIds) {
                    batch.update(fs.collection(COLLECTION_NOTIFICATIONS).document(id), "isRead", true)
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore batch update warning for markAllAsRead: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        mutex.withLock {
            val current = _notifications.value.toMutableMap()
            current.remove(notificationId)
            _notifications.value = current
        }

        firestore?.let { fs ->
            try {
                fs.collection(COLLECTION_NOTIFICATIONS)
                    .document(notificationId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore delete warning for deleteNotification: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override suspend fun clearReadNotifications(userId: String): Result<Unit> {
        val removedIds = mutableListOf<String>()
        mutex.withLock {
            val current = _notifications.value.toMutableMap()
            val iterator = current.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.value.recipientId == userId && entry.value.isRead) {
                    removedIds.add(entry.key)
                    iterator.remove()
                }
            }
            _notifications.value = current
        }

        firestore?.let { fs ->
            try {
                val batch = fs.batch()
                for (id in removedIds) {
                    batch.delete(fs.collection(COLLECTION_NOTIFICATIONS).document(id))
                }
                batch.commit().await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore batch delete warning for clearReadNotifications: ${e.message}")
            }
        }

        return Result.success(Unit)
    }

    override fun getUnreadCountFlow(userId: String): Flow<Int> {
        return _notifications.map { map ->
            map.values.count { it.recipientId == userId && !it.isRead }
        }
    }
}

