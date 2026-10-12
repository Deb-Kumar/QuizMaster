package com.example.onlinequizapp.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.domain.model.TimerMode
import com.example.onlinequizapp.ui.theme.AccentAmber
import com.example.onlinequizapp.ui.theme.AccentAmberBg
import com.example.onlinequizapp.ui.theme.DangerRed
import com.example.onlinequizapp.ui.theme.DangerRedBg
import com.example.onlinequizapp.ui.theme.Slate100

@Composable
fun QuizTopBar(
    title: String,
    timerMode: TimerMode,
    seconds: Int,
    isWarning: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().statusBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )

            if (timerMode != TimerMode.NONE) {
                val minutes = seconds / 60
                val secs = seconds % 60
                val formattedTime = String.format("%02d:%02d", minutes, secs)

                val isCritical = timerMode == TimerMode.COUNTDOWN && seconds in 1..30
                val isUrgent = timerMode == TimerMode.COUNTDOWN && seconds in 31..120

                // Infinite pulsing opacity for the last 30 seconds
                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "timerPulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isCritical) 0.55f else 1f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(500),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "pulseAlpha"
                )

                val (bgColor, textColor) = when {
                    isCritical || isWarning -> DangerRedBg to DangerRed
                    isUrgent -> AccentAmberBg to AccentAmber
                    timerMode == TimerMode.COUNTDOWN -> Color(0xFFEEF2FF) to Color(0xFF4F46E5)
                    else -> Slate100 to MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = if (isCritical) pulseAlpha else 1f }
                        .clip(RoundedCornerShape(20.dp))
                        .background(bgColor)
                        .then(
                            if (isCritical) Modifier.border(1.dp, DangerRed.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            else if (isUrgent) Modifier.border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            else Modifier
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics {
                            contentDescription = "Timer: $formattedTime"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
