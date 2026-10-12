package com.example.onlinequizapp.domain

import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.domain.model.BatchAccessMode
import com.example.onlinequizapp.domain.model.BatchJoinResult
import com.example.onlinequizapp.domain.model.BatchStatus
import com.example.onlinequizapp.domain.model.JoinRequestStatus
import com.example.onlinequizapp.domain.model.StudentBatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchManagementTest {

    private val repository = BatchRepositoryImpl.instance

    @Test
    fun testTeacherCreatesBatchWithValid6CharCode() = runBlocking {
        val teacherId = "teacher_unit_test_${System.currentTimeMillis()}"
        val batchName = "MCA Semester 1 Test"
        val description = "Master of Computer Applications - Algorithms"

        val createResult = repository.createBatch(
            teacherId = teacherId,
            name = batchName,
            description = description,
            accessMode = BatchAccessMode.PUBLIC
        )

        assertTrue("Batch creation must succeed", createResult.isSuccess)
        val batch = createResult.getOrThrow()

        // 1. Verify 6-character alphanumeric code
        assertEquals("Code must be exactly 6 characters", 6, batch.batchCode.length)
        assertTrue(
            "Code must contain only uppercase letters and digits",
            batch.batchCode.all { it.isUpperCase() || it.isDigit() }
        )

        // 2. Internal ID must NOT be the join code
        assertNotEquals("Internal ID must not equal public join code", batch.batchCode, batch.id)
        assertEquals("Teacher ID must match", teacherId, batch.teacherId)
        assertEquals("Batch name must match", batchName, batch.name)
        assertEquals("Status must be ACTIVE", BatchStatus.ACTIVE, batch.status)
        assertEquals("Initial student count must be 0", 0, batch.studentCount)
        assertEquals("Access mode must be PUBLIC", BatchAccessMode.PUBLIC, batch.accessMode)
    }

    @Test
    fun testCodeGenerationAvoidsCollisionsAcrossMultipleBatches() = runBlocking {
        val teacherId = "teacher_collision_test"
        val codes = mutableSetOf<String>()

        repeat(15) { i ->
            val res = repository.createBatch(
                teacherId = teacherId,
                name = "Batch Set $i",
                description = "Automated test batch"
            )
            assertTrue("Batch creation #$i must succeed", res.isSuccess)
            val code = res.getOrThrow().batchCode
            assertEquals("Code must be 6 characters", 6, code.length)
            assertFalse("Generated code must be unique (collision check)", codes.contains(code))
            codes.add(code)
        }
    }

    @Test
    fun testStudentJoinsPublicBatchFlow() = runBlocking {
        val teacherId = "teacher_join_flow"
        val batch = repository.createBatch(
            teacherId = teacherId,
            name = "Data Structures Batch",
            accessMode = BatchAccessMode.PUBLIC
        ).getOrThrow()

        val studentId = "student_alex_${System.currentTimeMillis()}"

        // 1. Student joins with matching code (testing case-insensitivity)
        val lowercaseCode = batch.batchCode.lowercase()
        val joinResult = repository.joinBatch(studentId = studentId, batchCode = lowercaseCode)
        assertTrue("Student must be able to join with valid code", joinResult.isSuccess)

        val joinRes = joinResult.getOrThrow()
        assertTrue("Result must be Joined for PUBLIC batch", joinRes is BatchJoinResult.Joined)
        val updatedBatch = (joinRes as BatchJoinResult.Joined).batch
        assertEquals("Student count must increment to 1", 1, updatedBatch.studentCount)

        // 2. Student is now enrolled in this batch
        val studentBatches = repository.getBatchesForStudent(studentId).first()
        assertTrue("Enrolled batches must contain the newly joined batch", studentBatches.any { it.id == batch.id })

        // 3. Duplicate join attempt should fail with proper message
        val duplicateResult = repository.joinBatch(studentId = studentId, batchCode = batch.batchCode)
        assertTrue("Duplicate join attempt must fail", duplicateResult.isFailure)
        val ex = duplicateResult.exceptionOrNull()
        assertTrue(
            "Exception message must mention already a member",
            ex?.message?.contains("already a member", ignoreCase = true) == true
        )
    }

    @Test
    fun testPrivateBatchJoinRequestFlowAndTeacherAccept() = runBlocking {
        val teacherId = "teacher_private_${System.currentTimeMillis()}"
        val batch = repository.createBatch(
            teacherId = teacherId,
            name = "Advanced Algorithms (Private)",
            accessMode = BatchAccessMode.PRIVATE
        ).getOrThrow()

        val studentId = "student_priv_${System.currentTimeMillis()}"

        // 1. Student enters code for PRIVATE batch -> RequestSubmitted (NOT joined yet)
        val joinResult = repository.joinBatch(
            studentId = studentId,
            batchCode = batch.batchCode,
            studentName = "Rahul Kumar",
            studentEmail = "rahul@example.com"
        )
        assertTrue("Join call on private batch must succeed", joinResult.isSuccess)

        val joinRes = joinResult.getOrThrow()
        assertTrue("Result must be RequestSubmitted", joinRes is BatchJoinResult.RequestSubmitted)
        val submitted = joinRes as BatchJoinResult.RequestSubmitted
        assertEquals("Batch ID must match", batch.id, submitted.batch.id)
        assertEquals("Join request status must be PENDING", JoinRequestStatus.PENDING, submitted.request.status)
        assertEquals("Teacher ID must match batch owner", teacherId, submitted.request.teacherId)

        // 2. Student is NOT yet in the enrolled list
        val enrolledBatches = repository.getBatchesForStudent(studentId).first()
        assertFalse("Student must not be enrolled while request is pending", enrolledBatches.any { it.id == batch.id })

        // 3. Student attempts duplicate request -> must be rejected
        val dupResult = repository.joinBatch(studentId = studentId, batchCode = batch.batchCode)
        assertTrue("Duplicate pending request must fail", dupResult.isFailure)
        assertTrue(
            "Error must mention pending request",
            dupResult.exceptionOrNull()?.message?.contains("already have a pending request", ignoreCase = true) == true
        )

        // 4. Teacher views join requests
        val teacherRequests = repository.getJoinRequestsForTeacher(teacherId).first()
        assertTrue("Teacher must see the pending request", teacherRequests.any { it.id == submitted.request.id })

        // 5. Unauthorized teacher cannot accept request
        val unauthorizedAccept = repository.acceptJoinRequest(submitted.request.id, "imposter_teacher")
        assertTrue("Imposter cannot accept request", unauthorizedAccept.isFailure)

        // 6. Authorized teacher accepts the request
        val acceptResult = repository.acceptJoinRequest(submitted.request.id, teacherId)
        assertTrue("Teacher acceptance must succeed", acceptResult.isSuccess)

        // 7. Student is now actively enrolled!
        val enrolledAfterAccept = repository.getBatchesForStudent(studentId).first()
        assertTrue("Student must be enrolled after acceptance", enrolledAfterAccept.any { it.id == batch.id })

        // 8. Teacher batch student count is incremented
        val refreshedBatch = repository.getBatchById(batch.id).getOrThrow()
        assertEquals("Student count must be 1 after acceptance", 1, refreshedBatch?.studentCount)
    }

    @Test
    fun testPrivateBatchJoinRequestTeacherReject() = runBlocking {
        val teacherId = "teacher_reject_${System.currentTimeMillis()}"
        val batch = repository.createBatch(
            teacherId = teacherId,
            name = "Security Systems",
            accessMode = BatchAccessMode.PRIVATE
        ).getOrThrow()

        val studentId = "student_rej_${System.currentTimeMillis()}"

        // 1. Submit request
        val joinRes = repository.joinBatch(
            studentId = studentId,
            batchCode = batch.batchCode,
            studentName = "Bob"
        ).getOrThrow() as BatchJoinResult.RequestSubmitted

        // 2. Teacher rejects request
        val rejectResult = repository.rejectJoinRequest(
            requestId = joinRes.request.id,
            teacherId = teacherId,
            rejectionReason = "Not enrolled in prerequisite course."
        )
        assertTrue("Rejection must succeed", rejectResult.isSuccess)

        // 3. Student is NOT enrolled
        val enrolled = repository.getBatchesForStudent(studentId).first()
        assertFalse("Student must not be enrolled", enrolled.any { it.id == batch.id })

        // 4. Student can submit request again after rejection (re-application policy)
        val reapplyResult = repository.joinBatch(studentId = studentId, batchCode = batch.batchCode)
        assertTrue("Student must be allowed to submit a new request after prior rejection", reapplyResult.isSuccess)
    }

    @Test
    fun testStudentJoinFailsOnInvalidOrArchivedCode() = runBlocking {
        val studentId = "student_invalid_test"

        // 1. Invalid code
        val invalidResult = repository.joinBatch(studentId = studentId, batchCode = "ZZZZZZ")
        assertTrue("Invalid code join must fail", invalidResult.isFailure)
        assertEquals("Invalid or expired batch code.", invalidResult.exceptionOrNull()?.message)

        // 2. Deleted batch cannot be joined
        val teacherId = "teacher_deleted_test"
        val batch = repository.createBatch(teacherId = teacherId, name = "Old Course").getOrThrow()
        repository.deleteBatch(batch.id, teacherId)

        val deletedJoinResult = repository.joinBatch(studentId = studentId, batchCode = batch.batchCode)
        assertTrue("Joining deleted batch must fail", deletedJoinResult.isFailure)
        assertEquals("This batch is no longer accepting students.", deletedJoinResult.exceptionOrNull()?.message)
    }

    @Test
    fun testTeacherBatchManagementAndStudentRemoval() = runBlocking {
        val teacherId = "teacher_mgmt_${System.currentTimeMillis()}"
        val batch = repository.createBatch(teacherId = teacherId, name = "Web Dev 101").getOrThrow()
        val studentId = "student_temp_${System.currentTimeMillis()}"

        // Student joins
        repository.joinBatch(studentId = studentId, batchCode = batch.batchCode)

        // Teacher removes student
        val removeResult = repository.removeStudentFromBatch(
            batchId = batch.id,
            studentId = studentId,
            teacherId = teacherId
        )
        assertTrue("Teacher removal of student must succeed", removeResult.isSuccess)

        // Verify batch student count decremented
        val refreshedBatch = repository.getBatchById(batch.id).getOrThrow()
        assertNotNull("Batch must exist", refreshedBatch)
        assertEquals("Student count must be 0 after removal", 0, refreshedBatch?.studentCount)

        // Unauthorized teacher cannot remove student
        val unauthorizedResult = repository.removeStudentFromBatch(
            batchId = batch.id,
            studentId = studentId,
            teacherId = "imposter_teacher"
        )
        assertTrue("Unauthorized removal must fail", unauthorizedResult.isFailure)
    }

    @Test
    fun testBatchUpdateAndDeleteLifecycle() = runBlocking {
        val teacherId = "teacher_lifecycle"
        val batch = repository.createBatch(teacherId = teacherId, name = "Original Name").getOrThrow()

        // 1. Update batch
        val updateRes = repository.updateBatch(
            batchId = batch.id,
            teacherId = teacherId,
            newName = "Updated Name",
            description = "New Description"
        )
        assertTrue("Update must succeed", updateRes.isSuccess)
        assertEquals("Updated Name", updateRes.getOrThrow().name)

        // 2. Delete batch
        val deleteRes = repository.deleteBatch(batch.id, teacherId)
        assertTrue("Delete must succeed", deleteRes.isSuccess)
        val deleted = repository.getBatchById(batch.id).getOrThrow()
        assertEquals(BatchStatus.DELETED, deleted?.status)
    }
}
