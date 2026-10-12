package com.example.onlinequizapp.domain.model

enum class AssignmentType {
    BATCH,
    INDIVIDUAL_STUDENT,
    PUBLIC
}

/**
 * Represents an assignment of a Quiz to either a class Batch, an individual Student, or Public.
 *
 * @param id Unique assignment ID (UUID)
 * @param quizId ID of the target quiz
 * @param batchId ID of the batch if assigned to a batch, null otherwise
 * @param studentId ID of the student if assigned to an individual student, null otherwise
 * @param assignedBy Teacher ID who published/assigned this quiz
 * @param assignedAt Timestamp when the assignment was created
 * @param status Lifecycle status (ACTIVE, COMPLETED, REVOKED)
 */
data class QuizAssignment(
    val id: String,
    val quizId: String,
    val batchId: String? = null,
    val studentId: String? = null,
    val assignedBy: String,
    val assignedAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
) {
    val type: AssignmentType
        get() = when {
            batchId != null -> AssignmentType.BATCH
            studentId != null -> AssignmentType.INDIVIDUAL_STUDENT
            else -> AssignmentType.PUBLIC
        }

    val isBatchAssignment: Boolean get() = batchId != null
    val isIndividualAssignment: Boolean get() = studentId != null
    val isPublicAssignment: Boolean get() = batchId == null && studentId == null
}
