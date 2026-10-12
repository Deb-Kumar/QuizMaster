package com.example.onlinequizapp.domain.repository

import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.BatchAccessMode
import com.example.onlinequizapp.domain.model.BatchJoinRequest
import com.example.onlinequizapp.domain.model.BatchJoinResult
import com.example.onlinequizapp.domain.model.EnrolledStudent
import kotlinx.coroutines.flow.Flow

interface BatchRepository {
    /**
     * Creates a new Batch owned by [teacherId] with a guaranteed unique 6-character alphanumeric code.
     */
    suspend fun createBatch(
        teacherId: String,
        name: String,
        description: String = "",
        accessMode: BatchAccessMode = BatchAccessMode.PUBLIC
    ): Result<Batch>

    /**
     * Returns a live flow of all active batches owned by [teacherId].
     */
    fun getBatchesByTeacher(teacherId: String): Flow<List<Batch>>

    /**
     * Returns a live flow of all cached batches mapped by ID.
     */
    fun getAllBatchesFlow(): Flow<Map<String, Batch>>

    /**
     * Looks up a batch by its internal ID.
     * When [teacherId] is provided, validates that the batch is owned by that teacher.
     */
    suspend fun getBatchById(batchId: String): Result<Batch?>
    suspend fun getBatchById(batchId: String, teacherId: String?): Result<Batch?>

    /**
     * Looks up a batch by its public 6-character joining code.
     */
    suspend fun getBatchByCode(batchCode: String): Result<Batch?>

    /**
     * Updates batch details (name, description, accessMode) ensuring caller owns the batch.
     */
    suspend fun updateBatch(
        batchId: String,
        teacherId: String,
        newName: String,
        description: String = "",
        accessMode: BatchAccessMode = BatchAccessMode.PUBLIC
    ): Result<Batch>

    /**
     * Soft-deletes a batch.
     */
    suspend fun deleteBatch(batchId: String, teacherId: String): Result<Unit>

    /**
     * Validates [batchCode] and processes student enrollment.
     * - For PUBLIC batches: adds student directly as an active member.
     * - For PRIVATE batches: creates a pending join request awaiting teacher approval.
     */
    suspend fun joinBatch(
        studentId: String,
        batchCode: String,
        studentName: String = "",
        studentEmail: String = ""
    ): Result<BatchJoinResult>

    /**
     * Returns a live flow of all active batches that [studentId] is enrolled in.
     */
    fun getBatchesForStudent(studentId: String): Flow<List<Batch>>

    /**
     * Returns the list of enrolled students inside [batchId], authorized for [teacherId].
     */
    suspend fun getStudentsInBatch(batchId: String, teacherId: String): Result<List<EnrolledStudent>>

    /**
     * Returns the list of enrolled classmates inside [batchId] for [studentId] (for batch leaderboard).
     */
    suspend fun getStudentsInBatchForStudent(batchId: String, studentId: String): Result<List<EnrolledStudent>>

    /**
     * Returns all distinct enrolled students across all batches owned by [teacherId].
     */
    suspend fun getStudentsForTeacher(teacherId: String): Result<List<EnrolledStudent>>

    /**
     * Removes a student from a batch (by teacher).
     */
    suspend fun removeStudentFromBatch(
        batchId: String,
        studentId: String,
        teacherId: String
    ): Result<Unit>

    /**
     * Allows a student to voluntarily leave a batch.
     */
    suspend fun leaveBatch(batchId: String, studentId: String): Result<Unit>

    /**
     * Returns a live flow of join requests for batches owned by [teacherId].
     */
    fun getJoinRequestsForTeacher(teacherId: String): Flow<List<BatchJoinRequest>>

    /**
     * Returns a live flow of join requests submitted by [studentId].
     */
    fun getJoinRequestsForStudent(studentId: String): Flow<List<BatchJoinRequest>>

    /**
     * Accepts a student's pending join request, granting them active batch membership.
     */
    suspend fun acceptJoinRequest(requestId: String, teacherId: String): Result<Unit>

    /**
     * Rejects a student's pending join request without granting membership.
     */
    suspend fun rejectJoinRequest(
        requestId: String,
        teacherId: String,
        rejectionReason: String? = null
    ): Result<Unit>

    /**
     * Refreshes join requests from remote cloud storage.
     */
    suspend fun refreshJoinRequests(teacherId: String? = null): List<BatchJoinRequest>

    /**
     * Accepts all pending join requests for the teacher's batches.
     * Returns Result with the count of successfully accepted requests.
     */
    suspend fun acceptAllJoinRequests(
        teacherId: String,
        requestIds: List<String>? = null
    ): Result<Int>

    /**
     * Rejects all pending join requests for the teacher's batches.
     * Returns Result with the count of successfully rejected requests.
     */
    suspend fun rejectAllJoinRequests(
        teacherId: String,
        requestIds: List<String>? = null,
        rejectionReason: String? = null
    ): Result<Int>
}

