package com.example.onlinequizapp.ui.teacher

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

// -------------------------------------------------------------
// Adaptive Palette for Teacher Side (Cyberpunk Dark & Crisp Light)
// -------------------------------------------------------------
internal val TeacherBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF030718) else Color(0xFFF1F5F9)

internal val TeacherCardBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF0A102E) else Color(0xFFFFFFFF)

internal val TeacherCardBorder: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF16214C) else Color(0xFFE2E8F0)

internal val TeacherTextWhite: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)

internal val TeacherTextMuted: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF64748B)

internal val TeacherTextDim: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF64748B) else Color(0xFF64748B)

internal val TeacherBrandGradient = Brush.horizontalGradient(
    listOf(Color(0xFF818CF8), Color(0xFFC084FC))
)

// -------------------------------------------------------------
// Quiz Builder Adaptive Design Tokens
// -------------------------------------------------------------
internal val QuizBuilderCanvasGradient: List<Color>
    @Composable get() = if (isTeacherInDarkTheme()) {
        listOf(Color(0xFF030A24), Color(0xFF070E32), Color(0xFF040618))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    }

internal val QuizBuilderCardBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF091030).copy(alpha = 0.88f) else Color(0xFFFFFFFF)

internal val QuizBuilderCardBorder: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF16255C) else Color(0xFFE2E8F0)

internal val QuizBuilderInputBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF050B22).copy(alpha = 0.85f) else Color(0xFFF8FAFC)

internal val QuizBuilderInputBorder: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF1B2E6C) else Color(0xFFCBD5E1)

internal val QuizBuilderChipBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF0C1438) else Color(0xFFF1F5F9)

internal val QuizBuilderChipBorder: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF243675) else Color(0xFFE2E8F0)

internal val QuizBuilderTextPrimary: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF0F172A)

internal val QuizBuilderTextSecondary: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF94A3B8) else Color(0xFF475569)

internal val QuizBuilderTextMuted: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)

internal val QuizBuilderNavBtnBg: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF1E2640).copy(alpha = 0.7f) else Color(0xFFFFFFFF)

internal val QuizBuilderNavBtnBorder: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF334168).copy(alpha = 0.5f) else Color(0xFFE2E8F0)

internal val QuizBuilderNavBtnTint: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color.White else Color(0xFF0F172A)

internal val QuizBuilderStepperInactive: Color
    @Composable get() = if (isTeacherInDarkTheme()) Color(0xFF141E42) else Color(0xFFE2E8F0)

@Composable
internal fun isTeacherInDarkTheme(): Boolean {
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    return when (themeMode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> isSystemDark
    }
}

