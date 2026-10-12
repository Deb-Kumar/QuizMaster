package com.example.onlinequizapp.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Re-export the global LocalQuizDark provided by QuizMasterTheme
internal val LocalQuizDark = com.example.onlinequizapp.ui.theme.LocalQuizDark

// Semantic color tokens — @Composable getters so they react to theme changes
internal val HomeDarkBg: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF04081E) else Color(0xFFF0F4FF)
internal val DarkCardSurface: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF0A102D) else Color(0xFFFFFFFF)
internal val DarkCardBorder: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF19244D) else Color(0xFFDDE3F5)
internal val TextWhite: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFFFFFFFF) else Color(0xFF0F172A)
internal val TextMuted: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF94A3B8) else Color(0xFF475569)
internal val TextDim: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF64748B) else Color(0xFF94A3B8)

// Additional semantic tokens for dialogs, dividers, and input fields
internal val DialogSurface: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF0E1638) else Color(0xFFFFFFFF)
internal val CardSurfaceAlt: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF131B3E) else Color(0xFFF8FAFF)
internal val InputBorderUnfocused: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF263366) else Color(0xFFCBD5E1)
internal val NavBorder: Color
    @Composable get() = if (LocalQuizDark.current) Color(0xFF0F1738) else Color(0xFFE2E8F0)

internal val LogoGradient = Brush.linearGradient(
    listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
)
internal val HomeBrandGradient = Brush.horizontalGradient(
    listOf(Color(0xFF818CF8), Color(0xFFC084FC))
)
