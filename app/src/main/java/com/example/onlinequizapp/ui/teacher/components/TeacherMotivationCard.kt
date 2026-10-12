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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// Component: Motivation / Performance Card ("Keep Going!")
// -------------------------------------------------------------
@Composable
internal fun TeacherMotivationCard(
    onViewPerformanceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()
    val motivationBg = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0D1742), Color(0xFF080F30))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFEDE9FE), Color(0xFFF3E8FF))
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(motivationBg)
            .border(1.dp, if (isDark) Color(0xFF1E2B64) else Color(0xFFDDD6FE), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Trophy graphic with sparkles
            Box(
                modifier = Modifier.size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 38.sp
                )
            }

            Text(
                text = "Keep Going!",
                color = TeacherTextWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )

            Text(
                text = "Your efforts are making a difference. More students are learning every day.",
                color = TeacherTextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            // Button: View Performance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                        )
                    )
                    .clickable(onClick = onViewPerformanceClick)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "View Performance",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
