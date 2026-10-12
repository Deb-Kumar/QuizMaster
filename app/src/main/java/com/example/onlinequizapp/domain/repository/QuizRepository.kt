package com.example.onlinequizapp.domain.repository

import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.QuizSessionState

interface QuizRepository {
    suspend fun getAvailableQuizzes(): List<Quiz>
    suspend fun getQuizById(id: String): Quiz?
    suspend fun getQuizById(id: String, teacherId: String?): Quiz?
    suspend fun saveCompletedQuizResult(state: QuizSessionState)
    suspend fun getLatestQuizSession(quizId: String): QuizSessionState?
    suspend fun getAllCompletedResults(): List<QuizSessionState>
    suspend fun getCompletedResultsForTeacher(teacherId: String): List<QuizSessionState>
    suspend fun getUserStats(): UserStats
    suspend fun getUserStats(userId: String?): UserStats

    // Teacher Powers (with strict ownership enforcement)
    suspend fun createQuiz(quiz: Quiz): Boolean
    suspend fun createQuiz(quiz: Quiz, teacherId: String?): Boolean
    suspend fun updateQuiz(quiz: Quiz): Boolean
    suspend fun updateQuiz(quiz: Quiz, teacherId: String?): Boolean
    suspend fun deleteQuiz(quizId: String): Boolean
    suspend fun deleteQuiz(quizId: String, teacherId: String?): Boolean
    suspend fun resetToDefaultQuizzes()

    // ── Quiz Code & Publishing Powers ──

    /**
     * Generates a guaranteed-unique 6-character uppercase alphanumeric code (e.g. "Q7K2P9").
     */
    suspend fun generateUniqueQuizCode(): String

    /**
     * Looks up a quiz by its public 6-character access code.
     */
    suspend fun getQuizByCode(quizCode: String): Quiz?

    /**
     * Validates student access to a quiz using its 6-character access code according to security rules:
     * 1. Code exists & matches quiz
     * 2. Quiz is published/active
     * 3. Current time is within quiz availability window
     * 4. Student is authorized (enrolled in assigned batch OR individually assigned OR public quiz)
     * 5. Student has not already completed the quiz (if one-attempt rule applies)
     */
    suspend fun validateAndAccessQuiz(quizCode: String, studentId: String): Result<Quiz>

    /**
     * Publishes a quiz with target batches and/or individual students, updating its status to PUBLISHED.
     * Validates teacher ownership over the quiz and target batches.
     */
    suspend fun publishQuiz(
        quizId: String,
        batchIds: List<String> = emptyList(),
        studentIds: List<String> = emptyList()
    ): Result<Quiz>

    suspend fun publishQuiz(
        quizId: String,
        batchIds: List<String>,
        studentIds: List<String>,
        teacherId: String?
    ): Result<Quiz>

    /**
     * Saves a quiz in DRAFT or READY_FOR_REVIEW status so the teacher can return to edit it later.
     */
    suspend fun saveQuizDraft(quiz: Quiz): Result<Quiz>
    suspend fun saveQuizDraft(quiz: Quiz, teacherId: String?): Result<Quiz>

    /**
     * Fetches all quizzes created by a specific teacher (strictly isolated).
     */
    suspend fun getQuizzesByTeacher(teacherId: String): List<Quiz>

    /**
     * Fetches accessible quizzes for a student based strictly on their batch memberships and assignments.
     */
    suspend fun getAccessibleQuizzesForStudent(studentId: String): List<Quiz>

    // ── Quiz Results Persistence (Firestore + Local) ──

    /**
     * Saves a completed quiz result to both Firebase Firestore and local storage.
     * Called automatically when a student submits a quiz.
     */
    suspend fun saveQuizResultToCloud(result: QuizResult): Boolean

    /**
     * Fetches all quiz results for a specific user from Firestore.
     * Falls back to local cache when offline.
     */
    suspend fun getQuizResultsForUser(userId: String): List<QuizResult>

    /**
     * Fetches all quiz results for a specific quiz owned by [teacherId].
     */
    suspend fun getQuizResultsByQuizId(quizId: String): List<QuizResult>
    suspend fun getQuizResultsByQuizId(quizId: String, teacherId: String?): List<QuizResult>

    /**
     * Fetches all quiz results across all quizzes owned by [teacherId].
     */
    suspend fun getQuizResultsForTeacher(teacherId: String): List<QuizResult>

    /**
     * Fetches quiz results from local cache only (offline mode).
     */
    suspend fun getLocalQuizResults(userId: String): List<QuizResult>

    /**
     * Submits user feedback and remarks to Firestore database and local storage.
     */
    suspend fun submitFeedback(
        userId: String,
        userName: String,
        userEmail: String,
        role: String,
        rating: Int,
        category: String,
        comment: String
    ): Boolean
}

data class UserStats(
    val quizzesTaken: Int = 0,
    val certificatesCount: Int = 0,
    val averageScorePercentage: Int = 0,
    val dayStreak: Int = 0,
    val hasPerfectScore: Boolean = false
)
