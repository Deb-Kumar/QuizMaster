package com.example.onlinequizapp.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.R
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.data.session.QuizDraftManager
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.ui.components.QuizStartModal
import com.example.onlinequizapp.ui.components.ResumeQuizModal
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.home.components.DarkQuizCard
import com.example.onlinequizapp.ui.home.components.HomeGreetingSection
import com.example.onlinequizapp.ui.home.components.HomeQuickActionsRow
import com.example.onlinequizapp.ui.home.components.HomeStatsCard
import com.example.onlinequizapp.ui.home.components.HomeTopBar
import com.example.onlinequizapp.ui.home.components.QuizBottomBar
import com.example.onlinequizapp.ui.home.components.ResumeQuizCard
import com.example.onlinequizapp.ui.home.profile.ProfileTabContent
import com.example.onlinequizapp.ui.home.components.JoinBatchDialog
import com.example.onlinequizapp.ui.home.components.JoinQuizByCodeDialog
import com.example.onlinequizapp.ui.home.components.MyBatchesDialog
import com.example.onlinequizapp.ui.home.components.StudentBatchBannerCard
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import kotlinx.coroutines.launch

object HomeTabManager {
    var pendingTab: Int? = null
}

@Composable
fun HomeScreen(
    onQuizClick: (String) -> Unit,
    onViewResultClick: ((String) -> Unit)? = null,
    onNotificationClick: (() -> Unit)? = null,
    onLeaderboardClick: (() -> Unit)? = null,
    onCertificatesClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Resolve the current dark/light preference from ThemeManager
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val activeDraft by QuizDraftManager.activeDraftFlow.collectAsStateWithLifecycle()
    var showResumeDialog by remember { mutableStateOf(false) }
    var hasPromptedThisLaunch by rememberSaveable { mutableStateOf(false) }
    var showNotificationsPage by rememberSaveable { mutableStateOf(false) }
    var selectedQuizForModal by remember { mutableStateOf<Quiz?>(null) }
    var showJoinBatchDialog by rememberSaveable { mutableStateOf(false) }
    var showMyBatchesDialog by rememberSaveable { mutableStateOf(false) }
    var showJoinQuizByCodeDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
        HomeTabManager.pendingTab?.let { tab ->
            viewModel.onTabSelected(tab)
            HomeTabManager.pendingTab = null
        }
    }

    LaunchedEffect(uiState.currentUser?.id) {
        QuizDraftManager.refreshActiveDraft(uiState.currentUser?.id)
    }

    LaunchedEffect(activeDraft) {
        if (activeDraft != null && !hasPromptedThisLaunch) {
            hasPromptedThisLaunch = true
            showResumeDialog = true
        }
    }

    // Pressing back on notifications page returns to previous home screen state
    BackHandler(enabled = showNotificationsPage) {
        showNotificationsPage = false
    }

    // Pressing back on non-home tabs returns to Home tab instead of closing or popping
    BackHandler(enabled = !showNotificationsPage && uiState.selectedTab != 0) {
        viewModel.onTabSelected(0)
    }

    if (showResumeDialog && activeDraft != null) {
        val draft = activeDraft!!
        ResumeQuizModal(
            draft = draft,
            isDark = isDark,
            onResume = {
                showResumeDialog = false
                val matchingQuiz = uiState.quizzes.find { it.id == draft.quizId }
                if (matchingQuiz != null) {
                    selectedQuizForModal = matchingQuiz
                } else {
                    coroutineScope.launch {
                        val fetched = QuizRepositoryImpl.instance.getQuizById(draft.quizId)
                        if (fetched != null) {
                            selectedQuizForModal = fetched
                        } else {
                            context.showToast("Resuming ${draft.quizTitle}...")
                            onQuizClick(draft.quizId)
                        }
                    }
                }
            },
            onLater = {
                showResumeDialog = false
            },
            onDiscard = {
                val uid = uiState.currentUser?.id ?: AuthSessionManager.getSavedUser()?.id ?: ""
                QuizDraftManager.clearDraft(uid, draft.quizId)
                showResumeDialog = false
                context.showToast("Draft discarded")
            }
        )
    }

    if (showJoinBatchDialog) {
        val uid = uiState.currentUser?.id ?: AuthSessionManager.getSavedUser()?.id ?: ""
        JoinBatchDialog(
            studentId = uid,
            isDark = isDark,
            onDismiss = { showJoinBatchDialog = false },
            onSuccessJoined = {
                showJoinBatchDialog = false
                showMyBatchesDialog = true
            }
        )
    }

    if (showJoinQuizByCodeDialog) {
        val uid = uiState.currentUser?.id ?: AuthSessionManager.getSavedUser()?.id ?: ""
        JoinQuizByCodeDialog(
            studentId = uid,
            isDark = isDark,
            onDismiss = { showJoinQuizByCodeDialog = false },
            onQuizValidated = { validatedQuiz ->
                showJoinQuizByCodeDialog = false
                context.showToast("Access granted: Starting ${validatedQuiz.title}...")
                onQuizClick(validatedQuiz.id)
            }
        )
    }

    if (showMyBatchesDialog) {
        val uid = uiState.currentUser?.id ?: AuthSessionManager.getSavedUser()?.id ?: ""
        MyBatchesDialog(
            studentId = uid,
            isDark = isDark,
            onDismiss = { showMyBatchesDialog = false },
            onJoinAnotherBatch = {
                showMyBatchesDialog = false
                showJoinBatchDialog = true
            }
        )
    }

    CompositionLocalProvider(LocalQuizDark provides isDark) {
        if (showNotificationsPage) {
            StudentNotificationsScreen(
                onBackClick = { showNotificationsPage = false },
                onQuizClick = { qid ->
                    showNotificationsPage = false
                    onQuizClick(qid)
                },
                modifier = modifier.fillMaxSize()
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = HomeDarkBg,
                bottomBar = {
                    QuizBottomBar(
                        selectedTab = uiState.selectedTab,
                        hasProfileBadge = uiState.currentUser?.needsPasswordSetup == true,
                        onTabSelected = { viewModel.onTabSelected(it) }
                    )
                }
            ) { innerPadding ->
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF818CF8))
                    }
                } else {
                    when (uiState.selectedTab) {
                        3 -> {
                            ProfileTabContent(
                                user = uiState.currentUser,
                                stats = uiState.stats,
                                onBackToHome = {
                                    viewModel.onTabSelected(0)
                                },
                                onNotificationClick = {
                                    if (onNotificationClick != null) {
                                        onNotificationClick()
                                    } else {
                                        showNotificationsPage = true
                                    }
                                },
                                onLogoutClick = {
                                    if (onLogoutClick != null) {
                                        viewModel.logout { onLogoutClick() }
                                    }
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
                                onCertificatesClick = onCertificatesClick,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        2 -> {
                            StudentHistoryScreen(
                                quizHistory = uiState.quizHistory,
                                isLoading = uiState.isLoadingHistory,
                                onBackClick = {
                                    viewModel.onTabSelected(0)
                                },
                                onViewResultClick = { quizId ->
                                    onViewResultClick?.invoke(quizId)
                                },
                                onStartQuizClick = {
                                    showJoinQuizByCodeDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        1 -> {
                            MyQuizzesScreen(
                                quizzes = uiState.quizzes,
                                userStats = uiState.stats,
                                onQuizClick = { quizId ->
                                    context.showToast("Starting quiz...")
                                    onQuizClick(quizId)
                                },
                                onViewResultClick = { quizId ->
                                    onViewResultClick?.invoke(quizId)
                                },
                                onBackClick = {
                                    viewModel.onTabSelected(0)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                        else -> {
                            val availableQuizzes = remember(uiState.quizzes, uiState.quizHistory, uiState.stats.quizzesTaken, activeDraft) {
                                uiState.quizzes
                                    .distinctBy { it.id }
                                    .filter { quiz ->
                                        !QuizRepositoryImpl.instance.hasCompletedQuizLocally(quiz.id) &&
                                        uiState.quizHistory.none { it.quizId == quiz.id }
                                    }
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // 1. Top Section: Logo/TopBar + Greeting Banner (compact gap)
                                item {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        HomeTopBar(
                                            isTeacher = uiState.isTeacher,
                                            onNotificationClick = {
                                                if (onNotificationClick != null) {
                                                    onNotificationClick()
                                                } else {
                                                    showNotificationsPage = true
                                                }
                                            }
                                        )
                                        HomeGreetingSection(
                                            userName = uiState.userName
                                        )
                                    }
                                }

                                // 2.5 Incomplete Quiz Resume Card
                                if (activeDraft != null) {
                                    item {
                                        ResumeQuizCard(
                                            draft = activeDraft!!,
                                            onResumeClick = {
                                                val draft = activeDraft
                                                if (draft != null) {
                                                    val matchingQuiz = uiState.quizzes.find { it.id == draft.quizId }
                                                    if (matchingQuiz != null) {
                                                        selectedQuizForModal = matchingQuiz
                                                    } else {
                                                        coroutineScope.launch {
                                                            val fetched = QuizRepositoryImpl.instance.getQuizById(draft.quizId)
                                                            if (fetched != null) {
                                                                selectedQuizForModal = fetched
                                                            } else {
                                                                context.showToast("Resuming ${draft.quizTitle}...")
                                                                onQuizClick(draft.quizId)
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            onDiscardClick = {
                                                val uid = uiState.currentUser?.id ?: AuthSessionManager.getSavedUser()?.id ?: ""
                                                QuizDraftManager.clearDraft(uid, activeDraft!!.quizId)
                                                context.showToast("Draft discarded")
                                            }
                                        )
                                    }
                                }

                                // 3. Stats Card (Quizzes Taken | Certificates | Avg Score)
                                item {
                                    HomeStatsCard(
                                        stats = uiState.stats
                                    )
                                }

                                // 4. Quick Action Cards (Take Quiz | View Progress | Certificates | Leaderboard)
                                item {
                                    HomeQuickActionsRow(
                                        onTakeQuizClick = {
                                            showJoinQuizByCodeDialog = true
                                        },
                                        onViewProgressClick = {
                                            viewModel.onTabSelected(2)
                                        },
                                        onCertificatesClick = {
                                            if (onCertificatesClick != null) {
                                                onCertificatesClick()
                                            } else {
                                                val certs = uiState.stats.certificatesCount
                                                if (certs > 0) {
                                                    context.showToast("$certs Certificate${if (certs > 1) "s" else ""} Earned! View in Profile.")
                                                } else {
                                                    context.showToast("No certificates earned yet. Score 80%+ on quizzes to earn certificates!")
                                                }
                                            }
                                        },
                                        onLeaderboardClick = {
                                            if (onLeaderboardClick != null) {
                                                onLeaderboardClick()
                                            } else {
                                                if (uiState.stats.quizzesTaken > 0) {
                                                    context.showToast("Leaderboard: Rank #1 this week! 🏆")
                                                } else {
                                                    context.showToast("Complete quizzes to join the weekly leaderboard! 🏆")
                                                }
                                            }
                                        }
                                    )
                                }

                                // 4.5. Batch Joining & Class Management Card
                                item {
                                    StudentBatchBannerCard(
                                        onEnterQuizCodeClick = { showJoinQuizByCodeDialog = true },
                                        onJoinBatchClick = { showJoinBatchDialog = true },
                                        onMyBatchesClick = { showMyBatchesDialog = true },
                                        isDark = isDark
                                    )
                                }

                                // 5. Available Quizzes Section Header
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Available Quizzes",
                                            style = TextStyle(
                                                color = TextWhite,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        )
                                        Text(
                                            text = "${availableQuizzes.size} available >",
                                            style = TextStyle(
                                                color = Color(0xFF818CF8),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 13.sp
                                            ),
                                            modifier = Modifier.clickable {
                                                viewModel.onTabSelected(1)
                                            }
                                        )
                                    }
                                }

                                // 6. Available Quizzes List (Dark Quiz Cards)
                                if (availableQuizzes.isEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(DarkCardSurface)
                                                .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                                                .padding(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "🎉",
                                                    fontSize = 32.sp
                                                )
                                                Text(
                                                    text = "All Caught Up!",
                                                    style = TextStyle(
                                                        color = TextWhite,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                )
                                                Text(
                                                    text = "You've completed all available quizzes.",
                                                    style = TextStyle(
                                                        color = TextMuted,
                                                        fontSize = 13.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    items(
                                        items = availableQuizzes,
                                        key = { it.id }
                                    ) { quiz ->
                                        DarkQuizCard(
                                            quiz = quiz,
                                            onClick = {
                                                selectedQuizForModal = quiz
                                            }
                                        )
                                    }
                                }

                                // 7. Motivation Banner Card
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable {
                                                context.showToast("Stay focused! You got this! 🎯")
                                            }
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.img_home_banner),
                                            contentDescription = "Stay Focused Motivation Banner",
                                            modifier = Modifier.fillMaxWidth(),
                                            contentScale = ContentScale.FillWidth
                                        )
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedQuizForModal != null) {
        val quiz = selectedQuizForModal!!
        val isPending = activeDraft?.quizId == quiz.id
        QuizStartModal(
            quiz = quiz,
            isPending = isPending,
            isCompleted = false,
            progressPercent = if (isPending && activeDraft != null && activeDraft!!.totalQuestions > 0) {
                ((activeDraft!!.currentIndex + 1) * 100 / activeDraft!!.totalQuestions)
            } else 0,
            savedQuestionIndex = if (isPending) activeDraft?.currentIndex else null,
            isDark = isDark,
            onStartQuiz = {
                val qid = quiz.id
                selectedQuizForModal = null
                onQuizClick(qid)
            },
            onViewResult = {
                val qid = quiz.id
                selectedQuizForModal = null
                onViewResultClick?.invoke(qid)
            },
            onDismiss = {
                selectedQuizForModal = null
            }
        )
    }
}