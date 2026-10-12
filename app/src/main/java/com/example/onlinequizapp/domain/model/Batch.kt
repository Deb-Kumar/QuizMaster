package com.example.onlinequizapp.domain.model

enum class BatchStatus {
    ACTIVE,
    DELETED
}

enum class BatchAccessMode {
    PUBLIC,
    PRIVATE
}

/**
 * Represents a study/class batch created and managed by a Teacher.
 *
 * @param id Internal database primary key (UUID / Firestore Document ID). Never exposed as join code.
 * @param teacherId The ID of the teacher who owns this batch.
 * @param name Display name of the batch (e.g. "MCA Semester 1").
 * @param batchCode 6-character unique uppercase alphanumeric joining code (e.g. "A7K29P").
 * @param description Optional description or subject details.
 * @param accessMode Access mode: PUBLIC (direct join) or PRIVATE (requires teacher approval).
 * @param studentCount Current count of active enrolled students.
 * @param status Batch lifecycle status (ACTIVE, DELETED).
 * @param createdAt Creation timestamp in milliseconds.
 * @param updatedAt Last updated timestamp in milliseconds.
 */
data class Batch(
    val id: String,
    val teacherId: String,
    val name: String,
    val batchCode: String,
    val description: String = "",
    val accessMode: BatchAccessMode = BatchAccessMode.PUBLIC,
    val studentCount: Int = 0,
    val status: BatchStatus = BatchStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isPublic: Boolean get() = accessMode == BatchAccessMode.PUBLIC
    val isPrivate: Boolean get() = accessMode == BatchAccessMode.PRIVATE
    val isActive: Boolean get() = status == BatchStatus.ACTIVE
}

/**
 * Enriched presentation model for a student enrolled in a batch.
 */
data class EnrolledStudent(
    val studentId: String,
    val name: String,
    val email: String,
    val joinedAt: Long = System.currentTimeMillis(),
    val quizzesCompleted: Int = 0,
    val avgScore: Int = 0,
    val avatarUrl: String? = null,
    val status: StudentBatchStatus = StudentBatchStatus.ACTIVE,
    val degree: String = ""
)
