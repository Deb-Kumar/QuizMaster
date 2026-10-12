package com.example.onlinequizapp.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
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
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.domain.model.User
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.teacher.components.TeacherNotificationButton

// -------------------------------------------------------------
// Cyberpunk & Dark Tech Palette matching uploaded design
// -------------------------------------------------------------
private val ScreenBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF030718) else Color(0xFFF1F5F9)
private val CardBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF080E2A) else Color(0xFFFFFFFF)
private val CardBorderColor: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF141F48) else Color(0xFFE2E8F0)
private val StatCardBg: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF091030) else Color(0xFFFFFFFF)
private val StatBorderColor: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF162354) else Color(0xFFE2E8F0)

private val PurplePrimary = Color(0xFF8B5CF6)
private val PurpleGradient = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))
private val BlueGradient = Brush.horizontalGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
private val BannerGradient = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1), Color(0xFF0284C7)))

private val TextPrimary: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)
private val TextSecondary: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)
private val TextMuted: Color @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)

// Status Enums for My Quizzes
enum class QuizStatus(val displayName: String) {
    PUBLISHED("Published"),
    DRAFT("Draft");

    @Composable
    fun getBadgeColor(): Color {
        val isDark = isTeacherInDarkTheme()
        return when (this) {
            PUBLISHED -> if (isDark) Color(0xFF34D399) else Color(0xFF059669)
            DRAFT -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)
        }
    }

    @Composable
    fun getBadgeBg(): Color {
        val isDark = isTeacherInDarkTheme()
        return when (this) {
            PUBLISHED -> if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
            DRAFT -> if (isDark) Color(0xFF1E293B) else Color(0xFFFFFBEB)
        }
    }

    @Composable
    fun getBorderColor(): Color {
        val isDark = isTeacherInDarkTheme()
        return when (this) {
            PUBLISHED -> if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)
            DRAFT -> if (isDark) Color(0xFF334155) else Color(0xFFFDE68A)
        }
    }
}

internal fun formatQuizDuration(timerMode: com.example.onlinequizapp.domain.model.TimerMode, timeLimitSeconds: Int): String {
    if (timerMode != com.example.onlinequizapp.domain.model.TimerMode.COUNTDOWN || timeLimitSeconds <= 0) {
        return "Unlimited"
    }
    val hours = timeLimitSeconds / 3600
    val remainingSeconds = timeLimitSeconds % 3600
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours} hr"
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "$minutes min"
        else -> "$seconds sec"
    }
}

// UI Model for rich display matching screenshot
data class TeacherQuizItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val difficulty: String,
    val questionCount: Int,
    val durationMinutes: Int,
    val hasTimeLimit: Boolean = false,
    val formattedDuration: String = "Unlimited",
    val attemptCount: Int,
    val status: QuizStatus,
    val iconBgColor: Brush,
    val iconType: String, // "CODE", "REACT", "ANDROID", "CHEM", "HISTORY", "MATH"
    val quizCode: String? = null,
    val logoDrawableRes: Int? = null,
    val targetBatchIds: List<String> = emptyList(),
    val batchNames: List<String> = emptyList(),
    val targetStudentIds: List<String> = emptyList(),
    val availabilityStartMillis: Long? = null,
    val availabilityEndMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

private fun getCategoryIconType(category: String): String {
    val lower = category.lowercase()
    return when {
        lower.contains("android") || lower.contains("mobile") || lower.contains("kotlin") -> "ANDROID"
        lower.contains("react") || lower.contains("web") || lower.contains("frontend") -> "REACT"
        lower.contains("code") || lower.contains("program") || lower.contains("java") || lower.contains("python") || lower.contains("c++") || lower.contains("c#") -> "CODE"
        lower.contains("chem") || lower.contains("science") || lower.contains("bio") || lower.contains("physics") -> "CHEM"
        lower.contains("history") || lower.contains("culture") || lower.contains("social") || lower.contains("geography") -> "HISTORY"
        lower.contains("math") || lower.contains("algebra") || lower.contains("stat") -> "MATH"
        else -> "DEFAULT"
    }
}

internal fun getCategoryGradient(category: String): Brush {
    val lower = category.lowercase()
    return when {
        lower.contains("android") || lower.contains("mobile") -> Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
        lower.contains("react") || lower.contains("web") -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF06B6D4)))
        lower.contains("chem") || lower.contains("science") -> Brush.linearGradient(listOf(Color(0xFFE11D48), Color(0xFFF43F5E)))
        lower.contains("history") || lower.contains("culture") -> Brush.linearGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7)))
        lower.contains("math") -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFF38BDF8)))
        lower.contains("code") || lower.contains("program") || lower.contains("java") || lower.contains("python") -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))
        else -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF38BDF8)))
    }
}

internal fun formatTimeAgo(timestamp: Long): String {
    if (timestamp <= 0L) return "Just now"
    val now = System.currentTimeMillis()
    val diffMs = (now - timestamp).coerceAtLeast(0L)
    val diffSec = diffMs / 1000
    val diffMin = diffSec / 60
    val diffHours = diffMin / 60
    val diffDays = diffHours / 24

    return when {
        diffMin < 1 -> "Just now"
        diffMin < 60 -> "${diffMin}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays == 1L -> "Yesterday"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

internal fun getQuizLatestTimeLabel(
    status: QuizStatus,
    createdAt: Long,
    updatedAt: Long
): String {
    val isEdited = updatedAt > createdAt + 60_000L
    val targetTime = if (isEdited) updatedAt else if (createdAt > 0) createdAt else updatedAt
    val timeAgo = formatTimeAgo(targetTime)

    return when {
        status == QuizStatus.DRAFT -> if (isEdited) "Draft edited • $timeAgo" else "Draft • $timeAgo"
        isEdited -> "Edited • $timeAgo"
        else -> "Published • $timeAgo"
    }
}

@Composable
fun TeacherMyQuizzesScreen(
    quizzes: List<Quiz>,
    teacherUser: User? = null,
    submissions: List<QuizSessionState> = emptyList(),
    onBackClick: () -> Unit = {},
    onCreateQuizClick: () -> Unit,
    onEditQuizClick: (String) -> Unit = {},
    onPublishQuizClick: (String) -> Unit = onEditQuizClick,
    onTestQuizClick: (String) -> Unit = {},
    onDeleteQuizClick: (Quiz) -> Unit,
    onDuplicateQuizClick: ((Quiz) -> Unit)? = null,
    onToggleQuizStatusClick: ((Quiz, com.example.onlinequizapp.domain.model.QuizStatus) -> Unit)? = null,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Teacher View Mode Dialog State
    var viewingQuiz by remember { mutableStateOf<Quiz?>(null) }

    // Search & Filter State
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<QuizStatus?>(null) } // null = All
    var selectedDifficultyFilter by remember { mutableStateOf<String?>(null) } // null = All
    var showFilterMenu by remember { mutableStateOf(false) }

    // Batches map for resolving batch names
    val allBatchesMap by BatchRepositoryImpl.instance.getAllBatchesFlow().collectAsState(initial = emptyMap())

    // Pure real data: mapped strictly from fetched domain quizzes
    val allQuizzes = remember(quizzes, submissions, allBatchesMap) {
        quizzes.map { q ->
            val attempts = submissions.count { it.quiz.id == q.id }
            val hasTimeLimit = q.config.timerMode == com.example.onlinequizapp.domain.model.TimerMode.COUNTDOWN && q.config.timeLimitSeconds > 0
            val formattedDuration = formatQuizDuration(q.config.timerMode, q.config.timeLimitSeconds)
            val resolvedBatchNames = q.targetBatchIds.mapNotNull { allBatchesMap[it]?.name?.takeIf { n -> n.isNotBlank() } }
            TeacherQuizItem(
                id = q.id,
                title = q.title,
                description = q.description.ifEmpty { "Created quiz with ${q.questions.size} questions." },
                category = q.category.ifEmpty { "General" },
                difficulty = q.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                questionCount = q.questions.size,
                durationMinutes = if (hasTimeLimit) maxOf(1, q.config.timeLimitSeconds / 60) else 0,
                hasTimeLimit = hasTimeLimit,
                formattedDuration = formattedDuration,
                attemptCount = attempts,
                status = when (q.status) {
                    com.example.onlinequizapp.domain.model.QuizStatus.PUBLISHED,
                    com.example.onlinequizapp.domain.model.QuizStatus.ACTIVE,
                    com.example.onlinequizapp.domain.model.QuizStatus.APPROVED -> QuizStatus.PUBLISHED
                    else -> QuizStatus.DRAFT
                },
                iconBgColor = getCategoryGradient(q.category),
                iconType = getCategoryIconType(q.category),
                quizCode = q.quizCode.ifBlank { null },
                logoDrawableRes = com.example.onlinequizapp.ui.components.SubjectLogoRegistry.getDrawableRes(q.icon),
                targetBatchIds = q.targetBatchIds,
                batchNames = resolvedBatchNames,
                targetStudentIds = q.targetStudentIds,
                availabilityStartMillis = q.availabilityStartMillis,
                availabilityEndMillis = q.availabilityEndMillis,
                createdAt = q.createdAt,
                updatedAt = q.updatedAt
            )
        }
    }

    // Filtered quizzes
    val filteredQuizzes = allQuizzes.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
        val matchesDifficulty = selectedDifficultyFilter == null || item.difficulty.equals(selectedDifficultyFilter, ignoreCase = true)
        matchesSearch && matchesStatus && matchesDifficulty
    }

    // Dynamic stats counts
    val totalQuizzesCount = allQuizzes.size
    val publishedCount = allQuizzes.count { it.status == QuizStatus.PUBLISHED }
    val draftCount = allQuizzes.count { it.status == QuizStatus.DRAFT }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg),
        contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Section: Top Bar + Hero Banner
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TeacherMyQuizzesTopBar(
                    teacherUser = teacherUser,
                    onBackClick = onBackClick,
                    onNotificationClick = onNotificationClick,
                    onProfileClick = onProfileClick
                )
                TeacherMyQuizzesHeroBanner(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // 3. Stat Cards Row (Total, Published, Draft)
        item {
            TeacherMyQuizzesStatsRow(
                total = totalQuizzesCount,
                published = publishedCount,
                draft = draftCount,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 4. Search and Filter Bar
        item {
            TeacherMyQuizzesSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                selectedDifficulty = selectedDifficultyFilter,
                showFilterMenu = showFilterMenu,
                onToggleFilterMenu = { showFilterMenu = !showFilterMenu },
                onSelectDifficulty = {
                    selectedDifficultyFilter = it
                    showFilterMenu = false
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 5. Status Filter Pills Row (All, Published, Draft, Archived)
        item {
            TeacherMyQuizzesFilterPills(
                selectedStatus = selectedStatusFilter,
                onSelectStatus = { selectedStatusFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 6. Quizzes List
        if (allQuizzes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, CardBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Quizzes Created Yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You haven't created any quizzes yet. Start building quizzes manually or generate them instantly with AI.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))))
                                .clickable { onCreateQuizClick() }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Your First Quiz",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        } else if (filteredQuizzes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No quizzes found matching your filter.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredQuizzes, key = { it.id }) { quizItem ->
                val orig = quizzes.find { it.id == quizItem.id }
                TeacherQuizCard(
                    quiz = quizItem,
                    onViewClick = {
                        if (orig != null) {
                            viewingQuiz = orig
                        }
                    },
                    onEditClick = { onEditQuizClick(quizItem.id) },
                    onPublishClick = { onPublishQuizClick(quizItem.id) },
                    onDeleteClick = {
                        if (orig != null) {
                            onDeleteQuizClick(orig)
                        }
                    },
                    onToggleStatus = { newUiStatus ->
                        if (orig != null) {
                            val domainStatus = if (newUiStatus == QuizStatus.PUBLISHED) {
                                com.example.onlinequizapp.domain.model.QuizStatus.PUBLISHED
                            } else {
                                com.example.onlinequizapp.domain.model.QuizStatus.DRAFT
                            }
                            onToggleQuizStatusClick?.invoke(orig, domainStatus)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    // Teacher Quiz View Mode Dialog
    viewingQuiz?.let { targetQuiz ->
        TeacherQuizViewDialog(
            quiz = targetQuiz,
            onDismiss = { viewingQuiz = null },
            onEditClick = {
                val qId = targetQuiz.id
                viewingQuiz = null
                onEditQuizClick(qId)
            },
            onPublishClick = {
                val qId = targetQuiz.id
                viewingQuiz = null
                onPublishQuizClick(qId)
            }
        )
    }
}

// -------------------------------------------------------------
// 1. Top Bar: Back Arrow, Brand Logo, Bell Badge, Avatar
// -------------------------------------------------------------
@Composable
private fun TeacherMyQuizzesTopBar(
    teacherUser: User?,
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // QuizMaster Brand Logo
        QuizMasterBrandTitle(
            fontSize = 22.sp,
            isDarkTheme = isTeacherInDarkTheme()
        )

        Spacer(modifier = Modifier.weight(1f))

        // Notification Bell with Red Badge Dot
        TeacherNotificationButton(
            onClick = onNotificationClick
        )
    }
}

// -------------------------------------------------------------
// 2. Hero Banner: "My Quizzes" & Tablet Illustration
// -------------------------------------------------------------
@Composable
private fun TeacherMyQuizzesHeroBanner(
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val heroBg = if (isDark) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF150A4E).copy(alpha = 0.95f),
                Color(0xFF0C1448).copy(alpha = 0.95f),
                Color(0xFF092055).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                Color(0xFFEFF3FF),
                Color(0xFFE0E7FF),
                Color(0xFFF5F3FF)
            )
        )
    }

    val heroBorder = if (isDark) {
        BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFC084FC),
                    Color(0xFF818CF8),
                    Color(0xFF38BDF8)
                )
            )
        )
    } else {
        BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF818CF8).copy(alpha = 0.6f),
                    Color(0xFFC7D2FE),
                    Color(0xFFE2E8F0)
                )
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(heroBg)
            .border(
                heroBorder.width,
                heroBorder.brush,
                RoundedCornerShape(22.dp)
            )
            .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glowing Document Icon Squircle
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                if (isDark) {
                                    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFA855F7))
                                } else {
                                    listOf(Color(0xFF6366F1), Color(0xFF4F46E5), Color(0xFF7C3AED))
                                }
                            )
                        )
                        .border(
                            1.dp,
                            if (isDark) Color(0xFFC084FC).copy(alpha = 0.6f) else Color(0xFFA5B4FC).copy(alpha = 0.8f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Texts
                Column {
                    Text(
                        text = "My Quizzes",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create, manage and track your quizzes.\nKeep learning. Keep growing.",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            // 3D Illustration: Checklist, Trophy, Pencil & Books
            Box(
                modifier = Modifier
                    .size(width = 135.dp, height = 96.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_my_quizzes_hero_decor),
                    contentDescription = "My Quizzes Hero Illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Stats Row: 3 equal cards (Total, Published, Draft)
// -------------------------------------------------------------
@Composable
private fun TeacherMyQuizzesStatsRow(
    total: Int,
    published: Int,
    draft: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatItemCard(
            count = total,
            label = "Total Quizzes",
            icon = Icons.Default.Description,
            iconBg = Brush.linearGradient(listOf(Color(0xFF818CF8), Color(0xFF6366F1))),
            borderGradient = listOf(Color(0xFF6366F1), Color(0xFF38BDF8), Color(0xFF1E2958)),
            arrowColor = Color(0xFF818CF8),
            modifier = Modifier.weight(1f)
        )
        StatItemCard(
            count = published,
            label = "Published",
            icon = Icons.Default.PlayArrow,
            iconBg = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            borderGradient = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF1E2958)),
            arrowColor = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )
        StatItemCard(
            count = draft,
            label = "Draft",
            icon = Icons.Default.AccessTime,
            iconBg = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
            borderGradient = listOf(Color(0xFFF59E0B), Color(0xFFEA580C), Color(0xFF1E2958)),
            arrowColor = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItemCard(
    count: Int,
    label: String,
    icon: ImageVector,
    iconBg: Brush,
    borderGradient: List<Color>,
    arrowColor: Color,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val cardBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF091136).copy(alpha = 0.95f),
                Color(0xFF060B24).copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White,
                Color(0xFFF8FAFC)
            )
        )
    }

    val cardBorder = if (isDark) {
        BorderStroke(1.2.dp, Brush.horizontalGradient(borderGradient))
    } else {
        BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(cardBorder.width, cardBorder.brush, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top Row: Icon squircle on left + Mini Arrow Button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0F1738) else Color(0xFFF1F5F9))
                        .border(1.dp, arrowColor.copy(alpha = if (isDark) 0.5f else 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = arrowColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Number
            Text(
                text = count.toString(),
                color = if (isDark) Color.White else Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Label
            Text(
                text = label,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -------------------------------------------------------------
// 4. Search and Filter Bar
// -------------------------------------------------------------
@Composable
private fun TeacherMyQuizzesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedDifficulty: String?,
    showFilterMenu: Boolean,
    onToggleFilterMenu: () -> Unit,
    onSelectDifficulty: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Box
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Search quizzes by title, subject or topic...",
                    color = TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = CardBorderColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PurplePrimary
            ),
            textStyle = TextStyle(fontSize = 13.sp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Filter Dropdown Button
        Box {
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardBg)
                    .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(14.dp))
                    .clickable { onToggleFilterMenu() }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filter",
                    tint = if (selectedDifficulty != null) PurplePrimary else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedDifficulty ?: "Filter",
                    color = if (selectedDifficulty != null) TextPrimary else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = onToggleFilterMenu,
                modifier = Modifier
                    .background(CardBg)
                    .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(8.dp))
            ) {
                DropdownMenuItem(
                    text = { Text("All Difficulties", color = TextPrimary, fontSize = 13.sp) },
                    onClick = { onSelectDifficulty(null) }
                )
                DropdownMenuItem(
                    text = { Text("Easy", color = Color(0xFF34D399), fontSize = 13.sp) },
                    onClick = { onSelectDifficulty("Easy") }
                )
                DropdownMenuItem(
                    text = { Text("Medium", color = Color(0xFFFBBF24), fontSize = 13.sp) },
                    onClick = { onSelectDifficulty("Medium") }
                )
                DropdownMenuItem(
                    text = { Text("Hard", color = Color(0xFFF87171), fontSize = 13.sp) },
                    onClick = { onSelectDifficulty("Hard") }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Status Filter Pills Row (All, Published, Draft, Archived)
// -------------------------------------------------------------
@Composable
private fun TeacherMyQuizzesFilterPills(
    selectedStatus: QuizStatus?,
    onSelectStatus: (QuizStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val isAllSelected = selectedStatus == null
    val isPublishedSelected = selectedStatus == QuizStatus.PUBLISHED
    val isDraftSelected = selectedStatus == QuizStatus.DRAFT

    val containerBg = if (isDark) Color(0xFF091030).copy(alpha = 0.9f) else Color(0xFFE2E8F0)
    val containerBorder = if (isDark) Color(0xFF1E2A58) else Color(0xFFCBD5E1)
    val unselectedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(containerBg)
            .border(
                1.2.dp,
                containerBorder,
                RoundedCornerShape(25.dp)
            )
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "All" Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(21.dp))
                    .background(
                        if (isAllSelected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelectStatus(null) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isAllSelected) Color.White else unselectedColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "All",
                        color = if (isAllSelected) Color.White else unselectedColor,
                        fontSize = 13.sp,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // "Published" Pill
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(21.dp))
                    .background(
                        if (isPublishedSelected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelectStatus(QuizStatus.PUBLISHED) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Text(
                        text = "Published",
                        color = if (isPublishedSelected) Color.White else unselectedColor,
                        fontSize = 13.sp,
                        fontWeight = if (isPublishedSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            // "Draft" Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(21.dp))
                    .background(
                        if (isDraftSelected) {
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFF38BDF8))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelectStatus(QuizStatus.DRAFT) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(Color(0xFFF59E0B), CircleShape)
                    )
                    Text(
                        text = "Draft",
                        color = if (isDraftSelected) Color.White else unselectedColor,
                        fontSize = 13.sp,
                        fontWeight = if (isDraftSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterPill(
    title: String,
    dotColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val pillBg = if (isSelected) {
        if (isDark) PurplePrimary.copy(alpha = 0.25f) else Color(0xFFEDE9FE)
    } else {
        CardBg
    }
    val pillBorder = if (isSelected) {
        PurplePrimary
    } else {
        CardBorderColor
    }
    val pillTextColor = if (isSelected) {
        if (isDark) Color.White else PurplePrimary
    } else {
        TextSecondary
    }

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(pillBg)
            .border(
                BorderStroke(if (isSelected) 1.5.dp else 1.dp, pillBorder),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = pillTextColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// -------------------------------------------------------------
// 6. Quiz Card: Replicating design with tags, stats, action buttons
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun TeacherQuizCard(
    quiz: TeacherQuizItem,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleStatus: (QuizStatus) -> Unit,
    onPublishClick: () -> Unit = onEditClick,
    onDuplicateClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMoreMenu by remember { mutableStateOf(false) }

    val isDark = isTeacherInDarkTheme()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Card Information Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 14.dp, end = 14.dp, bottom = 12.dp)
            ) {
                // Thematic Leading Icon Squircle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(quiz.iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (quiz.logoDrawableRes != null) {
                        Image(
                            painter = painterResource(id = quiz.logoDrawableRes),
                            contentDescription = quiz.title,
                            modifier = Modifier
                                .size(30.dp)
                                .padding(2.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        when (quiz.iconType) {
                            "CODE" -> {
                                Text(
                                    text = "</>",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            "REACT" -> {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "React",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            "ANDROID" -> {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = "Android",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            "CHEM" -> {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Chemistry",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            "HISTORY" -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "History",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            "MATH" -> {
                                Text(
                                    text = "1+2",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title, Subtitle, Badges, Metrics
                Column(modifier = Modifier.weight(1f)) {
                    // Title and Batch/Public Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = quiz.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        // Audience Badges (Only shown when quiz is PUBLISHED, never for DRAFT)
                        if (quiz.status != QuizStatus.DRAFT) {
                            if (quiz.targetBatchIds.isNotEmpty()) {
                                val batchBg = if (isDark) Color(0xFF4C1D95).copy(alpha = 0.35f) else Color(0xFFF3E8FF)
                                val batchText = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED)
                                val batchBorder = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color(0xFFDDD6FE)

                                if (quiz.batchNames.isNotEmpty()) {
                                    quiz.batchNames.forEach { bName ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(batchBg)
                                                .border(BorderStroke(1.dp, batchBorder), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = batchText,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = bName,
                                                    color = batchText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Target batches configured but names not yet resolved in map
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(batchBg)
                                            .border(BorderStroke(1.dp, batchBorder), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = batchText,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = "${quiz.targetBatchIds.size} Batches",
                                                color = batchText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            } else if (quiz.targetStudentIds.isNotEmpty()) {
                                val studBg = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                                val studText = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                                val studBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFFC7D2FE)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(studBg)
                                        .border(BorderStroke(1.dp, studBorder), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = studText,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "${quiz.targetStudentIds.size} Student${if (quiz.targetStudentIds.size > 1) "s" else ""}",
                                            color = studText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                // Public for all badge
                                val pubBg = if (isDark) Color(0xFF0369A1).copy(alpha = 0.25f) else Color(0xFFE0F2FE)
                                val pubText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                val pubBorder = if (isDark) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFFBAE6FD)

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(pubBg)
                                        .border(BorderStroke(1.dp, pubBorder), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Public,
                                            contentDescription = null,
                                            tint = pubText,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = "Public for all",
                                            color = pubText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Description
                    Text(
                        text = quiz.description,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Tags: Category and Difficulty
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Category Tag
                        val catBg = if (isDark) Color(0xFF2563EB).copy(alpha = 0.2f) else Color(0xFFEFF6FF)
                        val catText = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                        val catBorder = if (isDark) Color(0xFF1D4ED8).copy(alpha = 0.4f) else Color(0xFFBFDBFE)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(catBg)
                                .border(BorderStroke(1.dp, catBorder), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = quiz.category,
                                color = catText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Difficulty Tag
                        val (diffBg, diffColor, diffBorder) = when (quiz.difficulty.lowercase()) {
                            "easy" -> if (isDark) Triple(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399), Color(0xFF059669).copy(alpha = 0.4f))
                                      else Triple(Color(0xFFECFDF5), Color(0xFF059669), Color(0xFFA7F3D0))
                            "medium" -> if (isDark) Triple(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFFBBF24), Color(0xFFD97706).copy(alpha = 0.4f))
                                        else Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Color(0xFFFDE68A))
                            else -> if (isDark) Triple(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFF87171), Color(0xFFDC2626).copy(alpha = 0.4f))
                                    else Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Color(0xFFFECACA))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(diffBg)
                                .border(BorderStroke(1.dp, diffBorder), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = quiz.difficulty,
                                color = diffColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stats: Questions • Time • Attempts
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Questions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${quiz.questionCount} Questions",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Duration
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = quiz.formattedDuration,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }

                        // Attempts (Only shown for published quizzes, never for draft)
                        if (quiz.status != QuizStatus.DRAFT) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${quiz.attemptCount} Attempts",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Scheduled Window Badge (if configured, never for draft)
                    val hasWindow = (quiz.availabilityStartMillis != null || quiz.availabilityEndMillis != null) && quiz.status != QuizStatus.DRAFT
                    if (hasWindow) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val now = System.currentTimeMillis()
                        val isUpcoming = quiz.availabilityStartMillis?.let { now < it } == true
                        val isExpired = quiz.availabilityEndMillis?.let { now > it } == true
                        val sdf = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault())

                        val (winBg, winBorder, winColor, winText) = when {
                            isUpcoming -> {
                                val sStr = quiz.availabilityStartMillis?.let { sdf.format(java.util.Date(it)) } ?: ""
                                QuadrupleVal(
                                    if (isDark) Color(0x2838BDF8) else Color(0xFFF0F9FF),
                                    if (isDark) Color(0x5538BDF8) else Color(0xFFBAE6FD),
                                    if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                    "Opens $sStr"
                                )
                            }
                            isExpired -> {
                                QuadrupleVal(
                                    if (isDark) Color(0x28EF4444) else Color(0xFFFEF2F2),
                                    if (isDark) Color(0x55EF4444) else Color(0xFFFECACA),
                                    if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                                    "Window Closed"
                                )
                            }
                            else -> {
                                val eStr = quiz.availabilityEndMillis?.let { sdf.format(java.util.Date(it)) } ?: ""
                                QuadrupleVal(
                                    if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB),
                                    if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A),
                                    if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                    if (eStr.isNotEmpty()) "Deadline $eStr" else "Scheduled Window"
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(winBg)
                                .border(0.6.dp, winBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = winColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = winText,
                                style = TextStyle(
                                    color = winColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Top Right Section: Status Badge & Code Capsule
                Column(horizontalAlignment = Alignment.End) {
                    val clipboardManager = LocalClipboardManager.current
                    val context = LocalContext.current

                    // Status Badge fixed to the far right
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(quiz.status.getBadgeBg())
                            .border(BorderStroke(1.dp, quiz.status.getBorderColor()), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 3.5.dp)
                    ) {
                        Text(
                            text = quiz.status.displayName,
                            color = quiz.status.getBadgeColor(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Dynamic timestamp label (e.g., "Draft • 2h ago" or "Published • Yesterday")
                    val timeLabel = getQuizLatestTimeLabel(quiz.status, quiz.createdAt, quiz.updatedAt)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = timeLabel,
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )

                    // Join Code Capsule (increased dimensions by 2 to 4 dp)
                    if (!quiz.quizCode.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.6f) else Color(0xFFEEF2FF))
                                .border(
                                    BorderStroke(1.dp, if (isDark) Color(0xFF4338CA).copy(alpha = 0.8f) else Color(0xFFC7D2FE)),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(quiz.quizCode))
                                    Toast.makeText(context, "Quiz Code ${quiz.quizCode} copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 11.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Code",
                                tint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = quiz.quizCode,
                                color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Bottom Actions Bar (3 Pill Buttons: View, Edit, More - Duplicate removed)
            val actionsBarBg = if (isDark) Color(0xFF060B24) else Color(0xFFF8FAFC)
            val actionsBarBorder = if (isDark) Color(0xFF121B42) else Color(0xFFE2E8F0)
            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(actionsBarBg)
                    .border(BorderStroke(0.5.dp, actionsBarBorder))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. View Button
                    QuizActionPill(
                        label = "View",
                        icon = Icons.Default.Visibility,
                        onClick = onViewClick,
                        modifier = Modifier.weight(1f)
                    )

                    // 2. Edit Button
                    QuizActionPill(
                        label = "Edit",
                        icon = Icons.Default.Edit,
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    )

                    // 3. More Button with Dropdown Menu
                    Box(modifier = Modifier.weight(1f)) {
                        QuizActionPill(
                            label = "More",
                            icon = Icons.Default.MoreHoriz,
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier
                                .background(CardBg)
                                .border(BorderStroke(1.dp, CardBorderColor), RoundedCornerShape(8.dp))
                        ) {
                            if (!quiz.quizCode.isNullOrBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Copy Code (${quiz.quizCode})", color = TextPrimary, fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color(0xFF818CF8)) },
                                    onClick = {
                                        showMoreMenu = false
                                        clipboardManager.setText(AnnotatedString(quiz.quizCode))
                                        Toast.makeText(context, "Quiz Code ${quiz.quizCode} copied!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (quiz.status == QuizStatus.PUBLISHED) "Move to Draft" else "Publish",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF34D399)) },
                                onClick = {
                                    showMoreMenu = false
                                    if (quiz.status == QuizStatus.DRAFT) {
                                        onPublishClick()
                                    } else {
                                        onToggleStatus(QuizStatus.DRAFT)
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Quiz", color = Color(0xFFEF4444), fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Close, null, tint = Color(0xFFEF4444)) },
                                onClick = {
                                    showMoreMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizActionPill(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val pillBg = if (isDark) Color(0xFF0B1437) else Color(0xFFFFFFFF)
    val pillBorder = if (isDark) Color(0xFF1B2856) else Color(0xFFE2E8F0)
    val pillText = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val pillIconTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(pillBg)
            .border(BorderStroke(1.dp, pillBorder), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = pillIconTint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = pillText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private data class QuadrupleVal<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

// -------------------------------------------------------------
// 7. Teacher Quiz View Mode Dialog
// -------------------------------------------------------------
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TeacherQuizViewDialog(
    quiz: Quiz,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onPublishClick: (() -> Unit)? = null
) {
    val isDark = isTeacherInDarkTheme()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val dialogBg = if (isDark) Color(0xFF090F2C) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E2958) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val textMuted = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val cardBg = if (isDark) Color(0xFF0F173D) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF223068) else Color(0xFFE2E8F0)

    val logoRes = com.example.onlinequizapp.ui.components.SubjectLogoRegistry.getDrawableRes(quiz.icon)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = dialogBg),
                border = BorderStroke(1.dp, dialogBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Top Header Row (Logo, Title, Badges, Close Button)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo Squircle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (logoRes != null) {
                                Image(
                                    painter = painterResource(id = logoRes),
                                    contentDescription = quiz.title,
                                    modifier = Modifier.size(32.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Text(
                                    text = quiz.category.take(2).uppercase().ifBlank { "QZ" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quiz.title,
                                color = textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val allBatchesMap by BatchRepositoryImpl.instance.getAllBatchesFlow().collectAsState(initial = emptyMap())
                            val resolvedBatchNames = quiz.targetBatchIds.mapNotNull { allBatchesMap[it]?.name?.takeIf { n -> n.isNotBlank() } }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Category Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Color(0xFFEFF6FF))
                                        .border(0.8.dp, if (isDark) Color(0xFF3B82F6).copy(alpha = 0.6f) else Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = quiz.category,
                                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Difficulty Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isDark) Color(0xFF065F46).copy(alpha = 0.4f) else Color(0xFFECFDF5))
                                        .border(0.8.dp, if (isDark) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFFA7F3D0), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = quiz.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Status Pill
                                val isPublished = quiz.status == com.example.onlinequizapp.domain.model.QuizStatus.PUBLISHED
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isPublished) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                                        .border(0.8.dp, if (isPublished) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isPublished) "Published" else "Draft",
                                        color = if (isPublished) Color(0xFF34D399) else Color(0xFFFBBF24),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Audience Badges (Only shown when quiz is PUBLISHED, never for DRAFT)
                                if (isPublished) {
                                    if (quiz.targetBatchIds.isNotEmpty()) {
                                        val batchBg = if (isDark) Color(0xFF4C1D95).copy(alpha = 0.35f) else Color(0xFFF3E8FF)
                                        val batchText = if (isDark) Color(0xFFC4B5FD) else Color(0xFF7C3AED)
                                        val batchBorder = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color(0xFFDDD6FE)

                                        if (resolvedBatchNames.isNotEmpty()) {
                                            resolvedBatchNames.forEach { bName ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(batchBg)
                                                        .border(BorderStroke(0.8.dp, batchBorder), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.School,
                                                            contentDescription = null,
                                                            tint = batchText,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Text(
                                                            text = bName,
                                                            color = batchText,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(batchBg)
                                                    .border(BorderStroke(0.8.dp, batchBorder), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.School,
                                                        contentDescription = null,
                                                        tint = batchText,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Text(
                                                        text = "${quiz.targetBatchIds.size} Batches",
                                                        color = batchText,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    } else if (quiz.targetStudentIds.isNotEmpty()) {
                                        val studBg = if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF)
                                        val studText = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
                                        val studBorder = if (isDark) Color(0xFF6366F1).copy(alpha = 0.5f) else Color(0xFFC7D2FE)

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(studBg)
                                                .border(BorderStroke(0.8.dp, studBorder), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = studText,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "${quiz.targetStudentIds.size} Student${if (quiz.targetStudentIds.size > 1) "s" else ""}",
                                                    color = studText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    } else {
                                        val pubBg = if (isDark) Color(0xFF0369A1).copy(alpha = 0.25f) else Color(0xFFE0F2FE)
                                        val pubText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                        val pubBorder = if (isDark) Color(0xFF0284C7).copy(alpha = 0.5f) else Color(0xFFBAE6FD)

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(pubBg)
                                                .border(BorderStroke(0.8.dp, pubBorder), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = null,
                                                    tint = pubText,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "Public for all",
                                                    color = pubText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(dialogBorder)
                    )

                    // 2. Scrollable Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(20.dp)
                    ) {
                        // Description
                        if (quiz.description.isNotBlank()) {
                            Text(
                                text = quiz.description,
                                color = textSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Join Code Banner (if present)
                        if (quiz.quizCode.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF1E1B4B) else Color(0xFFEEF2FF))
                                    .border(1.dp, if (isDark) Color(0xFF4F46E5) else Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "QUIZ ACCESS CODE",
                                        color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = quiz.quizCode,
                                        color = if (isDark) Color.White else Color(0xFF312E81),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDark) Color(0xFF4338CA) else Color(0xFF6366F1))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(quiz.quizCode))
                                            Toast.makeText(context, "Code ${quiz.quizCode} copied!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy Code",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Metrics Cards Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Questions
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(text = "Questions", color = textMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${quiz.questions.size}", color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Duration
                            val durationText = formatQuizDuration(quiz.config.timerMode, quiz.config.timeLimitSeconds)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(text = "Duration", color = textMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = durationText, color = textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Hints
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(text = "Hints", color = textMuted, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (quiz.config.allowHints) "Allowed" else "Off",
                                        color = textPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Availability Window (if configured, never for draft)
                        val hasWindow = (quiz.availabilityStartMillis != null || quiz.availabilityEndMillis != null) && quiz.status != com.example.onlinequizapp.domain.model.QuizStatus.DRAFT
                        if (hasWindow) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                            val startStr = quiz.availabilityStartMillis?.let { sdf.format(java.util.Date(it)) } ?: "Anytime"
                            val endStr = quiz.availabilityEndMillis?.let { sdf.format(java.util.Date(it)) } ?: "No deadline"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                                    .border(0.8.dp, cardBorder, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Window: $startStr → $endStr",
                                    color = textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Section Title: Questions & Answer Key
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Questions & Answer Key (${quiz.questions.size})",
                                color = textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Teacher View",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // List of Questions
                        quiz.questions.forEachIndexed { qIdx, question ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, cardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Question Header (Q#, Type)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Question ${qIdx + 1}",
                                            color = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = when (question.type) {
                                                    com.example.onlinequizapp.domain.model.QuestionType.SINGLE -> "Single Choice"
                                                    com.example.onlinequizapp.domain.model.QuestionType.MULTIPLE -> "Multi-Select"
                                                    else -> "Mixed"
                                                },
                                                color = textMuted,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Prompt
                                    Text(
                                        text = question.prompt,
                                        color = textPrimary,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 19.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Options
                                    question.options.forEach { option ->
                                        val isCorrect = question.correctAnswers.contains(option.id)
                                        val optBg = if (isCorrect) {
                                            if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)
                                        } else {
                                            if (isDark) Color(0xFF0B1437) else Color(0xFFFFFFFF)
                                        }
                                        val optBorder = if (isCorrect) {
                                            if (isDark) Color(0xFF10B981) else Color(0xFF34D399)
                                        } else {
                                            if (isDark) Color(0xFF1B2856) else Color(0xFFE2E8F0)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(optBg)
                                                .border(BorderStroke(if (isCorrect) 1.2.dp else 0.8.dp, optBorder), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Option ID letter circle
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isCorrect) Color(0xFF10B981) else if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = option.id,
                                                    color = if (isCorrect) Color.White else textSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Text(
                                                text = option.text,
                                                color = if (isCorrect) (if (isDark) Color(0xFFA7F3D0) else Color(0xFF065F46)) else textPrimary,
                                                fontSize = 12.5.sp,
                                                fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (isCorrect) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Correct",
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Explanation Box (if present)
                                    if (question.explanation.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isDark) Color(0xFF1E3A8A).copy(alpha = 0.2f) else Color(0xFFEFF6FF))
                                                .border(0.6.dp, if (isDark) Color(0xFF3B82F6).copy(alpha = 0.3f) else Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "💡 Explanation: ${question.explanation}",
                                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Bottom Action Bar (Close & Edit Quiz Buttons)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dialogBg)
                            .border(BorderStroke(1.dp, dialogBorder))
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Close Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Color(0xFF131B45) else Color(0xFFF1F5F9))
                                    .border(1.dp, if (isDark) Color(0xFF243370) else Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                    .clickable { onDismiss() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Close",
                                    color = textPrimary,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Edit Quiz Button
                            val isDraftQuiz = quiz.status == com.example.onlinequizapp.domain.model.QuizStatus.DRAFT
                            val editBgBrush: Brush = if (isDraftQuiz && onPublishClick != null) {
                                androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0xFF1E2958) else Color(0xFFE2E8F0))
                            } else {
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(editBgBrush)
                                    .clickable { onEditClick() }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = if (isDraftQuiz && onPublishClick != null) textPrimary else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Quiz",
                                        color = if (isDraftQuiz && onPublishClick != null) textPrimary else Color.White,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (isDraftQuiz && onPublishClick != null) {
                                // Publish Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF10B981), Color(0xFF059669))
                                            )
                                        )
                                        .clickable { onPublishClick() }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Publish",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Publish",
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

