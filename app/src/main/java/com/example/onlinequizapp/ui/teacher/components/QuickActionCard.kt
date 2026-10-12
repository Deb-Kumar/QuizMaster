package com.example.onlinequizapp.ui.teacher.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// Component: Quick Action Card (Adaptive for Light & Dark Themes)
// -------------------------------------------------------------
@Composable
internal fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    borderColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()

    val cardBg = if (isDark) {
        gradient
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White,
                accentColor.copy(alpha = 0.05f)
            )
        )
    }

    val cardBorder = if (isDark) {
        borderColor
    } else {
        accentColor.copy(alpha = 0.22f)
    }

    val iconBoxBg = if (isDark) {
        accentColor.copy(alpha = 0.35f)
    } else {
        accentColor.copy(alpha = 0.12f)
    }

    val iconBoxBorder = if (isDark) {
        accentColor
    } else {
        accentColor.copy(alpha = 0.25f)
    }

    val iconTint = if (isDark) {
        Color.White
    } else {
        accentColor
    }

    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF64748B)

    val arrowBg = if (isDark) accentColor else accentColor.copy(alpha = 0.14f)
    val arrowTint = if (isDark) Color.White else accentColor

    Box(
        modifier = modifier
            .height(116.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(if (isDark) 1.2.dp else 1.dp, cardBorder, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Squircle Icon on left + Arrow button on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBoxBg)
                        .border(1.dp, iconBoxBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(arrowBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = arrowTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Bottom Column: Title + Subtitle
            Column {
                Text(
                    text = title,
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = subtitleColor,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
