package com.example.onlinequizapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.onlinequizapp.domain.model.StudentProfile
import com.example.onlinequizapp.domain.model.TeacherProfile
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.domain.model.UserRole
import com.google.firebase.auth.FirebaseAuth

/**
 * Manages persistent user authentication sessions across app launches and restarts.
 */
object AuthSessionManager {

    private const val PREFS_NAME = "quiz_master_session_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_ROLE = "user_role"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_UNIVERSITY = "user_university"
    private const val KEY_DEPARTMENT = "user_department"
    private const val KEY_COURSE = "user_course"
    private const val KEY_AUTH_PROVIDER = "auth_provider"
    private const val KEY_HAS_PASSWORD_SET = "has_password_set"
    private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private const val KEY_CONTACT_NUMBER = "user_contact_number"
    private const val KEY_LAST_SEEN_WELCOME_VERSION = "last_seen_welcome_version"

    // Profile details
    private const val KEY_UNIV = "student_univ"
    private const val KEY_STUDENT_DEPT = "student_dept"
    private const val KEY_STUDENT_COURSE = "student_course"
    private const val KEY_STUDENT_REF = "student_ref"

    private const val KEY_ORG = "teacher_org"
    private const val KEY_TEACHER_DEPT = "teacher_dept"
    private const val KEY_TEACHER_DESIG = "teacher_desig"
    private const val KEY_TEACHER_REF = "teacher_ref"

    private var context: Context? = null
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        android.util.Log.d("AuthSessionManager", "init called, prefs=${prefs != null}")
    }

    private fun getPrefs(): SharedPreferences? {
        if (prefs == null && context != null) {
            prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return prefs
    }

    fun saveUser(user: User) {
        val p = getPrefs()
        if (p == null) {
            android.util.Log.e("AuthSessionManager", "Cannot saveUser: prefs is null!")
            return
        }
        val editor = p.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_NAME, user.name)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_ROLE, user.role.name)
        editor.putString(KEY_AVATAR_URL, user.avatarUrl ?: "")
        editor.putString(KEY_UNIVERSITY, user.university)
        editor.putString(KEY_DEPARTMENT, user.department)
        editor.putString(KEY_COURSE, user.course)
        editor.putString(KEY_AUTH_PROVIDER, user.authProvider)
        editor.putBoolean(KEY_HAS_PASSWORD_SET, user.hasPasswordSet)
        editor.putBoolean(KEY_ONBOARDING_COMPLETED, user.hasCompletedOnboarding)
        editor.putString(KEY_CONTACT_NUMBER, user.contactNumber)

        user.studentProfile?.let {
            editor.putString(KEY_UNIV, it.university)
            editor.putString(KEY_STUDENT_DEPT, it.department)
            editor.putString(KEY_STUDENT_COURSE, it.course)
            editor.putString(KEY_STUDENT_REF, it.referralSource)
        }

        user.teacherProfile?.let {
            editor.putString(KEY_ORG, it.organization)
            editor.putString(KEY_TEACHER_DEPT, it.department)
            editor.putString(KEY_TEACHER_DESIG, it.designation)
            editor.putString(KEY_TEACHER_REF, it.referralSource)
        }
        val committed = editor.commit()
        android.util.Log.d("AuthSessionManager", "saveUser committed=$committed for ${user.email}")
    }

    fun getSavedUser(): User? {
        val p = getPrefs()
        val isLoggedIn = p?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
        val firebaseUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Throwable) {
            null
        }

        android.util.Log.d("AuthSessionManager", "getSavedUser: isLoggedIn=$isLoggedIn, firebaseUser=${firebaseUser?.email}")

        // If neither SharedPreferences nor Firebase Auth has a logged-in user, return null
        if (!isLoggedIn && firebaseUser == null) {
            return null
        }

        val id = p?.getString(KEY_USER_ID, null) ?: firebaseUser?.uid ?: return null
        val email = p?.getString(KEY_EMAIL, null) ?: firebaseUser?.email ?: ""
        val name = p?.getString(KEY_NAME, null)
            ?: firebaseUser?.displayName?.ifBlank { null }
            ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
        val roleStr = p?.getString(KEY_ROLE, UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = runCatching { UserRole.valueOf(roleStr) }.getOrDefault(UserRole.STUDENT)
        val savedProvider = p?.getString(KEY_AUTH_PROVIDER, null)?.takeIf { it.isNotBlank() }
        val firebaseProvider = firebaseUser?.providerData?.firstOrNull { it.providerId == "github.com" }?.let { "github" }
            ?: firebaseUser?.providerData?.firstOrNull { it.providerId == "google.com" }?.let { "google" }
        val authProvider = savedProvider ?: firebaseProvider ?: "email"

        val rawAvatar = p?.getString(KEY_AVATAR_URL, null)?.ifEmpty { null }
            ?: firebaseUser?.photoUrl?.toString()
        val githubHandle = name.substringBefore("@").replace(" ", "").trim()
        val avatarUrl = rawAvatar ?: if (authProvider == "github" && githubHandle.isNotBlank()) "https://github.com/$githubHandle.png" else null

        val studentUniv = p?.getString(KEY_UNIV, null)
        val studentProfile = if (studentUniv != null) {
            StudentProfile(
                university = studentUniv,
                department = p.getString(KEY_STUDENT_DEPT, "") ?: "",
                course = p.getString(KEY_STUDENT_COURSE, "") ?: "",
                referralSource = p.getString(KEY_STUDENT_REF, "") ?: ""
            )
        } else null

        val teacherOrg = p?.getString(KEY_ORG, null)
        val teacherProfile = if (teacherOrg != null) {
            TeacherProfile(
                organization = teacherOrg,
                department = p.getString(KEY_TEACHER_DEPT, "") ?: "",
                designation = p.getString(KEY_TEACHER_DESIG, "") ?: "",
                referralSource = p.getString(KEY_TEACHER_REF, "") ?: ""
            )
        } else null

        val university = p?.getString(KEY_UNIVERSITY, null)
            ?: studentUniv
            ?: teacherOrg
            ?: ""

        val department = p?.getString(KEY_DEPARTMENT, null)
            ?: p?.getString(KEY_STUDENT_DEPT, null)
            ?: p?.getString(KEY_TEACHER_DEPT, null)
            ?: ""

        val course = p?.getString(KEY_COURSE, null)
            ?: p?.getString(KEY_STUDENT_COURSE, null)
            ?: p?.getString(KEY_TEACHER_DESIG, null)
            ?: ""

        val hasPasswordSet = if (p != null && p.contains(KEY_HAS_PASSWORD_SET)) {
            p.getBoolean(KEY_HAS_PASSWORD_SET, true)
        } else {
            authProvider == "email"
        }

        val isOnboardingCompleted = p?.getBoolean(KEY_ONBOARDING_COMPLETED, false) ?: false
        val contactNumber = p?.getString(KEY_CONTACT_NUMBER, "") ?: ""

        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            avatarUrl = avatarUrl,
            university = university,
            department = department,
            course = course,
            authProvider = authProvider,
            hasPasswordSet = hasPasswordSet,
            isOnboardingCompleted = isOnboardingCompleted,
            teacherProfile = teacherProfile,
            studentProfile = studentProfile,
            contactNumber = contactNumber
        )
    }

    fun isLoggedIn(): Boolean {
        if (getPrefs()?.getBoolean(KEY_IS_LOGGED_IN, false) == true) return true
        val firebaseUser = try {
            FirebaseAuth.getInstance().currentUser
        } catch (_: Throwable) {
            null
        }
        return firebaseUser != null
    }

    fun shouldShowWelcomeScreen(): Boolean {
        val ctx = context ?: return true
        val p = getPrefs() ?: return true

        // If key does not exist, it's either first download or app data was cleared
        if (!p.contains(KEY_LAST_SEEN_WELCOME_VERSION)) {
            return true
        }

        val currentVersionCode = getAppVersionCode(ctx)
        val lastSeenVersion = p.getLong(KEY_LAST_SEEN_WELCOME_VERSION, -1L)

        // Show again if app was updated to a newer version
        return currentVersionCode > lastSeenVersion
    }

    fun markWelcomeScreenCompleted() {
        val ctx = context ?: return
        val currentVersionCode = getAppVersionCode(ctx)
        getPrefs()?.edit()?.putLong(KEY_LAST_SEEN_WELCOME_VERSION, currentVersionCode)?.apply()
    }

    private fun getAppVersionCode(context: Context): Long {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            androidx.core.content.pm.PackageInfoCompat.getLongVersionCode(pInfo)
        } catch (_: Throwable) {
            1L
        }
    }

    fun clear() {
        val p = getPrefs()
        val lastSeenVersion = p?.getLong(KEY_LAST_SEEN_WELCOME_VERSION, -1L) ?: -1L
        p?.edit()?.clear()?.apply()
        if (lastSeenVersion != -1L) {
            p?.edit()?.putLong(KEY_LAST_SEEN_WELCOME_VERSION, lastSeenVersion)?.apply()
        }
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Throwable) {}
        android.util.Log.d("AuthSessionManager", "Session cleared")
    }
}
