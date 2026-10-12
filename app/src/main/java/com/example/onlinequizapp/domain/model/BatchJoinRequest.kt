package com.example.onlinequizapp.domain.model

enum class JoinRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED
}

/**
 * Entity representing an access request submitted by a student for a PRIVATE batch.
 *
 * @param id Unique identifier (UUID).
 * @param batchId The ID of the private batch being requested.
 * @param batchName Name of the batch for quick display.
 * @param studentId The authenticated student's user ID.
 * @param studentName Display name of the student.
 * @param studentEmail Contact email of the student.
 * @param teacherId Authoritative owner of the batch, derived securely from batch.teacherId.
 * @param status Lifecycle status (PENDING, ACCEPTED, REJECTED, CANCELLED).
 * @param requestedAt Timestamp of submission.
 * @param respondedAt Timestamp of teacher decision (accept/reject).
 * @param respondedBy Teacher ID who made the decision.
 * @param rejectionReason Optional reason provided upon rejection.
 */
data class BatchJoinRequest(
    val id: String,
    val batchId: String,
    val batchName: String,
    val studentId: String,
    val studentName: String,
    val studentEmail: String,
    val teacherId: String,
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val respondedBy: String? = null,
    val rejectionReason: String? = null
) {
    val isPending: Boolean get() = status == JoinRequestStatus.PENDING
    val isAccepted: Boolean get() = status == JoinRequestStatus.ACCEPTED
    val isRejected: Boolean get() = status == JoinRequestStatus.REJECTED
}

/**
 * Result returned when a student attempts to join a batch using its 6-character code.
 */
sealed interface BatchJoinResult {
    /** The batch is PUBLIC; membership was created immediately. */
    data class Joined(val batch: Batch) : BatchJoinResult

    /** The batch is PRIVATE; a join request was submitted for teacher approval. */
    data class RequestSubmitted(val batch: Batch, val request: BatchJoinRequest) : BatchJoinResult
}

