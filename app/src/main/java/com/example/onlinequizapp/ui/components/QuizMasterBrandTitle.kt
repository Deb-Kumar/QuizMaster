package com.example.onlinequizapp.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

/**
 * Standardized, unified QuizMaster brand header used across:
 * - Student Home (`HomeScreen.kt` via `HomeTopBar.kt`)
 * - Student My Quizzes (`MyQuizzesScreen.kt`)
 * - Student History (`StudentHistoryScreen.kt`)
 * - Student Profile (`ProfileTabContent.kt`)
 *
 * Guarantees identical font size, font weight, letter spacing, and
 * cohesive styling across both Light and Dark themes.
 */
@Composable
fun QuizMasterBrandTitle(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    isDarkTheme: Boolean? = null
) {
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = isDarkTheme ?: when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemDark
    }

    // In dark theme: Crisp White "Quiz" + luminous lavender-violet gradient "Master"
    // In light theme: Deep Slate "Quiz" + rich indigo-purple gradient "Master"
    val quizColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val masterGradient = if (isDark) {
        Brush.horizontalGradient(
            listOf(Color(0xFF818CF8), Color(0xFFC084FC))
        )
    } else {
        Brush.horizontalGradient(
            listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        )
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Quiz",
            style = TextStyle(
                color = quizColor,
                fontWeight = fontWeight,
                fontSize = fontSize,
                letterSpacing = (-0.3).sp
            )
        )
        Text(
            text = "Master",
            style = TextStyle(
                brush = masterGradient,
                fontWeight = fontWeight,
                fontSize = fontSize,
                letterSpacing = (-0.3).sp
            )
        )
    }
}
