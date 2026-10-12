package com.example.onlinequizapp.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.data.repository.NotificationRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.AppNotification
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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

enum class NotificationFilter(val label: String, val icon: ImageVector) {
    ALL("All", Icons.Default.Widgets),
    UNREAD("Unread", Icons.Default.NotificationsActive),
    QUIZZES("Quizzes", Icons.Default.Quiz),
    AWARDS("Awards", Icons.Default.EmojiEvents)
}

enum class NotificationCategory {
    QUIZ,
    ACHIEVEMENT,
    RESULT,
    REMINDER,
    SYSTEM
}

data class StudentNotificationItem(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val category: NotificationCategory,
    val isRead: Boolean = false,
    val targetQuizId: String? = null
)

private fun AppNotification.toStudentItem(): StudentNotificationItem {
    val cat = when (category.uppercase()) {
        "QUIZ" -> NotificationCategory.QUIZ
        "ACHIEVEMENT" -> NotificationCategory.ACHIEVEMENT
        "RESULT" -> NotificationCategory.RESULT
        "REMINDER" -> NotificationCategory.REMINDER
        else -> NotificationCategory.SYSTEM
    }
    return StudentNotificationItem(
        id = id,
        title = title,
        description = description,
        timestamp = formatTimeAgo(),
        category = cat,
        isRead = isRead,
        targetQuizId = targetQuizId
    )
}

/**
 * Full page for viewing Student Notifications with a left arrow back button,
 * categorized filters, and interactive notification cards.
 *
 * Fully respects Android system bars (status bar & gesture/navigation bar insets)
 * to prevent overlaps with device clocks, battery indicators, or cutouts.
 */
@Composable
fun StudentNotificationsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onQuizClick: ((String) -> Unit)? = null
) {
    val isDark = isAppInDarkTheme()
    val scope = rememberCoroutineScope()
    val currentUser = remember { AuthSessionManager.getSavedUser() }
    val studentId = currentUser?.id ?: ""

    // Real-time notification flow observed from repository
    val repoNotifications by NotificationRepositoryImpl.instance
        .getNotificationsFlow(studentId)
        .collectAsState(initial = emptyList())

    val notifications = remember(repoNotifications) {
        repoNotifications.map { it.toStudentItem() }
    }

    var selectedFilter by remember { mutableStateOf(NotificationFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    val unreadCount = notifications.count { !it.isRead }

    val filteredList = notifications.filter { item ->
        val matchesFilter = when (selectedFilter) {
            NotificationFilter.ALL -> true
            NotificationFilter.UNREAD -> !item.isRead
            NotificationFilter.QUIZZES -> item.category == NotificationCategory.QUIZ || item.category == NotificationCategory.REMINDER
            NotificationFilter.AWARDS -> item.category == NotificationCategory.ACHIEVEMENT || item.category == NotificationCategory.RESULT
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true)
        }
        matchesFilter && matchesSearch
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Top Bar with Left Arrow Back Button (safely padded below status bar)
        NotificationTopBar(
            unreadCount = unreadCount,
            onBackClick = onBackClick,
            onMarkAllRead = {
                if (studentId.isNotBlank()) {
                    scope.launch {
                        NotificationRepositoryImpl.instance.markAllAsRead(studentId)
                    }
                }
            },
            onSearchToggle = { isSearchVisible = !isSearchVisible },
            isSearchActive = isSearchVisible
        )

        // 2. Animated Search Bar
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
                            "Search notifications...",
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
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = CardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Filter Chips Row
        NotificationFilterRow(
            selectedFilter = selectedFilter,
            unreadCount = unreadCount,
            onFilterSelected = { selectedFilter = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Notifications List or Caught-up Empty State
        if (filteredList.isEmpty()) {
            NotificationEmptyState(
                hasAny = notifications.isNotEmpty(),
                onBackClick = onBackClick,
                onResetFilter = {
                    selectedFilter = NotificationFilter.ALL
                    searchQuery = ""
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = filteredList,
                    key = { it.id }
                ) { item ->
                    NotificationCard(
                        item = item,
                        onClick = {
                            if (!item.isRead) {
                                scope.launch {
                                    NotificationRepositoryImpl.instance.markAsRead(item.id)
                                }
                            }
                            if (item.targetQuizId != null && onQuizClick != null) {
                                onQuizClick(item.targetQuizId)
                            }
                        },
                        onDismiss = {
                            scope.launch {
                                NotificationRepositoryImpl.instance.deleteNotification(item.id)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NotificationTopBar(
    unreadCount: Int,
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSearchToggle: () -> Unit,
    isSearchActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Arrow Back Button matching other pages (clean, no container box)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF818CF8), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$unreadCount new",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Updates, reminders & achievements",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Search icon button with circular background
        IconButton(
            onClick = onSearchToggle,
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSearchActive) Color(0xFF818CF8).copy(alpha = 0.2f) else CardBg,
                    shape = CircleShape
                )
                .border(
                    1.dp,
                    if (isSearchActive) Color(0xFF818CF8) else CardBorder,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isSearchActive) Color(0xFF818CF8) else TextPrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Mark all as read button with circular background
        IconButton(
            onClick = onMarkAllRead,
            modifier = Modifier
                .size(40.dp)
                .background(CardBg, shape = CircleShape)
                .border(1.dp, CardBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Mark all read",
                tint = Color(0xFF818CF8),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun NotificationFilterRow(
    selectedFilter: NotificationFilter,
    unreadCount: Int,
    onFilterSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationFilter.values().forEach { filter ->
            val isSelected = selectedFilter == filter
            val bg = if (isSelected) Color(0xFF818CF8) else CardBg
            val border = if (isSelected) Color(0xFF818CF8) else CardBorder
            val contentColor = if (isSelected) Color.White else TextSecondary

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(12.dp))
                    .clickable { onFilterSelected(filter) }
                    .padding(vertical = 9.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = filter.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = filter.label,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    item: StudentNotificationItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val visualStyle = getNotificationVisualStyle(item.category)
    val isUnread = !item.isRead
    val isDark = isAppInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isUnread) Color(0xFF818CF8).copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) {
                if (isDark) Color(0xFF0F183C) else Color(0xFFFFFFFF)
            } else {
                CardBg
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon with colored background
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(visualStyle.gradientBrush, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visualStyle.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF818CF8))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    fontSize = 12.5.sp,
                    color = if (isUnread) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Pill Tag
                        Box(
                            modifier = Modifier
                                .background(
                                    visualStyle.gradientColors.first().copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.category.name,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = visualStyle.gradientColors.first()
                            )
                        }

                        // Relative timestamp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = TextMutedDim,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.timestamp,
                                fontSize = 11.sp,
                                color = TextMutedDim
                            )
                        }
                    }

                    // Action: delete / dismiss single notification with padded touch target
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFFF1F5F9)
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationEmptyState(
    hasAny: Boolean,
    onBackClick: () -> Unit,
    onResetFilter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Soft circular container with Golden Bell illustration
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAppInDarkTheme()) Color(0xFF1E1E46) else Color(0xFFF3E8FF)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (hasAny) "No Matching Notifications" else "You're all caught up",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasAny) {
                    "Try changing your filter tabs or search query to see other notifications."
                } else {
                    "New quiz updates, scores, streak milestones, and announcements will show up here."
                },
                fontSize = 13.5.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (hasAny) {
                Button(
                    onClick = onResetFilter,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Show All Notifications", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onBackClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF818CF8)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Back to Home", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class NotificationVisualStyle(
    val icon: ImageVector,
    val gradientColors: List<Color>
) {
    val gradientBrush: Brush get() = Brush.linearGradient(gradientColors)
}

private fun getNotificationVisualStyle(category: NotificationCategory): NotificationVisualStyle {
    return when (category) {
        NotificationCategory.QUIZ -> NotificationVisualStyle(
            icon = Icons.Default.Quiz,
            gradientColors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
        )
        NotificationCategory.ACHIEVEMENT -> NotificationVisualStyle(
            icon = Icons.Default.LocalFireDepartment,
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
        )
        NotificationCategory.RESULT -> NotificationVisualStyle(
            icon = Icons.Default.CheckCircle,
            gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
        )
        NotificationCategory.REMINDER -> NotificationVisualStyle(
            icon = Icons.Default.AccessTime,
            gradientColors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
        )
        NotificationCategory.SYSTEM -> NotificationVisualStyle(
            icon = Icons.Default.Notifications,
            gradientColors = listOf(Color(0xFFEC4899), Color(0xFFBE185D))
        )
    }
}
