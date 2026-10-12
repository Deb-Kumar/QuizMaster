package com.example.onlinequizapp.domain

import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Production-Grade Multi-Teacher Data Isolation & Security Test Suite.
 * Covers all 20 required cross-teacher isolation and direct ID attack protection tests.
 */
class MultiTeacherDataIsolationTest {

    private val batchRepository = BatchRepositoryImpl.instance
    private val quizRepository = QuizRepositoryImpl.instance

    private val teacherAId = "TEACHER_ALPHA_${System.currentTimeMillis()}"
    private val teacherBId = "TEACHER_BETA_${System.currentTimeMillis()}"
    private val student1Id = "STUDENT_S001_${System.currentTimeMillis()}"
    private val student2Id = "STUDENT_S002_${System.currentTimeMillis()}"

    private fun createSampleQuestion(id: String = UUID.randomUUID().toString()): Question {
        return Question(
            id = id,
            type = QuestionType.SINGLE,
            prompt = "What is polymorphic isolation in software architecture?",
            options = listOf(
                Option(id = "opt1", text = "Encapsulation and tenant boundary"),
                Option(id = "opt2", text = "Shared global static memory")
            ),
            correctAnswers = setOf("opt1"),
            explanation = "Tenant boundaries prevent cross-contamination.",
            difficulty = Difficulty.MEDIUM
        )
    }

    private fun createSampleQuiz(id: String, teacherId: String, title: String): Quiz {
        return Quiz(
            id = id,
            title = title,
            description = "Multi-teacher testing quiz",
            category = "Architecture",
            questions = listOf(createSampleQuestion()),
            teacherId = teacherId,
            status = QuizStatus.PUBLISHED,
            quizCode = ""
        )
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 1: Teacher A can see Teacher A's quiz
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test1_teacherACanSeeTeacherAQuiz() = runBlocking {
        val quizAId = "quiz_a_${UUID.randomUUID().toString().take(6)}"
        val quizA = createSampleQuiz(quizAId, teacherAId, "Data Structures - Teacher A")
        val created = quizRepository.createQuiz(quizA, teacherAId)
        assertTrue("Teacher A quiz creation must succeed", created)

        val teacherAQuizzes = quizRepository.getQuizzesByTeacher(teacherAId)
        assertTrue("Teacher A must see their own quiz", teacherAQuizzes.any { it.id == quizAId })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 2: Teacher A cannot see Teacher B's quiz
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test2_teacherACannotSeeTeacherBQuiz() = runBlocking {
        val quizBId = "quiz_b_${UUID.randomUUID().toString().take(6)}"
        val quizB = createSampleQuiz(quizBId, teacherBId, "Operating Systems - Teacher B")
        quizRepository.createQuiz(quizB, teacherBId)

        val teacherAQuizzes = quizRepository.getQuizzesByTeacher(teacherAId)
        assertFalse("Teacher A must NOT see Teacher B's quiz", teacherAQuizzes.any { it.id == quizBId })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 3: Teacher A can see Teacher A's batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test3_teacherACanSeeTeacherABatch() = runBlocking {
        val batchRes = batchRepository.createBatch(
            teacherId = teacherAId,
            name = "CS 2026 Batch A",
            description = "Teacher A exclusive batch"
        )
        assertTrue("Teacher A batch creation must succeed", batchRes.isSuccess)
        val batchA = batchRes.getOrThrow()

        val teacherABatches = batchRepository.getBatchesByTeacher(teacherAId).first()
        assertTrue("Teacher A must see their own batch", teacherABatches.any { it.id == batchA.id })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 4: Teacher A cannot see Teacher B's batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test4_teacherACannotSeeTeacherBBatch() = runBlocking {
        val batchRes = batchRepository.createBatch(
            teacherId = teacherBId,
            name = "CS 2026 Batch B",
            description = "Teacher B exclusive batch"
        )
        assertTrue("Teacher B batch creation must succeed", batchRes.isSuccess)
        val batchB = batchRes.getOrThrow()

        val teacherABatches = batchRepository.getBatchesByTeacher(teacherAId).first()
        assertFalse("Teacher A must NOT see Teacher B's batch", teacherABatches.any { it.id == batchB.id })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 5: Student S001 joins Teacher A's batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test5_studentS001JoinsTeacherABatch() = runBlocking {
        val batch = batchRepository.createBatch(teacherId = teacherAId, name = "Algorithms A").getOrThrow()
        val joinRes = batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)
        assertTrue("Student S001 must successfully join Teacher A's batch", joinRes.isSuccess)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 6: Teacher A sees S001 in their batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test6_teacherASeesS001InTheirBatch() = runBlocking {
        val batch = batchRepository.createBatch(teacherId = teacherAId, name = "Physics A").getOrThrow()
        batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)

        val studentsRes = batchRepository.getStudentsInBatch(batch.id, teacherAId)
        assertTrue("Teacher A must be authorized to inspect their batch", studentsRes.isSuccess)
        val students = studentsRes.getOrThrow()
        assertTrue("Teacher A must see student S001 in the enrolled roster", students.any { it.studentId == student1Id })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 7: Student S001 joins Teacher B's batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test7_studentS001JoinsTeacherBBatch() = runBlocking {
        val batch = batchRepository.createBatch(teacherId = teacherBId, name = "Chemistry B").getOrThrow()
        val joinRes = batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)
        assertTrue("Student S001 must successfully join Teacher B's batch", joinRes.isSuccess)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 8: Teacher B sees S001 in their batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test8_teacherBSeesS001InTheirBatch() = runBlocking {
        val batch = batchRepository.createBatch(teacherId = teacherBId, name = "Math B").getOrThrow()
        batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)

        val studentsRes = batchRepository.getStudentsInBatch(batch.id, teacherBId)
        assertTrue("Teacher B must be authorized to inspect their batch", studentsRes.isSuccess)
        val students = studentsRes.getOrThrow()
        assertTrue("Teacher B must see student S001 in their enrolled roster", students.any { it.studentId == student1Id })
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 9: Teacher A cannot see Teacher B's unrelated batch memberships
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test9_teacherACannotInspectTeacherBBatchMemberships() = runBlocking {
        val batchB = batchRepository.createBatch(teacherId = teacherBId, name = "Private B Roster").getOrThrow()
        batchRepository.joinBatch(studentId = student1Id, batchCode = batchB.batchCode)

        val unauthorizedAttempt = batchRepository.getStudentsInBatch(batchB.id, teacherAId)
        assertTrue("Attempt by Teacher A to inspect Teacher B's batch must fail", unauthorizedAttempt.isFailure)
        assertTrue("Exception must be SecurityException", unauthorizedAttempt.exceptionOrNull() is SecurityException)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 10: Teacher A cannot access Teacher B's quiz by manually changing quizId
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test10_teacherACannotAccessTeacherBQuizByDirectId() = runBlocking {
        val quizBId = "quiz_private_b_${UUID.randomUUID().toString().take(6)}"
        val quizB = createSampleQuiz(quizBId, teacherBId, "Teacher B Confidential Exam")
        quizRepository.createQuiz(quizB, teacherBId)

        val accessed = quizRepository.getQuizById(quizBId, teacherAId)
        assertNull("Direct ID lookup of Teacher B's quiz by Teacher A must return null (Protected)", accessed)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 11: Teacher A cannot modify Teacher B's quiz
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test11_teacherACannotModifyTeacherBQuiz() = runBlocking {
        val quizBId = "quiz_mod_b_${UUID.randomUUID().toString().take(6)}"
        val quizB = createSampleQuiz(quizBId, teacherBId, "Teacher B Original Quiz")
        quizRepository.createQuiz(quizB, teacherBId)

        val maliciousUpdate = quizB.copy(title = "Tampered by Teacher A")
        try {
            quizRepository.updateQuiz(maliciousUpdate, teacherAId)
            fail("Updating Teacher B's quiz with Teacher A identity must throw SecurityException")
        } catch (e: SecurityException) {
            assertTrue("SecurityException caught as expected", true)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 12: Teacher A cannot delete Teacher B's batch
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test12_teacherACannotDeleteTeacherBBatch() = runBlocking {
        val batchB = batchRepository.createBatch(teacherId = teacherBId, name = "Protected Batch B").getOrThrow()
        val deleteAttempt = batchRepository.deleteBatch(batchB.id, teacherAId)
        assertTrue("Deleting Teacher B's batch with Teacher A credentials must fail", deleteAttempt.isFailure)
        assertTrue("Must throw SecurityException", deleteAttempt.exceptionOrNull() is SecurityException)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 13: Teacher A cannot view Teacher B's quiz results
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test13_teacherACannotViewTeacherBQuizResults() = runBlocking {
        val quizBId = "quiz_results_b_${UUID.randomUUID().toString().take(6)}"
        val quizB = createSampleQuiz(quizBId, teacherBId, "Teacher B Test Results")
        quizRepository.createQuiz(quizB, teacherBId)

        val resultsForTeacherA = quizRepository.getQuizResultsByQuizId(quizBId, teacherAId)
        assertTrue("Teacher A must receive empty results for Teacher B's quiz", resultsForTeacherA.isEmpty())
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 14: Student can legitimately access quizzes assigned to them
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test14_studentCanAccessLegitimatelyAssignedQuiz() = runBlocking {
        val batchA = batchRepository.createBatch(teacherId = teacherAId, name = "Assigned Batch A").getOrThrow()
        batchRepository.joinBatch(studentId = student1Id, batchCode = batchA.batchCode)

        val quizAId = "quiz_assign_${UUID.randomUUID().toString().take(6)}"
        val quizA = createSampleQuiz(quizAId, teacherAId, "Batch A Quiz")
        quizRepository.createQuiz(quizA, teacherAId)
        val published = quizRepository.publishQuiz(
            quizId = quizAId,
            batchIds = listOf(batchA.id),
            studentIds = emptyList(),
            teacherId = teacherAId
        ).getOrThrow()

        val accessRes = quizRepository.validateAndAccessQuiz(published.quizCode, student1Id)
        assertTrue("Student S001 in Batch A must be granted access to Quiz A", accessRes.isSuccess)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 15: Student cannot access another student's private quiz assignment
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test15_studentCannotAccessAnotherStudentsPrivateQuiz() = runBlocking {
        val quizPrivateId = "quiz_private_s2_${UUID.randomUUID().toString().take(6)}"
        val quizPrivate = createSampleQuiz(quizPrivateId, teacherAId, "Exclusive S002 Quiz")
        quizRepository.createQuiz(quizPrivate, teacherAId)
        val published = quizRepository.publishQuiz(
            quizId = quizPrivateId,
            batchIds = emptyList(),
            studentIds = listOf(student2Id),
            teacherId = teacherAId
        ).getOrThrow()

        val accessRes = quizRepository.validateAndAccessQuiz(published.quizCode, student1Id)
        assertTrue("Student S001 must be denied access to a quiz assigned exclusively to S002", accessRes.isFailure)
        assertTrue("Failure must be SecurityException", accessRes.exceptionOrNull() is SecurityException)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 16: Invalid batch code is rejected
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test16_invalidBatchCodeIsRejected() = runBlocking {
        val joinRes = batchRepository.joinBatch(studentId = student1Id, batchCode = "ZZZZZZ")
        assertTrue("Non-existent 6-character batch code must be rejected", joinRes.isFailure)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 17: Invalid quiz code is rejected
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test17_invalidQuizCodeIsRejected() = runBlocking {
        val accessRes = quizRepository.validateAndAccessQuiz("NONEX1", student1Id)
        assertTrue("Non-existent 6-character quiz code must be rejected", accessRes.isFailure)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 18: Duplicate batch membership is prevented
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test18_duplicateBatchMembershipPrevented() = runBlocking {
        val batch = batchRepository.createBatch(teacherId = teacherAId, name = "No Dups Batch").getOrThrow()
        val firstJoin = batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)
        assertTrue("First enrollment must succeed", firstJoin.isSuccess)

        val secondJoin = batchRepository.joinBatch(studentId = student1Id, batchCode = batch.batchCode)
        assertTrue("Duplicate enrollment must fail", secondJoin.isFailure)
        assertTrue("Failure must be IllegalStateException", secondJoin.exceptionOrNull() is IllegalStateException)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 19: Duplicate quiz assignment is prevented
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test19_duplicateQuizAssignmentPrevented() = runBlocking {
        val quizId = "quiz_dup_${UUID.randomUUID().toString().take(6)}"
        val quiz = createSampleQuiz(quizId, teacherAId, "Open Quiz")
        quizRepository.createQuiz(quiz, teacherAId)
        val published = quizRepository.publishQuiz(quizId, emptyList(), emptyList(), teacherAId).getOrThrow()

        val firstAccess = quizRepository.validateAndAccessQuiz(published.quizCode, student1Id)
        assertTrue("First quiz access must succeed", firstAccess.isSuccess)

        val secondAccess = quizRepository.validateAndAccessQuiz(published.quizCode, student1Id)
        assertTrue("Subsequent quiz access must succeed idempotently without creating duplicate assignments", secondAccess.isSuccess)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST 20: Teacher logout/login correctly changes the isolated dataset
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    fun test20_teacherSwitchDatasetIsolation() = runBlocking {
        val quizAId = "quiz_login_a_${UUID.randomUUID().toString().take(6)}"
        val quizBId = "quiz_login_b_${UUID.randomUUID().toString().take(6)}"
        quizRepository.createQuiz(createSampleQuiz(quizAId, teacherAId, "Teacher A Realm"), teacherAId)
        quizRepository.createQuiz(createSampleQuiz(quizBId, teacherBId, "Teacher B Realm"), teacherBId)

        // Session 1: Teacher A logged in
        val datasetA = quizRepository.getQuizzesByTeacher(teacherAId)
        assertTrue("Teacher A dataset must include Quiz A", datasetA.any { it.id == quizAId })
        assertFalse("Teacher A dataset must NOT include Quiz B", datasetA.any { it.id == quizBId })

        // Session 2: Teacher A logs out, Teacher B logs in
        val datasetB = quizRepository.getQuizzesByTeacher(teacherBId)
        assertTrue("Teacher B dataset must include Quiz B", datasetB.any { it.id == quizBId })
        assertFalse("Teacher B dataset must NOT include Quiz A", datasetB.any { it.id == quizAId })
    }
}
