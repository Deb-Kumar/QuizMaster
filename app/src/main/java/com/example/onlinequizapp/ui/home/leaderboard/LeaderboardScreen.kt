package com.example.onlinequizapp.ui.home.leaderboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.domain.model.Batch
import com.example.onlinequizapp.domain.model.BatchLeaderboardData
import com.example.onlinequizapp.domain.model.BatchLeaderboardStudent
import com.example.onlinequizapp.domain.model.IndividualQuizLeaderboardItem
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizLeaderboardStudentAttempt
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.components.UserAvatar
import com.example.onlinequizapp.ui.home.components.JoinBatchDialog
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

// Theme & Accent tokens
private val GoldAccent = Color(0xFFF59E0B)
private val SilverAccent = Color(0xFF94A3B8)
private val BronzeAccent = Color(0xFFD97706)
private val BrandIndigo = Color(0xFF6366F1)
private val BrandPurple = Color(0xFF8B5CF6)
private val EmeraldSuccess = Color(0xFF10B981)

@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit,
    onQuizClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Resolve Dark/Light Theme
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    val screenBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.6f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    var showJoinBatchDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val selectedQuiz = uiState.selectedQuizForDetail

    if (selectedQuiz != null) {
        BackHandler {
            viewModel.selectQuizForDetail(null)
        }
        QuizLeaderboardFullScreen(
            quizItem = selectedQuiz,
            onBackClick = { viewModel.selectQuizForDetail(null) },
            onTakeQuiz = { quizId ->
                viewModel.selectQuizForDetail(null)
                onQuizClick?.invoke(quizId)
            },
            isDark = isDark,
            screenBg = screenBg,
            cardBg = cardBg,
            cardBorder = cardBorder,
            textPrimary = textPrimary,
            textSecondary = textSecondary
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = screenBg,
            topBar = {
                LeaderboardTopBar(
                    onBackClick = onBackClick,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Main Segmented Tab Row: [ 👥 Batches ] vs [ 📝 Individual Quizzes ]
                LeaderboardTabs(
                    selectedTab = uiState.selectedTab,
                    onTabSelect = { viewModel.selectTab(it) },
                    batchesCount = uiState.studentBatches.size,
                    quizzesCount = uiState.individualQuizzes.size,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = BrandIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    when (uiState.selectedTab) {
                        0 -> {
                            // TAB 1: BATCHES LEADERBOARD
                            BatchLeaderboardTabContent(
                                uiState = uiState,
                                onBatchSelect = { viewModel.selectBatchIndex(it) },
                                onJoinBatchClick = { showJoinBatchDialog = true },
                                onTakeQuiz = { onQuizClick?.invoke(it) },
                                isDark = isDark,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                        1 -> {
                            // TAB 2: INDIVIDUAL QUIZZES LEADERBOARD
                            IndividualQuizzesTabContent(
                                quizzes = uiState.individualQuizzes,
                                onQuizSelect = { viewModel.selectQuizForDetail(it) },
                                onTakeQuiz = { onQuizClick?.invoke(it) },
                                isDark = isDark,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                textPrimary = textPrimary,
                                textSecondary = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal to Join Batch if student has no batches
    if (showJoinBatchDialog) {
        JoinBatchDialog(
            studentId = uiState.currentUserId,
            isDark = isDark,
            onDismiss = { showJoinBatchDialog = false },
            onSuccessJoined = {
                showJoinBatchDialog = false
                viewModel.refresh()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TOP BAR (With Safe Insets Status Bar Padding)
// ─────────────────────────────────────────────────────────────
@Composable
private fun LeaderboardTopBar(
    onBackClick: () -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    val barBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val borderCol = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Surface(
        color = barBg,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(BorderStroke(0.5.dp, borderCol))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Leaderboard",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Batch standings & quiz rankings",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TABS: BATCHES VS INDIVIDUAL QUIZZES
// ─────────────────────────────────────────────────────────────
@Composable
private fun LeaderboardTabs(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit,
    batchesCount: Int,
    quizzesCount: Int,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    val tabRowBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val indicatorCol = BrandIndigo

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = tabRowBg,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                height = 3.dp,
                color = indicatorCol
            )
        },
        divider = {
            HorizontalDivider(
                color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                thickness = 0.8.dp
            )
        }
    ) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onTabSelect(0) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = if (selectedTab == 0) BrandIndigo else textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Batches ($batchesCount)",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (selectedTab == 0) BrandIndigo else textSecondary
                    )
                }
            }
        )

        Tab(
            selected = selectedTab == 1,
            onClick = { onTabSelect(1) },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (selectedTab == 1) BrandIndigo else textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Individual Quizzes ($quizzesCount)",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.5.sp,
                        color = if (selectedTab == 1) BrandIndigo else textSecondary
                    )
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1: BATCH LEADERBOARD CONTENT
// ─────────────────────────────────────────────────────────────
@Composable
private fun BatchLeaderboardTabContent(
    uiState: LeaderboardUiState,
    onBatchSelect: (Int) -> Unit,
    onJoinBatchClick: () -> Unit,
    onTakeQuiz: ((String) -> Unit)? = null,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    if (uiState.studentBatches.isEmpty()) {
        // EMPTY STATE: Student not in any batches
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BrandIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = BrandIndigo,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "No Batches Joined Yet",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "You are not currently enrolled in any class batches. Join a batch with a 6-character code from your teacher to compete on batch leaderboards with classmates.",
                    fontSize = 13.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = onJoinBatchClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Batch by Code", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val batchData = uiState.batchLeaderboard
    val currentStudent = batchData?.students?.find { it.isCurrentStudent }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = if (currentStudent != null) 96.dp else 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Horizontal Batch Selector Pills
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Batch:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        itemsIndexed(uiState.studentBatches) { idx, batch ->
                            val isSelected = idx == uiState.selectedBatchIndex
                            val pillBg = if (isSelected) {
                                BrandIndigo.copy(alpha = 0.15f)
                            } else {
                                if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
                            }
                            val pillBorder = if (isSelected) {
                                BrandIndigo
                            } else {
                                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(pillBg)
                                    .border(1.2.dp, pillBorder, RoundedCornerShape(12.dp))
                                    .clickable { onBatchSelect(idx) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isSelected) BrandIndigo else textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = batch.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) BrandIndigo else textPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = batch.batchCode,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Batch Overview Summary Card
            if (batchData != null) {
                item {
                    BatchOverviewCard(
                        data = batchData,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                // 2.5 Assigned Batch Quizzes Section (Allows immediate attempt)
                if (batchData.assignedQuizzes.isNotEmpty()) {
                    item {
                        BatchAssignedQuizzesSection(
                            quizzes = batchData.assignedQuizzes,
                            currentStudent = currentStudent,
                            onTakeQuiz = onTakeQuiz,
                            isDark = isDark,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }

                // 3. Batch Podium View
                item {
                    BatchPodiumSection(
                        students = batchData.students,
                        totalAssigned = batchData.assignedQuizzes.size,
                        onTakeQuiz = onTakeQuiz,
                        firstQuizId = batchData.assignedQuizzes.firstOrNull()?.id,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }

                // 4. Detailed Students Rankings List
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Batch Standings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "${batchData.students.size} Students",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                    }
                }

                if (batchData.students.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No students have attempted quizzes in this batch yet.",
                                fontSize = 13.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(batchData.students) { student ->
                        BatchStudentRankCard(
                            student = student,
                            totalAssigned = batchData.assignedQuizzes.size,
                            isDark = isDark,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary
                        )
                    }
                }
            }
        }

        // 5. Sticky "Your Rank" Bottom Card (with Safe navigationBarsPadding)
        if (currentStudent != null && batchData != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                StickyYourRankCard(
                    student = currentStudent,
                    totalStudents = batchData.students.size,
                    batchName = batchData.batch.name,
                    isDark = isDark
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BATCH OVERVIEW CARD
// ─────────────────────────────────────────────────────────────
@Composable
private fun BatchOverviewCard(
    data: BatchLeaderboardData,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = data.batch.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                if (data.batch.description.isNotBlank()) {
                    Text(
                        text = data.batch.description,
                        fontSize = 11.5.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandIndigo.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Code: ${data.batch.batchCode}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandIndigo
                )
            }
        }

        HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            OverviewStatColumn(
                title = "Quizzes",
                value = "${data.assignedQuizzes.size}",
                sub = "Assigned",
                icon = Icons.Default.MenuBook,
                iconTint = BrandIndigo,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
            OverviewStatColumn(
                title = "Students",
                value = "${data.students.size}",
                sub = "Enrolled",
                icon = Icons.Default.Group,
                iconTint = Color(0xFF3B82F6),
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
            OverviewStatColumn(
                title = "Class Avg",
                value = "${data.classAverageScore}%",
                sub = "Overall",
                icon = Icons.Default.Star,
                iconTint = GoldAccent,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }
    }
}

@Composable
private fun OverviewStatColumn(
    title: String,
    value: String,
    sub: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Text(
            text = "$title ($sub)",
            fontSize = 10.5.sp,
            color = textSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// ASSIGNED BATCH QUIZZES SECTION (Allows immediate taking)
// ─────────────────────────────────────────────────────────────
@Composable
private fun BatchAssignedQuizzesSection(
    quizzes: List<Quiz>,
    currentStudent: BatchLeaderboardStudent?,
    onTakeQuiz: ((String) -> Unit)?,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Assigned Batch Quizzes",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = "${quizzes.size} Available",
                fontSize = 11.5.sp,
                color = textSecondary
            )
        }

        quizzes.forEach { quiz ->
            val scoreEntry = currentStudent?.quizBreakdown?.find { it.quizId == quiz.id }
            val isCompleted = scoreEntry != null

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(cardBg)
                    .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val visuals = QuizVisualHelper.getVisuals(quiz)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(visuals.gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        QuizVisualHelper.QuizLogoEmblem(
                            visuals = visuals,
                            contentDescription = quiz.title,
                            iconSize = 22.dp,
                            iconTint = Color.White
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = quiz.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${quiz.questions.size} Questions • ${quiz.category}",
                            fontSize = 11.sp,
                            color = textSecondary
                        )
                    }
                }

                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Done: ${scoreEntry?.scorePercentage}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }
                } else {
                    Button(
                        onClick = { onTakeQuiz?.invoke(quiz.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Take Quiz", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PODIUM VIEW (Smartly balanced: Empty, 1-person, 2-person, or 3-person)
// ─────────────────────────────────────────────────────────────
@Composable
private fun BatchPodiumSection(
    students: List<BatchLeaderboardStudent>,
    totalAssigned: Int,
    onTakeQuiz: ((String) -> Unit)?,
    firstQuizId: String?,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    // Only students who have completed AT LEAST 1 quiz qualify for the podium
    val activeSubmissions = students.filter { it.completedCount > 0 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "🏆", fontSize = 16.sp)
            Text(
                text = "Batch Podium",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        }

        if (activeSubmissions.isEmpty()) {
            // Awaiting First Champion State
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Podium Awaiting First Champion! 🚀",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Text(
                    text = "No students have completed batch quizzes yet.\nBe the first to finish a quiz and claim 1st place on the podium!",
                    fontSize = 12.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                if (firstQuizId != null && onTakeQuiz != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onTakeQuiz(firstQuizId) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start First Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (activeSubmissions.size == 1) {
            // Exactly 1 Active Student: Single Center Champion Podium
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                PodiumPillar(
                    student = activeSubmissions[0],
                    rank = 1,
                    pillarHeight = 110.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(140.dp)
                )
            }
        } else if (activeSubmissions.size == 2) {
            // Exactly 2 Active Students: Symmetrical 2-Pillar Podium (1st Gold, 2nd Silver)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                PodiumPillar(
                    student = activeSubmissions[0],
                    rank = 1,
                    pillarHeight = 110.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(130.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                PodiumPillar(
                    student = activeSubmissions[1],
                    rank = 2,
                    pillarHeight = 90.dp,
                    accentColor = SilverAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(130.dp)
                )
            }
        } else {
            // 3 or more active students: Standard 3-Pillar Podium (2nd, 1st, 3rd)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                PodiumPillar(
                    student = activeSubmissions[1],
                    rank = 2,
                    pillarHeight = 90.dp,
                    accentColor = SilverAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )

                // 1st Place (Gold)
                PodiumPillar(
                    student = activeSubmissions[0],
                    rank = 1,
                    pillarHeight = 115.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1.1f)
                )

                // 3rd Place (Bronze)
                PodiumPillar(
                    student = activeSubmissions[2],
                    rank = 3,
                    pillarHeight = 75.dp,
                    accentColor = BronzeAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    student: BatchLeaderboardStudent,
    rank: Int,
    pillarHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    val medalSymbol = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        else -> "🥉"
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Medal / Crown Icon above avatar
        Text(text = medalSymbol, fontSize = 16.sp)

        // Avatar
        UserAvatar(
            avatarUrl = student.avatarUrl,
            name = student.studentName,
            size = 46.dp,
            borderWidth = 2.dp,
            borderColor = accentColor
        )

        // Student Name
        Text(
            text = if (student.isCurrentStudent) "${student.studentName} (You)" else student.studentName,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (student.isCurrentStudent) BrandIndigo else textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Pillar block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    1.dp,
                    accentColor.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${student.averageScore}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = student.completionRatioText,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                Text(
                    text = "completed",
                    fontSize = 9.sp,
                    color = textSecondary
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BATCH STUDENT RANK CARD
// ─────────────────────────────────────────────────────────────
@Composable
private fun BatchStudentRankCard(
    student: BatchLeaderboardStudent,
    totalAssigned: Int,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val isCurrent = student.isCurrentStudent
    val hasSubmissions = student.completedCount > 0

    val rankBadgeBg = when {
        !hasSubmissions -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
        student.rank == 1 -> GoldAccent
        student.rank == 2 -> SilverAccent
        student.rank == 3 -> BronzeAccent
        else -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }
    val rankTextColor = when {
        !hasSubmissions -> if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
        student.rank in 1..3 -> Color.White
        else -> if (isDark) Color(0xFFF1F5F9) else Color(0xFF334155)
    }

    val itemCardBg = if (isCurrent) {
        BrandIndigo.copy(alpha = 0.12f)
    } else {
        cardBg
    }
    val itemBorder = if (isCurrent) {
        BrandIndigo.copy(alpha = 0.8f)
    } else {
        cardBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(itemCardBg)
            .border(if (isCurrent) 1.5.dp else 1.dp, itemBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Rank Badge (#1, #2, #3, or —)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rankBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.rankText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankTextColor
                )
            }

            // Student Profile Picture
            UserAvatar(
                avatarUrl = student.avatarUrl,
                name = student.studentName,
                size = 36.dp,
                borderWidth = if (isCurrent) 1.5.dp else 1.dp,
                borderColor = if (isCurrent) BrandIndigo else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
            )

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = student.studentName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandIndigo)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Text(
                    text = if (hasSubmissions) {
                        "${student.completionRatioText} quizzes completed"
                    } else {
                        "0 of $totalAssigned quizzes • No submissions"
                    },
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
        }

        // Score Pill: [ 78% Avg ] or [ — ]
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (hasSubmissions) {
                val pillCol = when {
                    student.averageScore >= 80 -> EmeraldSuccess
                    student.averageScore >= 50 -> Color(0xFF3B82F6)
                    else -> Color(0xFF64748B)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(pillCol.copy(alpha = 0.15f))
                        .border(1.dp, pillCol.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${student.averageScore}% Avg",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = pillCol
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Not Started",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STICKY YOUR RANK BOTTOM CARD (With navigationBarsPadding)
// ─────────────────────────────────────────────────────────────
@Composable
private fun StickyYourRankCard(
    student: BatchLeaderboardStudent,
    totalStudents: Int,
    batchName: String,
    isDark: Boolean
) {
    val barBg = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val barBorder = BrandIndigo
    val hasRank = student.hasRank

    Surface(
        color = barBg,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, barBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (hasRank) BrandIndigo else Color(0xFF475569)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.rankText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                UserAvatar(
                    avatarUrl = student.avatarUrl,
                    name = student.studentName,
                    size = 36.dp,
                    borderWidth = 1.5.dp,
                    borderColor = BrandIndigo
                )

                Column {
                    Text(
                        text = "Your Standing in $batchName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandIndigo
                    )
                    Text(
                        text = if (hasRank) {
                            "Rank #${student.rank} of $totalStudents students"
                        } else {
                            "Not Ranked Yet • Complete a quiz!"
                        },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hasRank) "${student.averageScore}%" else "0%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasRank) EmeraldSuccess else Color(0xFF94A3B8)
                )
                Text(
                    text = "${student.completionRatioText} completed",
                    fontSize = 10.5.sp,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 2: INDIVIDUAL QUIZZES TAB CONTENT
// ─────────────────────────────────────────────────────────────
@Composable
private fun IndividualQuizzesTabContent(
    quizzes: List<IndividualQuizLeaderboardItem>,
    onQuizSelect: (IndividualQuizLeaderboardItem) -> Unit,
    onTakeQuiz: (String) -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    if (quizzes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Accessible Quizzes Found",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Quizzes assigned to your batches or individually assigned by your teacher will appear here with individual student leaderboards.",
                    fontSize = 13.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Quizzes & Completion Standings",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
            Text(
                text = "Tap any quiz to view all students who completed it and their ranked scores.",
                fontSize = 12.sp,
                color = textSecondary
            )
        }

        items(quizzes) { quizItem ->
            IndividualQuizCard(
                item = quizItem,
                onClick = { onQuizSelect(quizItem) },
                onTakeQuiz = { onTakeQuiz(quizItem.quiz.id) },
                isDark = isDark,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        }
    }
}

@Composable
private fun IndividualQuizCard(
    item: IndividualQuizLeaderboardItem,
    onClick: () -> Unit,
    onTakeQuiz: () -> Unit,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val quiz = item.quiz
    val currentAttempt = item.currentStudentAttempt

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                val visuals = QuizVisualHelper.getVisuals(quiz)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(visuals.gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    QuizVisualHelper.QuizLogoEmblem(
                        visuals = visuals,
                        contentDescription = quiz.title,
                        iconSize = 24.dp,
                        iconTint = Color.White
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = quiz.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${quiz.questions.size} Questions • ${item.assignmentSource}",
                        fontSize = 11.sp,
                        color = textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Leaderboard",
                tint = textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        HorizontalDivider(color = cardBorder, thickness = 0.6.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completions count pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = BrandIndigo,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${item.totalCompletionsCount} student${if (item.totalCompletionsCount != 1) "s" else ""} attempted",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary
                )
            }

            // Student's personal status or Take Quiz action
            if (currentAttempt != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${currentAttempt.scorePercentage}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Rank #${currentAttempt.rank}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }
            } else {
                Button(
                    onClick = onTakeQuiz,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Take Quiz", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PER-QUIZ LEADERBOARD FULL-PAGE VIEW
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizLeaderboardFullScreen(
    quizItem: IndividualQuizLeaderboardItem,
    onBackClick: () -> Unit,
    onTakeQuiz: (String) -> Unit,
    isDark: Boolean,
    screenBg: Color,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = screenBg,
        topBar = {
            QuizLeaderboardTopBar(
                quizTitle = quizItem.quiz.title,
                onBackClick = onBackClick,
                isDark = isDark,
                textPrimary = textPrimary,
                textSecondary = textSecondary
            )
        },
        bottomBar = {
            QuizLeaderboardBottomBar(
                quizId = quizItem.quiz.id,
                hasAttempted = quizItem.currentStudentAttempt != null,
                onBackClick = onBackClick,
                onTakeQuiz = onTakeQuiz,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 28.dp)
        ) {
            // 1. Comprehensive Header Card with All Quiz Metadata
            item {
                QuizLeaderboardHeaderCard(
                    quizItem = quizItem,
                    isDark = isDark,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // 2. Leaderboard Podium Section
            item {
                QuizLeaderboardPodiumSection(
                    attempts = quizItem.studentAttempts,
                    onTakeQuiz = { onTakeQuiz(quizItem.quiz.id) },
                    canTakeQuiz = quizItem.currentStudentAttempt == null,
                    isDark = isDark,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary
                )
            }

            // 3. Complete Rankings List
            if (quizItem.studentAttempts.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Full Rankings (${quizItem.studentAttempts.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )

                        quizItem.currentStudentAttempt?.let { myAttempt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandIndigo.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Your Standing: #${myAttempt.rank}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandIndigo,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                items(quizItem.studentAttempts) { attempt ->
                    QuizAttemptRankRow(
                        attempt = attempt,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FULL-PAGE TOP BAR
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizLeaderboardTopBar(
    quizTitle: String,
    onBackClick: () -> Unit,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isDark) Color(0xFF0F172A) else Color.White,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quiz Leaderboard",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = quizTitle,
                    fontSize = 12.sp,
                    color = BrandIndigo,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPREHENSIVE QUIZ DETAILS HEADER CARD
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizLeaderboardHeaderCard(
    quizItem: IndividualQuizLeaderboardItem,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val quiz = quizItem.quiz
    val visuals = QuizVisualHelper.getVisuals(quiz)

    // Formatted time duration
    val timeLimitSec = quiz.config.timeLimitSeconds
    val durationText = when {
        timeLimitSec <= 0 -> "Untimed"
        timeLimitSec >= 60 && timeLimitSec % 60 == 0 -> "${timeLimitSec / 60} Mins"
        timeLimitSec >= 60 -> "${timeLimitSec / 60}m ${timeLimitSec % 60}s"
        else -> "${timeLimitSec}s"
    }

    // Questions count
    val questionsCount = if (quiz.totalQuestions > 0) quiz.totalQuestions else quiz.questions.size
    val questionsText = if (questionsCount > 0) "$questionsCount Questions" else "Questions Included"

    // Difficulty with colors
    val diff = quiz.difficulty
    val diffColor = when (diff) {
        com.example.onlinequizapp.domain.model.Difficulty.EASY -> Color(0xFF10B981)
        com.example.onlinequizapp.domain.model.Difficulty.MEDIUM -> Color(0xFFF59E0B)
        com.example.onlinequizapp.domain.model.Difficulty.HARD -> Color(0xFFEF4444)
    }

    // Assignment Source & Icon
    val source = quizItem.assignmentSource
    val (sourceIcon, sourceColor) = when {
        source.startsWith("Batch", ignoreCase = true) -> Pair(Icons.Default.Group, Color(0xFF8B5CF6))
        source.contains("Direct", ignoreCase = true) -> Pair(Icons.Default.School, Color(0xFF3B82F6))
        else -> Pair(Icons.Default.Public, Color(0xFF06B6D4))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header: Real Quiz Logo Emblem + Title + Submissions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(visuals.gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                QuizVisualHelper.QuizLogoEmblem(
                    visuals = visuals,
                    contentDescription = quiz.title,
                    iconSize = 30.dp,
                    iconTint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${quizItem.totalCompletionsCount} Total Submissions",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textSecondary
                    )
                }
            }
        }

        HorizontalDivider(color = cardBorder, thickness = 0.8.dp)

        // Metadata Chips Grid (Difficulty, Questions, Duration, Category)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Difficulty Level
            QuizMetadataChip(
                icon = Icons.Default.Star,
                label = "Difficulty",
                value = diff.label,
                accentColor = diffColor,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Total Questions Count
            QuizMetadataChip(
                icon = Icons.Default.MenuBook,
                label = "Questions",
                value = questionsText,
                accentColor = BrandIndigo,
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Time / Duration
            QuizMetadataChip(
                icon = Icons.Default.Timer,
                label = "Time Limit",
                value = durationText,
                accentColor = Color(0xFF14B8A6),
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )

            // Category
            QuizMetadataChip(
                icon = Icons.Default.School,
                label = "Category",
                value = quiz.category.ifBlank { "General" },
                accentColor = Color(0xFFEC4899),
                isDark = isDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Where Assigned: Batch / Direct Assignment / Public Quiz
        QuizMetadataChip(
            icon = sourceIcon,
            label = "Assigned Scope",
            value = source,
            accentColor = sourceColor,
            isDark = isDark,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ─────────────────────────────────────────────────────────────
// METADATA CHIP COMPONENT
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizMetadataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = if (isDark) 0.12f else 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Column {
                Text(
                    text = label.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = value,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// QUIZ PODIUM SECTION (1-person, 2-person, or 3-person)
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizLeaderboardPodiumSection(
    attempts: List<QuizLeaderboardStudentAttempt>,
    onTakeQuiz: () -> Unit,
    canTakeQuiz: Boolean,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "🏆", fontSize = 16.sp)
            Text(
                text = "Leaderboard Podium",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary
            )
        }

        if (attempts.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Podium Awaiting First Champion! 🚀",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "No students have completed this quiz yet.\nBe the first to finish and claim #1 on the podium!",
                    fontSize = 12.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                if (canTakeQuiz) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onTakeQuiz,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Start This Quiz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (attempts.size == 1) {
            // Exactly 1 submission: Single center champion pillar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                QuizPodiumPillar(
                    attempt = attempts[0],
                    rank = 1,
                    pillarHeight = 115.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(140.dp)
                )
            }
        } else if (attempts.size == 2) {
            // Exactly 2 submissions: 1st and 2nd side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                QuizPodiumPillar(
                    attempt = attempts[0],
                    rank = 1,
                    pillarHeight = 115.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(130.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                QuizPodiumPillar(
                    attempt = attempts[1],
                    rank = 2,
                    pillarHeight = 90.dp,
                    accentColor = SilverAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.width(130.dp)
                )
            }
        } else {
            // 3 or more: 2nd on left (Silver), 1st center (Gold, elevated), 3rd right (Bronze)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                QuizPodiumPillar(
                    attempt = attempts[1],
                    rank = 2,
                    pillarHeight = 90.dp,
                    accentColor = SilverAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
                QuizPodiumPillar(
                    attempt = attempts[0],
                    rank = 1,
                    pillarHeight = 115.dp,
                    accentColor = GoldAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1.1f)
                )
                QuizPodiumPillar(
                    attempt = attempts[2],
                    rank = 3,
                    pillarHeight = 75.dp,
                    accentColor = BronzeAccent,
                    isDark = isDark,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PODIUM PILLAR FOR A QUIZ ATTEMPT
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizPodiumPillar(
    attempt: QuizLeaderboardStudentAttempt,
    rank: Int,
    pillarHeight: androidx.compose.ui.unit.Dp,
    accentColor: Color,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    val medalSymbol = when (rank) {
        1 -> "👑"
        2 -> "🥈"
        else -> "🥉"
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Medal / Crown
        Text(text = medalSymbol, fontSize = 16.sp)

        // Student Avatar with Rank Border
        UserAvatar(
            avatarUrl = attempt.avatarUrl,
            name = attempt.studentName,
            size = 46.dp,
            borderWidth = 2.dp,
            borderColor = accentColor
        )

        // Student Name
        Text(
            text = if (attempt.isCurrentStudent) "${attempt.studentName} (You)" else attempt.studentName,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (attempt.isCurrentStudent) BrandIndigo else textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Pillar block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .border(
                    1.dp,
                    accentColor.copy(alpha = 0.5f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${attempt.scorePercentage}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                if (attempt.elapsedTimeSeconds > 0) {
                    val mins = attempt.elapsedTimeSeconds / 60
                    val secs = attempt.elapsedTimeSeconds % 60
                    Text(
                        text = if (mins > 0) "${mins}m ${secs}s" else "${secs}s",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// FULL-PAGE BOTTOM ACTION BAR
// ─────────────────────────────────────────────────────────────
@Composable
private fun QuizLeaderboardBottomBar(
    quizId: String,
    hasAttempted: Boolean,
    onBackClick: () -> Unit,
    onTakeQuiz: (String) -> Unit,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardBg,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back", color = textPrimary, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { onTakeQuiz(quizId) },
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandIndigo)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasAttempted) "Retake Quiz" else "Take Quiz",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun QuizAttemptRankRow(
    attempt: QuizLeaderboardStudentAttempt,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val rankBadgeBg = when (attempt.rank) {
        1 -> GoldAccent
        2 -> SilverAccent
        3 -> BronzeAccent
        else -> if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    }

    val rowBg = if (attempt.isCurrentStudent) {
        BrandIndigo.copy(alpha = 0.12f)
    } else {
        cardBg
    }
    val rowBorder = if (attempt.isCurrentStudent) {
        BrandIndigo
    } else {
        cardBorder
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(rowBg)
            .border(1.dp, rowBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(rankBadgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${attempt.rank}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            UserAvatar(
                avatarUrl = attempt.avatarUrl,
                name = attempt.studentName,
                size = 34.dp,
                borderWidth = if (attempt.isCurrentStudent) 1.5.dp else 1.dp,
                borderColor = if (attempt.isCurrentStudent) BrandIndigo else (if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1))
            )

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = attempt.studentName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    if (attempt.isCurrentStudent) {
                        Text(
                            text = "(You)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandIndigo
                        )
                    }
                }

                if (attempt.elapsedTimeSeconds > 0) {
                    val mins = attempt.elapsedTimeSeconds / 60
                    val secs = attempt.elapsedTimeSeconds % 60
                    Text(
                        text = "Time: ${mins}m ${secs}s",
                        fontSize = 10.5.sp,
                        color = textSecondary
                    )
                }
            }
        }

        // Score Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EmeraldSuccess.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${attempt.scorePercentage}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldSuccess
            )
        }
    }
}
