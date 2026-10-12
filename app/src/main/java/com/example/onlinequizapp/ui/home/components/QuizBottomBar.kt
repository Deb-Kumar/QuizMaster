package com.example.onlinequizapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.home.NavBorder

@Composable
internal fun QuizBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    hasProfileBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkCardSurface)
            .border(width = 1.dp, color = NavBorder)
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuizBottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            QuizBottomNavItem(
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = "My Quizzes",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            QuizBottomNavItem(
                icon = Icons.Default.History,
                label = "History",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
            QuizBottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = selectedTab == 3,
                hasBadge = hasProfileBadge,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuizBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    hasBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val activeColor = if (LocalQuizDark.current) Color(0xFFA78BFA) else Color(0xFF7C3AED)
    val inactiveColor = if (LocalQuizDark.current) Color(0xFF64748B) else Color(0xFF94A3B8)

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
            if (hasBadge) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = TextStyle(
                color = if (isSelected) activeColor else inactiveColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.5.sp
            ),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (isSelected) {
                        if (LocalQuizDark.current) Color(0xFF818CF8) else Color(0xFF7C3AED)
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}
