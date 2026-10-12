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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole = UserRole.STUDENT,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        val trimmed = if (password.length > 16) password.take(16) else password
        _uiState.update { it.copy(password = trimmed, errorMessage = null) }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        val trimmed = if (confirmPassword.length > 16) confirmPassword.take(16) else confirmPassword
        _uiState.update { it.copy(confirmPassword = trimmed, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(role = role, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun register(onSuccess: (User) -> Unit) {
        val s = _uiState.value

        if (s.name.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your full name.") }
            return
        }

        if (s.email.trim().isBlank() || !s.email.contains("@") || !s.email.contains(".")) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }

        if (s.password.length !in 6..16) {
            _uiState.update { it.copy(errorMessage = "Password must be between 6 and 16 characters.") }
            return
        }

        if (s.password != s.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                name = s.name,
                email = s.email,
                password = s.password,
                role = s.role
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Registration failed."
                        )
                    }
                }
            )
        }
    }

    fun registerWithGoogle(onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGoogle(_uiState.value.role)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Google registration failed.")
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
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Google registration failed.")
                    }
                }
            )
        }
    }

    fun registerWithGithub(onSuccess: (User) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.loginWithGithub(_uiState.value.role)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "GitHub registration failed.")
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
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(user)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "GitHub registration failed.")
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
}
