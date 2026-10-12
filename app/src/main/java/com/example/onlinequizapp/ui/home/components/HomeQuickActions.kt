package com.example.onlinequizapp.ui.home.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.onlinequizapp.ui.home.DarkCardBorder
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.TextWhite

@Composable
internal fun HomeQuickActionsRow(
    onTakeQuizClick: () -> Unit,
    onViewProgressClick: () -> Unit,
    onCertificatesClick: () -> Unit,
    onLeaderboardClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionCard(
            title = "Take Quiz",
            icon = Icons.Default.SportsEsports,
            accentColor = Color(0xFFA78BFA),
            onClick = onTakeQuizClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "View Progress",
            icon = Icons.Default.BarChart,
            accentColor = Color(0xFF2DD4BF),
            onClick = onViewProgressClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Certificates",
            icon = Icons.Default.WorkspacePremium,
            accentColor = Color(0xFFF472B6),
            onClick = onCertificatesClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            title = "Leaderboard",
            icon = Icons.Default.EmojiEvents,
            accentColor = Color(0xFFFBBF24),
            onClick = onLeaderboardClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCardSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Tiny Arrow Button
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = TextStyle(
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                maxLines = 1
            )
        }
    }
}
