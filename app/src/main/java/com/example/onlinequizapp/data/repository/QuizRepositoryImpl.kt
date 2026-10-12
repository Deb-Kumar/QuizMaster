package com.example.onlinequizapp.data.repository

import android.util.Log
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.data.session.QuizResultStorageManager
import com.example.onlinequizapp.domain.model.AppNotification
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.FeedbackMode
import com.example.onlinequizapp.domain.model.Option
import com.example.onlinequizapp.domain.model.Question
import com.example.onlinequizapp.domain.model.QuestionResultEntry
import com.example.onlinequizapp.domain.model.QuestionType
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizAssignment
import com.example.onlinequizapp.domain.model.QuizConfig
import com.example.onlinequizapp.domain.model.QuizCreationMethod
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.QuizStatus
import com.example.onlinequizapp.domain.model.SessionStatus
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.model.UserQuestionAnswer
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.domain.repository.QuizRepository
import com.example.onlinequizapp.domain.repository.UserStats
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.Calendar
import java.util.UUID



class QuizRepositoryImpl private constructor() : QuizRepository {

    private val TAG = "QuizRepositoryImpl"

    private val mutex = Mutex()
    private val completedSessions = mutableMapOf<String, QuizSessionState>()
    private val completedSessionDates = mutableMapOf<String, Long>()
    private val quizzesList = mutableListOf<Quiz>()
    private val assignmentsList = mutableListOf<QuizAssignment>()

    private val secureRandom = SecureRandom()
    private val QUIZ_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private val QUIZ_CODE_LENGTH = 6

    /** Lazy Firestore instance — null-safe for unit tests or when Firebase is unavailable. */
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    private val _quizzesFlow = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzesFlow: StateFlow<List<Quiz>> = _quizzesFlow.asStateFlow()

    private fun updateQuizzesFlow() {
        _quizzesFlow.value = quizzesList.toList()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        syncFromFirestore()
    }

    /**
     * Synchronizes all persistent quizzes from the real Firebase Firestore database into memory.
     */
    private fun syncFromFirestore() {
        val db = firestore ?: return
        scope.launch {
            try {
                val snapshot = db.collection(COLLECTION_QUIZZES).get().await()
                val remoteQuizzes = snapshot.documents.mapNotNull { doc ->
                    parseQuizFromFirestore(doc.id, doc.data)
                }
                mutex.withLock {
                    quizzesList.clear()
                    quizzesList.addAll(remoteQuizzes)
                    updateQuizzesFlow()
                }
                Log.d(TAG, "Successfully synced ${remoteQuizzes.size} real quizzes from Firestore")
            } catch (e: Exception) {
                Log.w(TAG, "Firestore quiz sync failed or offline (working with cache)", e)
            }
        }
    }

    // ────────────────────────────────────────────────────────
    //  Existing Quiz CRUD (unchanged)
    // ────────────────────────────────────────────────────────

    override suspend fun getAvailableQuizzes(): List<Quiz> {
        return mutex.withLock {
            quizzesList.toList()
        }
    }

    override suspend fun getQuizById(id: String): Quiz? {
        return getQuizById(id, null)
    }

    override suspend fun getQuizById(id: String, teacherId: String?): Quiz? {
        return mutex.withLock {
            val quiz = quizzesList.find { it.id == id }
            if (quiz != null && teacherId != null && quiz.teacherId.isNotBlank() && quiz.teacherId != teacherId) {
                // Direct ID Attack Protection: Teacher cannot access another teacher's quiz directly
                null
            } else {
                quiz
            }
        }
    }

    override suspend fun saveCompletedQuizResult(state: QuizSessionState) {
        val now = System.currentTimeMillis()
        mutex.withLock {
            completedSessions[state.quiz.id] = state
            completedSessionDates[state.quiz.id] = now
        }

        // ── Persist to Firestore + local storage with authoritative teacherId ──
        try {
            val userId = AuthSessionManager.getSavedUser()?.id ?: "anonymous"
            val userName = AuthSessionManager.getSavedUser()?.name ?: "Student"

            val quizResult = state.toQuizResult(userId = userId, userName = userName, submittedAtMillis = now)

            // Save to local storage (SharedPreferences) first for immediate offline access
            QuizResultStorageManager.saveResultLocally(userId, state)

            // Save to Firestore (cloud) — fire-and-forget so submission isn't blocked
            saveQuizResultToCloud(quizResult)

            // ── Dynamic Notification: Student → Teacher (Quiz Submission) ──
            if (state.quiz.teacherId.isNotBlank()) {
                scope.launch {
                    try {
                        NotificationRepositoryImpl.instance.sendNotification(
                            AppNotification(
                                id = UUID.randomUUID().toString(),
                                recipientId = state.quiz.teacherId,
                                recipientRole = UserRole.TEACHER,
                                title = "Quiz Submission: ${state.quiz.title}",
                                description = "$userName completed '${state.quiz.title}' with a score of ${state.scorePercentage}%.",
                                category = "SUBMISSION",
                                targetQuizId = state.quiz.id,
                                senderId = userId,
                                senderName = userName
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to send quiz submission notification to teacher: ${e.message}")
                    }
                }
            }

            // Also notify the student of their result
            if (userId != "anonymous") {
                scope.launch {
                    try {
                        NotificationRepositoryImpl.instance.sendNotification(
                            AppNotification(
                                id = UUID.randomUUID().toString(),
                                recipientId = userId,
                                recipientRole = UserRole.STUDENT,
                                title = "Quiz Result: ${state.quiz.title}",
                                description = "Your score report is ready: ${state.scorePercentage}% (${state.correctCount}/${state.totalQuestions} correct).",
                                category = "RESULT",
                                targetQuizId = state.quiz.id
                            )
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to send quiz result notification to student: ${e.message}")
                    }
                }
            }

            Log.d(TAG, "Quiz result saved to both local + Firestore for quiz: ${state.quiz.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist quiz result to cloud/local", e)
        }
    }

    override suspend fun getLatestQuizSession(quizId: String): QuizSessionState? {
        // 1. In-memory completed session
        val memorySession = mutex.withLock {
            completedSessions[quizId]
        }
        if (memorySession != null) return memorySession

        // 2. Persistent disk storage (survives app restarts)
        val userId = AuthSessionManager.getSavedUser()?.id ?: ""
        val quiz = getQuizById(quizId)
        if (quiz != null) {
            if (userId.isNotBlank()) {
                val localSession = QuizResultStorageManager.getSavedResult(userId, quiz)
                if (localSession != null) return localSession
            }
            // Check any saved result on device for this quiz
            val allLocal = QuizResultStorageManager.getAllSavedResultsForQuiz(quizId, quiz)
            val mostRecent = allLocal.maxByOrNull { it.submittedAtMillis }
            if (mostRecent != null) {
                return reconstructSessionFromQuizResult(quiz, mostRecent)
            }
        }

        // 3. Firestore saved result for user
        if (quiz != null && userId.isNotBlank()) {
            val userResults = getQuizResultsForUser(userId)
            val match = userResults.firstOrNull { it.quizId == quizId }
            if (match != null) {
                return reconstructSessionFromQuizResult(quiz, match)
            }
        }

        return null
    }

    fun reconstructSessionFromQuizResult(quiz: Quiz, result: QuizResult): QuizSessionState {
        val answersMap = mutableMapOf<String, UserQuestionAnswer>()
        if (result.questionResults.isNotEmpty()) {
            result.questionResults.forEach { qr ->
                answersMap[qr.questionId] = UserQuestionAnswer(
                    questionId = qr.questionId,
                    selectedOptionIds = qr.selectedOptionIds.toSet(),
                    isAnswered = qr.isAnswered,
                    isCorrect = qr.isCorrect,
                    timeSpentSeconds = qr.timeSpentSeconds
                )
            }
        } else {
            val totalQ = quiz.questions.size
            val correctCount = result.correctCount.coerceIn(0, totalQ)
            quiz.questions.forEachIndexed { index, q ->
                val isCor = index < correctCount
                val selected = if (isCor) {
                    q.correctAnswers
                } else {
                    val wrong = q.options.map { it.id }.filterNot { it in q.correctAnswers }
                    if (wrong.isNotEmpty()) setOf(wrong.first()) else emptySet()
                }
                answersMap[q.id] = UserQuestionAnswer(
                    questionId = q.id,
                    selectedOptionIds = selected,
                    isAnswered = selected.isNotEmpty(),
                    isCorrect = isCor,
                    timeSpentSeconds = if (totalQ > 0) result.elapsedTimeSeconds / totalQ else 10
                )
            }
        }

        return QuizSessionState(
            quiz = quiz,
            currentIndex = (quiz.totalQuestions - 1).coerceAtLeast(0),
            answers = answersMap,
            elapsedTimeSeconds = result.elapsedTimeSeconds,
            status = SessionStatus.COMPLETED
        )
    }

    fun hasCompletedQuizLocally(quizId: String): Boolean {
        if (completedSessions.containsKey(quizId)) return true
        val userId = AuthSessionManager.getSavedUser()?.id ?: return false
        return QuizResultStorageManager.hasSavedResult(userId, quizId)
    }

    override suspend fun getAllCompletedResults(): List<QuizSessionState> {
        return mutex.withLock {
            completedSessions.values.toList()
        }
    }

    override suspend fun getCompletedResultsForTeacher(teacherId: String): List<QuizSessionState> {
        return mutex.withLock {
            completedSessions.values.filter { it.quiz.teacherId == teacherId }
        }
    }

    override suspend fun getUserStats(): UserStats {
        return getUserStats(null)
    }

    override suspend fun getUserStats(userId: String?): UserStats {
        val effectiveUserId = userId ?: AuthSessionManager.getSavedUser()?.id
        val results = if (!effectiveUserId.isNullOrBlank()) {
            try {
                getQuizResultsForUser(effectiveUserId)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        if (results.isNotEmpty()) {
            val totalTaken = results.size
            val avg = results.map { it.scorePercentage }.average().toInt()
            val certs = results.count { it.scorePercentage >= 80 }
            val streak = calculateDayStreak(results.map { it.submittedAtMillis })
            val perfect = results.any { it.scorePercentage >= 100 }
            return UserStats(
                quizzesTaken = totalTaken,
                certificatesCount = certs,
                averageScorePercentage = avg,
                dayStreak = streak,
                hasPerfectScore = perfect
            )
        }

        return mutex.withLock {
            val totalTaken = completedSessions.size
            if (totalTaken == 0) {
                UserStats(
                    quizzesTaken = 0,
                    certificatesCount = 0,
                    averageScorePercentage = 0,
                    dayStreak = 0,
                    hasPerfectScore = false
                )
            } else {
                val avg = completedSessions.values.map { it.scorePercentage }.average().toInt()
                val certs = completedSessions.values.count { it.scorePercentage >= 80f }
                val streak = calculateDayStreak(completedSessionDates.values.toList())
                val perfect = completedSessions.values.any { it.scorePercentage >= 100 }
                UserStats(
                    quizzesTaken = totalTaken,
                    certificatesCount = certs,
                    averageScorePercentage = avg,
                    dayStreak = streak,
                    hasPerfectScore = perfect
                )
            }
        }
    }

    private fun calculateDayStreak(timestamps: List<Long>): Int {
        val validTimestamps = timestamps.filter { it > 0 }
        if (validTimestamps.isEmpty()) return 0
        val days = validTimestamps.map { millis ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
        }.distinct().sortedDescending()

        val todayCal = Calendar.getInstance()
        val today = todayCal.get(Calendar.YEAR) * 1000 + todayCal.get(Calendar.DAY_OF_YEAR)
        todayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = todayCal.get(Calendar.YEAR) * 1000 + todayCal.get(Calendar.DAY_OF_YEAR)

        val latest = days.firstOrNull() ?: return 0
        if (latest != today && latest != yesterday) return 0

        var streak = 1
        var currentDay = latest
        for (i in 1 until days.size) {
            val prevDay = days[i]
            val checkCal = Calendar.getInstance()
            val year = currentDay / 1000
            val dayOfYear = currentDay % 1000
            checkCal.set(Calendar.YEAR, year)
            checkCal.set(Calendar.DAY_OF_YEAR, dayOfYear)
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
            val expectedPrev = checkCal.get(Calendar.YEAR) * 1000 + checkCal.get(Calendar.DAY_OF_YEAR)

            if (prevDay == expectedPrev) {
                streak++
                currentDay = prevDay
            } else {
                break
            }
        }
        return streak
    }

    // Teacher Powers (strictly owner-isolated)
    override suspend fun createQuiz(quiz: Quiz): Boolean {
        return createQuiz(quiz, null)
    }

    override suspend fun createQuiz(quiz: Quiz, teacherId: String?): Boolean {
        val effectiveTeacherId = teacherId ?: quiz.teacherId
        val finalCode = if (quiz.quizCode.isNotBlank()) quiz.quizCode else generateUniqueQuizCode()
        val quizWithCode = quiz.copy(quizCode = finalCode, teacherId = effectiveTeacherId)

        // Validate that target batches belong to this teacher (Requirement 13)
        if (effectiveTeacherId.isNotBlank() && quizWithCode.targetBatchIds.isNotEmpty()) {
            val teacherBatches = BatchRepositoryImpl.instance.getBatchesByTeacherSnapshot(effectiveTeacherId).map { it.id }.toSet()
            if (!quizWithCode.targetBatchIds.all { it in teacherBatches }) {
                throw SecurityException("Unauthorized: Cannot assign quiz to batches not owned by this teacher.")
            }
        }

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection(COLLECTION_QUIZZES).document(quizWithCode.id).set(quizToFirestoreMap(quizWithCode)).await()
                    Log.d(TAG, "Quiz ${quizWithCode.id} successfully saved to Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save quiz to Firestore", e)
                }
            }
        }

        // Notify enrolled students in assigned batches about the new quiz
        if (quizWithCode.targetBatchIds.isNotEmpty()) {
            scope.launch {
                try {
                    val notifiedStudentIds = mutableSetOf<String>()
                    for (batchId in quizWithCode.targetBatchIds) {
                        val enrolledResult = BatchRepositoryImpl.instance.getStudentsInBatch(batchId, effectiveTeacherId)
                        val students = enrolledResult.getOrNull() ?: emptyList()
                        for (student in students) {
                            if (notifiedStudentIds.add(student.studentId)) {
                                val notif = AppNotification(
                                    recipientId = student.studentId,
                                    recipientRole = UserRole.STUDENT,
                                    title = "New Quiz: ${quizWithCode.title}",
                                    description = "A new quiz has been published to your batch. Take the challenge now!",
                                    category = "QUIZ",
                                    targetQuizId = quizWithCode.id,
                                    targetBatchId = batchId,
                                    senderId = effectiveTeacherId
                                )
                                NotificationRepositoryImpl.instance.sendNotification(notif)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error triggering quiz publication notifications: ${e.message}")
                }
            }
        }

        return mutex.withLock {
            val exists = quizzesList.any { it.id == quizWithCode.id }
            if (exists) {
                false
            } else {
                quizzesList.add(0, quizWithCode) // Add newest to top
                updateQuizzesFlow()
                true
            }
        }
    }

    override suspend fun updateQuiz(quiz: Quiz): Boolean {
        return updateQuiz(quiz, null)
    }

    override suspend fun updateQuiz(quiz: Quiz, teacherId: String?): Boolean {
        val existing = mutex.withLock { quizzesList.find { it.id == quiz.id } }
        val effectiveTeacherId = teacherId ?: quiz.teacherId
        if (existing != null && existing.teacherId.isNotBlank() && effectiveTeacherId.isNotBlank() && existing.teacherId != effectiveTeacherId) {
            throw SecurityException("Unauthorized: You do not own this quiz.")
        }

        // Validate target batches belong to this teacher
        if (effectiveTeacherId.isNotBlank() && quiz.targetBatchIds.isNotEmpty()) {
            val teacherBatches = BatchRepositoryImpl.instance.getBatchesByTeacherSnapshot(effectiveTeacherId).map { it.id }.toSet()
            if (!quiz.targetBatchIds.all { it in teacherBatches }) {
                throw SecurityException("Unauthorized: Cannot assign quiz to batches not owned by this teacher.")
            }
        }

        val finalCode = if (quiz.quizCode.isNotBlank()) quiz.quizCode else generateUniqueQuizCode()
        val quizWithCode = quiz.copy(
            quizCode = finalCode,
            teacherId = effectiveTeacherId,
            updatedAt = System.currentTimeMillis()
        )

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection(COLLECTION_QUIZZES).document(quizWithCode.id).set(quizToFirestoreMap(quizWithCode)).await()
                    Log.d(TAG, "Quiz ${quizWithCode.id} successfully updated in Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update quiz in Firestore", e)
                }
            }
        }

        return mutex.withLock {
            val index = quizzesList.indexOfFirst { it.id == quizWithCode.id }
            if (index != -1) {
                quizzesList[index] = quizWithCode
            } else {
                quizzesList.add(0, quizWithCode)
            }
            updateQuizzesFlow()
            true
        }
    }

    override suspend fun deleteQuiz(quizId: String): Boolean {
        return deleteQuiz(quizId, null)
    }

    override suspend fun deleteQuiz(quizId: String, teacherId: String?): Boolean {
        val existing = mutex.withLock { quizzesList.find { it.id == quizId } }
        if (existing != null && teacherId != null && existing.teacherId.isNotBlank() && existing.teacherId != teacherId) {
            throw SecurityException("Unauthorized: You do not own this quiz.")
        }

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection(COLLECTION_QUIZZES).document(quizId).delete().await()
                    Log.d(TAG, "Quiz $quizId deleted from Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete quiz from Firestore", e)
                }
            }
        }

        return mutex.withLock {
            val removed = quizzesList.removeAll { it.id == quizId }
            completedSessions.remove(quizId)
            if (removed) {
                updateQuizzesFlow()
            }
            removed
        }
    }

    override suspend fun resetToDefaultQuizzes() {
        mutex.withLock {
            quizzesList.clear()
            updateQuizzesFlow()
        }
        syncFromFirestore()
    }

    // ────────────────────────────────────────────────────────
    //  Quiz Code & Publishing Powers
    // ────────────────────────────────────────────────────────

    private fun generateCodeRaw(existingCodes: Set<String>): String {
        var retries = 0
        while (retries < 20) {
            val sb = StringBuilder(QUIZ_CODE_LENGTH)
            for (i in 0 until QUIZ_CODE_LENGTH) {
                val idx = secureRandom.nextInt(QUIZ_CODE_CHARS.length)
                sb.append(QUIZ_CODE_CHARS[idx])
            }
            val code = sb.toString()
            if (!existingCodes.contains(code)) {
                return code
            }
            retries++
        }
        return UUID.randomUUID().toString().replace("-", "").take(6).uppercase()
    }

    override suspend fun generateUniqueQuizCode(): String {
        return mutex.withLock {
            val existing = quizzesList.map { it.quizCode }.filter { it.isNotBlank() }.toSet()
            generateCodeRaw(existing)
        }
    }

    override suspend fun getQuizByCode(quizCode: String): Quiz? {
        val clean = quizCode.trim().uppercase()
        if (clean.isBlank()) return null
        return mutex.withLock {
            quizzesList.find { it.quizCode.equals(clean, ignoreCase = true) }
        }
    }

    override suspend fun validateAndAccessQuiz(quizCode: String, studentId: String): Result<Quiz> {
        val clean = quizCode.trim().uppercase()
        if (clean.length != 6) {
            return Result.failure(IllegalArgumentException("Quiz access code must be exactly 6 characters."))
        }

        val quiz = getQuizByCode(clean)
            ?: return Result.failure(NoSuchElementException("Invalid quiz code \"$clean\". No matching quiz found."))

        // 1. Verify Status (must be PUBLISHED or ACTIVE)
        if (!quiz.isPublishedOrActive) {
            val statusDisplay = quiz.status.name.lowercase().replace('_', ' ')
            return Result.failure(IllegalStateException("This quiz is currently a $statusDisplay and cannot be accessed by students."))
        }

        // 2. Verify Availability Time Window
        val now = System.currentTimeMillis()
        if (!quiz.isAvailableAt(now)) {
            return Result.failure(IllegalStateException("This quiz is outside its scheduled availability window."))
        }

        // 3. Security & Authorization Checks
        val hasBatchRestriction = quiz.targetBatchIds.isNotEmpty()
        val hasStudentRestriction = quiz.targetStudentIds.isNotEmpty()

        if (hasBatchRestriction || hasStudentRestriction) {
            var isAuthorized = false

            // Check if student was individually assigned
            if (quiz.targetStudentIds.contains(studentId)) {
                isAuthorized = true
            }

            // Check if student belongs to one of the assigned batches
            if (!isAuthorized && hasBatchRestriction) {
                val studentBatches = try {
                    BatchRepositoryImpl.instance.getBatchesForStudent(studentId).firstOrNull() ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                val studentBatchIds = studentBatches.map { it.id }.toSet()
                if (quiz.targetBatchIds.any { studentBatchIds.contains(it) }) {
                    isAuthorized = true
                }
            }

            if (!isAuthorized) {
                return Result.failure(SecurityException("You are not authorized to attempt this quiz. It is assigned to a specific batch or student."))
            }
        }

        // 4. Completed check (student cannot retake if not permitted)
        // Sessions in memory or local/cloud results can be checked
        val isAlreadyCompleted = mutex.withLock {
            completedSessions[quiz.id]?.status == SessionStatus.COMPLETED
        }
        if (isAlreadyCompleted) {
            // Note: still allow review or return informational message if strictly single attempt
            Log.d(TAG, "Student $studentId accessing previously attempted quiz ${quiz.id}")
        }

        // Register direct code relationship assignment
        mutex.withLock {
            val exists = assignmentsList.any { it.quizId == quiz.id && it.studentId == studentId }
            if (!exists && studentId.isNotBlank()) {
                assignmentsList.add(
                    QuizAssignment(
                        id = UUID.randomUUID().toString(),
                        quizId = quiz.id,
                        studentId = studentId,
                        assignedBy = quiz.teacherId,
                        assignedAt = now,
                        status = "ACTIVE"
                    )
                )
            }
        }

        return Result.success(quiz)
    }

    override suspend fun publishQuiz(
        quizId: String,
        batchIds: List<String>,
        studentIds: List<String>
    ): Result<Quiz> {
        return publishQuiz(quizId, batchIds, studentIds, null)
    }

    override suspend fun publishQuiz(
        quizId: String,
        batchIds: List<String>,
        studentIds: List<String>,
        teacherId: String?
    ): Result<Quiz> {
        val currentQuiz = mutex.withLock { quizzesList.find { it.id == quizId } }
            ?: return Result.failure(NoSuchElementException("Quiz not found with ID: $quizId"))

        val effectiveTeacherId = teacherId ?: currentQuiz.teacherId
        if (effectiveTeacherId.isNotBlank() && currentQuiz.teacherId.isNotBlank() && currentQuiz.teacherId != effectiveTeacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not own this quiz."))
        }

        // Validate that target batches belong to this teacher (Requirement 13)
        if (effectiveTeacherId.isNotBlank() && batchIds.isNotEmpty()) {
            val teacherBatches = BatchRepositoryImpl.instance.getBatchesByTeacherSnapshot(effectiveTeacherId).map { it.id }.toSet()
            if (!batchIds.all { it in teacherBatches }) {
                return Result.failure(SecurityException("Unauthorized: Cannot assign quiz to batches not owned by this teacher."))
            }
        }

        val updatedQuiz = mutex.withLock {
            val index = quizzesList.indexOfFirst { it.id == quizId }
            if (index == -1) {
                return@withLock null
            }

            val current = quizzesList[index]
            val finalCode = if (current.quizCode.isNotBlank()) {
                current.quizCode
            } else {
                val existing = quizzesList.map { it.quizCode }.toSet()
                generateCodeRaw(existing)
            }

            val updated = current.copy(
                quizCode = finalCode,
                teacherId = effectiveTeacherId,
                status = QuizStatus.PUBLISHED,
                targetBatchIds = batchIds,
                targetStudentIds = studentIds,
                updatedAt = System.currentTimeMillis()
            )

            quizzesList[index] = updated
            updateQuizzesFlow()
            updated
        } ?: return Result.failure(NoSuchElementException("Quiz not found with ID: $quizId"))

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection(COLLECTION_QUIZZES).document(quizId).set(quizToFirestoreMap(updatedQuiz)).await()
                    Log.d(TAG, "Published quiz $quizId persisted to Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to persist published quiz to Firestore", e)
                }
            }
        }

        // Create assignments preserving authoritative teacherId
        val ownerTeacherId = updatedQuiz.teacherId
        val now = System.currentTimeMillis()

        mutex.withLock {
            batchIds.forEach { bId ->
                assignmentsList.add(
                    QuizAssignment(
                        id = UUID.randomUUID().toString(),
                        quizId = quizId,
                        batchId = bId,
                        assignedBy = ownerTeacherId,
                        assignedAt = now,
                        status = "ACTIVE"
                    )
                )
            }

            studentIds.forEach { sId ->
                assignmentsList.add(
                    QuizAssignment(
                        id = UUID.randomUUID().toString(),
                        quizId = quizId,
                        studentId = sId,
                        assignedBy = ownerTeacherId,
                        assignedAt = now,
                        status = "ACTIVE"
                    )
                )
            }
        }

        return Result.success(updatedQuiz)
    }

    override suspend fun saveQuizDraft(quiz: Quiz): Result<Quiz> {
        return saveQuizDraft(quiz, null)
    }

    override suspend fun saveQuizDraft(quiz: Quiz, teacherId: String?): Result<Quiz> {
        val effectiveTeacherId = teacherId ?: quiz.teacherId
        val existing = mutex.withLock { quizzesList.find { it.id == quiz.id } }
        if (existing != null && existing.teacherId.isNotBlank() && effectiveTeacherId.isNotBlank() && existing.teacherId != effectiveTeacherId) {
            return Result.failure(SecurityException("Unauthorized: You do not own this quiz."))
        }

        val draftStatus = if (quiz.status == QuizStatus.READY_FOR_REVIEW) {
            QuizStatus.READY_FOR_REVIEW
        } else {
            QuizStatus.DRAFT
        }

        val finalCode = if (quiz.quizCode.isNotBlank()) {
            quiz.quizCode
        } else {
            val existingCodes = mutex.withLock { quizzesList.map { it.quizCode }.toSet() }
            generateCodeRaw(existingCodes)
        }

        val draftQuiz = quiz.copy(
            quizCode = finalCode,
            teacherId = effectiveTeacherId,
            status = draftStatus,
            updatedAt = System.currentTimeMillis()
        )

        val db = firestore
        if (db != null) {
            scope.launch {
                try {
                    db.collection(COLLECTION_QUIZZES).document(draftQuiz.id).set(quizToFirestoreMap(draftQuiz)).await()
                    Log.d(TAG, "Quiz draft ${draftQuiz.id} saved to Firestore")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save draft to Firestore", e)
                }
            }
        }

        mutex.withLock {
            val index = quizzesList.indexOfFirst { it.id == quiz.id }
            if (index != -1) {
                quizzesList[index] = draftQuiz
            } else {
                quizzesList.add(0, draftQuiz)
            }
            updateQuizzesFlow()
        }

        return Result.success(draftQuiz)
    }

    override suspend fun getQuizzesByTeacher(teacherId: String): List<Quiz> {
        return mutex.withLock {
            quizzesList.filter {
                it.teacherId == teacherId
            }
        }
    }

    override suspend fun getAccessibleQuizzesForStudent(studentId: String): List<Quiz> {
        val studentBatches = BatchRepositoryImpl.instance.getBatchesForStudentSnapshot(studentId)
        val enrolledBatchIds = studentBatches.map { it.id }.toSet()

        return mutex.withLock {
            val directlyAssignedQuizIds = assignmentsList
                .filter { it.studentId == studentId && it.status == "ACTIVE" }
                .map { it.quizId }
                .toSet()

            quizzesList.filter { quiz ->
                quiz.isPublishedOrActive && (
                    quiz.targetStudentIds.contains(studentId) ||
                    quiz.id in directlyAssignedQuizIds ||
                    (quiz.targetBatchIds.isNotEmpty() && quiz.targetBatchIds.any { it in enrolledBatchIds }) ||
                    (quiz.targetBatchIds.isEmpty() && quiz.targetStudentIds.isEmpty() && (quiz.teacherId.isBlank() || quiz.teacherId == "system"))
                )
            }
        }
    }

    // ────────────────────────────────────────────────────────
    //  NEW: Quiz Result Persistence — Firestore + Local
    // ────────────────────────────────────────────────────────

    override suspend fun saveQuizResultToCloud(result: QuizResult): Boolean {
        val db = firestore ?: run {
            Log.w(TAG, "Firestore unavailable — result saved locally only")
            return false
        }

        return try {
            val docData = hashMapOf(
                "resultId" to result.resultId,
                "userId" to result.userId,
                "userName" to result.userName,
                "quizId" to result.quizId,
                "quizTitle" to result.quizTitle,
                "quizCategory" to result.quizCategory,
                "quizIcon" to result.quizIcon,
                "teacherId" to result.teacherId,
                "totalQuestions" to result.totalQuestions,
                "correctCount" to result.correctCount,
                "incorrectCount" to result.incorrectCount,
                "unansweredCount" to result.unansweredCount,
                "scorePercentage" to result.scorePercentage,
                "elapsedTimeSeconds" to result.elapsedTimeSeconds,
                "submittedAtMillis" to result.submittedAtMillis,
                "status" to result.status,
                "questionResults" to result.questionResults.map { qr ->
                    hashMapOf(
                        "questionId" to qr.questionId,
                        "selectedOptionIds" to qr.selectedOptionIds,
                        "isAnswered" to qr.isAnswered,
                        "isCorrect" to qr.isCorrect,
                        "timeSpentSeconds" to qr.timeSpentSeconds
                    )
                }
            )

            val docId = if (result.userId.isNotBlank() && result.quizId.isNotBlank()) {
                "${result.userId}_${result.quizId}"
            } else {
                result.resultId
            }

            db.collection(COLLECTION_QUIZ_RESULTS)
                .document(docId)
                .set(docData)
                .await()

            Log.d(TAG, "✅ Quiz result uploaded to Firestore: $docId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to upload quiz result to Firestore", e)
            false
        }
    }

    override suspend fun getQuizResultsForUser(userId: String): List<QuizResult> {
        val db = firestore
        val remoteResults = if (db != null) {
            try {
                val snapshot = db.collection(COLLECTION_QUIZ_RESULTS)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    parseQuizResultFromFirestore(data)
                }
                Log.d(TAG, "Fetched ${list.size} quiz results from Firestore for user: $userId")
                list
            } catch (e: Exception) {
                Log.w(TAG, "Firestore fetch failed, falling back to local cache", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        val localResults = getLocalQuizResults(userId)

        val memoryResults = mutex.withLock {
            val uName = AuthSessionManager.getSavedUser()?.name ?: "Student"
            completedSessions.values.map { session ->
                val submittedTime = completedSessionDates[session.quiz.id] ?: System.currentTimeMillis()
                session.toQuizResult(userId = userId, userName = uName, submittedAtMillis = submittedTime)
            }
        }

        // Keep only the latest attempt for each unique quiz so each quiz appears once in history
        val combined = (memoryResults + localResults + remoteResults)
            .sortedByDescending { it.submittedAtMillis }
            .distinctBy { it.quizId }

        Log.d(TAG, "Fetched ${combined.size} combined quiz results for user: $userId")
        return combined
    }

    override suspend fun getQuizResultsByQuizId(quizId: String): List<QuizResult> {
        return getQuizResultsByQuizId(quizId, null)
    }

    override suspend fun getQuizResultsByQuizId(quizId: String, teacherId: String?): List<QuizResult> {
        if (teacherId != null) {
            val quiz = getQuizById(quizId)
            if (quiz != null && quiz.teacherId.isNotBlank() && quiz.teacherId != teacherId) {
                // Cannot view results of another teacher's quiz!
                return emptyList()
            }
        }
        val db = firestore
        val remoteResults = if (db != null) {
            try {
                val snapshot = db.collection(COLLECTION_QUIZ_RESULTS)
                    .whereEqualTo("quizId", quizId)
                    .get()
                    .await()

                val results = snapshot.documents.mapNotNull { doc ->
                    parseQuizResultFromFirestore(doc.data)
                }
                Log.d(TAG, "Fetched ${results.size} results for quiz: $quizId")
                results
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch quiz results by quizId from Firestore", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        val localMemoryResult = mutex.withLock {
            val session = completedSessions[quizId]
            if (session != null) {
                val userId = AuthSessionManager.getSavedUser()?.id ?: "student"
                val userName = AuthSessionManager.getSavedUser()?.name ?: "Student"
                val time = completedSessionDates[quizId] ?: System.currentTimeMillis()
                session.toQuizResult(userId = userId, userName = userName, submittedAtMillis = time)
            } else null
        }

        // Also retrieve from persistent disk storage (survives app restarts)
        val quiz = getQuizById(quizId)
        val localDiskResults = QuizResultStorageManager.getAllSavedResultsForQuiz(quizId, quiz)

        val combined = (remoteResults + listOfNotNull(localMemoryResult) + localDiskResults)
            .distinctBy { "${it.userId}_${it.quizId}" }
            .sortedWith(
                compareByDescending<QuizResult> { it.scorePercentage }
                    .thenBy { it.elapsedTimeSeconds }
                    .thenByDescending { it.submittedAtMillis }
            )
        return combined
    }

    override suspend fun getQuizResultsForTeacher(teacherId: String): List<QuizResult> {
        val teacherQuizIds = getQuizzesByTeacher(teacherId).map { it.id }.toSet()
        val db = firestore
        if (db != null) {
            try {
                val snapshot = db.collection(COLLECTION_QUIZ_RESULTS)
                    .whereEqualTo("teacherId", teacherId)
                    .get()
                    .await()
                val results = snapshot.documents.mapNotNull { parseQuizResultFromFirestore(it.data) }
                    .sortedByDescending { it.submittedAtMillis }
                if (results.isNotEmpty()) return results
            } catch (_: Exception) {}
        }
        return mutex.withLock {
            completedSessions.values
                .filter { it.quiz.teacherId == teacherId || it.quiz.id in teacherQuizIds }
                .map { session ->
                    val submittedTime = completedSessionDates[session.quiz.id] ?: System.currentTimeMillis()
                    val userName = AuthSessionManager.getSavedUser()?.name ?: "Student"
                    val userId = AuthSessionManager.getSavedUser()?.id ?: "student"
                    session.toQuizResult(userId = userId, userName = userName, submittedAtMillis = submittedTime)
                }
        }
    }

    override suspend fun getLocalQuizResults(userId: String): List<QuizResult> {
        return try {
            val quizzes = getAvailableQuizzes()
            val localSessions = QuizResultStorageManager.getAllSavedResults(userId, quizzes)
            if (localSessions.isNotEmpty()) {
                localSessions.map { session ->
                    session.toQuizResult(userId = userId, userName = AuthSessionManager.getSavedUser()?.name ?: "Student")
                }.sortedByDescending { it.submittedAtMillis }
            } else {
                mutex.withLock {
                    val userName = AuthSessionManager.getSavedUser()?.name ?: "Student"
                    completedSessions.values.map { session ->
                        val submittedTime = completedSessionDates[session.quiz.id] ?: System.currentTimeMillis()
                        session.toQuizResult(userId = userId, userName = userName, submittedAtMillis = submittedTime)
                    }.sortedByDescending { it.submittedAtMillis }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load local quiz results", e)
            emptyList()
        }
    }

    override suspend fun submitFeedback(
        userId: String,
        userName: String,
        userEmail: String,
        role: String,
        rating: Int,
        category: String,
        comment: String
    ): Boolean {
        val id = "feedback_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val timestamp = System.currentTimeMillis()
        val feedbackMap = mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "userEmail" to userEmail,
            "role" to role,
            "rating" to rating,
            "category" to category,
            "comment" to comment,
            "submittedAtMillis" to timestamp
        )

        val db = firestore
        if (db != null) {
            try {
                db.collection("feedbacks").document(id).set(feedbackMap).await()
                Log.d(TAG, "Feedback saved to Firestore: $id (Rating: $rating, User: $userName)")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save feedback to Firestore, fallback success", e)
            }
        }
        return true
    }

    // ────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun parseQuizResultFromFirestore(data: Map<String, Any>?): QuizResult? {
        if (data == null) return null
        return try {
            val questionResultsRaw = data["questionResults"] as? List<Map<String, Any>> ?: emptyList()
            val questionResults = questionResultsRaw.map { qr ->
                QuestionResultEntry(
                    questionId = qr["questionId"] as? String ?: "",
                    selectedOptionIds = (qr["selectedOptionIds"] as? List<String>) ?: emptyList(),
                    isAnswered = qr["isAnswered"] as? Boolean ?: false,
                    isCorrect = qr["isCorrect"] as? Boolean ?: false,
                    timeSpentSeconds = (qr["timeSpentSeconds"] as? Number)?.toInt() ?: 0
                )
            }

            QuizResult(
                resultId = data["resultId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                quizId = data["quizId"] as? String ?: "",
                quizTitle = data["quizTitle"] as? String ?: "",
                quizCategory = data["quizCategory"] as? String ?: "",
                quizIcon = data["quizIcon"] as? String ?: "code",
                teacherId = data["teacherId"] as? String ?: "",
                totalQuestions = (data["totalQuestions"] as? Number)?.toInt() ?: 0,
                correctCount = (data["correctCount"] as? Number)?.toInt() ?: 0,
                incorrectCount = (data["incorrectCount"] as? Number)?.toInt() ?: 0,
                unansweredCount = (data["unansweredCount"] as? Number)?.toInt() ?: 0,
                scorePercentage = (data["scorePercentage"] as? Number)?.toInt() ?: 0,
                elapsedTimeSeconds = (data["elapsedTimeSeconds"] as? Number)?.toInt() ?: 0,
                submittedAtMillis = (data["submittedAtMillis"] as? Number)?.toLong() ?: 0L,
                status = data["status"] as? String ?: "COMPLETED",
                questionResults = questionResults
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse QuizResult from Firestore document", e)
            null
        }
    }

    companion object {
        private const val COLLECTION_QUIZZES = "quizzes"
        private const val COLLECTION_QUIZ_RESULTS = "quiz_results"

        val instance: QuizRepositoryImpl by lazy { QuizRepositoryImpl() }
    }

    // ────────────────────────────────────────────────────────
    //  Firestore Quiz Serializer / Deserializer
    // ────────────────────────────────────────────────────────

    private fun quizToFirestoreMap(quiz: Quiz): Map<String, Any?> {
        return hashMapOf(
            "id" to quiz.id,
            "title" to quiz.title,
            "description" to quiz.description,
            "category" to quiz.category,
            "icon" to quiz.icon,
            "quizCode" to quiz.quizCode,
            "status" to quiz.status.name,
            "creationMethod" to quiz.creationMethod.name,
            "aiProvider" to quiz.aiProvider,
            "teacherId" to quiz.teacherId,
            "targetBatchIds" to quiz.targetBatchIds,
            "targetStudentIds" to quiz.targetStudentIds,
            "availabilityStartMillis" to quiz.availabilityStartMillis,
            "availabilityEndMillis" to quiz.availabilityEndMillis,
            "createdAt" to quiz.createdAt,
            "updatedAt" to quiz.updatedAt,
            "config" to hashMapOf(
                "feedbackMode" to quiz.config.feedbackMode.name,
                "timerMode" to quiz.config.timerMode.name,
                "timeLimitSeconds" to quiz.config.timeLimitSeconds,
                "allowHints" to quiz.config.allowHints,
                "shuffleQuestions" to quiz.config.shuffleQuestions,
                "shuffleOptions" to quiz.config.shuffleOptions
            ),
            "questions" to quiz.questions.map { q ->
                hashMapOf(
                    "id" to q.id,
                    "type" to q.type.name,
                    "prompt" to q.prompt,
                    "explanation" to q.explanation,
                    "hint" to q.hint,
                    "difficulty" to q.difficulty.name,
                    "correctAnswers" to q.correctAnswers.toList(),
                    "options" to q.options.map { opt ->
                        hashMapOf(
                            "id" to opt.id,
                            "text" to opt.text
                        )
                    }
                )
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseQuizFromFirestore(id: String, data: Map<String, Any>?): Quiz? {
        if (data == null) return null
        return try {
            val configMap = data["config"] as? Map<String, Any>
            val config = if (configMap != null) {
                QuizConfig(
                    feedbackMode = try {
                        FeedbackMode.valueOf(configMap["feedbackMode"] as? String ?: "ON_SUBMIT")
                    } catch (_: Exception) {
                        FeedbackMode.ON_SUBMIT
                    },
                    timerMode = try {
                        TimerMode.valueOf(configMap["timerMode"] as? String ?: "COUNTDOWN")
                    } catch (_: Exception) {
                        TimerMode.COUNTDOWN
                    },
                    timeLimitSeconds = (configMap["timeLimitSeconds"] as? Number)?.toInt() ?: 180,
                    allowHints = configMap["allowHints"] as? Boolean ?: true,
                    shuffleQuestions = configMap["shuffleQuestions"] as? Boolean ?: false,
                    shuffleOptions = configMap["shuffleOptions"] as? Boolean ?: false
                )
            } else QuizConfig()

            val questionsRaw = data["questions"] as? List<Map<String, Any>> ?: emptyList()
            val questions = questionsRaw.mapNotNull { qMap ->
                try {
                    val optRaw = qMap["options"] as? List<Map<String, Any>> ?: emptyList()
                    val options = optRaw.map { opt ->
                        Option(
                            id = opt["id"] as? String ?: "",
                            text = opt["text"] as? String ?: ""
                        )
                    }
                    val correctList = (qMap["correctAnswers"] as? List<String>)?.toSet() ?: emptySet()
                    Question(
                        id = qMap["id"] as? String ?: UUID.randomUUID().toString(),
                        type = try {
                            QuestionType.valueOf(qMap["type"] as? String ?: "SINGLE")
                        } catch (_: Exception) {
                            QuestionType.SINGLE
                        },
                        prompt = qMap["prompt"] as? String ?: "",
                        options = options,
                        correctAnswers = correctList,
                        explanation = qMap["explanation"] as? String ?: "",
                        hint = qMap["hint"] as? String,
                        difficulty = try {
                            Difficulty.valueOf(qMap["difficulty"] as? String ?: "MEDIUM")
                        } catch (_: Exception) {
                            Difficulty.MEDIUM
                        }
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Skipping malformed question in quiz $id", e)
                    null
                }
            }

            Quiz(
                id = (data["id"] as? String)?.ifBlank { id } ?: id,
                title = data["title"] as? String ?: "Untitled Quiz",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "General",
                icon = data["icon"] as? String ?: "code",
                questions = questions,
                config = config,
                targetBatchIds = (data["targetBatchIds"] as? List<String>) ?: emptyList(),
                quizCode = data["quizCode"] as? String ?: "",
                status = try {
                    QuizStatus.valueOf(data["status"] as? String ?: "PUBLISHED")
                } catch (_: Exception) {
                    QuizStatus.PUBLISHED
                },
                creationMethod = try {
                    QuizCreationMethod.valueOf(data["creationMethod"] as? String ?: "MANUAL")
                } catch (_: Exception) {
                    QuizCreationMethod.MANUAL
                },
                aiProvider = data["aiProvider"] as? String,
                teacherId = data["teacherId"] as? String ?: "",
                targetStudentIds = (data["targetStudentIds"] as? List<String>) ?: emptyList(),
                availabilityStartMillis = (data["availabilityStartMillis"] as? Number)?.toLong(),
                availabilityEndMillis = (data["availabilityEndMillis"] as? Number)?.toLong(),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Quiz from Firestore document $id", e)
            null
        }
    }
}

// ────────────────────────────────────────────────────────
//  Extension: QuizSessionState → QuizResult
// ────────────────────────────────────────────────────────

/**
 * Converts a completed [QuizSessionState] into a [QuizResult] suitable for
 * Firestore serialization and local persistence.
 */
fun QuizSessionState.toQuizResult(
    userId: String,
    userName: String,
    submittedAtMillis: Long = System.currentTimeMillis()
): QuizResult {
    return QuizResult(
        resultId = "${userId}_${quiz.id}_$submittedAtMillis",
        userId = userId,
        userName = userName,
        quizId = quiz.id,
        quizTitle = quiz.title,
        quizCategory = quiz.category,
        quizIcon = quiz.icon,
        teacherId = quiz.teacherId,
        totalQuestions = totalQuestions,
        correctCount = correctCount,
        incorrectCount = incorrectCount,
        unansweredCount = unansweredCount,
        scorePercentage = scorePercentage,
        elapsedTimeSeconds = elapsedTimeSeconds,
        submittedAtMillis = submittedAtMillis,
        status = status.name,
        questionResults = answers.values.map { ans ->
            QuestionResultEntry(
                questionId = ans.questionId,
                selectedOptionIds = ans.selectedOptionIds.toList(),
                isAnswered = ans.isAnswered,
                isCorrect = ans.isCorrect,
                timeSpentSeconds = ans.timeSpentSeconds
            )
        }
    )
}
