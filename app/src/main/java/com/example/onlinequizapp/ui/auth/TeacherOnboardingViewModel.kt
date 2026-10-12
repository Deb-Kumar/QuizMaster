package com.example.onlinequizapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.domain.model.TeacherProfile
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TeacherOnboardingUiState(
    val organization: String = "",
    val department: String = "",
    val designation: String = "",
    val referralSource: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TeacherOnboardingViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherOnboardingUiState())
    val uiState: StateFlow<TeacherOnboardingUiState> = _uiState.asStateFlow()

    fun onOrganizationChanged(value: String) {
        _uiState.update { it.copy(organization = value, errorMessage = null) }
    }

    fun onDepartmentSelected(value: String) {
        _uiState.update { it.copy(department = value, errorMessage = null) }
    }

    fun onDesignationSelected(value: String) {
        _uiState.update { it.copy(designation = value, errorMessage = null) }
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
        val org = s.organization.trim()
        val dept = s.department.trim()
        val desig = s.designation.trim()

        if (org.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter the name of your organization, college, or university.") }
            return
        }

        if (dept.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please specify your department.") }
            return
        }

        if (desig.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please select or enter your designation.") }
            return
        }

        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "No authenticated user session found.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profile = TeacherProfile(
                organization = org,
                department = dept,
                designation = desig,
                referralSource = s.referralSource.trim()
            )
            val result = authRepository.updateTeacherProfile(currentUser.id, profile)
            result.fold(
                onSuccess = { updatedUser ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(updatedUser)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to save profile."
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
