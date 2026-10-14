package com.example.onlinequizapp.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object TeacherOnboarding : Screen("teacher_onboarding")
    data object StudentOnboarding : Screen("student_onboarding")
    data object Home : Screen("home")
    data object Notifications : Screen("notifications")
    data object TeacherDashboard : Screen("teacher_dashboard")
    data object TeacherCreateQuiz : Screen("teacher_create_quiz?quizId={quizId}&startAtReview={startAtReview}") {
        fun createRoute(quizId: String? = null, startAtReview: Boolean = false) = when {
            quizId != null && startAtReview -> "teacher_create_quiz?quizId=$quizId&startAtReview=true"
            quizId != null -> "teacher_create_quiz?quizId=$quizId&startAtReview=false"
            else -> "teacher_create_quiz"
        }
    }
    data object Quiz : Screen("quiz/{quizId}") {
        fun createRoute(quizId: String) = "quiz/$quizId"
    }
    data object Result : Screen("result/{quizId}") {
        fun createRoute(quizId: String) = "result/$quizId"
    }
    data object Leaderboard : Screen("leaderboard")
    data object Certificates : Screen("certificates")
    data object TeacherAnalytics : Screen("teacher_analytics")
    data object Review : Screen("review/{quizId}?initialIndex={initialIndex}") {
        fun createRoute(quizId: String, initialIndex: Int = 0) = "review/$quizId?initialIndex=$initialIndex"
    }
}
