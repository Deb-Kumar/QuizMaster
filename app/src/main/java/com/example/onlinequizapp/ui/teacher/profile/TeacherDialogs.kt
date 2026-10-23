package com.example.onlinequizapp.ui.teacher.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.data.session.AiConfigManager
import com.example.onlinequizapp.data.session.AiConnectionResult
import com.example.onlinequizapp.data.session.AiConnectionTester
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.components.showToast
import kotlinx.coroutines.launch

import com.example.onlinequizapp.domain.model.Quiz
import com.example.onlinequizapp.domain.model.QuizSessionState
import com.example.onlinequizapp.ui.teacher.TeacherCardBg
import com.example.onlinequizapp.ui.teacher.TeacherCardBorder
import com.example.onlinequizapp.ui.teacher.TeacherDashboardUiState
import com.example.onlinequizapp.ui.teacher.TeacherNotificationsScreen
import com.example.onlinequizapp.ui.teacher.TeacherTextDim
import com.example.onlinequizapp.ui.teacher.TeacherTextMuted
import com.example.onlinequizapp.ui.teacher.TeacherTextWhite
import com.example.onlinequizapp.ui.teacher.isTeacherInDarkTheme

// -------------------------------------------------------------
// Interactive Dialogs for Profile Page & Quick Actions
// -------------------------------------------------------------
@Composable
internal fun TeacherLeaderboardDialog(
    submissions: List<QuizSessionState> = emptyList(),
    onDismiss: () -> Unit
) {
    val topStudents: List<Triple<String, String, String>> = remember(submissions) {
        if (submissions.isEmpty()) {
            emptyList<Triple<String, String, String>>()
        } else {
            submissions
                .sortedByDescending { it.scorePercentage }
                .take(5)
                .mapIndexed { idx, sub ->
                    val rankText = when (idx) {
                        0 -> "1st Place 🥇"
                        1 -> "2nd Place 🥈"
                        2 -> "3rd Place 🥉"
                        else -> "Top 10 ⭐"
                    }
                    val quizName = sub.quiz.title.ifBlank { "Quiz" }
                    Triple("Attempt #${idx + 1} ($quizName)", "${sub.scorePercentage}%", rankText)
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Student Leaderboard", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (topStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No student scores recorded yet.",
                            color = TeacherTextMuted,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    topStudents.forEachIndexed { index, student ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TeacherCardBg)
                                .border(1.dp, TeacherCardBorder, RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (index == 0) Color(0xFFF59E0B) else Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(student.first, color = TeacherTextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(student.third, color = TeacherTextMuted, fontSize = 11.sp)
                                    }
                                }

                                Text(
                                    student.second,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherSettingsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var openAiKey by remember { mutableStateOf(AiConfigManager.getOpenAiKey()) }
    var geminiKey by remember { mutableStateOf(AiConfigManager.getGeminiKey()) }
    var openRouterKey by remember { mutableStateOf(AiConfigManager.getOpenRouterKey()) }
    var claudeKey by remember { mutableStateOf(AiConfigManager.getClaudeKey()) }
    var deepSeekKey by remember { mutableStateOf(AiConfigManager.getDeepSeekKey()) }
    var agentRouterKey by remember { mutableStateOf(AiConfigManager.getAgentRouterKey()) }

    var openAiEnabled by remember { mutableStateOf(AiConfigManager.isOpenAiEnabled()) }
    var geminiEnabled by remember { mutableStateOf(AiConfigManager.isGeminiEnabled()) }
    var openRouterEnabled by remember { mutableStateOf(AiConfigManager.isOpenRouterEnabled()) }
    var claudeEnabled by remember { mutableStateOf(AiConfigManager.isClaudeEnabled()) }
    var deepSeekEnabled by remember { mutableStateOf(AiConfigManager.isDeepSeekEnabled()) }
    var agentRouterEnabled by remember { mutableStateOf(AiConfigManager.isAgentRouterEnabled()) }

    var showOpenAiKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var showOpenRouterKey by remember { mutableStateOf(false) }
    var showClaudeKey by remember { mutableStateOf(false) }
    var showDeepSeekKey by remember { mutableStateOf(false) }
    var showAgentRouterKey by remember { mutableStateOf(false) }

    val isDark = isTeacherInDarkTheme()
    val dialogBg = if (isDark) Color(0xFF0A102E) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF10193D) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF22326C) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
            ) {
                // Fixed Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF7C3AED), Color(0xFF3B82F6))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Settings & AI Keys",
                                style = androidx.compose.ui.text.TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Configure AI model API credentials",
                                style = androidx.compose.ui.text.TextStyle(
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0))

                // Scrollable Content Body with all API Key inputs
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Informational Description Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) cardBg else Color(0xFFF1F5F9))
                            .border(1.dp, if (isDark) cardBorder else Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Provide your personal API keys to power AI quiz creation, automatic questions generation, and deep explanations.",
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // 1. OpenAI Key
                    ApiKeyInputField(
                        title = "OpenAI API Key",
                        subtitle = "For GPT-4o-mini & GPT-3.5",
                        tag = "OpenAI",
                        tagColor = if (isDark) Color(0xFF10A37F) else Color(0xFF059669),
                        isEnabled = openAiEnabled,
                        onToggleEnabled = { openAiEnabled = it },
                        value = openAiKey,
                        isPasswordVisible = showOpenAiKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { openAiKey = it },
                        onToggleVisibility = { showOpenAiKey = !showOpenAiKey },
                        onClear = {
                            openAiKey = ""
                            AiConfigManager.setOpenAiKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testOpenAi(openAiKey)
                        }
                    )

                    // 2. Google Gemini Key
                    ApiKeyInputField(
                        title = "Google Gemini API Key",
                        subtitle = "For Gemini 2.0 Flash, 2.5 & Pro models",
                        tag = "Gemini",
                        tagColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        isEnabled = geminiEnabled,
                        onToggleEnabled = { geminiEnabled = it },
                        value = geminiKey,
                        isPasswordVisible = showGeminiKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { geminiKey = it },
                        onToggleVisibility = { showGeminiKey = !showGeminiKey },
                        onClear = {
                            geminiKey = ""
                            AiConfigManager.setGeminiKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testGemini(geminiKey)
                        }
                    )

                    // 3. OpenRouter Key (Free Tier Models)
                    ApiKeyInputField(
                        title = "OpenRouter API Key",
                        subtitle = "For NVIDIA Nemotron 3 Ultra",
                        tag = "OpenRouter",
                        tagColor = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                        isEnabled = openRouterEnabled,
                        onToggleEnabled = { openRouterEnabled = it },
                        value = openRouterKey,
                        isPasswordVisible = showOpenRouterKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { openRouterKey = it },
                        onToggleVisibility = { showOpenRouterKey = !showOpenRouterKey },
                        onClear = {
                            openRouterKey = ""
                            AiConfigManager.setOpenRouterKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testOpenRouter(openRouterKey)
                        }
                    )

                    // 4. Anthropic Claude Key
                    ApiKeyInputField(
                        title = "Anthropic Claude API Key",
                        subtitle = "For Claude 3.5 Haiku & 3.5 Sonnet",
                        tag = "Claude",
                        tagColor = if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706),
                        isEnabled = claudeEnabled,
                        onToggleEnabled = { claudeEnabled = it },
                        value = claudeKey,
                        isPasswordVisible = showClaudeKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { claudeKey = it },
                        onToggleVisibility = { showClaudeKey = !showClaudeKey },
                        onClear = {
                            claudeKey = ""
                            AiConfigManager.setClaudeKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testClaude(claudeKey)
                        }
                    )

                    // 5. DeepSeek Key
                    ApiKeyInputField(
                        title = "DeepSeek API Key",
                        subtitle = "For DeepSeek-V3 & DeepSeek-R1 models",
                        tag = "DeepSeek",
                        tagColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        isEnabled = deepSeekEnabled,
                        onToggleEnabled = { deepSeekEnabled = it },
                        value = deepSeekKey,
                        isPasswordVisible = showDeepSeekKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { deepSeekKey = it },
                        onToggleVisibility = { showDeepSeekKey = !showDeepSeekKey },
                        onClear = {
                            deepSeekKey = ""
                            AiConfigManager.setDeepSeekKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testDeepSeek(deepSeekKey)
                        }
                    )

                    // 6. AgentRouter Key
                    ApiKeyInputField(
                        title = "AgentRouter API Key",
                        subtitle = "For GPT-5.6-Sol, GPT-6-Astra, GPT-6.1-Sol",
                        tag = "AgentRouter",
                        tagColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        isEnabled = agentRouterEnabled,
                        onToggleEnabled = { agentRouterEnabled = it },
                        value = agentRouterKey,
                        isPasswordVisible = showAgentRouterKey,
                        isDark = isDark,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        dialogBg = dialogBg,
                        onValueChange = { agentRouterKey = it },
                        onToggleVisibility = { showAgentRouterKey = !showAgentRouterKey },
                        onClear = {
                            agentRouterKey = ""
                            AiConfigManager.setAgentRouterKey("")
                        },
                        onConnect = {
                            AiConnectionTester.testAgentRouter(agentRouterKey)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(color = if (isDark) Color(0xFF1E2D64) else Color(0xFFE2E8F0))

                // Pinned Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDark) textSecondary else Color(0xFF475569)
                        )
                    ) {
                        Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            AiConfigManager.saveAllKeys(
                                openAi = openAiKey,
                                gemini = geminiKey,
                                openRouter = openRouterKey,
                                claude = claudeKey,
                                deepSeek = deepSeekKey,
                                agentRouter = agentRouterKey
                            )
                            AiConfigManager.setOpenAiEnabled(openAiEnabled)
                            AiConfigManager.setGeminiEnabled(geminiEnabled)
                            AiConfigManager.setOpenRouterEnabled(openRouterEnabled)
                            AiConfigManager.setClaudeEnabled(claudeEnabled)
                            AiConfigManager.setDeepSeekEnabled(deepSeekEnabled)
                            AiConfigManager.setAgentRouterEnabled(agentRouterEnabled)
                            context.showToast("AI configuration saved successfully! 🔑")
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Keys", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiKeyInputField(
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Color,
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    value: String,
    isPasswordVisible: Boolean,
    isDark: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    dialogBg: Color,
    onValueChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onClear: () -> Unit,
    onConnect: suspend () -> AiConnectionResult
) {
    val coroutineScope = rememberCoroutineScope()
    var isConnecting by remember { mutableStateOf(false) }
    var connectionResult by remember { mutableStateOf<AiConnectionResult?>(null) }
    var hasAttemptedConnect by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header Row: Title + Tag badge + On/Off Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(tagColor.copy(alpha = if (isDark) 0.2f else 0.12f))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = tag,
                        color = tagColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = tagColor,
                    uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                )
            )
        }

        Text(
            text = subtitle,
            color = textSecondary,
            fontSize = 11.5.sp
        )

        // API Key paste option, buttons, and response area only enabled/visible when toggle is ON
        AnimatedVisibility(visible = isEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // API Key text input field
                OutlinedTextField(
                    value = value,
                    onValueChange = { input ->
                        onValueChange(input)
                    },
                    placeholder = { Text("Paste your $tag API key...", color = textSecondary.copy(alpha = 0.6f), fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle key visibility",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedContainerColor = if (isDark) dialogBg else Color.White,
                        unfocusedContainerColor = if (isDark) dialogBg else Color.White,
                        focusedBorderColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                        unfocusedBorderColor = if (isDark) cardBorder else Color(0xFFCBD5E1),
                        cursorColor = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Buttons Row: Clear & Connect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Clear Button
                    OutlinedButton(
                        onClick = {
                            onClear()
                            connectionResult = null
                            hasAttemptedConnect = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444).copy(alpha = 0.45f) else Color(0xFFFCA5A5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 2. Connect Button
                    Button(
                        onClick = {
                            hasAttemptedConnect = true
                            isConnecting = true
                            coroutineScope.launch {
                                connectionResult = onConnect()
                                isConnecting = false
                            }
                        },
                        enabled = !isConnecting,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tagColor,
                            disabledContainerColor = tagColor.copy(alpha = 0.6f)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connecting...",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Connect",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Connect",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Response Text Area (Hidden by default, shown ONLY after user clicks Connect)
                if (hasAttemptedConnect || isConnecting || connectionResult != null) {
                    val isSuccess = connectionResult?.isSuccess == true
                    val statusColor = when {
                        isConnecting -> Color(0xFF38BDF8)
                        isSuccess -> Color(0xFF10B981)
                        else -> Color(0xFFEF4444)
                    }
                    val containerBg = when {
                        isDark -> Color(0xFF070C22)
                        isConnecting -> Color(0xFFF0F9FF)
                        isSuccess -> Color(0xFFF0FDF4)
                        else -> Color(0xFFFEF2F2)
                    }
                    val borderTint = when {
                        isDark -> statusColor.copy(alpha = 0.4f)
                        isConnecting -> Color(0xFFBAE6FD)
                        isSuccess -> Color(0xFFBBF7D0)
                        else -> Color(0xFFFECACA)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(containerBg)
                            .border(1.dp, borderTint, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Header of Response Area
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
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Text(
                                    text = if (isConnecting) "Verifying $tag Connection..." else "AI Response ($tag)",
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isConnecting && connectionResult != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(statusColor.copy(alpha = if (isDark) 0.2f else 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isSuccess) "CONNECTED" else "FAILED",
                                        color = statusColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Response Text Area Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF0B1028) else Color.White)
                                .border(1.dp, if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            if (isConnecting) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(13.dp),
                                        color = statusColor,
                                        strokeWidth = 1.8.dp
                                    )
                                    Text(
                                        text = "Sending ping to $tag servers...",
                                        color = textSecondary,
                                        fontSize = 11.sp,
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            } else {
                                Text(
                                    text = connectionResult?.aiResponse ?: "No response received.",
                                    color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Metadata footer
                        if (!isConnecting && connectionResult != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = connectionResult?.modelName?.let { "Model: $it" } ?: "",
                                    color = textSecondary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Time: ${connectionResult?.timestamp}",
                                    color = textSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun TeacherHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Text("Help & Support", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Frequently Asked Questions", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("• How do I publish a draft quiz?\nGo to My Quizzes, click the quiz card and toggle Publish.", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• How do I issue a certificate?\nStudents who achieve 80% or higher automatically qualify for completion certificates.", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Contacting Support:\nsupport@quizmaster.edu (Mon-Fri 9AM-6PM)", color = Color(0xFF38BDF8), fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherAchievementsDialog(
    stats: TeacherDashboardUiState = TeacherDashboardUiState(),
    onDismiss: () -> Unit
) {
    val quizCreatorStatus = if (stats.totalQuizzes >= 10) "Unlocked 🏆" else "${stats.totalQuizzes}/10 ⏳"
    val studentChampStatus = if (stats.totalStudents >= 50) "Unlocked 🏆" else "${stats.totalStudents}/50 ⏳"
    val topEducatorStatus = if (stats.averageScore >= 80 && stats.totalSubmissions > 0) "Unlocked 🏆" else if (stats.totalSubmissions > 0) "${stats.averageScore}% / 80% ⏳" else "0% / 80% ⏳"
    val certProStatus = if (stats.totalSubmissions >= 5) "Unlocked 🏆" else "${stats.totalSubmissions}/5 ⏳"
    val activeEducatorStatus = if (stats.totalQuizzes >= 1) "Unlocked 🏆" else "${stats.totalQuizzes}/1 ⏳"
    val reviewerStatus = if (stats.totalSubmissions >= 1) "Unlocked 🏆" else "0/1 ⏳"

    val allAchievements = listOf(
        Triple("Quiz Creator", "Created 10 quizzes", quizCreatorStatus),
        Triple("Student Champion", "Reached 50+ students", studentChampStatus),
        Triple("Top Educator", "Average class score > 80%", topEducatorStatus),
        Triple("Certificate Pro", "Issued 5+ certified awards", certProStatus),
        Triple("Active Educator", "Published at least 1 quiz", activeEducatorStatus),
        Triple("Speed Grader", "Reviewed student submissions", reviewerStatus)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Teacher Achievements", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allAchievements.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TeacherCardBg)
                            .border(1.dp, TeacherCardBorder, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.first, color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(item.second, color = TeacherTextMuted, fontSize = 11.sp)
                            }
                            Text(
                                item.third,
                                color = if (item.third.contains("Unlocked")) Color(0xFF34D399) else Color(0xFFFBBF24),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherStudentsDialog(submissions: List<QuizSessionState>, onDismiss: () -> Unit) {
    val totalSubmissions = submissions.size
    val activeToday = submissions.count {
        it.startTimeTimestamp > System.currentTimeMillis() - 24 * 60 * 60 * 1000L
    }
    val avgCompletion = if (submissions.isEmpty()) 0 else submissions.map { it.scorePercentage }.average().toInt()
    val topPerformer = submissions.maxByOrNull { it.scorePercentage }?.let {
        "${it.scorePercentage}% (${it.quiz.title.ifBlank { "Top Attempt" }})"
    } ?: "None yet"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Text("Manage Students", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total quiz submissions: $totalSubmissions", color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Text("• Active today: $activeToday attempt${if (activeToday != 1) "s" else ""}", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Average completion rate: $avgCompletion%", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Top score: $topPerformer", color = TeacherTextMuted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherAnalyticsDialog(quizzes: List<Quiz>, submissions: List<QuizSessionState>, onDismiss: () -> Unit) {
    val totalAttempts = submissions.size
    val avgScore = if (submissions.isEmpty()) 0 else submissions.map { it.scorePercentage }.average().toInt()
    val mostPopularQuiz = if (submissions.isEmpty()) {
        quizzes.firstOrNull()?.title ?: "None yet"
    } else {
        submissions.groupBy { it.quiz.id }
            .maxByOrNull { it.value.size }
            ?.let { (quizId, _) ->
                quizzes.find { it.id == quizId }?.title ?: quizId
            } ?: (quizzes.firstOrNull()?.title ?: "None yet")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Text("Performance & Analytics", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Overall Statistics", color = Color(0xFF34D399), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Text("• Total Attempts: $totalAttempts", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Average Score: $avgScore% across all subjects", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Most popular quiz: $mostPopularQuiz", color = TeacherTextMuted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherCertificatesDialog(
    submissions: List<QuizSessionState> = emptyList(),
    onDismiss: () -> Unit
) {
    val certifiedCount = submissions.count { it.scorePercentage > 80 }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Text("Issued Certificates", color = TeacherTextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Certified Students", color = Color(0xFFF472B6), fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Text("• $certifiedCount Certificates auto-issued for 80%+ scores", color = TeacherTextMuted, fontSize = 12.sp)
                Text("• Verifiable digital credentials enabled", color = TeacherTextMuted, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))) {
                Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun TeacherNotificationsDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        TeacherNotificationsScreen(
            onBackClick = onDismiss
        )
    }
}

@Composable
internal fun TeacherAllQuickActionsDialog(
    onCreateQuizClick: () -> Unit,
    onManageStudentsClick: () -> Unit,
    onViewAnalyticsClick: () -> Unit,
    onCertificatesClick: () -> Unit,
    onJoinRequestsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isTeacherInDarkTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TeacherCardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Quick Actions",
                        color = TeacherTextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TeacherTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data class QuickActionItem(
                    val title: String,
                    val subtitle: String,
                    val icon: ImageVector,
                    val color: Color,
                    val onClick: () -> Unit
                )

                val actions = listOf(
                    QuickActionItem(
                        title = "Create Quiz",
                        subtitle = "Build an AI or manual quiz",
                        icon = Icons.Default.Add,
                        color = Color(0xFF8B5CF6),
                        onClick = {
                            onDismiss()
                            onCreateQuizClick()
                        }
                    ),
                    QuickActionItem(
                        title = "Manage Students",
                        subtitle = "View & track student submissions",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF38BDF8),
                        onClick = {
                            onDismiss()
                            onManageStudentsClick()
                        }
                    ),
                    QuickActionItem(
                        title = "View Analytics",
                        subtitle = "Performance charts & insights",
                        icon = Icons.Default.BarChart,
                        color = Color(0xFF34D399),
                        onClick = {
                            onDismiss()
                            onViewAnalyticsClick()
                        }
                    ),
                    QuickActionItem(
                        title = "Certificates",
                        subtitle = "Issue and manage certificates",
                        icon = Icons.Default.CardMembership,
                        color = Color(0xFFF472B6),
                        onClick = {
                            onDismiss()
                            onCertificatesClick()
                        }
                    ),
                    QuickActionItem(
                        title = "Join Requests",
                        subtitle = "Review student class access requests",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFFFBBF24),
                        onClick = {
                            onDismiss()
                            onJoinRequestsClick()
                        }
                    ),
                    QuickActionItem(
                        title = "Notifications",
                        subtitle = "Check system updates & alerts",
                        icon = Icons.Default.Notifications,
                        color = Color(0xFF818CF8),
                        onClick = {
                            onDismiss()
                            onNotificationsClick()
                        }
                    )
                )

                actions.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                            .border(1.dp, TeacherCardBorder, RoundedCornerShape(12.dp))
                            .clickable(onClick = action.onClick)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(action.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = null,
                                tint = action.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = action.title,
                                color = TeacherTextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                            Text(
                                text = action.subtitle,
                                color = TeacherTextMuted,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = TeacherTextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}

