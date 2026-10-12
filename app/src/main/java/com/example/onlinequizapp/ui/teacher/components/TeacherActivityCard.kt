package com.example.onlinequizapp.ui.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherTextDim
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// Component: Recent Activity Card
// -------------------------------------------------------------
@Composable
internal fun TeacherActivityCard(
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    submissions: List<com.example.onlinequizapp.domain.model.QuizSessionState> = emptyList(),
    quizzes: List<com.example.onlinequizapp.domain.model.Quiz> = emptyList()
) {
    val isDark = isTeacherInDarkTheme()
    val bellBg = if (isDark) Color(0xFF818CF8).copy(alpha = 0.2f) else Color(0xFFEDE9FE)
    val bellTint = if (isDark) Color(0xFF818CF8) else Color(0xFF6D28D9)
    val viewAllColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF2563EB)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(TeacherCardBg)
            .border(1.dp, TeacherCardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(bellBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = bellTint,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Activity",
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
                Text(
                    text = "View All >",
                    color = viewAllColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            // Real dynamic activity items or clean empty state
            val hasActivities = submissions.isNotEmpty() || quizzes.isNotEmpty()
            if (hasActivities) {
                // Show real completed submissions first
                submissions.take(2).forEach { sub ->
                    ActivityTimelineItem(
                        icon = Icons.Default.Check,
                        iconBg = Color(0xFF059669),
                        title = "Quiz completed: \"${sub.quiz.title}\"",
                        subtitle = "${sub.scorePercentage}% score • ${sub.correctCount}/${sub.totalQuestions} correct"
                    )
                }

                // Show real created/published quizzes
                quizzes.take(1).forEach { quiz ->
                    ActivityTimelineItem(
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        iconBg = Color(0xFF7C3AED),
                        title = "Active quiz: \"${quiz.title}\"",
                        subtitle = "${quiz.questions.size} questions • ${quiz.category}"
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent activity yet.",
                        color = TeacherTextDim,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
internal fun ActivityTimelineItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = title,
                color = TeacherTextWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 2
            )
            Text(
                text = subtitle,
                color = TeacherTextDim,
                fontSize = 9.5.sp
            )
        }
    }
}
