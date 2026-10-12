package com.example.onlinequizapp.domain.repository

import com.example.onlinequizapp.domain.model.StudentProfile
import com.example.onlinequizapp.domain.model.TeacherProfile
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(email: String, password: String, role: UserRole): Result<User>
    suspend fun register(name: String, email: String, password: String, role: UserRole): Result<User>
    suspend fun loginWithGoogle(
        role: UserRole,
        name: String? = null,
        email: String? = null,
        idToken: String? = null,
        avatarUrl: String? = null
    ): Result<User>
    suspend fun loginWithGithub(
        role: UserRole,
        name: String? = null,
        email: String? = null,
        avatarUrl: String? = null
    ): Result<User>
    suspend fun updateTeacherProfile(userId: String, profile: TeacherProfile): Result<User>
    suspend fun updateStudentProfile(userId: String, profile: StudentProfile): Result<User>
    suspend fun updateUserProfile(
        userId: String,
        name: String,
        university: String,
        department: String = "",
        course: String = "",
        avatarUrl: String? = null,
        email: String? = null,
        contactNumber: String? = null
    ): Result<User>
    suspend fun setOrChangePassword(userId: String, currentPassword: String?, newPassword: String): Result<User>
    suspend fun deleteAccount(userId: String): Result<Unit>
    suspend fun logout()
    fun getCurrentUser(): User?
}
