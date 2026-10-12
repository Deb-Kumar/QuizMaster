package com.example.onlinequizapp.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.home.CardSurfaceAlt
import com.example.onlinequizapp.ui.home.DarkCardBorder
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.home.TextDim
import com.example.onlinequizapp.ui.home.TextMuted
import com.example.onlinequizapp.ui.home.TextWhite

@Composable
internal fun ProfileStatColumn(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = value,
            style = TextStyle(
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = TextStyle(
                color = TextMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
internal fun ProfileAchievementCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    badgeBg: Brush,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalQuizDark.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isDark) Color(0xFF0A1333).copy(alpha = 0.8f)
                else Color(0xFFF8FAFC)
            )
            .border(
                1.dp,
                if (isDark) Color(0xFF1B2856)
                else Color(0xFFE2E8F0),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Icon squircle badge matching reference screenshot
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .then(
                        if (isLocked && !isDark) {
                            Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(15.dp))
                        } else Modifier
                    )
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = TextStyle(
                    color = if (isLocked) (if (isDark) Color(0xFF64748B) else Color(0xFF64748B))
                            else (if (isDark) TextWhite else Color(0xFF0F172A)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = TextStyle(
                    color = if (isLocked) (if (isDark) Color(0xFF475569) else Color(0xFF94A3B8))
                            else (if (isDark) Color(0xFF8B9CB8) else Color(0xFF64748B)),
                    fontSize = 9.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun SettingsRowItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    hasBadge: Boolean = false,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Square with optional notification badge dot
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
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
                        .border(1.5.dp, DarkCardSurface, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Titles
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
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
                            style = TextStyle(
                                color = Color(0xFFF87171),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    color = TextMuted,
                    fontSize = 11.5.sp
                )
            )
        }

        // Chevron
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextDim,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
internal fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardSurfaceAlt)
    )
}

@Composable
internal fun InfoCardItem(
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
                    .background(iconTint.copy(alpha = 0.12f)),
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
internal fun ModalDivider(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}
