package com.example.onlinequizapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.components.QuizMasterBrandTitle
import com.example.onlinequizapp.ui.home.CardSurfaceAlt
import com.example.onlinequizapp.ui.home.InputBorderUnfocused
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.home.LogoGradient
import com.example.onlinequizapp.ui.home.TextWhite

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.onlinequizapp.data.repository.NotificationRepositoryImpl
import com.example.onlinequizapp.data.session.AuthSessionManager

@Composable
internal fun HomeTopBar(
    isTeacher: Boolean,
    onNotificationClick: () -> Unit
) {
    val currentUser = remember { AuthSessionManager.getSavedUser() }
    val userId = currentUser?.id ?: ""
    val unreadCount by NotificationRepositoryImpl.instance
        .getUnreadCountFlow(userId)
        .collectAsState(initial = 0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 0.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Brand Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(LogoGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "QuizMaster Logo",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            QuizMasterBrandTitle(
                isDarkTheme = LocalQuizDark.current
            )
        }

        // Right Actions: Role Badge + Notification Bell
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Role Pill
            val roleLabel = if (isTeacher) "👨‍🏫 Teacher" else "🎓 Student"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardSurfaceAlt)
                    .border(1.dp, InputBorderUnfocused, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = roleLabel,
                    style = TextStyle(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                )
            }

            // Notification Bell with Red Indicator Dot
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onNotificationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = if (LocalQuizDark.current) Color(0xFFCBD5E1) else Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )

                // Red Notification Dot
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-5).dp, y = 5.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                    )
                }
            }
        }
    }
}
