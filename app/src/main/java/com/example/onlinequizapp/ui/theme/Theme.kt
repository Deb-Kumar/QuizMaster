package com.example.onlinequizapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = PureWhite,
    primaryContainer = Slate100,
    onPrimaryContainer = BrandPrimaryDark,
    secondary = SuccessGreen,
    onSecondary = PureWhite,
    secondaryContainer = SuccessGreenBg,
    onSecondaryContainer = SuccessGreen,
    tertiary = AccentAmber,
    onTertiary = PureWhite,
    error = DangerRed,
    onError = PureWhite,
    errorContainer = DangerRedBg,
    onErrorContainer = DangerRed,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    outlineVariant = Slate300
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = BrandPrimaryLight,
    secondary = SuccessGreenLight,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = SuccessGreenLight,
    tertiary = AccentAmberLight,
    onTertiary = Slate950,
    error = DangerRedLight,
    onError = Slate950,
    errorContainer = Slate800,
    onErrorContainer = DangerRedLight,
    background = Slate900,
    onBackground = Slate50,
    surface = Slate800,
    onSurface = Slate50,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    outlineVariant = Slate600
)

// Composition local that tells every composable whether we're in dark mode
val LocalQuizDark = androidx.compose.runtime.compositionLocalOf { true }

@Composable
fun QuizMasterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalQuizDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QuizTypography,
            content = content
        )
    }
}
