package com.example.onlinequizapp.ui.teacher.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherTextDim
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
internal fun TeacherProfileMenuItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    hasBadge: Boolean = false,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    val isDark = isTeacherInDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (hasBadge) {
                    Box(
                        modifier = Modifier
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .border(1.5.dp, if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    if (hasBadge && badgeText != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = badgeText,
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    color = TeacherTextMuted,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TeacherTextDim,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun TeacherMenuDivider() {
    val isDark = isTeacherInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(if (isDark) Color(0xFF16214C).copy(alpha = 0.8f) else Color(0xFFE2E8F0))
    )
}

@Composable
internal fun TeacherAchievementBadgeCard(
    icon: ImageVector,
    badgeColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val glassBg = if (isDark) {
        Brush.linearGradient(
            listOf(
                Color(0xFF1E293B).copy(alpha = 0.55f),
                Color(0xFF0F172A).copy(alpha = 0.45f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
        )
    }

    val glassBorder = if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        Color(0xFFCBD5E1).copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(glassBg)
            .border(1.dp, glassBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = if (isDark) 0.22f else 0.14f))
                    .border(1.dp, badgeColor.copy(alpha = if (isDark) 0.5f else 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                color = TeacherTextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                color = TeacherTextMuted,
                fontSize = 9.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun TeacherInfoCardItem(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                color = textSecondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
internal fun TeacherModalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}
