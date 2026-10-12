package com.example.onlinequizapp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onlinequizapp.data.repository.AuthRepositoryImpl
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        val trimmed = if (password.length > 16) password.take(16) else password
        _uiState.update { it.copy(password = trimmed, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update {
            it.copy(
                role = role,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login(onSuccess: (User) -> Unit) {
        val currentState = _uiState.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password cannot be empty.") }
            return
        }

        if (currentState.password.length > 16) {
            _uiState.update { it.copy(errorMessage = "Password cannot exceed 16 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                email = currentState.email,
                password = currentState.password,
                role = currentState.role
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Authentication failed."
                        )
                    }
                }
            )
        }
    }

    fun loginWithGoogle(onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGoogle(_uiState.value.role)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Google sign-in failed.")
                    }
                }
            )
        }
    }

    fun handleGoogleSignInResult(
        name: String,
        email: String,
        idToken: String?,
        avatarUrl: String? = null,
        onSuccess: (User) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGoogle(
                role = _uiState.value.role,
                name = name,
                email = email,
                idToken = idToken,
                avatarUrl = avatarUrl
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Google sign-in failed.")
                    }
                }
            )
        }
    }

    fun loginWithGithub(onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGithub(_uiState.value.role)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "GitHub sign-in failed.")
                    }
                }
            )
        }
    }

    fun handleGithubSignInResult(
        name: String,
        email: String,
        avatarUrl: String? = null,
        onSuccess: (User) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGithub(
                role = _uiState.value.role,
                name = name,
                email = email,
                avatarUrl = avatarUrl
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "GitHub sign-in failed.")
                    }
                }
            )
        }
    }

    fun onAuthError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
