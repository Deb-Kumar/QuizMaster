package com.example.onlinequizapp.ui.home.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.onlinequizapp.ui.components.consumeClicks
import com.example.onlinequizapp.ui.components.dismissOnClickOutside
import com.example.onlinequizapp.ui.home.LocalQuizDark
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun NotificationsDialog(
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var quizReminders by remember { mutableStateOf(true) }
    var streakAlerts by remember { mutableStateOf(true) }
    var leaderboards by remember { mutableStateOf(true) }
    var soundEffects by remember { mutableStateOf(false) }

    val isDark = LocalQuizDark.current
    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val cardBg = if (isDark) Color(0xFF1E293B).copy(alpha = 0.65f) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
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
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Notifications",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Quiz alerts & sound preferences",
                                style = TextStyle(
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

                ModalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Toggle list in card container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    ModernNotificationToggleRow(
                        title = "Daily Quiz Reminders",
                        subtitle = "Get notified to keep your day streak active",
                        isChecked = quizReminders,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        isDark = isDark,
                        onCheckedChange = { quizReminders = it }
                    )
                    ModalDivider(color = cardBorder)
                    ModernNotificationToggleRow(
                        title = "Streak & Rank Alerts",
                        subtitle = "Updates on leaderboard badges & progress",
                        isChecked = streakAlerts,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        isDark = isDark,
                        onCheckedChange = { streakAlerts = it }
                    )
                    ModalDivider(color = cardBorder)
                    ModernNotificationToggleRow(
                        title = "New Quiz Announcements",
                        subtitle = "Alerts when new questions & topics publish",
                        isChecked = leaderboards,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        isDark = isDark,
                        onCheckedChange = { leaderboards = it }
                    )
                    ModalDivider(color = cardBorder)
                    ModernNotificationToggleRow(
                        title = "Sound Effects & Haptics",
                        subtitle = "In-quiz answer feedback sounds & vibrations",
                        isChecked = soundEffects,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        isDark = isDark,
                        onCheckedChange = { soundEffects = it }
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = textSecondary
                        )
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD97706)
                        )
                    ) {
                        Text("Save Preferences", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun ModernNotificationToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(text = title, color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = subtitle, color = textSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFD97706),
                uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color(0xFFCBD5E1),
                uncheckedTrackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            )
        )
    }
}

@Composable
internal fun ThemeDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(ThemeManager.themeMode.value) }
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalQuizDark.current

    val dialogBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF1E1B4B)
    val textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .dismissOnClickOutside(onDismiss)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .consumeClicks()
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row: Avatar Icon / Logo + Title + Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Appearance",
                                style = TextStyle(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                            Text(
                                text = "Choose your display theme",
                                style = TextStyle(
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

                ModalDivider(color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppearanceOptionRow(
                        title = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = selectedMode == ThemeMode.LIGHT,
                        textPrimary = textPrimary,
                        onSelect = {
                            selectedMode = ThemeMode.LIGHT
                            coroutineScope.launch {
                                delay(150)
                                onThemeSelected(ThemeMode.LIGHT)
                            }
                        }
                    )

                    AppearanceOptionRow(
                        title = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = selectedMode == ThemeMode.DARK,
                        textPrimary = textPrimary,
                        onSelect = {
                            selectedMode = ThemeMode.DARK
                            coroutineScope.launch {
                                delay(150)
                                onThemeSelected(ThemeMode.DARK)
                            }
                        }
                    )

                    AppearanceOptionRow(
                        title = "System default",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = selectedMode == ThemeMode.SYSTEM,
                        textPrimary = textPrimary,
                        onSelect = {
                            selectedMode = ThemeMode.SYSTEM
                            coroutineScope.launch {
                                delay(150)
                                onThemeSelected(ThemeMode.SYSTEM)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun AppearanceOptionRow(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    textPrimary: Color,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                color = textPrimary,
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
