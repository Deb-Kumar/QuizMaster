package com.example.onlinequizapp.ui.components

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.session.QuizDraft
import com.example.onlinequizapp.ui.theme.BrandPrimary

/**
 * Premium Modern Modal for resuming an incomplete quiz draft.
 * Styled to match QuizStartModal and other core app dialogs.
 */
@Composable
fun ResumeQuizModal(
    draft: QuizDraft,
    isDark: Boolean,
    onResume: () -> Unit,
    onLater: () -> Unit,
    onDiscard: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF131A33) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF23325E) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val visuals = remember(draft) { QuizVisualHelper.getVisuals(title = draft.quizTitle, iconKey = draft.quizIcon) }

    val progress = if (draft.totalQuestions > 0) {
        (draft.currentIndex + 1).toFloat() / draft.totalQuestions
    } else 0f
    val progressPercent = (progress * 100).toInt()

    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onLater),
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
                // Top Row: Category / Status pill & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = "RESUME QUIZ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Tactile Close Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E284A) else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) Color(0xFF2E3E75) else Color(0xFFE2E8F0), CircleShape)
                            .clickable(onClick = onLater),
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

                // Emblem matching specific quiz logo
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(visuals.gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    QuizVisualHelper.QuizLogoEmblem(
                        visuals = visuals,
                        contentDescription = draft.quizTitle,
                        iconSize = 36.dp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Modal Title & Subtitle
                Text(
                    text = "Resume Incomplete Quiz?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "You have an unfinished attempt saved on this device",
                    fontSize = 13.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quiz Draft Details Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDark) Color(0xFF1E284A) else Color(0xFFF8FAFC))
                        .border(
                            1.dp,
                            if (isDark) Color(0xFF2F3E70) else Color(0xFFE2E8F0),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = draft.quizTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrandPrimary,
                            trackColor = if (isDark) Color(0xFF131A33) else Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Question ${draft.currentIndex + 1} of ${draft.totalQuestions}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textSecondary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${draft.answeredCount} answered ($progressPercent%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Would you like to resume where you left off?",
                    fontSize = 13.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) BrandPrimary else Color(0xFF4F46E5),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isDark) 0.dp else 3.dp)
                ) {
                    Text(
                        text = "Resume Quiz ➔",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onLater,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)
                    )
                ) {
                    Text(
                        text = "Continue Later",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onDiscard
                ) {
                    Text(
                        text = "Discard Draft",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
        }
    }
}
