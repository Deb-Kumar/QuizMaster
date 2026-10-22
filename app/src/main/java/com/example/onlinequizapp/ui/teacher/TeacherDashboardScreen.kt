package com.example.onlinequizapp.ui.teacher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.teacher.components.TeacherBottomNavBar
import com.example.onlinequizapp.ui.teacher.components.TeacherHomeContent
import com.example.onlinequizapp.ui.teacher.components.TeacherJoinRequestsDialog
import com.example.onlinequizapp.ui.home.profile.LogoutConfirmDialog
import com.example.onlinequizapp.ui.teacher.profile.TeacherAnalyticsDialog
import com.example.onlinequizapp.ui.teacher.profile.TeacherCertificatesDialog
import com.example.onlinequizapp.ui.teacher.profile.TeacherNotificationsDialog
import com.example.onlinequizapp.ui.teacher.profile.TeacherProfileContent
import com.example.onlinequizapp.ui.teacher.profile.TeacherStudentsDialog

object TeacherTabManager {
    var pendingTab: Int? = null
}

@Composable
fun TeacherDashboardScreen(
    onCreateQuizClick: () -> Unit,
    onEditQuizClick: (String) -> Unit = {},
    onPublishQuizClick: (String) -> Unit = onEditQuizClick,
    onTestQuizClick: (String) -> Unit,
    onViewAnalyticsClick: () -> Unit = {},
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeacherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedBottomTab by rememberSaveable { mutableIntStateOf(TeacherTabManager.pendingTab ?: 0) } // 0: Home, 1: My Quizzes, 2: Students, 3: Profile

    // Dialog state holders for Quick Actions & Notifications
    var showStudentsDialog by remember { mutableStateOf(false) }
    var showCertificatesDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showJoinRequestsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        TeacherTabManager.pendingTab?.let { tab ->
            selectedBottomTab = tab
            TeacherTabManager.pendingTab = null
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            context.showToast(msg)
        }
    }

    // System Back Handler: return to Home tab if on other tabs
    BackHandler(enabled = selectedBottomTab != 0) {
        selectedBottomTab = 0
    }

    val isDark = isTeacherInDarkTheme()
    androidx.compose.runtime.CompositionLocalProvider(com.example.onlinequizapp.ui.theme.LocalQuizDark provides isDark) {
        // Delete Confirmation Dialog
        uiState.quizPendingDeletion?.let { quiz ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteDialog() },
                containerColor = if (isDark) Color(0xFF0E1638) else Color(0xFFFFFFFF),
                title = {
                    Text(
                        text = "Delete Quiz?",
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to permanently delete \"${quiz.title}\"? This action cannot be undone.",
                        color = TeacherTextMuted,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmDeleteQuiz() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                        Text("Cancel", color = TeacherTextMuted)
                    }
                }
            )
        }

    // Quick Action Dialogs
    if (showStudentsDialog) {
        TeacherStudentsDialog(
            submissions = uiState.submissions,
            onDismiss = { showStudentsDialog = false }
        )
    }

    if (showCertificatesDialog) {
        TeacherCertificatesDialog(
            submissions = uiState.submissions,
            onDismiss = { showCertificatesDialog = false }
        )
    }

    if (showNotificationsDialog) {
        TeacherNotificationsDialog(
            onDismiss = { showNotificationsDialog = false }
        )
    }

    val teacherId = uiState.teacherUser?.id ?: (AuthSessionManager.getSavedUser()?.id ?: "")

    if (showJoinRequestsDialog) {
        TeacherJoinRequestsDialog(
            teacherId = teacherId,
            onDismiss = { showJoinRequestsDialog = false }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout(onLogoutClick)
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TeacherBg,
        bottomBar = {
            TeacherBottomNavBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { tab ->
                    selectedBottomTab = tab
                }
            )
        }
    ) { innerPadding ->
        when (selectedBottomTab) {
                0 -> {
                    // Main Teacher Home Page
                    TeacherHomeContent(
                        uiState = uiState,
                        onCreateQuizClick = onCreateQuizClick,
                        onTestQuizClick = onTestQuizClick,
                        onManageStudentsClick = { selectedBottomTab = 2 },
                        onViewAnalyticsClick = onViewAnalyticsClick,
                        onCertificatesClick = { showCertificatesDialog = true },
                        onNotificationClick = { showNotificationsDialog = true },
                        onLogoutClick = { showLogoutDialog = true },
                        onRequestDeleteQuiz = { viewModel.requestDeleteQuiz(it) },
                        onJoinRequestsClick = { showJoinRequestsDialog = true },
                        onEditQuizClick = onEditQuizClick,
                        onPublishQuizClick = onPublishQuizClick,
                        onViewAllQuizzesClick = { selectedBottomTab = 1 },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                1 -> {
                    // My Quizzes Tab
                    TeacherMyQuizzesScreen(
                        quizzes = uiState.quizzes,
                        teacherUser = uiState.teacherUser,
                        submissions = uiState.submissions,
                        onBackClick = { selectedBottomTab = 0 },
                        onCreateQuizClick = onCreateQuizClick,
                        onEditQuizClick = onEditQuizClick,
                        onPublishQuizClick = onPublishQuizClick,
                        onTestQuizClick = onTestQuizClick,
                        onDeleteQuizClick = { viewModel.requestDeleteQuiz(it) },
                        onDuplicateQuizClick = { viewModel.duplicateQuiz(it) },
                        onToggleQuizStatusClick = { quiz, newStatus -> viewModel.toggleQuizStatus(quiz, newStatus) },
                        onNotificationClick = { showNotificationsDialog = true },
                        onProfileClick = { selectedBottomTab = 3 },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                2 -> {
                    // Students & Performance Tab
                    TeacherStudentsScreen(
                        teacherUser = uiState.teacherUser,
                        submissions = uiState.submissions,
                        onBackClick = { selectedBottomTab = 0 },
                        onNotificationClick = { showNotificationsDialog = true },
                        onProfileClick = { selectedBottomTab = 3 },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
                3 -> {
                    // Teacher Profile Tab
                    TeacherProfileContent(
                        user = uiState.teacherUser,
                        stats = uiState,
                        onBackClick = { selectedBottomTab = 0 },
                        onNotificationClick = { showNotificationsDialog = true },
                        onCreateQuizClick = onCreateQuizClick,
                        onManageStudentsClick = { showStudentsDialog = true },
                        onViewAnalyticsClick = onViewAnalyticsClick,
                        onCertificatesClick = { showCertificatesDialog = true },
                        onMyQuizzesClick = { selectedBottomTab = 1 },
                        onLogoutClick = {
                            viewModel.logout(onLogoutClick)
                        },
                        onUpdateProfile = { name, university, department, course, avatarUrl, email, contactNumber, onResult ->
                            viewModel.updateProfile(name, university, department, course, avatarUrl, email, contactNumber, onResult)
                        },
                        onSetOrChangePassword = { currentPass, newPass, onResult ->
                            viewModel.setOrChangePassword(currentPass, newPass, onResult)
                        },
                        onDeleteAccount = { onResult ->
                            viewModel.deleteAccount(onResult)
                        },
                        onSubmitFeedback = { rating, category, comment, onResult ->
                            viewModel.submitFeedback(rating, category, comment, onResult)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

