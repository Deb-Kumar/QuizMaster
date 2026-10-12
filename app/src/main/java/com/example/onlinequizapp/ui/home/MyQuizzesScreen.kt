package com.example.onlinequizapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.data.repository.QuizRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.data.session.QuizDraftManager
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.domain.repository.UserStats
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.components.QuizStartModal
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

// -------------------------------------------------------------
// Theme Helpers
// -------------------------------------------------------------
@Composable
private fun isAppInDarkTheme(): Boolean {
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    return when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemDark
    }
}

private val DarkBg: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF04081E) else Color(0xFFF1F5F9)

private val CardBg: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF0A122E) else Color(0xFFFFFFFF)

private val CardBorder: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFE2E8F0)

private val TextPrimary: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)

private val TextSecondary: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)

private val TextMutedDim: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)

private val BrandTextGradient = Brush.horizontalGradient(
    listOf(Color(0xFF818CF8), Color(0xFFC084FC))
)

// -------------------------------------------------------------
// Status & Progress Models
// -------------------------------------------------------------
enum class StudentQuizStatus(val label: String) {
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    NOT_YET("Not Yet")
}

enum class StudentQuizFilter(val label: String, val icon: ImageVector) {
    ALL("All", Icons.Default.Widgets),
    COMPLETED("Completed", Icons.Default.CheckCircle),
    IN_PROGRESS("In Progress", Icons.Default.Schedule),
    NOT_YET("Not Yet", Icons.Default.HourglassTop)
}

data class QuizWithDisplayData(
    val quiz: Quiz,
    val status: StudentQuizStatus,
    val progressPercent: Int
)

// -------------------------------------------------------------
// Main MyQuizzesScreen Composable
// -------------------------------------------------------------
@Composable
fun MyQuizzesScreen(
    quizzes: List<Quiz>,
    userStats: UserStats? = null,
    onQuizClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onViewResultClick: ((String) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
    val isDark = isAppInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(StudentQuizFilter.ALL) }

    val activeDraft by QuizDraftManager.activeDraftFlow.collectAsState()
    val savedUserId = remember { AuthSessionManager.getSavedUser()?.id ?: "" }
    var selectedQuizForModal by remember { mutableStateOf<QuizWithDisplayData?>(null) }

    // Map each quiz with real-time status & progress
    val quizItemsWithProgress = remember(quizzes, activeDraft) {
        quizzes.distinctBy { it.id }.map { quiz ->
            // Check actual completed session in repository synchronously without blocking
            val isCompletedInRepo = QuizRepositoryImpl.instance.hasCompletedQuizLocally(quiz.id)

            // Check actual in-progress draft
            val draft = QuizDraftManager.getDraftForQuiz(savedUserId, quiz.id)

            val (status, progress) = when {
                isCompletedInRepo -> StudentQuizStatus.COMPLETED to 100
                draft != null && draft.totalQuestions > 0 -> {
                    val pct = ((draft.currentIndex + 1).toFloat() / draft.totalQuestions.toFloat() * 100f).toInt()
                    StudentQuizStatus.IN_PROGRESS to pct.coerceIn(1, 99)
                }
                else -> StudentQuizStatus.NOT_YET to 0
            }

            QuizWithDisplayData(
                quiz = quiz,
                status = status,
                progressPercent = progress
            )
        }
    }

    // Dynamic counts
    val totalCount = quizItemsWithProgress.size
    val completedCount = quizItemsWithProgress.count { it.status == StudentQuizStatus.COMPLETED }
    val inProgressCount = quizItemsWithProgress.count { it.status == StudentQuizStatus.IN_PROGRESS }
    val notYetCount = quizItemsWithProgress.count { it.status == StudentQuizStatus.NOT_YET }

    // Filtered quizzes
    val filteredQuizzes = remember(quizItemsWithProgress, searchQuery, selectedFilter) {
        quizItemsWithProgress.filter { item ->
            val matchesFilter = when (selectedFilter) {
                StudentQuizFilter.ALL -> true
                StudentQuizFilter.COMPLETED -> item.status == StudentQuizStatus.COMPLETED
                StudentQuizFilter.IN_PROGRESS -> item.status == StudentQuizStatus.IN_PROGRESS
                StudentQuizFilter.NOT_YET -> item.status == StudentQuizStatus.NOT_YET
            }
            val matchesSearch = searchQuery.isBlank() ||
                    item.quiz.title.contains(searchQuery, ignoreCase = true) ||
                    item.quiz.category.contains(searchQuery, ignoreCase = true) ||
                    item.quiz.description.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 1. Top Bar: Back Button, QuizMaster Brand Logo, Search Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // QuizMaster Logo
            QuizMasterBrandTitle(isDarkTheme = isDark)

            Spacer(modifier = Modifier.weight(1f))

            // Search Icon Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f)
                        else Color(0xFFE2E8F0).copy(alpha = 0.8f)
                    )
                    .clickable { isSearchVisible = !isSearchVisible },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expandable Search Input
        AnimatedVisibility(
            visible = isSearchVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search your quizzes...",
                        color = TextMutedDim,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = Color(0xFF818CF8),
                    unfocusedBorderColor = CardBorder,
                    focusedContainerColor = CardBg,
                    unfocusedContainerColor = CardBg,
                    cursorColor = Color(0xFF818CF8)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Main Scrollable Quiz Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 2. Hero Section: "My Quizzes" Title, Subtitle, and 3D Books Illustration
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Quizzes",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Keep practicing. Keep improving.",
                            color = TextSecondary,
                            fontSize = 13.5.sp
                        )
                    }

                    // 3D Graduation Cap on Books Illustration
                    Image(
                        painter = painterResource(id = R.drawable.img_my_quizzes_hero),
                        contentDescription = "My Quizzes Hero",
                        modifier = Modifier
                            .height(78.dp)
                            .width(110.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // 3. Analytics / Summary Donut & Metric Cards
            item {
                StudentQuizStatsDonutCard(
                    totalQuizzes = totalCount,
                    completedCount = completedCount,
                    inProgressCount = inProgressCount,
                    notYetCount = notYetCount,
                    isDark = isDark
                )
            }

            // 4. Status Filter Pills Row (All, Completed, In Progress, Skipped)
            item {
                StudentQuizFilterPills(
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it },
                    isDark = isDark
                )
            }

            // 5. Quiz Items List or Empty State
            if (filteredQuizzes.isEmpty()) {
                item {
                    StudentEmptyQuizzesState(
                        hasFilters = searchQuery.isNotEmpty() || selectedFilter != StudentQuizFilter.ALL,
                        onResetFilters = {
                            searchQuery = ""
                            selectedFilter = StudentQuizFilter.ALL
                        }
                    )
                }
            } else {
                items(
                    items = filteredQuizzes,
                    key = { it.quiz.id }
                ) { item ->
                    StudentQuizCard(
                        item = item,
                        onClick = {
                            if (item.status == StudentQuizStatus.COMPLETED && onViewResultClick != null) {
                                onViewResultClick(item.quiz.id)
                            } else {
                                selectedQuizForModal = item
                            }
                        },
                        isDark = isDark
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (selectedQuizForModal != null) {
        val item = selectedQuizForModal!!
        val isPending = item.status == StudentQuizStatus.IN_PROGRESS
        val isCompleted = item.status == StudentQuizStatus.COMPLETED
        val draft = remember(item.quiz.id) {
            QuizDraftManager.getDraftForQuiz(savedUserId, item.quiz.id)
        }
        QuizStartModal(
            quiz = item.quiz,
            isPending = isPending,
            isCompleted = isCompleted,
            progressPercent = item.progressPercent,
            savedQuestionIndex = draft?.currentIndex,
            isDark = isDark,
            onStartQuiz = {
                val quizId = item.quiz.id
                selectedQuizForModal = null
                onQuizClick(quizId)
            },
            onViewResult = {
                val quizId = item.quiz.id
                selectedQuizForModal = null
                onViewResultClick?.invoke(quizId)
            },
            onDismiss = {
                selectedQuizForModal = null
            }
        )
    }
}

// -------------------------------------------------------------
// Analytics / Donut Card Composable
// -------------------------------------------------------------
@Composable
private fun StudentQuizStatsDonutCard(
    totalQuizzes: Int,
    completedCount: Int,
    inProgressCount: Int,
    notYetCount: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBackground = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F1A3E).copy(alpha = 0.95f),
                Color(0xFF142457).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEEF2FF),
                Color(0xFFF5F3FF)
            )
        )
    }

    val cardBorderColor = if (isDark) {
        Color(0xFF2563EB).copy(alpha = 0.45f)
    } else {
        Color(0xFF818CF8).copy(alpha = 0.4f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardBackground)
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Circular Gauge
                Box(
                    modifier = Modifier.size(86.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val completionRatio = if (totalQuizzes > 0) {
                        (completedCount.toFloat() / totalQuizzes.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    val trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFCBD5E1)
                    val progressBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFF06B6D4),
                            Color(0xFF3B82F6),
                            Color(0xFF8B5CF6),
                            Color(0xFF06B6D4)
                        )
                    )

                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 7.dp.toPx()

                        // Background track circle
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )

                        // Filled progress arc
                        if (completionRatio > 0f) {
                            drawArc(
                                brush = progressBrush,
                                startAngle = -90f,
                                sweepAngle = completionRatio * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$totalQuizzes",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Total Quizzes",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Vertical partition line 1 (Between Donut Gauge and Metrics)
                VerticalPartitionLine(isDark = isDark)

                Spacer(modifier = Modifier.width(4.dp))

                // 3 Metrics Columns with vertical partition dividers
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Metric 1: Completed
                    StatMetricColumn(
                        icon = Icons.Default.Check,
                        badgeColor = Color(0xFF10B981),
                        count = completedCount,
                        label = "Completed",
                        modifier = Modifier.weight(1f)
                    )

                    // Vertical partition line 2 (Between Completed and In Progress)
                    VerticalPartitionLine(isDark = isDark)

                    // Metric 2: In Progress
                    StatMetricColumn(
                        icon = Icons.Default.Schedule,
                        badgeColor = Color(0xFFF59E0B),
                        count = inProgressCount,
                        label = "In Progress",
                        modifier = Modifier.weight(1f)
                    )

                    // Vertical partition line 3 (Between In Progress and Not Yet)
                    VerticalPartitionLine(isDark = isDark)

                    // Metric 3: Not Yet
                    StatMetricColumn(
                        icon = Icons.Default.HourglassTop,
                        badgeColor = Color(0xFF8B5CF6),
                        count = notYetCount,
                        label = "Not Yet",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalPartitionLine(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val gradientBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.04f),
                    Color(0xFF38BDF8).copy(alpha = 0.35f),
                    Color(0xFF38BDF8).copy(alpha = 0.04f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF94A3B8).copy(alpha = 0.08f),
                    Color(0xFF94A3B8).copy(alpha = 0.45f),
                    Color(0xFF94A3B8).copy(alpha = 0.08f)
                )
            )
        }
    }

    Box(
        modifier = modifier
            .width(1.dp)
            .height(48.dp)
            .background(gradientBrush, RoundedCornerShape(1.dp))
    )
}

@Composable
private fun StatMetricColumn(
    icon: ImageVector,
    badgeColor: Color,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "$count",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

// -------------------------------------------------------------
// Filter Pills Row Composable
// -------------------------------------------------------------
@Composable
private fun StudentQuizFilterPills(
    selectedFilter: StudentQuizFilter,
    onSelectFilter: (StudentQuizFilter) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StudentQuizFilter.entries.forEach { filter ->
            val isSelected = selectedFilter == filter

            val bgModifier = if (isSelected) {
                Modifier.background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    )
                )
            } else {
                Modifier
                    .background(
                        if (isDark) Color(0xFF0F1A38).copy(alpha = 0.85f)
                        else Color(0xFFFFFFFF)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF1E2D56) else Color(0xFFE2E8F0),
                        RoundedCornerShape(14.dp)
                    )
            }

            val iconColor = if (isSelected) Color.White else TextSecondary
            val textColor = if (isSelected) Color.White else TextSecondary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .then(bgModifier)
                    .clickable { onSelectFilter(filter) }
                    .padding(vertical = 9.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = filter.icon,
                        contentDescription = filter.label,
                        tint = iconColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = filter.label,
                        style = TextStyle(
                            color = textColor,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Quiz Card Composable (Pixel-Matched to Screenshot)
// -------------------------------------------------------------
@Composable
private fun StudentQuizCard(
    item: QuizWithDisplayData,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val quiz = item.quiz
    val status = item.status
    val progress = item.progressPercent

    // Time estimate in minutes if explicitly mentioned by teacher
    val hasTimeLimit = remember(quiz) {
        quiz.config.timerMode == TimerMode.COUNTDOWN && quiz.config.timeLimitSeconds > 0
    }
    val timeLimitMinutes = remember(quiz, hasTimeLimit) {
        if (hasTimeLimit) {
            maxOf(1, quiz.config.timeLimitSeconds / 60)
        } else null
    }

    // Determine category icon & gradient via QuizVisualHelper
    val visuals = remember(quiz) { QuizVisualHelper.getVisuals(quiz) }
    val categoryIcon = visuals.icon
    val categoryGradient = visuals.gradientColors

    val cardBgColor = if (isDark) Color(0xFF0C1636).copy(alpha = 0.9f) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF1B284D) else Color(0xFFE2E8F0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBgColor)
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Squircle Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(categoryGradient)),
                contentAlignment = Alignment.Center
            ) {
                QuizVisualHelper.QuizLogoEmblem(
                    visuals = visuals,
                    contentDescription = quiz.category,
                    iconSize = 30.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Quiz Information Column
            Column(modifier = Modifier.weight(1f)) {
                // Top Row: Title + Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quiz.title,
                        style = TextStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Status Pill Badge
                    StudentStatusBadge(status = status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle Row: Questions count • Optional Time • Difficulty level
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = if (quiz.questions.size == 1) "1 question" else "${quiz.questions.size} questions",
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    )

                    Text(
                        text = "•",
                        style = TextStyle(
                            color = TextMutedDim,
                            fontSize = 11.sp
                        )
                    )
                    val durationText = if (hasTimeLimit && timeLimitMinutes != null) {
                        if (quiz.config.timeLimitSeconds % 60 == 0) "${quiz.config.timeLimitSeconds / 60} min"
                        else "${quiz.config.timeLimitSeconds / 60}m ${quiz.config.timeLimitSeconds % 60}s"
                    } else {
                        "Unlimited"
                    }
                    Text(
                        text = durationText,
                        style = TextStyle(
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                    )

                    Text(
                        text = "•",
                        style = TextStyle(
                            color = TextMutedDim,
                            fontSize = 11.sp
                        )
                    )

                    // Difficulty Chip: Easy, Medium, Hard (Hard in red)
                    val (diffBg, diffText) = when (quiz.difficulty) {
                        Difficulty.EASY -> (if (isDark) Color(0x2810B981) else Color(0xFFECFDF5)) to (if (isDark) Color(0xFF34D399) else Color(0xFF059669))
                        Difficulty.MEDIUM -> (if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB)) to (if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706))
                        Difficulty.HARD -> (if (isDark) Color(0x33EF4444) else Color(0xFFFEF2F2)) to (if (isDark) Color(0xFFF87171) else Color(0xFFDC2626))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffBg)
                            .border(0.6.dp, diffText.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = quiz.difficulty.label,
                            style = TextStyle(
                                color = diffText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Schedule Window Pill (if set)
                val schedStatus = remember(quiz) { quiz.getScheduleStatus() }
                if (schedStatus != com.example.onlinequizapp.domain.model.QuizScheduleStatus.ALWAYS_AVAILABLE) {
                    Spacer(modifier = Modifier.height(5.dp))
                    val (sBg, sBorder, sColor, sIcon, sText) = when (schedStatus) {
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.UPCOMING -> {
                            val timeStr = quiz.availabilityStartMillis?.let {
                                java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it))
                            } ?: "Soon"
                            PentadVal(
                                if (isDark) Color(0x2838BDF8) else Color(0xFFF0F9FF),
                                if (isDark) Color(0x5538BDF8) else Color(0xFFBAE6FD),
                                if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                Icons.Default.Schedule,
                                "Opens $timeStr"
                            )
                        }
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.ACTIVE_WITH_DEADLINE -> {
                            val timeStr = quiz.availabilityEndMillis?.let {
                                java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it))
                            } ?: "Soon"
                            PentadVal(
                                if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB),
                                if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A),
                                if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                Icons.Default.Schedule,
                                "Closes $timeStr"
                            )
                        }
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED -> {
                            PentadVal(
                                if (isDark) Color(0x28EF4444) else Color(0xFFFEF2F2),
                                if (isDark) Color(0x55EF4444) else Color(0xFFFECACA),
                                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                Icons.Default.Lock,
                                "Closed (Expired)"
                            )
                        }
                        else -> PentadVal(Color.Transparent, Color.Transparent, Color.Transparent, Icons.Default.Schedule, "")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sBg)
                            .border(0.6.dp, sBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = sIcon,
                            contentDescription = null,
                            tint = sColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.5.dp))
                        Text(
                            text = sText,
                            style = TextStyle(
                                color = sColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar and Percentage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Custom rounded progress bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                    ) {
                        val progressFraction = (progress / 100f).coerceIn(0f, 1f)
                        if (progressFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "$progress%",
                        style = TextStyle(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chevron Right Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Start ${quiz.title}",
                tint = TextMutedDim,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Status Badge Composable
// -------------------------------------------------------------
@Composable
private fun StudentStatusBadge(status: StudentQuizStatus) {
    val (bgColor, icon, text) = when (status) {
        StudentQuizStatus.COMPLETED -> {
            Triple(Color(0xFF10B981), Icons.Default.Check, "Completed")
        }
        StudentQuizStatus.IN_PROGRESS -> {
            Triple(Color(0xFFF59E0B), Icons.Default.Schedule, "In Progress")
        }
        StudentQuizStatus.NOT_YET -> {
            Triple(Color(0xFF8B5CF6), Icons.Default.HourglassTop, "Not Yet")
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 9.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = TextStyle(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

// -------------------------------------------------------------
// Empty State Composable
// -------------------------------------------------------------
@Composable
private fun StudentEmptyQuizzesState(
    hasFilters: Boolean,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "No results",
            tint = TextMutedDim,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "No Quizzes Found",
            style = TextStyle(
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasFilters) {
                "We couldn't find any quizzes matching your search or active filter."
            } else {
                "There are no quizzes currently available. Check back soon!"
            },
            style = TextStyle(
                color = TextSecondary,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center
            )
        )
        if (hasFilters) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF6366F1))
                    .clickable(onClick = onResetFilters)
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    text = "Reset Filters",
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

private data class PentadVal<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)