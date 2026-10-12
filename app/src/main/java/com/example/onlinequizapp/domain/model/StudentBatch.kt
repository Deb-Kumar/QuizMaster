package com.example.onlinequizapp.domain.model

enum class StudentBatchStatus {
    ACTIVE,
    REMOVED,
    LEFT
}

/**
 * Represents a many-to-many relationship association between a Student and a Batch.
 *
 * @param id Internal association ID (conventionally "${studentId}_${batchId}").
 * @param studentId The unique ID of the enrolled student.
 * @param batchId The internal ID of the batch.
 * @param batchCode The public joining code used at enrollment time.
 * @param joinedAt Enrollment timestamp in milliseconds.
 * @param status Membership status (ACTIVE, REMOVED by teacher, or LEFT by student).
 */
data class StudentBatch(
    val id: String,
    val studentId: String,
    val batchId: String,
    val batchCode: String,
    val teacherId: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val status: StudentBatchStatus = StudentBatchStatus.ACTIVE
) {
    val isActive: Boolean get() = status == StudentBatchStatus.ACTIVE
}
