package com.example.onlinequizapp.ui.teacher.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Groups
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

// -------------------------------------------------------------
// Component: Teacher Bottom Navigation Bar
// -------------------------------------------------------------
@Composable
internal fun TeacherBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF05091F) else Color(0xFFFFFFFF))
            .border(width = 1.dp, color = if (isDark) Color(0xFF121B42) else Color(0xFFE2E8F0))
            .navigationBarsPadding()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeacherBottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TeacherBottomNavItem(
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = "My Quizzes",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            TeacherBottomNavItem(
                icon = Icons.Default.Groups,
                label = "Students",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
            TeacherBottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun TeacherBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme()
    val activeColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
    val inactiveColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = TextStyle(
                color = if (isSelected) activeColor else inactiveColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp
            ),
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) Color(0xFF818CF8) else Color.Transparent)
        )
    }
}
