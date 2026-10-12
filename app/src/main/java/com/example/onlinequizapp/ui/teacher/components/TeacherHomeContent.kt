package com.example.onlinequizapp.ui.teacher.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.onlinequizapp.data.repository.BatchRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.teacher.TeacherBg
import com.example.onlinequizapp.ui.teacher.TeacherBrandGradient
import com.example.onlinequizapp.ui.teacher.TeacherDashboardUiState
import com.example.onlinequizapp.ui.teacher.TeacherTextDim
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherQuizViewDialog
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// 1. Teacher Home Tab Content (Exact Match to Screenshot)
// -------------------------------------------------------------
@Composable
internal fun TeacherHomeContent(
    uiState: TeacherDashboardUiState,
    onCreateQuizClick: () -> Unit,
    onTestQuizClick: (String) -> Unit,
    onManageStudentsClick: () -> Unit,
    onViewAnalyticsClick: () -> Unit,
    onCertificatesClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRequestDeleteQuiz: (Quiz) -> Unit,
    onJoinRequestsClick: () -> Unit = {},
    onEditQuizClick: (String) -> Unit = {},
    onPublishQuizClick: (String) -> Unit = onEditQuizClick,
    onViewAllQuizzesClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val teacherName = uiState.teacherUser?.name?.takeIf { it.isNotBlank() } ?: "Teacher"
    val teacherId = uiState.teacherUser?.id ?: (AuthSessionManager.getSavedUser()?.id ?: "")

    var viewingQuiz by remember { mutableStateOf<Quiz?>(null) }

    val joinRequestsFlow = remember(teacherId) {
        BatchRepositoryImpl.instance.getJoinRequestsForTeacher(teacherId)
    }
    val joinRequests by joinRequestsFlow.collectAsState(initial = emptyList())
    val pendingRequestsCount = remember(joinRequests) {
        joinRequests.count { it.isPending }
    }

    LaunchedEffect(teacherId) {
        BatchRepositoryImpl.instance.refreshJoinRequests(teacherId)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // --- 1. Header: Logo, Notifications, Profile ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo & App Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    QuizMasterBrandTitle(
                        fontSize = 22.sp,
                        isDarkTheme = isDark
                    )
                }

                // Actions: Teacher Badge, Bell & Profile Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Teacher Role Badge (placed before notification icon like Student Home Page)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isDark) Color(0xFF0F173C) else Color(0xFFEDE9FE))
                            .border(1.dp, if (isDark) Color(0xFF1E2858) else Color(0xFFDDD6FE), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "👨‍🏫 Teacher",
                            style = TextStyle(
                                color = if (isDark) Color.White else Color(0xFF6D28D9),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Standardized notification button with red indicator dot
                    TeacherNotificationButton(
                        onClick = onNotificationClick
                    )
                }
            }
        }

        // --- 2. Hero Card with Teacher Illustration ---
        item {
            val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
            val greetingPrefix = when {
                currentHour < 12 -> "Good Morning,"
                currentHour < 17 -> "Good Afternoon,"
                else -> "Good Evening,"
            }

            val heroBg = if (isDark) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0C143B), // Deep royal navy
                        Color(0xFF131D56), // Midnight indigo
                        Color(0xFF090D2A)  // Deep shadow navy
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEFF3FF), // Soft icy indigo
                        Color(0xFFE0E7FF), // Lavender blue mist
                        Color(0xFFF5F3FF)  // Pale violet shimmer
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            }

            val heroBorder = if (isDark) {
                BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF4F46E5).copy(alpha = 0.7f),
                            Color(0xFF2563EB).copy(alpha = 0.35f),
                            Color(0xFF38BDF8).copy(alpha = 0.25f)
                        )
                    )
                )
            } else {
                BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF818CF8).copy(alpha = 0.6f),
                            Color(0xFFC7D2FE),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(heroBg)
                    .border(heroBorder, RoundedCornerShape(26.dp))
            ) {
                // Ambient lighting halo behind the 3D illustration
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(175.dp)
                        .offset(x = 15.dp, y = 8.dp)
                        .background(
                            Brush.radialGradient(
                                colors = if (isDark) listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.32f),
                                    Color(0xFF3B82F6).copy(alpha = 0.14f),
                                    Color.Transparent
                                ) else listOf(
                                    Color(0xFF818CF8).copy(alpha = 0.35f),
                                    Color(0xFFC7D2FE).copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, top = 16.dp, bottom = 16.dp, end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column (Greeting Text & Badges)
                    Column(
                        modifier = Modifier
                            .weight(1.15f)
                            .padding(end = 6.dp)
                    ) {
                        // Badge Pill Tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isDark) Color(0xFF1E2868).copy(alpha = 0.65f) else Color(0xFFE0E7FF),
                            border = BorderStroke(
                                0.8.dp,
                                if (isDark) Color(0xFF60A5FA).copy(alpha = 0.4f) else Color(0xFFA5B4FC)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "TEACHER DASHBOARD",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF93C5FD) else Color(0xFF4338CA),
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }

                        Text(
                            text = greetingPrefix,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "$teacherName 👋",
                            color = TeacherTextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create. Manage. Inspire.",
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF4F46E5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Build better quizzes, track progress,\nand help your students grow.",
                            color = TeacherTextDim,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    // Right Column (3D Illustration - Fully shown without box cropping)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_teacher_hero),
                            contentDescription = "Teacher Illustration",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            alignment = Alignment.CenterEnd
                        )
                    }
                }
            }
        }

        // --- 2.5. Pending Batch Join Requests Notification Banner ---
        if (pendingRequestsCount > 0) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onJoinRequestsClick() },
                    color = if (isDark) Color(0xFF1E1338) else Color(0xFFF5F3FF),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF7C3AED).copy(alpha = 0.5f) else Color(0xFFDDD6FE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PendingActions,
                                    contentDescription = null,
                                    tint = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Pending Join Requests",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = if (isDark) Color.White else Color(0xFF0F172A)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                            .padding(horizontal = 7.dp, vertical = 1.5.dp)
                                    ) {
                                        Text(
                                            text = "$pendingRequestsCount new",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                                Text(
                                    text = "$pendingRequestsCount student${if (pendingRequestsCount > 1) "s" else ""} waiting for private batch approval",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF8B5CF6)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 3. Overview Stats Row (4 Cards) ---
        item {
            val totalStudentsCount = "${uiState.totalStudents}"
            val totalQuizzesCount = "${uiState.totalQuizzes}"
            val totalAttemptsCount = "${uiState.totalSubmissions}"
            val avgScoreVal = "${uiState.averageScore}%"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeacherMetricCard(
                    icon = Icons.Default.Groups,
                    iconBgColor = if (isDark) Color(0xFF7C3AED).copy(alpha = 0.25f) else Color(0xFFEDE9FE),
                    iconTintColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                    value = totalStudentsCount,
                    label = "Total Students",
                    modifier = Modifier.weight(1f)
                )

                TeacherMetricCard(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    iconBgColor = if (isDark) Color(0xFF2563EB).copy(alpha = 0.25f) else Color(0xFFEFF6FF),
                    iconTintColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                    value = totalQuizzesCount,
                    label = "Total Quizzes",
                    modifier = Modifier.weight(1f)
                )

                TeacherMetricCard(
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = if (isDark) Color(0xFF059669).copy(alpha = 0.25f) else Color(0xFFECFDF5),
                    iconTintColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669),
                    value = totalAttemptsCount,
                    label = "Total Attempts",
                    modifier = Modifier.weight(1f)
                )

                TeacherMetricCard(
                    icon = Icons.Default.Star,
                    iconBgColor = if (isDark) Color(0xFFD97706).copy(alpha = 0.25f) else Color(0xFFFEF3C7),
                    iconTintColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                    value = avgScoreVal,
                    label = "Avg. Score",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 4. Quick Actions Header & Cards ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Actions",
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCreateQuizClick() }
                    ) {
                        Text(
                            text = "View All",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 2x2 Grid: 4 Quick Actions Fixed on Screen
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Create Quiz & Manage Students
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "Create Quiz",
                            subtitle = "Build a new quiz",
                            icon = Icons.Default.Add,
                            gradient = Brush.verticalGradient(
                                listOf(Color(0xFF3B0764), Color(0xFF1E1038))
                            ),
                            borderColor = Color(0xFF7C3AED),
                            accentColor = Color(0xFF8B5CF6),
                            onClick = onCreateQuizClick,
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Manage Students",
                            subtitle = "View & track students",
                            icon = Icons.Default.Groups,
                            gradient = Brush.verticalGradient(
                                listOf(Color(0xFF0C4A6E), Color(0xFF09233A))
                            ),
                            borderColor = Color(0xFF0284C7),
                            accentColor = Color(0xFF38BDF8),
                            onClick = onManageStudentsClick,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: View Analytics & Certificates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionCard(
                            title = "View Analytics",
                            subtitle = "Check performance",
                            icon = Icons.Default.BarChart,
                            gradient = Brush.verticalGradient(
                                listOf(Color(0xFF064E3B), Color(0xFF05241C))
                            ),
                            borderColor = Color(0xFF059669),
                            accentColor = Color(0xFF34D399),
                            onClick = onViewAnalyticsClick,
                            modifier = Modifier.weight(1f)
                        )

                        QuickActionCard(
                            title = "Certificates",
                            subtitle = "Issue & manage",
                            icon = Icons.Default.CardMembership,
                            gradient = Brush.verticalGradient(
                                listOf(Color(0xFF831843), Color(0xFF34071B))
                            ),
                            borderColor = Color(0xFFDB2777),
                            accentColor = Color(0xFFF472B6),
                            onClick = onCertificatesClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- 5. Recent Quizzes Header & List ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Quizzes",
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onViewAllQuizzesClick() }
                    ) {
                        val viewAllColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)
                        Text(
                            text = "View All",
                            color = viewAllColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = viewAllColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (uiState.quizzes.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.quizzes.take(4).forEach { quiz ->
                            TeacherRecentQuizCard(
                                quiz = quiz,
                                onClick = { viewingQuiz = quiz },
                                onDeleteClick = { onRequestDeleteQuiz(quiz) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF0A122E) else Color.White)
                            .border(1.dp, if (isDark) Color(0xFF1E2858) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Assignment,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No quizzes created yet",
                                color = TeacherTextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Tap '+ Create Quiz' above to build and publish your first quiz.",
                                color = TeacherTextDim,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // --- 6. Bottom Split Row: Recent Activity & Motivation Card ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left: Recent Activity (pure real data)
                TeacherActivityCard(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    onViewAllClick = onManageStudentsClick,
                    submissions = uiState.submissions,
                    quizzes = uiState.quizzes
                )

                // Right: Motivation ("Keep Going!")
                TeacherMotivationCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onViewPerformanceClick = onViewAnalyticsClick
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
