package com.example.onlinequizapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.ui.auth.LoginScreen
import com.example.onlinequizapp.ui.auth.RegisterScreen
import com.example.onlinequizapp.ui.auth.StudentOnboardingScreen
import com.example.onlinequizapp.ui.auth.TeacherOnboardingScreen
import com.example.onlinequizapp.ui.home.HomeScreen
import com.example.onlinequizapp.ui.quiz.QuizScreen
import com.example.onlinequizapp.ui.result.ResultScreen
import com.example.onlinequizapp.ui.result.ReviewScreen
import com.example.onlinequizapp.ui.splash.SplashScreen
import com.example.onlinequizapp.ui.welcome.WelcomeScreen
import com.example.onlinequizapp.ui.teacher.TeacherCreateQuizScreen
import com.example.onlinequizapp.ui.teacher.TeacherDashboardScreen
import com.example.onlinequizapp.ui.teacher.TeacherNotificationsScreen
import com.example.onlinequizapp.ui.teacher.TeacherTabManager
import com.example.onlinequizapp.ui.home.leaderboard.LeaderboardScreen
import com.example.onlinequizapp.ui.home.certificates.CertificatesScreen

@Composable
fun QuizNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = remember {
        val shouldShowWelcome = AuthSessionManager.shouldShowWelcomeScreen()
        if (shouldShowWelcome) {
            Screen.Splash.route
        } else {
            val user = AuthSessionManager.getSavedUser()
            if (user != null || AuthSessionManager.isLoggedIn()) {
                if (user?.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
            } else {
                Screen.Splash.route
            }
        }
    }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    val shouldShowWelcome = AuthSessionManager.shouldShowWelcomeScreen()
                    val user = AuthSessionManager.getSavedUser()
                    val isLoggedIn = user != null || AuthSessionManager.isLoggedIn()

                    val dest = if (shouldShowWelcome) {
                        Screen.Welcome.route
                    } else if (isLoggedIn) {
                        if (user != null && !user.hasCompletedOnboarding) {
                            if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                        } else {
                            if (user?.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                        }
                    } else {
                        Screen.Login.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                onGetStartedClick = {
                    AuthSessionManager.markWelcomeScreenCompleted()
                    val user = AuthSessionManager.getSavedUser()
                    val isLoggedIn = user != null || AuthSessionManager.isLoggedIn()

                    if (isLoggedIn) {
                        val dest = if (user != null && !user.hasCompletedOnboarding) {
                            if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                        } else {
                            if (user?.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                        }
                        navController.navigate(dest) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onLoginClick = {
                    AuthSessionManager.markWelcomeScreenCompleted()
                    val user = AuthSessionManager.getSavedUser()
                    val isLoggedIn = user != null || AuthSessionManager.isLoggedIn()

                    if (isLoggedIn) {
                        val dest = if (user != null && !user.hasCompletedOnboarding) {
                            if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                        } else {
                            if (user?.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                        }
                        navController.navigate(dest) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.Login.route) {
            LaunchedEffect(Unit) {
                val user = AuthSessionManager.getSavedUser()
                if (user != null || AuthSessionManager.isLoggedIn()) {
                    if (user != null) {
                        val dest = if (!user.hasCompletedOnboarding) {
                            if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                        } else {
                            if (user.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                        }
                        navController.navigate(dest) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            }
            LoginScreen(
                onLoginSuccess = { user ->
                    AuthSessionManager.saveUser(user)
                    val dest = if (!user.hasCompletedOnboarding) {
                        if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                    } else {
                        if (user.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                    }
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { user ->
                    AuthSessionManager.saveUser(user)
                    val dest = if (!user.hasCompletedOnboarding) {
                        if (user.role == UserRole.TEACHER) Screen.TeacherOnboarding.route else Screen.StudentOnboarding.route
                    } else {
                        if (user.role == UserRole.TEACHER) Screen.TeacherDashboard.route else Screen.Home.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBackToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = Screen.TeacherOnboarding.route) {
            TeacherOnboardingScreen(
                onComplete = { user ->
                    AuthSessionManager.saveUser(user)
                    navController.navigate(Screen.TeacherDashboard.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.StudentOnboarding.route) {
            StudentOnboardingScreen(
                onComplete = { user ->
                    AuthSessionManager.saveUser(user)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                onQuizClick = { quizId ->
                    navController.navigate(Screen.Quiz.createRoute(quizId))
                },
                onViewResultClick = { quizId ->
                    navController.navigate(Screen.Result.createRoute(quizId))
                },
                onNotificationClick = {
                    navController.navigate(Screen.Notifications.route)
                },
                onLeaderboardClick = {
                    navController.navigate(Screen.Leaderboard.route)
                },
                onCertificatesClick = {
                    navController.navigate(Screen.Certificates.route)
                },
                onLogoutClick = {
                    AuthSessionManager.clear()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.Leaderboard.route) {
            LeaderboardScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onQuizClick = { quizId ->
                    navController.navigate(Screen.Quiz.createRoute(quizId))
                }
            )
        }

        composable(route = Screen.Certificates.route) {
            CertificatesScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onRetakeQuizClick = { quizId ->
                    navController.navigate(Screen.Quiz.createRoute(quizId))
                }
            )
        }

        composable(route = Screen.Notifications.route) {
            val user = AuthSessionManager.getSavedUser()
            if (user?.role == UserRole.TEACHER) {
                TeacherNotificationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                com.example.onlinequizapp.ui.home.StudentNotificationsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onQuizClick = { quizId ->
                        navController.navigate(Screen.Quiz.createRoute(quizId))
                    }
                )
            }
        }

        composable(route = Screen.TeacherDashboard.route) {
            TeacherDashboardScreen(
                onCreateQuizClick = {
                    navController.navigate(Screen.TeacherCreateQuiz.createRoute())
                },
                onEditQuizClick = { quizId ->
                    navController.navigate(Screen.TeacherCreateQuiz.createRoute(quizId))
                },
                onPublishQuizClick = { quizId ->
                    navController.navigate(Screen.TeacherCreateQuiz.createRoute(quizId, startAtReview = false))
                },
                onTestQuizClick = { quizId ->
                    navController.navigate(Screen.Quiz.createRoute(quizId))
                },
                onViewAnalyticsClick = {
                    navController.navigate(Screen.TeacherAnalytics.route)
                },
                onLogoutClick = {
                    AuthSessionManager.clear()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.TeacherAnalytics.route) {
            com.example.onlinequizapp.ui.teacher.analytics.TeacherAnalyticsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.TeacherCreateQuiz.route,
            arguments = listOf(
                navArgument("quizId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("startAtReview") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId")
            val startAtReview = backStackEntry.arguments?.getBoolean("startAtReview") ?: false
            TeacherCreateQuizScreen(
                quizId = quizId,
                startAtReview = startAtReview,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQuizCreated = { _ ->
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            QuizScreen(
                quizId = quizId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQuizCompleted = { completedQuizId ->
                    navController.navigate(Screen.Result.createRoute(completedQuizId)) {
                        popUpTo(Screen.Quiz.createRoute(completedQuizId)) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            ResultScreen(
                quizId = quizId,
                onViewDetailsClick = { initialIndex ->
                    navController.navigate(Screen.Review.createRoute(quizId, initialIndex))
                },
                onTryAgainClick = {
                    navController.navigate(Screen.Quiz.createRoute(quizId)) {
                        popUpTo(Screen.Result.createRoute(quizId)) { inclusive = true }
                    }
                },
                onReturnHomeClick = {
                    val user = AuthSessionManager.getSavedUser()
                    if (user?.role == UserRole.TEACHER) {
                        TeacherTabManager.pendingTab = 1
                        navController.navigate(Screen.TeacherDashboard.route) {
                            popUpTo(Screen.TeacherDashboard.route) { inclusive = true }
                        }
                    } else {
                        com.example.onlinequizapp.ui.home.HomeTabManager.pendingTab = 1
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Review.route,
            arguments = listOf(
                navArgument("quizId") { type = NavType.StringType },
                navArgument("initialIndex") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: return@composable
            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
            ReviewScreen(
                quizId = quizId,
                initialIndex = initialIndex,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
