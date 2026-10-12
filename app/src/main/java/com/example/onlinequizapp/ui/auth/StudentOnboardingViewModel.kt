package com.example.onlinequizapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.domain.model.StudentProfile
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentOnboardingUiState(
    val university: String = "",
    val department: String = "",
    val course: String = "",
    val referralSource: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class StudentOnboardingViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentOnboardingUiState())
    val uiState: StateFlow<StudentOnboardingUiState> = _uiState.asStateFlow()

    fun onUniversityChanged(value: String) {
        _uiState.update { it.copy(university = value, errorMessage = null) }
    }

    fun onDepartmentSelected(value: String) {
        _uiState.update { it.copy(department = value, errorMessage = null) }
    }

    fun onCourseSelected(value: String) {
        _uiState.update { it.copy(course = value, errorMessage = null) }
    }

    fun onReferralSourceSelected(value: String) {
        _uiState.update { it.copy(referralSource = value, errorMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun submit(onSuccess: (User) -> Unit) {
        val s = _uiState.value
        val uni = s.university.trim()
        val dept = s.department.trim()
        val course = s.course.trim()

        if (uni.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter the name of your university or college.") }
            return
        }

        if (dept.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please specify your department.") }
            return
        }

        if (course.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select or enter your course/degree (e.g. BCA, MCA, BTech CSE).") }
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "No authenticated user session found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profile = StudentProfile(
                university = uni,
                department = dept,
                course = course,
                referralSource = s.referralSource.trim()
            )
            val result = authRepository.updateStudentProfile(currentUser.id, profile)
            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(updatedUser)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to save student profile."
                        )
                    }
                }
            )
        }
    }

    fun skip(onSuccess: (User) -> Unit) {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            val updated = currentUser.copy(isOnboardingCompleted = true)
            com.example.onlinequizapp.data.session.AuthSessionManager.saveUser(updated)
            onSuccess(updated)
        }
    }
}
