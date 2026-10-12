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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.home.DarkCardBorder
import com.example.onlinequizapp.ui.home.DarkCardSurface
import com.example.onlinequizapp.ui.home.TextDim
import com.example.onlinequizapp.ui.home.TextMuted
import com.example.onlinequizapp.ui.home.TextWhite

@Composable
internal fun DarkQuizCard(
    quiz: Quiz,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visuals = remember(quiz) { QuizVisualHelper.getVisuals(quiz) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCardSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Squircle Icon (Matches My Quizzes screen)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(visuals.gradientBrush),
                contentAlignment = Alignment.Center
            ) {
                QuizVisualHelper.QuizLogoEmblem(
                    visuals = visuals,
                    contentDescription = quiz.category,
                    iconSize = 28.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Quiz Details (Title, Questions Count, Difficulty Chip)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.title,
                    style = TextStyle(
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (quiz.questions.size == 1) "1 question" else "${quiz.questions.size} questions",
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )

                    val hasTimeLimit = quiz.config.timerMode == com.example.onlinequizapp.domain.model.TimerMode.COUNTDOWN && quiz.config.timeLimitSeconds > 0
                    val durationStr = if (hasTimeLimit) {
                        if (quiz.config.timeLimitSeconds % 60 == 0) "${quiz.config.timeLimitSeconds / 60} min"
                        else "${quiz.config.timeLimitSeconds / 60}m ${quiz.config.timeLimitSeconds % 60}s"
                    } else {
                        "Unlimited"
                    }
                    Text(
                        text = " • ",
                        style = TextStyle(
                            color = TextDim,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = durationStr,
                        style = TextStyle(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                    Text(
                        text = " • ",
                        style = TextStyle(
                            color = TextDim,
                            fontSize = 12.sp
                        )
                    )

                    // Difficulty Tag
                    val (badgeBg, badgeText) = when (quiz.difficulty) {
                        Difficulty.EASY -> Color(0x3310B981) to Color(0xFF34D399)
                        Difficulty.MEDIUM -> Color(0x33F59E0B) to Color(0xFFFBBF24)
                        Difficulty.HARD -> Color(0x33EF4444) to Color(0xFFF87171)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quiz.difficulty.label,
                            style = TextStyle(
                                color = badgeText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Availability Window Pill (if scheduled)
                val scheduleStatus = remember(quiz) { quiz.getScheduleStatus() }
                if (scheduleStatus != com.example.onlinequizapp.domain.model.QuizScheduleStatus.ALWAYS_AVAILABLE) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val (schedBg, schedBorder, schedText, schedIcon, schedLabel) = when (scheduleStatus) {
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.UPCOMING -> {
                            val timeStr = quiz.availabilityStartMillis?.let {
                                java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it))
                            } ?: "Soon"
                            Pentad(
                                Color(0x2638BDF8),
                                Color(0x5538BDF8),
                                Color(0xFF38BDF8),
                                androidx.compose.material.icons.Icons.Default.Schedule,
                                "Opens $timeStr"
                            )
                        }
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.ACTIVE_WITH_DEADLINE -> {
                            val timeStr = quiz.availabilityEndMillis?.let {
                                java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(it))
                            } ?: "Soon"
                            Pentad(
                                Color(0x26F59E0B),
                                Color(0x55F59E0B),
                                Color(0xFFFBBF24),
                                androidx.compose.material.icons.Icons.Default.Schedule,
                                "Closes $timeStr"
                            )
                        }
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED -> {
                            Pentad(
                                Color(0x26EF4444),
                                Color(0x55EF4444),
                                Color(0xFFF87171),
                                androidx.compose.material.icons.Icons.Default.Lock,
                                "Closed (Expired)"
                            )
                        }
                        else -> Pentad(Color.Transparent, Color.Transparent, Color.Transparent, androidx.compose.material.icons.Icons.Default.Schedule, "")
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(schedBg)
                            .border(0.8.dp, schedBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.5.dp)
                    ) {
                        Icon(
                            imageVector = schedIcon,
                            contentDescription = null,
                            tint = schedText,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = schedLabel,
                            style = TextStyle(
                                color = schedText,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Indicator (Lock for expired/upcoming, Chevron for active)
            val scheduleStatus = remember(quiz) { quiz.getScheduleStatus() }
            if (scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                    contentDescription = "Quiz Closed",
                    tint = TextDim,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Start ${quiz.title}",
                    tint = TextDim,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private data class Pentad<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
