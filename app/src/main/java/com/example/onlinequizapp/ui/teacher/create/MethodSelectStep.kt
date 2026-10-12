package com.example.onlinequizapp.ui.teacher.create

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.domain.model.QuizCreationMethod

import com.example.onlinequizapp.ui.teacher.QuizBuilderTextPrimary
import com.example.onlinequizapp.ui.teacher.QuizBuilderTextSecondary
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun MethodSelectStep(
    onSelectMethod: (QuizCreationMethod) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // --- 1. Header Banner Row with Title & 3D Clipboard Graphic ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Title and Subtitle Block
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Lightbulb Icon + Title Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDark) Color(0xFFFEF08A).copy(alpha = 0.15f)
                                    else Color(0xFFFEF9C3)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color(0xFFFACC15).copy(alpha = 0.35f)
                                    else Color(0xFFFDE047),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFFFACC15) else Color(0xFFCA8A04),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Choose Quiz Creation",
                                color = QuizBuilderTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                            Text(
                                text = "Method",
                                color = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Select how you would like to author your quiz questions today.",
                        color = QuizBuilderTextSecondary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Right: 3D Clipboard Illustration
                Box(
                    modifier = Modifier
                        .size(width = 165.dp, height = 145.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_quiz_builder_clipboard),
                        contentDescription = "Quiz Checklist Illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. Option 1: Manual Entry Card (Cyan/Blue Border & Glow) ---
        QuizMethodCard(
            title = "Manual Entry",
            badge = "FULL CONTROL",
            badgeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            badgeBg = if (isDark) Color(0xFF0369A1).copy(alpha = 0.45f) else Color(0xFFE0F2FE),
            subtitle = "Create questions yourself with options, answers, and explanations.",
            icon = Icons.Default.Description,
            iconGradient = listOf(Color(0xFF38BDF8), Color(0xFF2563EB)),
            borderColor = if (isDark) Color(0xFF0284C7) else Color(0xFFBAE6FD),
            borderGradient = if (isDark) {
                listOf(Color(0xFF38BDF8), Color(0xFF1D4ED8))
            } else {
                listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
            },
            cardBgGradient = if (isDark) {
                listOf(Color(0xFF021B4E), Color(0xFF06143A))
            } else {
                listOf(Color(0xFFFFFFFF), Color(0xFFF0F9FF))
            },
            chevronColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
            onClick = { onSelectMethod(QuizCreationMethod.MANUAL) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. Option 2: Generate with AI Card (Purple/Magenta Glow) ---
        QuizMethodCard(
            title = "Generate with AI",
            badge = "RECOMMENDED",
            badgeColor = if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED),
            badgeBg = if (isDark) Color(0xFF5B21B6).copy(alpha = 0.45f) else Color(0xFFF3E8FF),
            subtitle = "Automatically generate complete questions using Gemini or OpenAI.",
            icon = Icons.Default.Memory,
            iconGradient = listOf(Color(0xFFC084FC), Color(0xFF7C3AED)),
            borderColor = if (isDark) Color(0xFF9333EA) else Color(0xFFE9D5FF),
            borderGradient = if (isDark) {
                listOf(Color(0xFFA855F7), Color(0xFF4F46E5))
            } else {
                listOf(Color(0xFFA855F7), Color(0xFF7C3AED))
            },
            cardBgGradient = if (isDark) {
                listOf(Color(0xFF1E0A4E), Color(0xFF0C0932))
            } else {
                listOf(Color(0xFFFFFFFF), Color(0xFFFAF5FF))
            },
            chevronColor = if (isDark) Color(0xFFA855F7) else Color(0xFF7C3AED),
            onClick = { onSelectMethod(QuizCreationMethod.AI_GENERATED) }
        )
    }
}

@Composable
private fun QuizMethodCard(
    title: String,
    badge: String,
    badgeColor: Color,
    badgeBg: Color,
    subtitle: String,
    icon: ImageVector,
    iconGradient: List<Color>,
    borderColor: Color,
    borderGradient: List<Color>,
    cardBgGradient: List<Color>,
    chevronColor: Color = Color(0xFF94A3B8),
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(cardBgGradient))
            .border(
                1.5.dp,
                Brush.horizontalGradient(borderGradient),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Gradient Icon Box
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(iconGradient))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center: Title + Pill Badge + Description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        color = QuizBuilderTextPrimary,
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Pill Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badge,
                            color = badgeColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitle,
                    color = QuizBuilderTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.5.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = chevronColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}


