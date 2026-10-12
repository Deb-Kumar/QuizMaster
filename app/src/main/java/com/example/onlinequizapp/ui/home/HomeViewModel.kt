package com.example.onlinequizapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.domain.repository.AuthRepository
import com.example.onlinequizapp.domain.repository.QuizRepository
import com.example.onlinequizapp.domain.repository.UserStats
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class HomeUiState(
    val isLoading: Boolean = true,
    val quizzes: List<Quiz> = emptyList(),
    val stats: UserStats = UserStats(),
    val currentUser: User? = null,
    val selectedTab: Int = 0, // 0: Home, 1: My Quizzes, 2: History, 3: Profile
    val quizHistory: List<QuizResult> = emptyList(),
    val isLoadingHistory: Boolean = false
) {
    val userName: String get() = currentUser?.name ?: "Alex"
    val isTeacher: Boolean get() = currentUser?.role == UserRole.TEACHER
}

class HomeViewModel(
    private val repository: QuizRepository = QuizRepositoryImpl.instance,
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeUser()
        observeQuizzes()
        loadQuizHistory()
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user) }
                val stats = repository.getUserStats(user?.id)
                _uiState.update { it.copy(stats = stats) }
                // Re-filter quizzes when user or role changes
                refreshAccessibleQuizzes()
            }
        }
    }

    private fun observeQuizzes() {
        viewModelScope.launch {
            QuizRepositoryImpl.instance.quizzesFlow.collect {
                refreshAccessibleQuizzes()
            }
        }
    }

    private suspend fun refreshAccessibleQuizzes() {
        try {
            val user = _uiState.value.currentUser ?: authRepository.getCurrentUser()
            val accessible = if (user?.role == UserRole.TEACHER) {
                val teacherId = user.id
                repository.getQuizzesByTeacher(teacherId)
            } else {
                val studentId = user?.id ?: ""
                repository.getAccessibleQuizzesForStudent(studentId)
            }
            _uiState.update { it.copy(quizzes = accessible) }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to refresh accessible quizzes", e)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = authRepository.getCurrentUser()
                val stats = repository.getUserStats(user?.id)
                val accessibleQuizzes = if (user?.role == UserRole.TEACHER) {
                    val teacherId = user.id
                    repository.getQuizzesByTeacher(teacherId)
                } else {
                    val studentId = user?.id ?: ""
                    repository.getAccessibleQuizzesForStudent(studentId)
                }

                _uiState.update {
                    it.copy(
                        quizzes = accessibleQuizzes,
                        stats = stats,
                        currentUser = user
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading home data", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Loads completed quiz results from Firestore (with local fallback)
     * for the currently logged-in user.
     */
    fun loadQuizHistory() {
        val userId = _uiState.value.currentUser?.id
            ?: com.example.onlinequizapp.data.session.AuthSessionManager.getSavedUser()?.id
            ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHistory = true) }
            try {
                val results = repository.getQuizResultsForUser(userId)
                _uiState.update { it.copy(quizHistory = results, isLoadingHistory = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingHistory = false) }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 2) {
            loadQuizHistory()
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
        val user = _uiState.value.currentUser ?: return
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
                    _uiState.update { it.copy(currentUser = updated) }
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
        val user = _uiState.value.currentUser ?: run {
            onResult(Result.failure(IllegalStateException("No authenticated user found.")))
            return
        }
        viewModelScope.launch {
            val result = authRepository.setOrChangePassword(
                userId = user.id,
                currentPassword = currentPassword,
                newPassword = newPassword
            )
            result.onSuccess { updated ->
                _uiState.update { it.copy(currentUser = updated) }
            }
            onResult(result)
        }
    }

    fun deleteAccount(onResult: (Boolean) -> Unit) {
        val user = _uiState.value.currentUser ?: run {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val result = authRepository.deleteAccount(user.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(currentUser = null) }
                    onResult(true)
                },
                onFailure = {
                    onResult(false)
                }
            )
        }
    }

    fun submitFeedback(
        rating: Int,
        category: String,
        comment: String,
        onResult: (Boolean) -> Unit
    ) {
        val user = _uiState.value.currentUser
        viewModelScope.launch {
            val success = repository.submitFeedback(
                userId = user?.id ?: "anonymous",
                userName = user?.name ?: "Student",
                userEmail = user?.email ?: "student@quizmaster.com",
                role = user?.role?.label ?: "Student",
                rating = rating,
                category = category,
                comment = comment
            )
            onResult(success)
        }
    }
}
