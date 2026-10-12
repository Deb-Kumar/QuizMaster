package com.example.onlinequizapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.repository.UserStats
import com.example.onlinequizapp.ui.home.DarkCardBorder
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.home.TextMuted
import com.example.onlinequizapp.ui.home.TextWhite

@Composable
internal fun HomeStatsCard(stats: UserStats) {
    val isDark = LocalQuizDark.current

    val quizzesColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
    val certificatesColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val avgScoreColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Metric 1: Quizzes Taken
            HomeStatMetric(
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = quizzesColor,
                iconBg = quizzesColor.copy(alpha = if (isDark) 0.16f else 0.12f),
                value = "${stats.quizzesTaken}",
                label = "Quizzes Taken",
                modifier = Modifier.weight(1f)
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(DarkCardBorder)
            )

            // Metric 2: Certificates
            HomeStatMetric(
                icon = Icons.Default.CardMembership,
                iconTint = certificatesColor,
                iconBg = certificatesColor.copy(alpha = if (isDark) 0.16f else 0.12f),
                value = "${stats.certificatesCount}",
                label = "Certificates",
                modifier = Modifier.weight(1f)
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(DarkCardBorder)
            )

            // Metric 3: Avg Score
            HomeStatMetric(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconTint = avgScoreColor,
                iconBg = avgScoreColor.copy(alpha = if (isDark) 0.16f else 0.12f),
                value = "${stats.averageScorePercentage}%",
                label = "Avg Score",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeStatMetric(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
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
                fontSize = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = TextStyle(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
    }
}
