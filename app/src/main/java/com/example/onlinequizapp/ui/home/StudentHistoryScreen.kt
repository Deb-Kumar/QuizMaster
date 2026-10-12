package com.example.onlinequizapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.QuizResult
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

// -------------------------------------------------------------
// Cohesive Theme Helpers
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
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF03071E) else Color(0xFFF1F5F9)

private val CardBg: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF0A1133) else Color(0xFFFFFFFF)

private val CardBorder: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF162055) else Color(0xFFE2E8F0)

private val TextPrimary: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)

private val TextSecondary: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF8B9CB8) else Color(0xFF64748B)

private val TextMutedDim: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF5F7296) else Color(0xFF94A3B8)

enum class HistoryFilterTab(val label: String) {
    ALL("All"),
    PASSED("Passed"),
    NEED_PRACTICE("Need Practice"),
    IN_PROGRESS("In Progress")
}

enum class HistorySortOrder(val label: String) {
    LATEST_FIRST("Latest First"),
    OLDEST_FIRST("Oldest First"),
    HIGHEST_SCORE("Highest Score"),
    LOWEST_SCORE("Lowest Score")
}

/**
 * Presentation model for displaying past quiz attempt items, matching the exact UI mockup.
 */
data class DisplayHistoryItem(
    val id: String,
    val quizId: String = "",
    val resultId: String = "",
    val title: String,
    val category: String,
    val questionCount: Int,
    val formatType: String = "MCQ",
    val timeLabel: String,
    val durationMinutes: Int,
    val scorePercentage: Int,
    val correctCount: Int,
    val totalQuestions: Int,
    val isPassed: Boolean,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val timestampMillis: Long = System.currentTimeMillis(),
    val logoDrawableRes: Int? = null
)

/**
 * 100% Pixel-Accurate Quiz History Page matching the design screenshot.
 */
@Composable
fun StudentHistoryScreen(
    quizHistory: List<QuizResult>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onViewResultClick: (String) -> Unit,
    onStartQuizClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    var selectedFilter by remember { mutableStateOf(HistoryFilterTab.ALL) }
    var sortOrder by remember { mutableStateOf(HistorySortOrder.LATEST_FIRST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val allHistoryItems = remember(quizHistory) {
        quizHistory
            .sortedByDescending { it.submittedAtMillis }
            .distinctBy { it.quizId }
            .mapIndexed { index, qh ->
            val visuals = QuizVisualHelper.getVisuals(
                title = qh.quizTitle,
                category = qh.quizCategory,
                iconKey = qh.quizIcon
            )
            val cleanCategory = qh.quizCategory
                .split(",")
                .firstOrNull()
                ?.trim()
                ?.ifEmpty { "General" }
                ?: "General"

            // Construct a guaranteed unique item key for Compose LazyColumn
            // A student can attempt the same quiz multiple times, so qh.quizId alone causes duplicate keys
            val uniqueKey = when {
                qh.resultId.isNotBlank() -> "${qh.resultId}_$index"
                qh.submittedAtMillis > 0L -> "${qh.quizId}_${qh.submittedAtMillis}_$index"
                else -> "${qh.quizId}_$index"
            }

            DisplayHistoryItem(
                id = uniqueKey,
                quizId = qh.quizId,
                resultId = qh.resultId,
                title = qh.quizTitle,
                category = cleanCategory,
                questionCount = qh.totalQuestions,
                formatType = "MCQ",
                timeLabel = formatFriendlyDate(qh.submittedAtMillis),
                durationMinutes = maxOf(1, qh.elapsedTimeSeconds / 60),
                scorePercentage = qh.scorePercentage,
                correctCount = qh.correctCount,
                totalQuestions = qh.totalQuestions,
                isPassed = qh.isPassed,
                icon = visuals.icon,
                gradientColors = visuals.gradientColors,
                timestampMillis = qh.submittedAtMillis,
                logoDrawableRes = visuals.logoDrawableRes
            )
        }
    }

    // Dynamic metrics calculated from actual quiz results
    val totalQuizzesCount = allHistoryItems.size
    val passedQuizzesCount = allHistoryItems.count { it.isPassed }
    val needPracticeQuizzesCount = allHistoryItems.count { !it.isPassed }
    val averageScorePercentage = if (allHistoryItems.isNotEmpty()) {
        allHistoryItems.map { it.scorePercentage }.average().toInt()
    } else {
        0
    }

    // Filter by Tab and Search
    val filteredList = remember(allHistoryItems, selectedFilter, searchQuery, sortOrder) {
        val list = allHistoryItems.filter { item ->
            val matchesFilter = when (selectedFilter) {
                HistoryFilterTab.ALL -> true
                HistoryFilterTab.PASSED -> item.isPassed
                HistoryFilterTab.NEED_PRACTICE -> !item.isPassed
                HistoryFilterTab.IN_PROGRESS -> false // Completed history page
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.category.contains(searchQuery, ignoreCase = true)
            }
            matchesFilter && matchesSearch
        }

        when (sortOrder) {
            HistorySortOrder.LATEST_FIRST -> list.sortedByDescending { it.timestampMillis }
            HistorySortOrder.OLDEST_FIRST -> list.sortedBy { it.timestampMillis }
            HistorySortOrder.HIGHEST_SCORE -> list.sortedByDescending { it.scorePercentage }
            HistorySortOrder.LOWEST_SCORE -> list.sortedBy { it.scorePercentage }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // 1. Top Bar: Back Arrow + "QuizMaster" Logo + Search Button + Calendar Button
        HistoryTopBar(
            onBackClick = onBackClick,
            onSearchToggle = { isSearchVisible = !isSearchVisible },
            isSearchActive = isSearchVisible,
            onCalendarClick = {
                // Toggle sorting or filter by date
                sortOrder = if (sortOrder == HistorySortOrder.LATEST_FIRST) {
                    HistorySortOrder.OLDEST_FIRST
                } else {
                    HistorySortOrder.LATEST_FIRST
                }
            }
        )

        // Collapsible Search Input
        AnimatedVisibility(
            visible = isSearchVisible,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search quiz title or topic...",
                            color = TextMutedDim,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF818CF8),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CardBg,
                        unfocusedContainerColor = CardBg,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2. Hero Section: Purple Icon + Quiz History Title + 3D Clipboard/Trophy Image
            item(key = "hero_section") {
                QuizHistoryHeroBanner()
            }

            // 3. Performance Summary Card (calculated from real user history)
            item(key = "performance_summary") {
                PerformanceSummaryCard(
                    totalQuizzes = totalQuizzesCount,
                    passedCount = passedQuizzesCount,
                    needPracticeCount = needPracticeQuizzesCount,
                    avgScore = averageScorePercentage
                )
            }

            // 4. Filter Tabs Row (All, Passed, Need Practice, In Progress)
            item(key = "filter_tabs") {
                HistoryFilterPills(
                    selectedFilter = selectedFilter,
                    onTabSelected = { selectedFilter = it }
                )
            }

            // 5. Section Header: "Recent Quiz Attempts" + "Latest First ⌄" Dropdown
            item(key = "attempts_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Quiz Attempts",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardBg)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable { sortMenuExpanded = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sortOrder.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Sort order",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false },
                            modifier = Modifier.background(CardBg)
                        ) {
                            HistorySortOrder.values().forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.label,
                                            fontSize = 13.sp,
                                            color = if (sortOrder == order) Color(0xFF818CF8) else TextPrimary,
                                            fontWeight = if (sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        sortOrder = order
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 6. List of Quiz Attempt Cards
            if (filteredList.isEmpty()) {
                item(key = "empty_state") {
                    HistoryEmptyStateCard(
                        onStartQuizClick = onStartQuizClick,
                        onResetFilters = {
                            selectedFilter = HistoryFilterTab.ALL
                            searchQuery = ""
                        }
                    )
                }
            } else {
                items(
                    items = filteredList,
                    key = { it.id }
                ) { item ->
                    QuizAttemptItemCard(
                        item = item,
                        onClick = { onViewResultClick(item.quizId.ifBlank { item.id }) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// Top Bar: Back Button, QuizMaster Branding, Search & Calendar
// -------------------------------------------------------------
@Composable
private fun HistoryTopBar(
    onBackClick: () -> Unit,
    onSearchToggle: () -> Unit,
    isSearchActive: Boolean,
    onCalendarClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Arrow Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // QuizMaster Logo
        QuizMasterBrandTitle(
            modifier = Modifier.weight(1f),
            isDarkTheme = isDark
        )

        // Search Button in rounded box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSearchActive) Color(0xFF6366F1).copy(alpha = 0.2f)
                    else (if (isDark) Color(0xFF0A1236) else Color.White)
                )
                .border(
                    1.dp,
                    if (isSearchActive) Color(0xFF6366F1)
                    else (if (isDark) Color(0xFF1B265E) else Color(0xFFE2E8F0)),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onSearchToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isSearchActive) Color(0xFF6366F1) else TextPrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Calendar Button in rounded box
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0xFF0A1236) else Color.White)
                .border(
                    1.dp,
                    if (isDark) Color(0xFF1B265E) else Color(0xFFE2E8F0),
                    RoundedCornerShape(12.dp)
                )
                .clickable { onCalendarClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Hero Section with 3D Illustration
// -------------------------------------------------------------
@Composable
private fun QuizHistoryHeroBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left History Icon Squircle
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Quiz History",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Track your past attempts, score breakdown and performance insights.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Exact 3D Clipboard & Trophy Hero Illustration Asset (No border frame, clean transparent 3D asset)
        Image(
            painter = painterResource(id = R.drawable.img_quiz_history_hero),
            contentDescription = "Quiz History Illustration",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .height(100.dp)
                .width(135.dp)
        )
    }
}

// -------------------------------------------------------------
// Your Performance Summary Card
// -------------------------------------------------------------
@Composable
private fun PerformanceSummaryCard(
    totalQuizzes: Int,
    passedCount: Int,
    needPracticeCount: Int,
    avgScore: Int
) {
    val isDark = isAppInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = if (isDark) {
                    Brush.linearGradient(
                        listOf(Color(0xFF1D4ED8).copy(alpha = 0.6f), Color(0xFF3B82F6).copy(alpha = 0.25f))
                    )
                } else {
                    Brush.linearGradient(
                        listOf(Color(0xFF818CF8).copy(alpha = 0.4f), Color(0xFFC7D2FE).copy(alpha = 0.6f))
                    )
                },
                shape = RoundedCornerShape(18.dp)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF071138) else Color(0xFFFFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Trending Icon + Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1D4ED8)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your Performance Summary",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Keep going! You're making great progress!",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Mini Stat Boxes (Total Quizzes, Passed, Needs Practice, Avg. Score)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Total Quizzes
                SummaryStatBox(
                    value = "$totalQuizzes",
                    label = "Total Quizzes",
                    icon = Icons.Default.Description,
                    iconBgColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )

                // 2. Passed
                SummaryStatBox(
                    value = "$passedCount",
                    label = "Passed",
                    icon = Icons.Default.Check,
                    iconBgColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )

                // 3. Needs Practice
                SummaryStatBox(
                    value = "$needPracticeCount",
                    label = "Needs Practice",
                    icon = Icons.Default.Close,
                    iconBgColor = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )

                // 4. Avg. Score
                SummaryStatBox(
                    value = "$avgScore%",
                    label = "Avg. Score",
                    icon = Icons.Default.AccessTime,
                    iconBgColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryStatBox(
    value: String,
    label: String,
    icon: ImageVector,
    iconBgColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF0A143D) else Color(0xFFF8FAFC))
            .border(
                1.dp,
                if (isDark) Color(0xFF162564) else Color(0xFFE2E8F0),
                RoundedCornerShape(12.dp)
            )
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Icon Pill
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Value
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Label
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// Filter Tabs (All, Passed, Need Practice, In Progress)
// -------------------------------------------------------------
@Composable
private fun HistoryFilterPills(
    selectedFilter: HistoryFilterTab,
    onTabSelected: (HistoryFilterTab) -> Unit
) {
    val isDark = isAppInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tab 1: All
        FilterPillButton(
            title = "All",
            icon = Icons.Default.FormatListBulleted,
            isSelected = selectedFilter == HistoryFilterTab.ALL,
            activeColor = Color(0xFF6366F1),
            contentColor = if (selectedFilter == HistoryFilterTab.ALL) Color.White else TextSecondary,
            onClick = { onTabSelected(HistoryFilterTab.ALL) },
            modifier = Modifier.weight(1f)
        )

        // Tab 2: Passed
        FilterPillButton(
            title = "Passed",
            icon = Icons.Default.CheckCircle,
            isSelected = selectedFilter == HistoryFilterTab.PASSED,
            activeColor = Color(0xFF10B981),
            contentColor = if (selectedFilter == HistoryFilterTab.PASSED) Color.White else (if (isDark) Color(0xFF10B981) else Color(0xFF059669)),
            onClick = { onTabSelected(HistoryFilterTab.PASSED) },
            modifier = Modifier.weight(1.1f)
        )

        // Tab 3: Need Practice
        FilterPillButton(
            title = "Need Practice",
            icon = Icons.Default.Error,
            isSelected = selectedFilter == HistoryFilterTab.NEED_PRACTICE,
            activeColor = Color(0xFFF43F5E),
            contentColor = if (selectedFilter == HistoryFilterTab.NEED_PRACTICE) Color.White else (if (isDark) Color(0xFFF43F5E) else Color(0xFFE11D48)),
            onClick = { onTabSelected(HistoryFilterTab.NEED_PRACTICE) },
            modifier = Modifier.weight(1.35f)
        )

        // Tab 4: In Progress
        FilterPillButton(
            title = "In Progress",
            icon = Icons.Default.AccessTime,
            isSelected = selectedFilter == HistoryFilterTab.IN_PROGRESS,
            activeColor = Color(0xFF0284C7),
            contentColor = if (selectedFilter == HistoryFilterTab.IN_PROGRESS) Color.White else (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)),
            onClick = { onTabSelected(HistoryFilterTab.IN_PROGRESS) },
            modifier = Modifier.weight(1.25f)
        )
    }
}

@Composable
private fun FilterPillButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val bg = if (isSelected) activeColor else (if (isDark) Color(0xFF0B143A) else Color.White)
    val border = if (isSelected) activeColor else (if (isDark) Color(0xFF18265E) else Color(0xFFE2E8F0))

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// -------------------------------------------------------------
// Individual Quiz Attempt Card (Exact match to screenshot)
// -------------------------------------------------------------
@Composable
private fun QuizAttemptItemCard(
    item: DisplayHistoryItem,
    onClick: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val ringColor = when {
        item.scorePercentage >= 70 -> Color(0xFF10B981)
        item.scorePercentage >= 60 -> Color(0xFF38BDF8)
        else -> Color(0xFFF43F5E)
    }

    val badgeBg = if (item.isPassed) Color(0xFF059669) else Color(0xFFE11D48)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDark) Color(0xFF142054) else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF091236) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left Gradient Squircle with Category Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(item.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                if (item.logoDrawableRes != null) {
                    Image(
                        painter = painterResource(id = item.logoDrawableRes),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Middle Content: Title, Category/Questions, Timestamp/Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Metadata: "HTML • 10 Questions • MCQ"
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.category,
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "${item.questionCount} Questions",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = " • ",
                        fontSize = 11.sp,
                        color = if (isDark) Color(0xFF64748B) else Color(0xFFCBD5E1),
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = item.formatType,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Timestamp & Time Spent
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.timeLabel,
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${item.durationMinutes} min",
                        fontSize = 10.5.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Circular Gauge Ring with Percentage
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF050D2C) else Color(0xFFF1F5F9))
                    .border(width = 2.dp, color = ringColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.scorePercentage}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. Score Fraction & Label ("10/10 Score")
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "${item.correctCount}/${item.totalQuestions}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Score",
                    fontSize = 9.5.sp,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 5. Passed / Need Practice Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.isPassed) "Passed" else "Need Practice",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 6. Right Chevron Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Review",
                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// Empty State Card
// -------------------------------------------------------------
@Composable
private fun HistoryEmptyStateCard(
    onStartQuizClick: () -> Unit,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No Attempts Found",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "No quiz attempts match your current filters. Adjust your filter or take a new quiz!",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val isDark = isAppInDarkTheme()
                Button(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Reset Filters",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onStartQuizClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Take a Quiz", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Date Formatting Helper
// -------------------------------------------------------------
private fun formatFriendlyDate(timestampMillis: Long): String {
    if (timestampMillis <= 0L) return "Recent"
    val now = java.util.Calendar.getInstance()
    val attempt = java.util.Calendar.getInstance().apply { timeInMillis = timestampMillis }
    val timeFormat = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

    val isSameYear = now.get(java.util.Calendar.YEAR) == attempt.get(java.util.Calendar.YEAR)
    val isSameDay = isSameYear && now.get(java.util.Calendar.DAY_OF_YEAR) == attempt.get(java.util.Calendar.DAY_OF_YEAR)

    val yesterday = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(java.util.Calendar.YEAR) == attempt.get(java.util.Calendar.YEAR) &&
            yesterday.get(java.util.Calendar.DAY_OF_YEAR) == attempt.get(java.util.Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "Today, ${timeFormat.format(java.util.Date(timestampMillis))}"
        isYesterday -> "Yesterday, ${timeFormat.format(java.util.Date(timestampMillis))}"
        isSameYear -> java.text.SimpleDateFormat("MMM d, h:mm a", java.util.Locale.getDefault()).format(java.util.Date(timestampMillis))
        else -> java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestampMillis))
    }
}
