package com.example.onlinequizapp.ui.home.leaderboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.BatchLeaderboardData
import com.example.onlinequizapp.domain.model.BatchLeaderboardStudent
import com.example.onlinequizapp.domain.model.BatchStudentQuizScore
import com.example.onlinequizapp.domain.model.EnrolledStudent
import com.example.onlinequizapp.domain.model.IndividualQuizLeaderboardItem
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizLeaderboardStudentAttempt
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.StudentBatchStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0, // 0: Batches, 1: Individual Quizzes
    val currentUserId: String = "",
    val currentUserName: String = "",
    val studentBatches: List<Batch> = emptyList(),
    val selectedBatchIndex: Int = 0,
    val batchLeaderboard: BatchLeaderboardData? = null,
    val individualQuizzes: List<IndividualQuizLeaderboardItem> = emptyList(),
    val selectedQuizForDetail: IndividualQuizLeaderboardItem? = null,
    val error: String? = null
) {
    val selectedBatch: Batch?
        get() = studentBatches.getOrNull(selectedBatchIndex)
}

class LeaderboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val batchRepository = BatchRepositoryImpl.instance
    private val quizRepository = QuizRepositoryImpl.instance

    private val userAvatarCache = mutableMapOf<String, String?>()

    init {
        loadData()
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun selectBatchIndex(index: Int) {
        if (index != _uiState.value.selectedBatchIndex && index in _uiState.value.studentBatches.indices) {
            _uiState.update { it.copy(selectedBatchIndex = index, isLoading = true) }
            viewModelScope.launch {
                computeBatchLeaderboard(index)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectQuizForDetail(item: IndividualQuizLeaderboardItem?) {
        _uiState.update { it.copy(selectedQuizForDetail = item) }
    }

    fun refresh() {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = AuthSessionManager.getSavedUser() ?: AuthRepositoryImpl.instance.getCurrentUser()
                val studentId = user?.id ?: "student"
                val studentName = user?.name ?: "Student"

                if (user != null && !user.avatarUrl.isNullOrBlank()) {
                    userAvatarCache[user.id] = user.avatarUrl
                    if (user.name.isNotBlank()) {
                        userAvatarCache[user.name.trim().lowercase()] = user.avatarUrl
                    }
                }

                _uiState.update {
                    it.copy(
                        currentUserId = studentId,
                        currentUserName = studentName
                    )
                }

                // 1. Fetch batches student is enrolled in
                val batchesFlow = batchRepository.getBatchesForStudent(studentId)
                val batches = batchesFlow.firstOrNull() ?: batchRepository.getBatchesForStudentSnapshot(studentId)

                _uiState.update {
                    it.copy(
                        studentBatches = batches,
                        selectedBatchIndex = if (it.selectedBatchIndex in batches.indices) it.selectedBatchIndex else 0
                    )
                }

                // 2. Compute Batch Leaderboard for selected batch
                if (batches.isNotEmpty()) {
                    val batchIdx = _uiState.value.selectedBatchIndex.coerceIn(0, batches.lastIndex)
                    computeBatchLeaderboard(batchIdx)
                } else {
                    _uiState.update { it.copy(batchLeaderboard = null) }
                }

                // 3. Compute Individual Quizzes Tab
                computeIndividualQuizzesLeaderboard(studentId, batches)

            } catch (e: Exception) {
                Log.e("LeaderboardViewModel", "Failed to load leaderboard data", e)
                _uiState.update { it.copy(error = e.localizedMessage ?: "Failed to load leaderboard") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun computeBatchLeaderboard(batchIndex: Int) {
        val batches = _uiState.value.studentBatches
        if (batchIndex !in batches.indices) return

        val batch = batches[batchIndex]
        val studentId = _uiState.value.currentUserId
        val currentUser = AuthSessionManager.getSavedUser() ?: AuthRepositoryImpl.instance.getCurrentUser()

        // A. Find all published quizzes assigned to this batch
        val allQuizzes = quizRepository.getAvailableQuizzes()
        val batchQuizzes = allQuizzes.filter { quiz ->
            quiz.targetBatchIds.contains(batch.id)
        }
        val totalAssignedQuizzes = batchQuizzes.size

        // B. Get enrolled students in this batch
        val classmatesRes = batchRepository.getStudentsInBatchForStudent(batch.id, studentId)
        val classmates = (classmatesRes.getOrNull() ?: emptyList()).toMutableList()

        // Ensure the current student is listed in their own batch standings
        val hasCurrent = classmates.any {
            it.studentId == studentId ||
            (currentUser != null && (it.studentId == currentUser.id || (it.email.isNotBlank() && it.email.equals(currentUser.email, ignoreCase = true))))
        }
        if (!hasCurrent && currentUser != null) {
            classmates.add(
                EnrolledStudent(
                    studentId = studentId,
                    name = currentUser.name.ifBlank { "Student" },
                    email = currentUser.email,
                    joinedAt = System.currentTimeMillis(),
                    avatarUrl = currentUser.avatarUrl,
                    status = StudentBatchStatus.ACTIVE,
                    degree = currentUser.displayCourse
                )
            )
        }

        for (c in classmates) {
            if (!c.avatarUrl.isNullOrBlank()) {
                userAvatarCache[c.studentId] = c.avatarUrl
                if (c.name.isNotBlank()) {
                    userAvatarCache[c.name.trim().lowercase()] = c.avatarUrl
                }
            }
        }

        // C. Fetch all quiz results for all quizzes in this batch
        val allBatchQuizResults = mutableListOf<QuizResult>()
        for (quiz in batchQuizzes) {
            val results = quizRepository.getQuizResultsByQuizId(quiz.id)
            allBatchQuizResults.addAll(results)
        }
        val currentStudentResults = quizRepository.getQuizResultsForUser(studentId)

        // D. Calculate each student's score according to user specification:
        // Sum of all completed quiz scores / count of completed quizzes
        val leaderboardStudents = mutableListOf<BatchLeaderboardStudent>()

        for (student in classmates) {
            val completedBreakdown = mutableListOf<BatchStudentQuizScore>()
            val isCurrent = student.studentId == studentId ||
                    (currentUser != null && (student.studentId == currentUser.id || (student.email.isNotBlank() && student.email.equals(currentUser.email, ignoreCase = true))))

            for (quiz in batchQuizzes) {
                // Find student's best result for this quiz
                val candidateResults = allBatchQuizResults.filter {
                    it.quizId == quiz.id && (
                        it.userId == student.studentId ||
                        (it.userName.isNotBlank() && it.userName.equals(student.name, ignoreCase = true)) ||
                        (isCurrent && (
                            it.userId == studentId ||
                            (currentUser != null && it.userId == currentUser.id) ||
                            (currentUser != null && it.userName.equals(currentUser.name, ignoreCase = true))
                        ))
                    )
                }
                val bestFromBatch = candidateResults.maxByOrNull { it.scorePercentage }
                val bestForCurrent = if (isCurrent) {
                    currentStudentResults.filter { it.quizId == quiz.id }.maxByOrNull { it.scorePercentage }
                } else null

                val result = listOfNotNull(bestFromBatch, bestForCurrent).maxByOrNull { it.scorePercentage }

                if (result != null) {
                    completedBreakdown.add(
                        BatchStudentQuizScore(
                            quizId = quiz.id,
                            quizTitle = quiz.title,
                            scorePercentage = result.scorePercentage,
                            submittedAtMillis = result.submittedAtMillis
                        )
                    )
                }
            }

            val completedCount = completedBreakdown.size
            val avgScore = if (completedCount > 0) {
                completedBreakdown.sumOf { it.scorePercentage } / completedCount
            } else {
                0
            }

            val resolvedAvatar = if (isCurrent) {
                currentUser?.avatarUrl
                    ?: student.avatarUrl
                    ?: userAvatarCache[student.studentId]
                    ?: userAvatarCache[student.name.trim().lowercase()]
            } else {
                student.avatarUrl
                    ?: userAvatarCache[student.studentId]
                    ?: userAvatarCache[student.name.trim().lowercase()]
            }

            leaderboardStudents.add(
                BatchLeaderboardStudent(
                    studentId = student.studentId,
                    studentName = student.name,
                    studentEmail = student.email,
                    avatarUrl = resolvedAvatar,
                    completedCount = completedCount,
                    totalAssigned = totalAssignedQuizzes,
                    averageScore = avgScore,
                    quizBreakdown = completedBreakdown,
                    isCurrentStudent = isCurrent
                )
            )
        }

        // E. Sort: Active students (completed >= 1 quiz) get ranked 1, 2, 3...
        // Unranked students (0 completed) have rank = 0 and appear at the bottom
        val activeStudents = leaderboardStudents.filter { it.completedCount > 0 }.sortedWith(
            compareByDescending<BatchLeaderboardStudent> { it.averageScore }
                .thenByDescending { it.completedCount }
                .thenBy { it.studentName }
        ).mapIndexed { index, item ->
            item.copy(rank = index + 1)
        }

        val unrankedStudents = leaderboardStudents.filter { it.completedCount == 0 }.sortedBy {
            it.studentName
        }.map { item ->
            item.copy(rank = 0)
        }

        val sortedStudents = activeStudents + unrankedStudents

        val classAvg = if (activeStudents.isNotEmpty()) {
            activeStudents.sumOf { it.averageScore } / activeStudents.size
        } else {
            0
        }

        val currentRank = sortedStudents.find { it.isCurrentStudent }?.let {
            if (it.hasRank) it.rank else null
        }

        _uiState.update {
            it.copy(
                batchLeaderboard = BatchLeaderboardData(
                    batch = batch,
                    assignedQuizzes = batchQuizzes,
                    students = sortedStudents,
                    classAverageScore = classAvg,
                    currentStudentRank = currentRank
                )
            )
        }
    }

    private suspend fun resolveAvatar(userId: String, userName: String): String? {
        if (userId.isNotBlank() && userAvatarCache.containsKey(userId)) {
            val cached = userAvatarCache[userId]
            if (!cached.isNullOrBlank()) return cached
        }
        val lowerName = userName.trim().lowercase()
        if (lowerName.isNotBlank() && userAvatarCache.containsKey(lowerName)) {
            val cached = userAvatarCache[lowerName]
            if (!cached.isNullOrBlank()) return cached
        }

        if (userId.isNotBlank()) {
            val fs = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }
            if (fs != null) {
                try {
                    val studentDoc = fs.collection("students").document(userId).get().await()
                    var url = studentDoc.getString("avatarUrl")
                    if (url.isNullOrBlank()) {
                        val userDoc = fs.collection("users").document(userId).get().await()
                        url = userDoc.getString("avatarUrl")
                    }
                    if (!url.isNullOrBlank()) {
                        userAvatarCache[userId] = url
                        if (lowerName.isNotBlank()) userAvatarCache[lowerName] = url
                        return url
                    }
                } catch (_: Exception) {
                    userAvatarCache[userId] = null
                }
            }
        }
        return null
    }

    private suspend fun computeIndividualQuizzesLeaderboard(
        studentId: String,
        studentBatches: List<Batch>
    ) {
        val enrolledBatchIds = studentBatches.map { it.id }.toSet()
        val batchMap = studentBatches.associateBy { it.id }

        // Pre-cache all classmates across all student's batches
        for (b in studentBatches) {
            val classmates = batchRepository.getStudentsInBatchForStudent(b.id, studentId).getOrNull() ?: emptyList()
            for (c in classmates) {
                if (!c.avatarUrl.isNullOrBlank()) {
                    userAvatarCache[c.studentId] = c.avatarUrl
                    if (c.name.isNotBlank()) {
                        userAvatarCache[c.name.trim().lowercase()] = c.avatarUrl
                    }
                }
            }
        }

        // Accessible quizzes for this student:
        // 1. Quizzes assigned to their batches
        // 2. Quizzes assigned individually to them
        // 3. Public quizzes / code quizzes
        // 4. Any quizzes the student has previously attempted/completed
        val accessibleQuizzes = quizRepository.getAccessibleQuizzesForStudent(studentId)
        val userCompletedResults = quizRepository.getQuizResultsForUser(studentId)
        val completedQuizIds = userCompletedResults.map { it.quizId }.toSet()

        val completedQuizzes = completedQuizIds.mapNotNull { qId ->
            accessibleQuizzes.find { it.id == qId } ?: quizRepository.getQuizById(qId)
        }
        val allQuizzes = (accessibleQuizzes + completedQuizzes).distinctBy { it.id }

        val individualQuizItems = mutableListOf<IndividualQuizLeaderboardItem>()
        val currentUser = AuthSessionManager.getSavedUser() ?: AuthRepositoryImpl.instance.getCurrentUser()

        for (quiz in allQuizzes) {
            // Determine assignment source label
            val sourceLabel = when {
                quiz.targetBatchIds.isNotEmpty() -> {
                    val bName = quiz.targetBatchIds.firstNotNullOfOrNull { batchMap[it]?.name } ?: "Batch"
                    "Batch: $bName"
                }
                quiz.targetStudentIds.contains(studentId) -> {
                    "Direct Assignment"
                }
                else -> {
                    "Public Quiz"
                }
            }

            // Fetch all student completions for this specific quiz
            val completions = quizRepository.getQuizResultsByQuizId(quiz.id).toMutableList()

            // Guarantee that if the current student completed this quiz, their result is present
            val myResult = userCompletedResults.find { it.quizId == quiz.id }
            if (myResult != null && completions.none { 
                it.userId == studentId || (currentUser != null && (it.userId == currentUser.id || (it.userName.isNotBlank() && it.userName.equals(currentUser.name, ignoreCase = true))))
            }) {
                completions.add(myResult)
            }

            // Rank completions descending by scorePercentage, then ascending by time taken
            val rankedAttempts = completions
                .sortedWith(
                    compareByDescending<QuizResult> { it.scorePercentage }
                        .thenBy { it.elapsedTimeSeconds }
                        .thenBy { it.submittedAtMillis }
                )
                .mapIndexed { idx, res ->
                    val isCurrent = res.userId == studentId ||
                            (currentUser != null && (res.userId == currentUser.id || (res.userName.isNotBlank() && res.userName.equals(currentUser.name, ignoreCase = true))))
                    val resolvedAvatar = if (isCurrent) {
                        currentUser?.avatarUrl ?: resolveAvatar(res.userId, res.userName)
                    } else {
                        resolveAvatar(res.userId, res.userName)
                    }
                    QuizLeaderboardStudentAttempt(
                        studentId = res.userId,
                        studentName = res.userName.ifBlank { "Student" },
                        avatarUrl = resolvedAvatar,
                        scorePercentage = res.scorePercentage,
                        elapsedTimeSeconds = res.elapsedTimeSeconds,
                        submittedAtMillis = res.submittedAtMillis,
                        rank = idx + 1,
                        isCurrentStudent = isCurrent
                    )
                }

            val currentAttempt = rankedAttempts.find { it.isCurrentStudent }

            individualQuizItems.add(
                IndividualQuizLeaderboardItem(
                    quiz = quiz,
                    assignmentSource = sourceLabel,
                    totalCompletionsCount = rankedAttempts.size,
                    studentAttempts = rankedAttempts,
                    currentStudentAttempt = currentAttempt
                )
            )
        }

        // Sort quizzes: completed or assigned ones first
        val sortedQuizItems = individualQuizItems.sortedWith(
            compareByDescending<IndividualQuizLeaderboardItem> { it.currentStudentAttempt != null }
                .thenByDescending { it.totalCompletionsCount }
        )

        _uiState.update {
            it.copy(individualQuizzes = sortedQuizItems)
        }
    }
}
