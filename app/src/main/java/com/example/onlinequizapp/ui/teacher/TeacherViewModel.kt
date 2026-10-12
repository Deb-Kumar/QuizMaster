package com.example.onlinequizapp.ui.teacher

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.repository.AuthRepository
import com.example.onlinequizapp.domain.repository.BatchRepository
import com.example.onlinequizapp.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class TeacherDashboardUiState(
    val isLoading: Boolean = false,
    val quizzes: List<Quiz> = emptyList(),
    val submissions: List<QuizSessionState> = emptyList(),
    val teacherUser: User? = null,
    val quizPendingDeletion: Quiz? = null,
    val selectedTab: Int = 0, // 0: Quizzes, 1: Submissions
    val toastMessage: String? = null,
    val studentResults: List<QuizResult> = emptyList(),
    val isLoadingStudentResults: Boolean = false,
    val totalStudents: Int = 0
) {
    val totalQuizzes: Int get() = quizzes.size
    val totalQuestions: Int get() = quizzes.sumOf { it.questions.size }
    val totalSubmissions: Int get() = submissions.size
    val averageScore: Int
        get() = if (submissions.isEmpty()) 0 else submissions.map { it.scorePercentage }.average().toInt()
}

class TeacherViewModel(
    private val quizRepository: QuizRepository = QuizRepositoryImpl.instance,
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance,
    private val batchRepository: BatchRepository = BatchRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherDashboardUiState())
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeQuizzes()
        observeBatchesAndStudents()
    }

    private fun observeQuizzes() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val teacherId = user?.id ?: ""
            QuizRepositoryImpl.instance.quizzesFlow.collect { updatedQuizzes ->
                val filtered = if (teacherId.isNotBlank()) {
                    updatedQuizzes.filter { it.teacherId == teacherId }
                        .sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
                } else {
                    emptyList()
                }
                _uiState.update { it.copy(quizzes = filtered) }
            }
        }
    }

    private fun observeBatchesAndStudents() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val teacherId = user?.id ?: ""
            if (teacherId.isBlank()) return@launch

            batchRepository.getBatchesByTeacher(teacherId).collect { batches ->
                val aggregatedStudentIds = mutableSetOf<String>()
                for (batch in batches) {
                    val result = withTimeoutOrNull(1000L) {
                        batchRepository.getStudentsInBatch(batch.id, teacherId)
                    }
                    result?.getOrNull()?.forEach { enrolled ->
                        if (enrolled.studentId.isNotBlank()) {
                            aggregatedStudentIds.add(enrolled.studentId)
                        }
                    }
                }
                _uiState.update { it.copy(totalStudents = aggregatedStudentIds.size) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                val teacherId = user?.id ?: ""

                val quizzes = if (teacherId.isNotBlank()) {
                    quizRepository.getQuizzesByTeacher(teacherId)
                        .sortedByDescending { maxOf(it.updatedAt, it.createdAt) }
                } else {
                    emptyList()
                }
                val submissions = if (teacherId.isNotBlank()) {
                    quizRepository.getCompletedResultsForTeacher(teacherId)
                } else {
                    emptyList()
                }

                val batches = if (teacherId.isNotBlank()) {
                    BatchRepositoryImpl.instance.getBatchesByTeacherSnapshot(teacherId)
                } else {
                    emptyList()
                }

                val aggregatedStudentIds = mutableSetOf<String>()
                for (batch in batches) {
                    val result = withTimeoutOrNull(1000L) {
                        batchRepository.getStudentsInBatch(batch.id, teacherId)
                    }
                    result?.getOrNull()?.forEach { enrolled ->
                        if (enrolled.studentId.isNotBlank()) {
                            aggregatedStudentIds.add(enrolled.studentId)
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        quizzes = quizzes,
                        submissions = submissions,
                        teacherUser = user,
                        totalStudents = aggregatedStudentIds.size
                    )
                }
            } catch (e: Exception) {
                Log.e("TeacherViewModel", "Error loading teacher data", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun requestDeleteQuiz(quiz: Quiz) {
        val currentTeacherId = _uiState.value.teacherUser?.id ?: ""
        if (quiz.teacherId.isNotBlank() && currentTeacherId.isNotBlank() && quiz.teacherId != currentTeacherId) {
            _uiState.update { it.copy(toastMessage = "Unauthorized: You do not own this quiz.") }
            return
        }
        _uiState.update { it.copy(quizPendingDeletion = quiz) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(quizPendingDeletion = null) }
    }

    fun confirmDeleteQuiz() {
        val quiz = _uiState.value.quizPendingDeletion ?: return
        val teacherId = _uiState.value.teacherUser?.id ?: return
        viewModelScope.launch {
            val success = quizRepository.deleteQuiz(quiz.id, teacherId)
            if (success) {
                _uiState.update {
                    it.copy(
                        quizPendingDeletion = null,
                        toastMessage = "Deleted quiz '${quiz.title}'"
                    )
                }
                loadData()
            }
        }
    }

    fun duplicateQuiz(quiz: Quiz) {
        val teacherId = _uiState.value.teacherUser?.id ?: return
        viewModelScope.launch {
            val newCode = quizRepository.generateUniqueQuizCode()
            val copy = quiz.copy(
                id = "quiz_${java.util.UUID.randomUUID().toString().take(8)}",
                title = "${quiz.title} (Copy)",
                quizCode = newCode,
                teacherId = teacherId,
                status = com.example.onlinequizapp.domain.model.QuizStatus.DRAFT,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val success = quizRepository.createQuiz(copy, teacherId)
            if (success) {
                _uiState.update { it.copy(toastMessage = "Duplicated quiz '${quiz.title}'") }
                loadData()
            }
        }
    }

    fun toggleQuizStatus(quiz: Quiz, newStatus: com.example.onlinequizapp.domain.model.QuizStatus) {
        val teacherId = _uiState.value.teacherUser?.id ?: return
        viewModelScope.launch {
            val updated = quiz.copy(
                status = newStatus,
                teacherId = teacherId,
                updatedAt = System.currentTimeMillis()
            )
            val success = quizRepository.updateQuiz(updated, teacherId)
            if (success) {
                val statusLabel = if (newStatus == com.example.onlinequizapp.domain.model.QuizStatus.PUBLISHED) "published" else "moved to draft"
                _uiState.update { it.copy(toastMessage = "Quiz '${quiz.title}' $statusLabel") }
                loadData()
            }
        }
    }

    fun resetToDefaultQuizzes() {
        viewModelScope.launch {
            quizRepository.resetToDefaultQuizzes()
            _uiState.update { it.copy(toastMessage = "Reset to default quizzes.") }
            loadData()
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    fun updateProfile(
        name: String,
        university: String,
        department: String = "",
        course: String = "",
        avatarUrl: String? = null,
        email: String? = null,
        contactNumber: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        val user = _uiState.value.teacherUser ?: return
        viewModelScope.launch {
            val result = authRepository.updateUserProfile(
                userId = user.id,
                name = name,
                university = university,
                department = department,
                course = course,
                avatarUrl = avatarUrl,
                email = email,
                contactNumber = contactNumber
            )
            result.fold(
                onSuccess = { updated ->
                    _uiState.update { it.copy(teacherUser = updated) }
                    onResult(true)
                },
                onFailure = {
                    onResult(false)
                }
            )
        }
    }

    fun setOrChangePassword(
        currentPassword: String?,
        newPassword: String,
        onResult: (Result<User>) -> Unit
    ) {
        val user = _uiState.value.teacherUser ?: run {
            onResult(Result.failure(IllegalStateException("No authenticated teacher found.")))
            return
        }
        viewModelScope.launch {
            val result = authRepository.setOrChangePassword(
                userId = user.id,
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            result.onSuccess { updated ->
                _uiState.update { it.copy(teacherUser = updated) }
            }
            onResult(result)
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    /**
     * Loads all student quiz results for a specific quiz from Firestore.
     * Teachers can see who attempted their quizzes and their scores.
     */
    fun loadStudentResults(quizId: String) {
        val teacherId = _uiState.value.teacherUser?.id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingStudentResults = true) }
            try {
                val results = quizRepository.getQuizResultsByQuizId(quizId, teacherId)
                _uiState.update { it.copy(studentResults = results, isLoadingStudentResults = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingStudentResults = false) }
            }
        }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        val user = _uiState.value.teacherUser ?: return
        viewModelScope.launch {
            val result = authRepository.deleteAccount(user.id)
            onResult(result.isSuccess)
        }
    }

    fun submitFeedback(
        rating: Int,
        category: String,
        comment: String,
        onResult: (Boolean) -> Unit
    ) {
        val user = _uiState.value.teacherUser
        viewModelScope.launch {
            val success = quizRepository.submitFeedback(
                userId = user?.id ?: "anonymous",
                userName = user?.name ?: "Teacher",
                userEmail = user?.email ?: "teacher@quizmaster.com",
                role = user?.role?.label ?: "Teacher",
                rating = rating,
                category = category,
                comment = comment
            )
            onResult(success)
        }
    }
}
