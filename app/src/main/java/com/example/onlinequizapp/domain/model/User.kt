package com.example.onlinequizapp.domain.model

enum class UserRole(val label: String) {
    STUDENT("Student"),
    TEACHER("Teacher")
}

data class TeacherProfile(
    val organization: String = "",
    val department: String = "",
    val designation: String = "",
    val referralSource: String = ""
)

data class StudentProfile(
    val university: String = "",
    val department: String = "",
    val course: String = "",
    val referralSource: String = ""
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val university: String = "",
    val department: String = "",
    val course: String = "",
    val authProvider: String = "email", // "email", "google", "github"
    val hasPasswordSet: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val teacherProfile: TeacherProfile? = null,
    val studentProfile: StudentProfile? = null,
    val contactNumber: String = ""
) {
    val hasCompletedOnboarding: Boolean
        get() = isOnboardingCompleted ||
                (role == UserRole.STUDENT && studentProfile != null && studentProfile.university.isNotBlank()) ||
                (role == UserRole.TEACHER && teacherProfile != null && teacherProfile.organization.isNotBlank()) ||
                (university.isNotBlank() && department.isNotBlank() && course.isNotBlank() &&
                        university != "Delhi University" && university != "Oxford University")

    val displayUniversity: String
        get() = university.ifBlank {
            studentProfile?.university?.ifBlank { null }
                ?: teacherProfile?.organization?.ifBlank { null }
                ?: if (role == UserRole.TEACHER) "Oxford University" else "Delhi University"
        }

    val displayDepartment: String
        get() = department.ifBlank {
            studentProfile?.department?.ifBlank { null }
                ?: teacherProfile?.department?.ifBlank { null }
                ?: if (role == UserRole.TEACHER) "Computer Science & Engineering" else "Computer Science"
        }

    val displayCourse: String
        get() = course.ifBlank {
            studentProfile?.course?.ifBlank { null }
                ?: teacherProfile?.designation?.ifBlank { null }
                ?: if (role == UserRole.TEACHER) "Advanced Algorithms & AI" else "B.Tech Computer Science"
        }

    val isSocialAccount: Boolean
        get() = authProvider.equals("google", ignoreCase = true) ||
                authProvider.equals("github", ignoreCase = true) ||
                authProvider.contains("google", ignoreCase = true) ||
                authProvider.contains("github", ignoreCase = true)

    val needsPasswordSetup: Boolean
        get() = isSocialAccount && !hasPasswordSet
}
