package com.example.onlinequizapp.data.repository

import android.util.Log
import com.example.onlinequizapp.domain.model.StudentProfile
import com.example.onlinequizapp.domain.model.TeacherProfile
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.onlinequizapp.data.session.AuthSessionManager
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AuthRepositoryImpl private constructor() : AuthRepository {

    private val mutex = Mutex()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (_: Throwable) {
            null
        }
    }

    // User accounts storage: key (${email}_${role}) -> Pair(User, password)
    private val userDatabase = mutableMapOf<String, Pair<User, String>>()

    private fun getUserDbKey(email: String, role: UserRole): String = "${email.trim().lowercase()}_${role.name}"

    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Restore existing user session from AuthSessionManager
        try {
            val savedUser = AuthSessionManager.getSavedUser()
            if (savedUser != null) {
                _currentUser.value = savedUser
                Log.d("AuthRepository", "Restored session for ${savedUser.email} (${savedUser.role})")
            }
            firebaseAuth?.currentUser?.let { firebaseUser ->
                scope.launch {
                    val targetRole = savedUser?.role ?: UserRole.STUDENT
                    fetchUserFromFirestore(firebaseUser.uid, firebaseUser.email, targetRole)
                }
            }
        } catch (_: Throwable) {
            // Firebase not initialized in unit tests
        }
    }

    private fun parseUserFromDocument(
        doc: DocumentSnapshot,
        uid: String,
        fallbackEmail: String,
        role: UserRole,
        fallbackProvider: String
    ): User {
        val name = doc.getString("name") ?: "Quiz User"
        val email = doc.getString("email") ?: fallbackEmail
        val provider = doc.getString("authProvider") ?: fallbackProvider
        val avatarUrl = doc.getString("avatarUrl")
        val isOnboardingCompleted = doc.getBoolean("isOnboardingCompleted") ?: false

        val tpMap = doc.get("teacherProfile") as? Map<*, *>
        val teacherProfile = if (tpMap != null) {
            TeacherProfile(
                organization = tpMap["organization"] as? String ?: "",
                department = tpMap["department"] as? String ?: "",
                designation = tpMap["designation"] as? String ?: "",
                referralSource = tpMap["referralSource"] as? String ?: ""
            )
        } else null

        val spMap = doc.get("studentProfile") as? Map<*, *>
        val studentProfile = if (spMap != null) {
            StudentProfile(
                university = spMap["university"] as? String ?: "",
                department = spMap["department"] as? String ?: "",
                course = spMap["course"] as? String ?: "",
                referralSource = spMap["referralSource"] as? String ?: ""
            )
        } else null

        val university = doc.getString("university")
            ?: (spMap?.get("university") as? String)
            ?: (tpMap?.get("organization") as? String)
            ?: (if (role == UserRole.TEACHER) "Oxford University" else "Delhi University")

        val department = doc.getString("department")
            ?: (spMap?.get("department") as? String)
            ?: (tpMap?.get("department") as? String)
            ?: (if (role == UserRole.TEACHER) "Computer Science & Engineering" else "Computer Science")

        val course = doc.getString("course")
            ?: (spMap?.get("course") as? String)
            ?: (tpMap?.get("designation") as? String)
            ?: (if (role == UserRole.TEACHER) "Advanced Algorithms & AI" else "B.Tech Computer Science")

        val hasPasswordSet = doc.getBoolean("hasPasswordSet")
            ?: (provider == "email")

        val contactNumber = doc.getString("contactNumber") ?: ""

        return User(
            id = uid,
            name = name,
            email = email,
            role = role,
            avatarUrl = avatarUrl,
            university = university,
            department = department,
            course = course,
            authProvider = provider,
            hasPasswordSet = hasPasswordSet,
            isOnboardingCompleted = isOnboardingCompleted,
            teacherProfile = teacherProfile,
            studentProfile = studentProfile,
            contactNumber = contactNumber
        )
    }

    private suspend fun fetchUserFromFirestore(uid: String, fallbackEmail: String?, role: UserRole) {
        val fs = firestore ?: return
        try {
            val collectionName = if (role == UserRole.TEACHER) "teachers" else "students"
            var doc = fs.collection(collectionName).document(uid).get().await()
            if (!doc.exists()) {
                val legacyDoc = fs.collection("users").document(uid).get().await()
                if (legacyDoc.exists()) {
                    val legacyRoleStr = legacyDoc.getString("role")
                    if (legacyRoleStr.equals(role.name, ignoreCase = true)) {
                        doc = legacyDoc
                    }
                }
            }
            if (doc.exists()) {
                val user = parseUserFromDocument(doc, uid, fallbackEmail ?: "", role, doc.getString("authProvider") ?: "email")
                _currentUser.value = user
                AuthSessionManager.saveUser(user)
            }
        } catch (e: Exception) {
            Log.w("AuthRepository", "Could not fetch user profile from Firestore: ${e.message}")
        }
    }

    override suspend fun login(email: String, password: String, role: UserRole): Result<User> {
        return mutex.withLock {
            val trimmedEmail = email.trim().lowercase()

            if (trimmedEmail.isBlank() || password.isBlank()) {
                return@withLock Result.failure(IllegalArgumentException("Email and password cannot be empty."))
            }

            if (password.length > 16) {
                return@withLock Result.failure(IllegalArgumentException("Password cannot exceed 16 characters."))
            }

            // 1. Try Firebase Authentication first if available on device
            val auth = firebaseAuth
            val fs = firestore
            if (auth != null && fs != null) {
                try {
                    val authResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                    val uid = authResult.user?.uid ?: UUID.randomUUID().toString()

                    val collectionName = if (role == UserRole.TEACHER) "teachers" else "students"
                    var doc = try {
                        fs.collection(collectionName).document(uid).get().await()
                    } catch (_: Exception) {
                        null
                    }

                    if (doc == null || !doc.exists()) {
                        val legacyDoc = try {
                            fs.collection("users").document(uid).get().await()
                        } catch (_: Exception) {
                            null
                        }
                        if (legacyDoc != null && legacyDoc.exists()) {
                            val savedRoleStr = legacyDoc.getString("role")
                            if (savedRoleStr.equals(role.name, ignoreCase = true)) {
                                doc = legacyDoc
                            }
                        }
                    }

                    val user: User
                    if (doc != null && doc.exists()) {
                        user = parseUserFromDocument(doc, uid, trimmedEmail, role, "email")
                    } else {
                        // First time logging in with this role: create separate profile for this role
                        user = User(
                            id = uid,
                            name = authResult.user?.displayName ?: trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
                            email = trimmedEmail,
                            role = role,
                            authProvider = "email",
                            isOnboardingCompleted = false
                        )
                        saveUserToFirestore(user)
                    }

                    _currentUser.value = user
                    AuthSessionManager.saveUser(user)
                    val localKey = getUserDbKey(trimmedEmail, role)
                    userDatabase[localKey] = Pair(user, hashPassword(password))
                    return@withLock Result.success(user)
                } catch (e: FirebaseAuthException) {
                    return@withLock Result.failure(IllegalArgumentException(getFriendlyAuthErrorMessage(e)))
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firebase sign-in fallback: ${e.message}")
                }
            }

            // 2. Check local database
            val localKey = getUserDbKey(trimmedEmail, role)
            val userRecord = userDatabase[localKey] ?: userDatabase[trimmedEmail]?.takeIf { it.first.role == role }
            if (userRecord == null) {
                return@withLock Result.failure(IllegalArgumentException("Account not found for ${role.label}. Please register first."))
            }

            if (userRecord.second != hashPassword(password)) {
                return@withLock Result.failure(IllegalArgumentException("Incorrect password. Please try again."))
            }

            _currentUser.value = userRecord.first
            AuthSessionManager.saveUser(userRecord.first)
            Result.success(userRecord.first)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> {
        return mutex.withLock {
            val trimmedName = name.trim()
            val trimmedEmail = email.trim().lowercase()

            if (trimmedName.isBlank()) {
                return@withLock Result.failure(IllegalArgumentException("Full name cannot be empty."))
            }

            if (!trimmedEmail.contains("@") || !trimmedEmail.contains(".")) {
                return@withLock Result.failure(IllegalArgumentException("Please enter a valid email address."))
            }

            if (password.length !in 6..16) {
                return@withLock Result.failure(IllegalArgumentException("Password must be between 6 and 16 characters."))
            }

            val localKey = getUserDbKey(trimmedEmail, role)
            if (userDatabase.containsKey(localKey)) {
                return@withLock Result.failure(IllegalArgumentException("A ${role.label.lowercase()} account with this email already exists."))
            }

            // 1. Try Firebase Authentication if available
            val auth = firebaseAuth
            val fs = firestore
            if (auth != null) {
                try {
                    var uid: String
                    try {
                        val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                        uid = authResult.user?.uid ?: UUID.randomUUID().toString()
                    } catch (_: FirebaseAuthUserCollisionException) {
                        // User already registered in Firebase Auth (e.g. from the other role)
                        // Authenticate with existing password to verify ownership:
                        val signInResult = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                        uid = signInResult.user?.uid ?: UUID.randomUUID().toString()

                        // Check if this specific role already exists in database
                        val collectionName = if (role == UserRole.TEACHER) "teachers" else "students"
                        val existingDoc = fs?.collection(collectionName)?.document(uid)?.get()?.await()
                        if (existingDoc != null && existingDoc.exists()) {
                            return@withLock Result.failure(
                                IllegalArgumentException("You already have a ${role.label.lowercase()} account with this email. Please sign in.")
                            )
                        }
                    }

                    val newUser = User(
                        id = uid,
                        name = trimmedName,
                        email = trimmedEmail,
                        role = role,
                        authProvider = "email",
                        isOnboardingCompleted = false
                    )

                    saveUserToFirestore(newUser)
                    userDatabase[localKey] = Pair(newUser, hashPassword(password))
                    _currentUser.value = newUser
                    AuthSessionManager.saveUser(newUser)

                    return@withLock Result.success(newUser)
                } catch (e: FirebaseAuthException) {
                    return@withLock Result.failure(IllegalArgumentException(getFriendlyAuthErrorMessage(e)))
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firebase registration fallback: ${e.message}")
                }
            }

            val localId = "user_${UUID.randomUUID().toString().take(8)}"
            val fallbackUser = User(
                id = localId,
                name = trimmedName,
                email = trimmedEmail,
                role = role,
                authProvider = "email",
                isOnboardingCompleted = false
            )
            userDatabase[localKey] = Pair(fallbackUser, hashPassword(password))
            _currentUser.value = fallbackUser
            AuthSessionManager.saveUser(fallbackUser)
            Result.success(fallbackUser)
        }
    }

    override suspend fun updateTeacherProfile(userId: String, profile: TeacherProfile): Result<User> {
        return mutex.withLock {
            val current = _currentUser.value ?: AuthSessionManager.getSavedUser()
            if (current != null && current.id == userId) {
                val updated = current.copy(
                    teacherProfile = profile,
                    university = profile.organization,
                    department = profile.department,
                    course = profile.designation,
                    isOnboardingCompleted = true
                )
                _currentUser.value = updated
                AuthSessionManager.saveUser(updated)
                scope.launch {
                    saveUserToFirestore(updated)
                }
                val key = getUserDbKey(updated.email, updated.role)
                userDatabase[key]?.let { pair ->
                    userDatabase[key] = Pair(updated, pair.second)
                }
                Result.success(updated)
            } else {
                Result.failure(IllegalArgumentException("User not found or not logged in."))
            }
        }
    }

    override suspend fun updateStudentProfile(userId: String, profile: StudentProfile): Result<User> {
        return mutex.withLock {
            val current = _currentUser.value ?: AuthSessionManager.getSavedUser()
            if (current != null && current.id == userId) {
                val updated = current.copy(
                    studentProfile = profile,
                    university = profile.university,
                    department = profile.department,
                    course = profile.course,
                    isOnboardingCompleted = true
                )
                _currentUser.value = updated
                AuthSessionManager.saveUser(updated)
                scope.launch {
                    saveUserToFirestore(updated)
                }
                val key = getUserDbKey(updated.email, updated.role)
                userDatabase[key]?.let { pair ->
                    userDatabase[key] = Pair(updated, pair.second)
                }
                Result.success(updated)
            } else {
                Result.failure(IllegalArgumentException("User not found or not logged in."))
            }
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String,
        university: String,
        department: String,
        course: String,
        avatarUrl: String?,
        email: String?,
        contactNumber: String?
    ): Result<User> {
        return mutex.withLock {
            val current = _currentUser.value
            if (current != null && current.id == userId) {
                // If user is Google or GitHub, do NOT allow changing email
                val updatedEmail = if (current.isSocialAccount) {
                    current.email
                } else {
                    email?.trim()?.takeIf { it.isNotBlank() } ?: current.email
                }

                val trimmedUniv = university.trim().ifBlank { current.displayUniversity }
                val trimmedDept = department.trim().ifBlank { current.displayDepartment }
                val trimmedCourse = course.trim().ifBlank { current.displayCourse }
                val trimmedContact = contactNumber?.trim() ?: current.contactNumber

                val updatedStudentProfile = current.studentProfile?.copy(
                    university = trimmedUniv,
                    department = trimmedDept,
                    course = trimmedCourse
                ) ?: StudentProfile(
                    university = trimmedUniv,
                    department = trimmedDept,
                    course = trimmedCourse
                )

                val updatedTeacherProfile = current.teacherProfile?.copy(
                    organization = trimmedUniv,
                    department = trimmedDept,
                    designation = trimmedCourse
                ) ?: TeacherProfile(
                    organization = trimmedUniv,
                    department = trimmedDept,
                    designation = trimmedCourse
                )

                val updated = current.copy(
                    name = name.trim().ifBlank { current.name },
                    email = updatedEmail,
                    university = trimmedUniv,
                    department = trimmedDept,
                    course = trimmedCourse,
                    avatarUrl = avatarUrl ?: current.avatarUrl,
                    contactNumber = trimmedContact,
                    studentProfile = if (current.role == UserRole.STUDENT) updatedStudentProfile else current.studentProfile,
                    teacherProfile = if (current.role == UserRole.TEACHER) updatedTeacherProfile else current.teacherProfile
                )
                _currentUser.value = updated
                AuthSessionManager.saveUser(updated)
                scope.launch {
                    saveUserToFirestore(updated)
                }
                val key = getUserDbKey(updated.email, updated.role)
                userDatabase[key]?.let { pair ->
                    userDatabase[key] = Pair(updated, pair.second)
                }
                Result.success(updated)
            } else {
                Result.failure(IllegalArgumentException("User not found or not logged in."))
            }
        }
    }

    override suspend fun loginWithGoogle(
        role: UserRole,
        name: String?,
        email: String?,
        idToken: String?,
        avatarUrl: String?
    ): Result<User> {
        return mutex.withLock {
            val userEmail = email?.trim()?.lowercase()
                ?: if (role == UserRole.TEACHER) "teacher.google@quizmaster.com" else "student.google@quizmaster.com"
            val userName = name?.trim()
                ?: if (role == UserRole.TEACHER) "Teacher (Google)" else "Student (Google)"

            var firebaseUid: String? = null
            var firebasePhotoUrl: String? = null
            val auth = firebaseAuth
            if (auth != null && !idToken.isNullOrBlank()) {
                try {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = auth.signInWithCredential(credential).await()
                    firebaseUid = authResult.user?.uid
                    firebasePhotoUrl = authResult.user?.photoUrl?.toString()
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firebase Google credential sign in: ${e.message}")
                }
            }

            val roleCollection = if (role == UserRole.TEACHER) "teachers" else "students"
            val localKey = getUserDbKey(userEmail, role)
            val savedSessionUser = AuthSessionManager.getSavedUser()?.takeIf {
                it.email.equals(userEmail, ignoreCase = true) && it.role == role
            }
            val existingLocal = userDatabase[localKey]?.first ?: savedSessionUser

            var existingFirestoreUser: User? = null
            val fs = firestore
            if (fs != null) {
                try {
                    val doc = if (firebaseUid != null) {
                        var roleDoc = fs.collection(roleCollection).document(firebaseUid).get().await()
                        if (!roleDoc.exists()) {
                            val legacyDoc = fs.collection("users").document(firebaseUid).get().await()
                            if (legacyDoc.exists() && legacyDoc.getString("role").equals(role.name, ignoreCase = true)) {
                                roleDoc = legacyDoc
                            }
                        }
                        roleDoc
                    } else {
                        val query = fs.collection(roleCollection).whereEqualTo("email", userEmail).limit(1).get().await()
                        query.documents.firstOrNull()
                    }
                    if (doc != null && doc.exists()) {
                        existingFirestoreUser = parseUserFromDocument(
                            doc = doc,
                            uid = doc.id,
                            fallbackEmail = userEmail,
                            role = role,
                            fallbackProvider = "google"
                        )
                    }
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firestore lookup during Google sign in: ${e.message}")
                }
            }

            val resolvedExisting = existingFirestoreUser ?: existingLocal
            val userId = firebaseUid ?: resolvedExisting?.id ?: "google_${UUID.randomUUID().toString().take(8)}"
            val finalAvatar = avatarUrl ?: firebasePhotoUrl ?: resolvedExisting?.avatarUrl

            val user = resolvedExisting?.copy(
                role = role,
                name = if (name != null) userName else resolvedExisting.name,
                avatarUrl = finalAvatar ?: resolvedExisting.avatarUrl
            ) ?: User(
                id = userId,
                name = userName,
                email = userEmail,
                role = role,
                avatarUrl = finalAvatar,
                university = "",
                department = "",
                course = "",
                authProvider = "google",
                hasPasswordSet = false,
                isOnboardingCompleted = false,
                studentProfile = null,
                teacherProfile = null
            )

            scope.launch {
                saveUserToFirestore(user)
            }

            userDatabase[localKey] = Pair(user, "social_auth_token")
            _currentUser.value = user
            AuthSessionManager.saveUser(user)
            Result.success(user)
        }
    }

    override suspend fun loginWithGithub(
        role: UserRole,
        name: String?,
        email: String?,
        avatarUrl: String?
    ): Result<User> {
        return mutex.withLock {
            val userEmail = email?.trim()?.lowercase()
                ?: if (role == UserRole.TEACHER) "teacher.github@quizmaster.com" else "student.github@quizmaster.com"
            val userName = name?.trim()
                ?: if (role == UserRole.TEACHER) "Teacher (GitHub)" else "Student (GitHub)"

            var firebaseUid: String? = null
            var firebasePhotoUrl: String? = null
            val auth = firebaseAuth
            val currentUser = auth?.currentUser
            if (currentUser != null && (currentUser.email.equals(userEmail, ignoreCase = true) || currentUser.providerData.any { it.providerId == "github.com" })) {
                firebaseUid = currentUser.uid
                firebasePhotoUrl = currentUser.photoUrl?.toString()
            }

            val roleCollection = if (role == UserRole.TEACHER) "teachers" else "students"
            val localKey = getUserDbKey(userEmail, role)
            val savedSessionUser = AuthSessionManager.getSavedUser()?.takeIf {
                it.email.equals(userEmail, ignoreCase = true) && it.role == role
            }
            val existingLocal = userDatabase[localKey]?.first ?: savedSessionUser

            var existingFirestoreUser: User? = null
            val fs = firestore
            if (fs != null) {
                try {
                    val doc = if (firebaseUid != null) {
                        var roleDoc = fs.collection(roleCollection).document(firebaseUid).get().await()
                        if (!roleDoc.exists()) {
                            val legacyDoc = fs.collection("users").document(firebaseUid).get().await()
                            if (legacyDoc.exists() && legacyDoc.getString("role").equals(role.name, ignoreCase = true)) {
                                roleDoc = legacyDoc
                            }
                        }
                        roleDoc
                    } else {
                        val query = fs.collection(roleCollection).whereEqualTo("email", userEmail).limit(1).get().await()
                        query.documents.firstOrNull()
                    }
                    if (doc != null && doc.exists()) {
                        existingFirestoreUser = parseUserFromDocument(
                            doc = doc,
                            uid = doc.id,
                            fallbackEmail = userEmail,
                            role = role,
                            fallbackProvider = "github"
                        )
                    }
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Firestore lookup during GitHub sign in: ${e.message}")
                }
            }

            val resolvedExisting = existingFirestoreUser ?: existingLocal
            val userId = firebaseUid ?: resolvedExisting?.id ?: "github_${UUID.randomUUID().toString().take(8)}"

            val githubHandle = userName.substringBefore("@").replace(" ", "").trim()
            val fallbackGithubAvatar = if (githubHandle.isNotBlank()) "https://github.com/$githubHandle.png" else null
            val finalAvatar = avatarUrl?.takeIf { it.isNotBlank() }
                ?: firebasePhotoUrl?.takeIf { it.isNotBlank() }
                ?: resolvedExisting?.avatarUrl?.takeIf { it.isNotBlank() && (it.contains("github", ignoreCase = true) || it.contains("githubusercontent", ignoreCase = true)) }
                ?: fallbackGithubAvatar
                ?: resolvedExisting?.avatarUrl

            val user = resolvedExisting?.copy(
                role = role,
                name = if (!name.isNullOrBlank()) userName else resolvedExisting.name,
                avatarUrl = finalAvatar,
                authProvider = "github"
            ) ?: User(
                id = userId,
                name = userName,
                email = userEmail,
                role = role,
                avatarUrl = finalAvatar,
                university = "",
                department = "",
                course = "",
                authProvider = "github",
                hasPasswordSet = false,
                isOnboardingCompleted = false,
                studentProfile = null,
                teacherProfile = null
            )

            scope.launch {
                saveUserToFirestore(user)
            }

            userDatabase[localKey] = Pair(user, "social_auth_token")
            _currentUser.value = user
            AuthSessionManager.saveUser(user)
            Result.success(user)
        }
    }

    override suspend fun setOrChangePassword(
        userId: String,
        currentPassword: String?,
        newPassword: String
    ): Result<User> {
        return mutex.withLock {
            val current = _currentUser.value ?: AuthSessionManager.getSavedUser()
            if (current == null || current.id != userId) {
                return@withLock Result.failure(IllegalArgumentException("User session not found."))
            }

            val trimmedNew = newPassword.trim()
            if (trimmedNew.length !in 6..16) {
                return@withLock Result.failure(IllegalArgumentException("Password must be between 6 and 16 characters."))
            }

            val emailKey = getUserDbKey(current.email, current.role)
            val existingRecord = userDatabase[emailKey]

            if (current.hasPasswordSet) {
                // Changing existing password: verify current password
                val trimmedCurrent = currentPassword?.trim() ?: ""
                if (trimmedCurrent.isBlank()) {
                    return@withLock Result.failure(IllegalArgumentException("Please enter your current password."))
                }

                // If user is registered in local db with a regular password, verify old password
                if (existingRecord != null && existingRecord.second != "social_auth_token") {
                    if (existingRecord.second != trimmedCurrent) {
                        return@withLock Result.failure(IllegalArgumentException("Incorrect current password. Please try again."))
                    }
                }

                // If Firebase Auth is in use and user has password provider
                val firebaseUser = firebaseAuth?.currentUser
                if (firebaseUser != null && !current.isSocialAccount) {
                    try {
                        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(current.email, trimmedCurrent)
                        firebaseUser.reauthenticate(credential).await()
                        firebaseUser.updatePassword(trimmedNew).await()
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Firebase updatePassword failed: ${e.message}")
                    }
                }
            } else {
                // First-time setting password for social account:
                val firebaseUser = firebaseAuth?.currentUser
                if (firebaseUser != null) {
                    try {
                        firebaseUser.updatePassword(trimmedNew).await()
                    } catch (e: Exception) {
                        Log.w("AuthRepository", "Firebase initial updatePassword: ${e.message}")
                    }
                }
            }

            val updated = current.copy(hasPasswordSet = true)
            _currentUser.value = updated
            userDatabase[emailKey] = Pair(updated, trimmedNew)
            AuthSessionManager.saveUser(updated)
            scope.launch {
                saveUserToFirestore(updated)
            }
            Result.success(updated)
        }
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return mutex.withLock {
            val current = _currentUser.value ?: AuthSessionManager.getSavedUser()
            try {
                if (current != null) {
                    val roleCollection = if (current.role == UserRole.TEACHER) "teachers" else "students"
                    firestore?.collection(roleCollection)?.document(userId)?.delete()?.await()
                }
                firestore?.collection("users")?.document(userId)?.delete()?.await()
            } catch (e: Exception) {
                Log.w("AuthRepository", "Error deleting user from Firestore: ${e.message}")
            }
            try {
                firebaseAuth?.currentUser?.delete()?.await()
            } catch (e: Exception) {
                Log.w("AuthRepository", "Error deleting Firebase auth user: ${e.message}")
            }
            if (current != null) {
                userDatabase.remove(getUserDbKey(current.email, current.role))
            }
            AuthSessionManager.clear()
            _currentUser.value = null
            Result.success(Unit)
        }
    }

    override suspend fun logout() {
        mutex.withLock {
            try {
                firebaseAuth?.signOut()
            } catch (e: Exception) {
                Log.w("AuthRepository", "Error signing out of Firebase: ${e.message}")
            }
            AuthSessionManager.clear()
            _currentUser.value = null
        }
    }

    override fun getCurrentUser(): User? {
        return _currentUser.value ?: AuthSessionManager.getSavedUser()
    }

    private suspend fun saveUserToFirestore(user: User) {
        val fs = firestore ?: return
        try {
            val userDoc = mutableMapOf<String, Any?>(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "role" to user.role.name,
                "authProvider" to user.authProvider,
                "hasPasswordSet" to user.hasPasswordSet,
                "isOnboardingCompleted" to user.hasCompletedOnboarding,
                "avatarUrl" to user.avatarUrl,
                "university" to user.university,
                "department" to user.department,
                "course" to user.course,
                "contactNumber" to user.contactNumber,
                "updatedAt" to System.currentTimeMillis()
            )
            user.teacherProfile?.let {
                userDoc["teacherProfile"] = mapOf(
                    "organization" to it.organization,
                    "department" to it.department,
                    "designation" to it.designation,
                    "referralSource" to it.referralSource
                )
            }
            user.studentProfile?.let {
                userDoc["studentProfile"] = mapOf(
                    "university" to it.university,
                    "department" to it.department,
                    "course" to it.course,
                    "referralSource" to it.referralSource
                )
            }
            val roleCollection = if (user.role == UserRole.TEACHER) "teachers" else "students"
            fs.collection(roleCollection).document(user.id).set(userDoc).await()
            fs.collection("users").document(user.id).set(userDoc).await()
        } catch (e: Exception) {
            Log.w("AuthRepository", "Failed to save user to Firestore: ${e.message}")
        }
    }

    private fun getFriendlyAuthErrorMessage(e: FirebaseAuthException): String {
        val msg = e.localizedMessage ?: ""
        if (msg.contains("malformed", ignoreCase = true) || msg.contains("supplied auth credential", ignoreCase = true)) {
            return "Incorrect email or password. Please check your credentials and try again."
        }
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect email or password. Please check your credentials and try again."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email. Please register first."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }

    companion object {
        val instance: AuthRepositoryImpl by lazy { AuthRepositoryImpl() }
    }
}
