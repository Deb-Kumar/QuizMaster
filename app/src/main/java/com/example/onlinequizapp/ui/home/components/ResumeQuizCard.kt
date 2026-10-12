package com.example.onlinequizapp.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.data.session.QuizDraft
import com.example.onlinequizapp.ui.components.QuizVisualHelper
import com.example.onlinequizapp.ui.home.LocalQuizDark

@Composable
fun ResumeQuizCard(
    draft: QuizDraft,
    onResumeClick: () -> Unit,
    onDiscardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalQuizDark.current
    val progress = if (draft.totalQuestions > 0) {
        (draft.currentIndex + 1).toFloat() / draft.totalQuestions.toFloat()
    } else 0f

    val cardBg = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF1E1B4B).copy(alpha = 0.9f),
                Color(0xFF31104B).copy(alpha = 0.9f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEEF2FF),
                Color(0xFFFAF5FF)
            )
        )
    }

    val borderColor = if (isDark) Color(0xFF818CF8).copy(alpha = 0.45f) else Color(0xFF6366F1).copy(alpha = 0.35f)
    val titleColor = if (isDark) Color.White else Color(0xFF1E1B4B)
    val subtitleColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onResumeClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(cardBg)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                        Text(
                            text = "INCOMPLETE QUIZ SAVED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                        )
                    }

                    if (draft.remainingTimeSeconds > 0) {
                        val minutes = draft.remainingTimeSeconds / 60
                        val seconds = draft.remainingTimeSeconds % 60
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format("%02d:%02d left", minutes, seconds),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }

                // Quiz Title with Matching Icon
                val visuals = remember(draft) { QuizVisualHelper.getVisuals(title = draft.quizTitle, iconKey = draft.quizIcon) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(visuals.gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        QuizVisualHelper.QuizLogoEmblem(
                            visuals = visuals,
                            contentDescription = draft.quizTitle,
                            iconSize = 26.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = draft.quizTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = titleColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Question ${draft.currentIndex + 1} of ${draft.totalQuestions}",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtitleColor
                        )
                        Text(
                            text = "${draft.answeredCount} answered",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF6366F1),
                        trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDiscardClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Discard",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Button(
                        onClick = onResumeClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Resume Quiz",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
