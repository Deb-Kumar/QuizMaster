package com.example.onlinequizapp.ui.teacher.analytics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.ui.teacher.TeacherBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun TeacherAnalyticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TeacherAnalyticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isTeacherInDarkTheme()

    Scaffold(
        containerColor = TeacherBg,
        topBar = {
            AnalyticsTopBar(
                isDark = isDark,
                onBackClick = onBackClick,
                onRefreshClick = { viewModel.loadData() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF6366F1))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                // 1. Filter Chips Row (Batch, Quiz, Timeframe)
                item {
                    AnalyticsFilterSection(
                        uiState = uiState,
                        isDark = isDark,
                        onSelectBatch = { viewModel.onSelectBatch(it) },
                        onSelectQuiz = { viewModel.onSelectQuiz(it) },
                        onSelectTimeframe = { viewModel.onSelectTimeframe(it) }
                    )
                }

                // 2. Executive KPI Summary Cards Grid
                item {
                    KpiMetricsGrid(
                        uiState = uiState,
                        isDark = isDark
                    )
                }

                // 3. Score Distribution Chart Card
                item {
                    ScoreDistributionCard(
                        brackets = uiState.scoreBrackets,
                        totalAttempts = uiState.totalAttempts,
                        isDark = isDark
                    )
                }

                // 4. Per-Quiz Breakdown Section Header
                item {
                    SectionHeader(
                        title = "Quiz Performance Breakdown",
                        subtitle = "${uiState.quizAnalyticsList.size} Quizzes tracked",
                        icon = Icons.Default.Quiz
                    )
                }

                if (uiState.quizAnalyticsList.isEmpty()) {
                    item {
                        EmptyStateCard(
                            message = "No quiz attempts recorded for the selected filter.",
                            isDark = isDark
                        )
                    }
                } else {
                    items(uiState.quizAnalyticsList, key = { it.quiz.id }) { item ->
                        QuizPerformanceCard(
                            item = item,
                            isDark = isDark
                        )
                    }
                }

                // 5. Top Scorers vs Needs Attention Section
                item {
                    StudentInsightsSection(
                        topPerformers = uiState.topPerformers,
                        needsAttention = uiState.studentsNeedingAttention,
                        isDark = isDark
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp).navigationBarsPadding()) }
            }
        }
    }
}

// -------------------------------------------------------------
// Top Bar Composable
// -------------------------------------------------------------
@Composable
private fun AnalyticsTopBar(
    isDark: Boolean,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TeacherCardBg)
            .border(width = 1.dp, color = TeacherCardBorder)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TeacherTextWhite
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "Analytics & Performance",
                    color = TeacherTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Real-time class telemetry & metrics",
                    color = TeacherTextMuted,
                    fontSize = 11.5.sp
                )
            }
        }

        IconButton(onClick = onRefreshClick) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color(0xFF6366F1)
            )
        }
    }
}

// -------------------------------------------------------------
// Filter Section (Batch, Quiz, Timeframe)
// -------------------------------------------------------------
@Composable
private fun AnalyticsFilterSection(
    uiState: TeacherAnalyticsUiState,
    isDark: Boolean,
    onSelectBatch: (String?) -> Unit,
    onSelectQuiz: (String?) -> Unit,
    onSelectTimeframe: (AnalyticsTimeframe) -> Unit
) {
    var showBatchMenu by remember { mutableStateOf(false) }
    var showQuizMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Dropdown filter buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Batch Dropdown Button
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdownButton(
                    label = "Batch",
                    value = uiState.batches.find { it.id == uiState.selectedBatchId }?.name ?: "All Batches",
                    isDark = isDark,
                    onClick = { showBatchMenu = true }
                )
                DropdownMenu(
                    expanded = showBatchMenu,
                    onDismissRequest = { showBatchMenu = false },
                    modifier = Modifier.background(if (isDark) Color(0xFF0F172A) else Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Batches", color = if (isDark) Color.White else Color.Black) },
                        onClick = {
                            onSelectBatch(null)
                            showBatchMenu = false
                        }
                    )
                    uiState.batches.forEach { batch ->
                        DropdownMenuItem(
                            text = { Text(batch.name, color = if (isDark) Color.White else Color.Black) },
                            onClick = {
                                onSelectBatch(batch.id)
                                showBatchMenu = false
                            }
                        )
                    }
                }
            }

            // Quiz Dropdown Button
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdownButton(
                    label = "Quiz",
                    value = uiState.allQuizzes.find { it.id == uiState.selectedQuizId }?.title ?: "All Quizzes",
                    isDark = isDark,
                    onClick = { showQuizMenu = true }
                )
                DropdownMenu(
                    expanded = showQuizMenu,
                    onDismissRequest = { showQuizMenu = false },
                    modifier = Modifier.background(if (isDark) Color(0xFF0F172A) else Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("All Quizzes", color = if (isDark) Color.White else Color.Black) },
                        onClick = {
                            onSelectQuiz(null)
                            showQuizMenu = false
                        }
                    )
                    uiState.allQuizzes.forEach { quiz ->
                        DropdownMenuItem(
                            text = { Text(quiz.title, color = if (isDark) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            onClick = {
                                onSelectQuiz(quiz.id)
                                showQuizMenu = false
                            }
                        )
                    }
                }
            }
        }

        // Timeframe selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDark) Color(0xFF0A102E) else Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AnalyticsTimeframe.entries.forEach { timeframe ->
                val isSelected = uiState.selectedTimeframe == timeframe
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) Color(0xFF4F46E5) else Color.Transparent
                        )
                        .clickable { onSelectTimeframe(timeframe) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeframe.label,
                        color = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownButton(
    label: String,
    value: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    color = TeacherTextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp
                )
                Text(
                    text = value,
                    color = TeacherTextWhite,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TeacherTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Executive KPI Metrics Grid
// -------------------------------------------------------------
@Composable
private fun KpiMetricsGrid(
    uiState: TeacherAnalyticsUiState,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Total Attempts",
                value = "${uiState.totalAttempts}",
                subtext = "Across ${uiState.totalQuizzesAnalyzed} quiz(zes)",
                icon = Icons.Default.Quiz,
                accentColor = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )

            KpiMetricCard(
                title = "Average Score",
                value = "${uiState.averageScore}%",
                subtext = "High: ${uiState.highestScoreRecorded}% • Low: ${uiState.lowestScoreRecorded}%",
                icon = Icons.Default.BarChart,
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiMetricCard(
                title = "Pass Rate",
                value = "${uiState.passRate}%",
                subtext = "Score >= 60% threshold",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f)
            )

            KpiMetricCard(
                title = "Certificates Qualified",
                value = "${uiState.certifiedCount}",
                subtext = "Score >= 80% threshold",
                icon = Icons.Default.WorkspacePremium,
                accentColor = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TeacherTextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                color = TeacherTextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = subtext,
                color = TeacherTextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// Score Distribution Card
// -------------------------------------------------------------
@Composable
private fun ScoreDistributionCard(
    brackets: List<ScoreBracket>,
    totalAttempts: Int,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Score Distribution",
                        color = TeacherTextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Breakdown of all $totalAttempts submission(s)",
                        color = TeacherTextMuted,
                        fontSize = 11.5.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                brackets.forEach { bracket ->
                    val animatedProgress by animateFloatAsState(
                        targetValue = bracket.percentage,
                        animationSpec = tween(durationMillis = 600),
                        label = "bracketProgress"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(bracket.colorHex))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${bracket.label} (${bracket.rangeText})",
                                    color = TeacherTextWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "${bracket.count} (${(bracket.percentage * 100).toInt()}%)",
                                color = Color(bracket.colorHex),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Progress Bar Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isDark) Color(0xFF16214C) else Color(0xFFE2E8F0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(bracket.colorHex))
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Per-Quiz Performance Card
// -------------------------------------------------------------
@Composable
private fun QuizPerformanceCard(
    item: QuizAnalyticsItem,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: Title & Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.quiz.title,
                        color = TeacherTextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.quiz.questions.size} Questions • ${item.quiz.category.ifBlank { "General" }}",
                        color = TeacherTextMuted,
                        fontSize = 11.sp
                    )
                }

                // Difficulty badge
                val (diffBg, diffText) = when (item.quiz.difficulty.name.uppercase()) {
                    "HARD" -> Color(0xFFEF4444).copy(alpha = 0.15f) to Color(0xFFEF4444)
                    "MEDIUM" -> Color(0xFFF59E0B).copy(alpha = 0.15f) to Color(0xFFF59E0B)
                    else -> Color(0xFF10B981).copy(alpha = 0.15f) to Color(0xFF10B981)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(diffBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.quiz.difficulty.name,
                        color = diffText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Metric Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF030718).copy(alpha = 0.6f) else Color(0xFFF8FAFC))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuizStatColumn(label = "Attempts", value = "${item.attemptsCount}")
                QuizStatColumn(label = "Avg Score", value = "${item.averageScore}%", valueColor = Color(0xFF10B981))
                QuizStatColumn(label = "Pass Rate", value = "${item.passRate}%", valueColor = Color(0xFF38BDF8))
                QuizStatColumn(label = "High / Low", value = "${item.highestScore}% / ${item.lowestScore}%")
            }
        }
    }
}

@Composable
private fun QuizStatColumn(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = TeacherTextMuted,
            fontSize = 10.sp
        )
        Text(
            text = value,
            color = valueColor ?: TeacherTextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 12.5.sp
        )
    }
}

// -------------------------------------------------------------
// Student Insights: Top Performers & Needs Attention
// -------------------------------------------------------------
@Composable
private fun StudentInsightsSection(
    topPerformers: List<StudentPerformanceItem>,
    needsAttention: List<StudentPerformanceItem>,
    isDark: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Top Performers
        SectionHeader(
            title = "Top Student Performers",
            subtitle = "Students achieving highest mastery",
            icon = Icons.Default.EmojiEvents
        )

        if (topPerformers.isEmpty()) {
            EmptyStateCard(message = "No qualifying student attempts yet.", isDark = isDark)
        } else {
            topPerformers.forEachIndexed { index, student ->
                StudentPerformanceRow(
                    rank = index + 1,
                    student = student,
                    isDark = isDark,
                    isTopScorer = true
                )
            }
        }

        // 2. Students Needing Attention (< 50%)
        Spacer(modifier = Modifier.height(8.dp))
        SectionHeader(
            title = "Students Needing Support",
            subtitle = "Scoring below 50% average threshold",
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFEF4444)
        )

        if (needsAttention.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉 All students are scoring above 50% passing mark!",
                    color = Color(0xFF10B981),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            needsAttention.forEach { student ->
                StudentPerformanceRow(
                    rank = null,
                    student = student,
                    isDark = isDark,
                    isTopScorer = false
                )
            }
        }
    }
}

@Composable
private fun StudentPerformanceRow(
    rank: Int?,
    student: StudentPerformanceItem,
    isDark: Boolean,
    isTopScorer: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TeacherCardBg)
            .border(
                1.dp,
                if (!isTopScorer) Color(0xFFEF4444).copy(alpha = 0.4f) else TeacherCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (rank != null) {
                    val medalColor = when (rank) {
                        1 -> Color(0xFFF59E0B)
                        2 -> Color(0xFF94A3B8)
                        3 -> Color(0xFFD97706)
                        else -> Color(0xFF4F46E5)
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(medalColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$rank",
                            color = medalColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column {
                    Text(
                        text = student.studentName,
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${student.attemptsCount} Attempt(s) • Last: ${student.lastAttemptQuizTitle.ifBlank { "Quiz" }}",
                        color = TeacherTextMuted,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "${student.averageScore}%",
                color = if (isTopScorer) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

// -------------------------------------------------------------
// Common Section Helpers
// -------------------------------------------------------------
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = Color(0xFF6366F1)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TeacherTextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp
            )
            Text(
                text = subtitle,
                color = TeacherTextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    message: String,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TeacherTextMuted,
            fontSize = 12.sp
        )
    }
}
