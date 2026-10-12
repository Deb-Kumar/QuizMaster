package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.TimerMode

/**
 * Rich Quiz Start / Preview Modal
 * Shows:
 * 1. Quiz Name (Title)
 * 2. How many questions are present
 * 3. Time limit (Only if configured by teacher in countdown mode)
 * 4. Difficulty level (Easy, Medium, Hard) with matching color badge
 * 5. Pending / In-progress or Completed details
 * 6. "Start Quiz" / "Resume Quiz" or "View Latest Result" button to begin
 */
@Composable
fun QuizStartModal(
    quiz: Quiz,
    isPending: Boolean = false,
    isCompleted: Boolean = false,
    progressPercent: Int = 0,
    savedQuestionIndex: Int? = null,
    isDark: Boolean,
    onStartQuiz: () -> Unit,
    onViewResult: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val visuals = remember(quiz) { QuizVisualHelper.getVisuals(quiz) }
    val hasTimeLimit = remember(quiz) {
        quiz.config.timerMode == TimerMode.COUNTDOWN && quiz.config.timeLimitSeconds > 0
    }
    val timeLimitMinutes = remember(quiz, hasTimeLimit) {
        if (hasTimeLimit) maxOf(1, quiz.config.timeLimitSeconds / 60) else null
    }

    // Difficulty badge styling
    val (diffBg, diffBorder, diffText) = remember(quiz.difficulty, isDark) {
        when (quiz.difficulty) {
            Difficulty.EASY -> Triple(
                if (isDark) Color(0x2210B981) else Color(0xFFECFDF5),
                if (isDark) Color(0x5510B981) else Color(0xFFA7F3D0),
                if (isDark) Color(0xFF34D399) else Color(0xFF047857)
            )
            Difficulty.MEDIUM -> Triple(
                if (isDark) Color(0x22F59E0B) else Color(0xFFFFFBEB),
                if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A),
                if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
            )
            Difficulty.HARD -> Triple(
                if (isDark) Color(0x33EF4444) else Color(0xFFFEF2F2),
                if (isDark) Color(0x66EF4444) else Color(0xFFFECACA),
                if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
            )
        }
    }

    val cardBg = if (isDark) Color(0xFF131A33) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF23325E) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth(0.92f)
                    .border(
                        width = 1.dp,
                        color = cardBorder,
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBg,
                    contentColor = textPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 4.dp else 12.dp)
            ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Category pill & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (quiz.category.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isDark) Color(0xFF1E2A54) else Color(0xFFEEF2FF)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color(0xFF2E3E75) else Color(0xFFC7D2FE),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = quiz.category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                letterSpacing = 0.5.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Tactile Close Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E284A) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) Color(0xFF2E3E75) else Color(0xFFE2E8F0), CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Decorative Quiz Emblem (Matches quiz logo & gradient)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(visuals.gradientBrush)
                        .border(
                            width = 2.dp,
                            color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White,
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    QuizVisualHelper.QuizLogoEmblem(
                        visuals = visuals,
                        contentDescription = quiz.category,
                        iconSize = 38.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quiz Name (Title)
                Text(
                    text = quiz.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (quiz.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = quiz.description,
                        fontSize = 13.sp,
                        color = textSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Detail Badges Row: Questions Count | Difficulty Level | Time (if mentioned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Question Count Badge
                    QuizInfoBadge(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        text = if (quiz.totalQuestions == 1) "1 Question" else "${quiz.totalQuestions} Questions",
                        bgColor = if (isDark) Color(0xFF1E284A) else Color(0xFFF1F5F9),
                        borderColor = if (isDark) Color(0xFF2F3E70) else Color(0xFFE2E8F0),
                        textColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B),
                        iconTint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // 2. Difficulty Level Badge (Hard is red type)
                    QuizInfoBadge(
                        icon = Icons.Default.SignalCellularAlt,
                        text = quiz.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        bgColor = diffBg,
                        borderColor = diffBorder,
                        textColor = diffText,
                        iconTint = diffText
                    )

                    // 3. Time Limit Badge
                    Spacer(modifier = Modifier.width(8.dp))
                    if (hasTimeLimit && timeLimitMinutes != null) {
                        val durationStr = if (quiz.config.timeLimitSeconds % 60 == 0) {
                            "${quiz.config.timeLimitSeconds / 60} min"
                        } else {
                            "${quiz.config.timeLimitSeconds / 60}m ${quiz.config.timeLimitSeconds % 60}s"
                        }
                        QuizInfoBadge(
                            icon = Icons.Default.Schedule,
                            text = durationStr,
                            bgColor = if (isDark) Color(0xFF261E47) else Color(0xFFFAF5FF),
                            borderColor = if (isDark) Color(0xFF4C3683) else Color(0xFFE9D5FF),
                            textColor = if (isDark) Color(0xFFC084FC) else Color(0xFF7E22CE),
                            iconTint = if (isDark) Color(0xFFC084FC) else Color(0xFF7E22CE)
                        )
                    } else {
                        QuizInfoBadge(
                            icon = Icons.Default.Schedule,
                            text = "Unlimited",
                            bgColor = if (isDark) Color(0xFF1E284A) else Color(0xFFF1F5F9),
                            borderColor = if (isDark) Color(0xFF2F3E70) else Color(0xFFE2E8F0),
                            textColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B),
                            iconTint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                        )
                    }
                }

                // Scheduled Availability Window Banner (if configured)
                val scheduleStatus = remember(quiz) { quiz.getScheduleStatus() }
                val scheduleSummary = remember(quiz) { quiz.formatScheduleWindowSummary() }
                if (scheduleSummary != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    val (bannerBg, bannerBorder, bannerIconColor, bannerTitle, bannerDesc) = when (scheduleStatus) {
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.UPCOMING -> Quintuple(
                            if (isDark) Color(0x2238BDF8) else Color(0xFFF0F9FF),
                            if (isDark) Color(0x5538BDF8) else Color(0xFFBAE6FD),
                            if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            "Upcoming Quiz Window",
                            "Opens on $scheduleSummary. You cannot start until this window begins."
                        )
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED -> Quintuple(
                            if (isDark) Color(0x25EF4444) else Color(0xFFFEF2F2),
                            if (isDark) Color(0x55EF4444) else Color(0xFFFECACA),
                            if (isDark) Color(0xFFF87171) else Color(0xFFDC2626),
                            "Deadline Passed",
                            "This quiz window closed ($scheduleSummary). Submissions are now closed."
                        )
                        com.example.onlinequizapp.domain.model.QuizScheduleStatus.ACTIVE_WITH_DEADLINE -> Quintuple(
                            if (isDark) Color(0x22F59E0B) else Color(0xFFFFFBEB),
                            if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A),
                            if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                            "Active Window • Deadline Set",
                            "Window: $scheduleSummary. Make sure to complete and submit before time runs out."
                        )
                        else -> Quintuple(Color.Transparent, Color.Transparent, Color.Transparent, "", "")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bannerBg)
                            .border(1.dp, bannerBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.Schedule
                                },
                                contentDescription = null,
                                tint = bannerIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bannerTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = bannerIconColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bannerDesc,
                                    fontSize = 11.sp,
                                    color = textSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // If quiz is completed
                if (isCompleted) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x3310B981) else Color(0xFFECFDF5))
                            .border(
                                1.dp,
                                if (isDark) Color(0x5510B981) else Color(0xFFA7F3D0),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 9.dp, horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🏆",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quiz Completed! View your latest score & answers",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF34D399) else Color(0xFF047857)
                            )
                        }
                    }
                } else if (isPending || progressPercent > 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x33F59E0B) else Color(0xFFFFFBEB))
                            .border(
                                1.dp,
                                if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 9.dp, horizontal = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val savedMsg = if (savedQuestionIndex != null) {
                                "Saved at Question ${savedQuestionIndex + 1} of ${quiz.totalQuestions}"
                            } else {
                                "Quiz In Progress ($progressPercent%)"
                            }
                            Text(
                                text = savedMsg,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                if (isCompleted && onViewResult != null) {
                    // 1. Primary Button: View Latest Result
                    Button(
                        onClick = onViewResult,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 2.dp)
                    ) {
                        Text(
                            text = "View Latest Result ➔",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Secondary Button: Retake Quiz
                    OutlinedButton(
                        onClick = onStartQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF3B82F6) else Color(0xFF6366F1)
                        )
                    ) {
                        Text(
                            text = "Retake Quiz",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                        )
                    }
                } else {
                    val canStartQuiz = scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.ALWAYS_AVAILABLE ||
                            scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.ACTIVE_WITH_DEADLINE

                    val buttonLabel = when {
                        scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.UPCOMING -> "Quiz Opens Soon 🔒"
                        scheduleStatus == com.example.onlinequizapp.domain.model.QuizScheduleStatus.EXPIRED -> "Quiz Closed 🔒"
                        isPending -> "Resume Quiz ➔"
                        else -> "Start Quiz ➔"
                    }

                    // Primary Action Button (Start / Resume Quiz)
                    Button(
                        onClick = onStartQuiz,
                        enabled = canStartQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5),
                            contentColor = Color.White,
                            disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                            disabledContentColor = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 3.dp)
                    ) {
                        Text(
                            text = buttonLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun QuizInfoBadge(
    icon: ImageVector,
    text: String,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(13.5.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

