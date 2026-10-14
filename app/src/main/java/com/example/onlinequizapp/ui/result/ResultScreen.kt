package com.example.onlinequizapp.ui.result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.onlinequizapp.ui.components.showToast
import com.example.onlinequizapp.ui.theme.AccentAmber
import com.example.onlinequizapp.ui.theme.BrandPrimary
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.Slate200
import com.example.onlinequizapp.ui.theme.SuccessGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    quizId: String,
    onViewDetailsClick: (initialIndex: Int) -> Unit,
    onTryAgainClick: () -> Unit,
    onReturnHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = viewModel(
        factory = ResultViewModel.provideFactory(quizId)
    )
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val state = sessionState
    if (state == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = BrandPrimary)
        }
        return
    }

    val totalQuestions = state.totalQuestions
    val attemptedCount = state.answers.values.count { it.isAnswered }
    val leftCount = (totalQuestions - attemptedCount).coerceAtLeast(0)
    val correctCount = state.correctCount
    val incorrectCount = state.incorrectCount

    val progressValue = if (totalQuestions == 0) 0f else correctCount.toFloat() / totalQuestions
    val animatedProgress by animateFloatAsState(
        targetValue = progressValue,
        animationSpec = tween(durationMillis = 1000),
        label = "scoreProgress"
    )

    val minutes = state.elapsedTimeSeconds / 60
    val seconds = state.elapsedTimeSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val scrollState = rememberScrollState()

    BackHandler {
        onReturnHomeClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Trophy & Title Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "🏆",
                    fontSize = 52.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Quiz Completed!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Great job! Here is your complete performance report.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Circular Score Ring Card
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = Slate200,
                    strokeWidth = 14.dp,
                    trackColor = Color.Transparent
                )

                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (state.scorePercentage >= 60) SuccessGreen else DangerRed,
                    strokeWidth = 14.dp,
                    trackColor = Color.Transparent
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.correctCount}/${state.totalQuestions}",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${state.scorePercentage}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Performance Badge Pill
            val (badgeText, badgeBg, badgeColor) = when {
                state.scorePercentage >= 80 -> Triple("🌟 Outstanding Score!", SuccessGreen.copy(alpha = 0.12f), SuccessGreen)
                state.scorePercentage >= 60 -> Triple("🎉 Quiz Passed", BrandPrimary.copy(alpha = 0.12f), BrandPrimary)
                else -> Triple("📚 Needs Practice", DangerRed.copy(alpha = 0.12f), DangerRed)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = badgeColor
                )
            }

            // Attempted vs Left Summary Cards (2 Columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatOverviewCard(
                    icon = Icons.Default.CheckCircle,
                    iconTint = BrandPrimary,
                    title = "Attempted",
                    value = "$attemptedCount / $totalQuestions",
                    subtitle = "${if (totalQuestions > 0) (attemptedCount * 100) / totalQuestions else 0}% Answered",
                    backgroundColor = BrandPrimary.copy(alpha = 0.06f),
                    borderColor = BrandPrimary.copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                )

                StatOverviewCard(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = AccentAmber,
                    title = "Left / Skipped",
                    value = "$leftCount",
                    subtitle = "${if (totalQuestions > 0) (leftCount * 100) / totalQuestions else 0}% Unanswered",
                    backgroundColor = AccentAmber.copy(alpha = 0.06f),
                    borderColor = AccentAmber.copy(alpha = 0.25f),
                    modifier = Modifier.weight(1f)
                )
            }

            // Results Breakdown Row (3 Columns: Correct, Incorrect, Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ResultMetricCard(
                    icon = Icons.Default.Check,
                    iconColor = SuccessGreen,
                    value = "$correctCount",
                    label = "Correct",
                    modifier = Modifier.weight(1f)
                )

                ResultMetricCard(
                    icon = Icons.Default.Close,
                    iconColor = DangerRed,
                    value = "$incorrectCount",
                    label = "Incorrect",
                    modifier = Modifier.weight(1f)
                )

                ResultMetricCard(
                    icon = Icons.Default.Timer,
                    iconColor = AccentAmber,
                    value = timeFormatted,
                    label = "Time Taken",
                    modifier = Modifier.weight(1f)
                )
            }

            // Question Breakdown Review Section (Question Numbers with Status)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Question Breakdown",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap any question number to review",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$totalQuestions Questions",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = SuccessGreen, label = "Correct ($correctCount)")
                        LegendItem(color = DangerRed, label = "Wrong ($incorrectCount)")
                        LegendItem(color = Color(0xFF94A3B8), label = "Left ($leftCount)")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Badges FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until totalQuestions) {
                            val question = state.quiz.questions.getOrNull(i)
                            val answer = if (question != null) state.answers[question.id] else null
                            val isAnswered = answer?.isAnswered == true
                            val isCorrect = isAnswered && answer?.isCorrect == true

                            val (statusBg, statusBorder, statusText, statusIcon) = when {
                                isCorrect -> Quadruple(
                                    SuccessGreen.copy(alpha = 0.12f),
                                    SuccessGreen,
                                    SuccessGreen,
                                    Icons.Default.Check
                                )
                                isAnswered -> Quadruple(
                                    DangerRed.copy(alpha = 0.12f),
                                    DangerRed,
                                    DangerRed,
                                    Icons.Default.Close
                                )
                                else -> Quadruple(
                                    Color(0xFFF1F5F9),
                                    Color(0xFFCBD5E1),
                                    Color(0xFF64748B),
                                    null
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(statusBg)
                                    .border(1.5.dp, statusBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        context.showToast("Reviewing Question ${i + 1}...")
                                        onViewDetailsClick(i)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Q${i + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = statusText
                                    )
                                    if (statusIcon != null) {
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = statusText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "Left",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = statusText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val scorePercentage = if (totalQuestions > 0) {
                (correctCount.toDouble() / totalQuestions.toDouble()) * 100.0
            } else 0.0

            if (com.example.onlinequizapp.util.CertificateGenerator.isEligible(scorePercentage)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEF3C7)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD97706)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Certificate",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Certificate of Achievement Earned! 🏆",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Score: ${scorePercentage.toInt()}% (Eligible for Official Certificate)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val studentUser = com.example.onlinequizapp.data.session.AuthSessionManager.getSavedUser()
                                val studentName = studentUser?.name?.takeIf { it.isNotBlank() } ?: "Student"
                                val quizTitle = state.quiz.title.ifBlank { "Quiz" }
                                val category = state.quiz.category.ifBlank { "General" }
                                val nowMillis = System.currentTimeMillis()
                                val certificateId = "QM-${state.quiz.id.takeLast(4).uppercase()}-${nowMillis.toString().takeLast(6)}"

                                val certFile = com.example.onlinequizapp.util.CertificateGenerator.generateCertificate(
                                    context = context,
                                    studentName = studentName,
                                    quizName = quizTitle,
                                    score = scorePercentage,
                                    category = category,
                                    certificateId = certificateId,
                                    dateMillis = nowMillis
                                )

                                if (certFile != null) {
                                    context.showToast("Certificate generated: ${certFile.name}")
                                    try {
                                        val uri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            certFile
                                        )
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Open Certificate"))
                                    } catch (_: Exception) {
                                        context.showToast("Certificate saved: ${certFile.absolutePath}")
                                    }
                                } else {
                                    context.showToast("Certificate could not be generated.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Download Official Certificate (PDF)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        context.showToast("Opening detailed review...")
                        onViewDetailsClick(0)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review All Answers",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        context.showToast("Restarting quiz...")
                        onTryAgainClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                TextButton(
                    onClick = {
                        context.showToast("Returning to My Quizzes...")
                        onReturnHomeClick()
                    }
                ) {
                    Text(
                        text = "Return to My Quizzes",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatOverviewCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResultMetricCard(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
