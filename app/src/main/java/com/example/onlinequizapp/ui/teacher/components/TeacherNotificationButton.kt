package com.example.onlinequizapp.ui.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.onlinequizapp.data.repository.NotificationRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager

/**
 * Standardized Top-Right Notification Button for all Teacher Pages
 * (TeacherHome, TeacherMyQuizzes, TeacherStudents, TeacherProfile)
 */
@Composable
fun TeacherNotificationButton(
    onClick: () -> Unit,
    hasUnread: Boolean? = null,
    modifier: Modifier = Modifier
) {
    val currentUser = remember { AuthSessionManager.getSavedUser() }
    val teacherId = currentUser?.id ?: ""
    val repoUnreadCount by NotificationRepositoryImpl.instance
        .getUnreadCountFlow(teacherId)
        .collectAsState(initial = 0)

    val effectiveHasUnread = hasUnread ?: (repoUnreadCount > 0)
    val isDark = isTeacherInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = TeacherTextWhite,
                modifier = Modifier.size(20.dp)
            )
            if (effectiveHasUnread) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .border(1.5.dp, bgColor, CircleShape)
                )
            }
        }
    }
}
