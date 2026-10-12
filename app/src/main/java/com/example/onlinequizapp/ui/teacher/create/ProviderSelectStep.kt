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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.R
import com.example.onlinequizapp.data.ai.AIQuizGeneratorFactory
import com.example.onlinequizapp.domain.ai.AiProvider
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

@Composable
fun ProviderSelectStep(
    selectedProvider: AiProvider,
    onSelectProvider: (AiProvider) -> Unit,
    apiKeyInput: String = "",
    onApiKeyInputChanged: (String) -> Unit = {},
    isGenerating: Boolean = false,
    generatingStatusText: String = "",
    onBackClick: () -> Unit,
    onGenerateClick: () -> Unit = {},
    onContinueClick: () -> Unit = onGenerateClick,
    modifier: Modifier = Modifier
) {
    val isDark = isTeacherInDarkTheme()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Header Row with Title, Subtitle & 3D Cute AI Robot Banner ---
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Title & Description
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Choose AI Provider",
                        color = if (isDark) Color.White else Color(0xFF0F172A),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Select the artificial intelligence model provider to generate questions for your quiz.",
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                // Right: 3D Cute AI Robot Illustration with floating orbital rings
                Box(
                    modifier = Modifier.size(width = 140.dp, height = 125.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ai_robot_banner),
                        contentDescription = "AI Robot Assistant",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // --- 2. AI Provider Cards List ---
        AiProvider.entries.forEach { provider ->
            val isSelected = provider == selectedProvider
            val hasSavedKey = AIQuizGeneratorFactory.hasApiKey(provider)

            GlowingAiProviderCard(
                provider = provider,
                isSelected = isSelected,
                hasKey = hasSavedKey,
                onClick = { onSelectProvider(provider) }
            )
        }

        // Generating status banner if AI is running
        if (isGenerating && generatingStatusText.isNotBlank()) {
            val genBg = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.7f) else Color(0xFFEEF2FF)
            val genBorder = if (isDark) Color(0xFF6366F1) else Color(0xFF818CF8)
            val genText = if (isDark) Color(0xFFE0E7FF) else Color(0xFF3730A3)
            val genIndicator = if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(genBg)
                    .border(1.dp, genBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = genIndicator,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = generatingStatusText,
                        color = genText,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // --- 3. Bottom Action Buttons: [ Back ] & [ Generate Questions ] ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val backBg = if (isDark) Color(0xFF131B38).copy(alpha = 0.85f) else Color.White
            val backBorder = if (isDark) Color(0xFF334168).copy(alpha = 0.6f) else Color(0xFFCBD5E1)
            val backTint = if (isDark) Color.White else Color(0xFF334155)

            // Back Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(backBg)
                    .border(
                        1.2.dp,
                        backBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .clickable(enabled = !isGenerating, onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = backTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Back",
                        color = backTint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Generate Questions Button: Glowing Violet-to-Cyan Pill Gradient
            val isButtonEnabled = !isGenerating
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (isButtonEnabled) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF7C3AED), // Vibrant Violet
                                    Color(0xFF2563EB), // Rich Blue
                                    Color(0xFF06B6D4)  // Cyan
                                )
                            )
                        } else {
                            if (isDark) {
                                Brush.linearGradient(listOf(Color(0xFF1E284A), Color(0xFF1E284A)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0)))
                            }
                        }
                    )
                    .clickable(enabled = isButtonEnabled, onClick = onGenerateClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Generating...",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Generate Questions",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI Provider selection card with glowing gradient borders, brand logos, radio check, and badge.
 */
@Composable
private fun GlowingAiProviderCard(
    provider: AiProvider,
    isSelected: Boolean,
    hasKey: Boolean,
    onClick: () -> Unit
) {
    val isDark = isTeacherInDarkTheme()

    val cardBg = if (isSelected) {
        if (isDark) {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF0A1448).copy(alpha = 0.95f),
                    Color(0xFF0F1B58).copy(alpha = 0.95f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFEEF2FF),
                    Color(0xFFE0E7FF)
                )
            )
        }
    } else {
        if (isDark) {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF090F26).copy(alpha = 0.85f),
                    Color(0xFF070C1E).copy(alpha = 0.85f)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color.White,
                    Color(0xFFF8FAFC)
                )
            )
        }
    }

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                Color(0xFF818CF8), // Purple-blue glow
                Color(0xFF38BDF8), // Bright Cyan glow
                Color(0xFF6366F1)  // Indigo
            )
        )
    } else {
        if (isDark) {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF1E2945),
                    Color(0xFF141C35)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0xFFE2E8F0),
                    Color(0xFFCBD5E1)
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio Indicator Circle on the far left
            val radioBg = if (isSelected) {
                if (isDark) Color(0xFF1E2850) else Color(0xFFE0E7FF)
            } else Color.Transparent

            val radioBorder = if (isSelected) {
                if (isDark) Color(0xFF60A5FA) else Color(0xFF4F46E5)
            } else {
                if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1)
            }

            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(radioBg)
                    .border(
                        width = 1.5.dp,
                        color = radioBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5))
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Brand Logo Box (Gemini, OpenAI, or OpenRouter)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (provider) {
                    AiProvider.GEMINI -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_gemini_logo),
                            contentDescription = "Gemini Logo",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AiProvider.OPENAI -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_openai_logo),
                            contentDescription = "OpenAI Logo",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AiProvider.OPENROUTER -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_openrouter_logo),
                            contentDescription = "OpenRouter Logo",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AiProvider.CLAUDE -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_claude_logo),
                            contentDescription = "Claude Logo",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AiProvider.DEEPSEEK -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_deepseek_logo),
                            contentDescription = "DeepSeek Logo",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    AiProvider.AGENTROUTER -> {
                        Image(
                            painter = painterResource(id = R.drawable.ic_agentrouter_logo),
                            contentDescription = "AgentRouter Logo",
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Model Subtitle
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = provider.displayName,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Model: ${provider.recommendedModel}",
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Right Badge: "Selected" Pill Badge OR "NEEDS KEY" Warning Pill
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF818CF8))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = "Selected",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (!hasKey) {
                val warnBg = if (isDark) Color(0xFF38230D).copy(alpha = 0.8f) else Color(0xFFFEF3C7)
                val warnBorder = if (isDark) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color(0xFFFDE68A)
                val warnText = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(warnBg)
                        .border(1.dp, warnBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = warnText,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "NEEDS KEY",
                            color = warnText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val readyBg = if (isDark) Color(0xFF064E3B).copy(alpha = 0.7f) else Color(0xFFDCFCE7)
                val readyBorder = if (isDark) Color(0xFF10B981).copy(alpha = 0.35f) else Color(0xFFA7F3D0)
                val readyText = if (isDark) Color(0xFF34D399) else Color(0xFF15803D)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(readyBg)
                        .border(1.dp, readyBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "READY",
                        color = readyText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

