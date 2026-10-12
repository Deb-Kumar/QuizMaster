package com.example.onlinequizapp.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Represents the three app theme modes. */
enum class ThemeMode {
    DARK,   // Always dark (cyberpunk)
    LIGHT,  // Always light
    SYSTEM  // Follow device setting
}

/**
 * Process-level singleton that holds the selected [ThemeMode] and persists
 * it to SharedPreferences so the choice survives app restarts.
 */
object ThemeManager {

    private const val PREFS_NAME = "quiz_master_prefs"
    private const val KEY_THEME = "theme_mode"

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    /** Call once from Application or MainActivity to restore the saved preference. */
    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        _themeMode.value = runCatching { ThemeMode.valueOf(saved) }.getOrDefault(ThemeMode.DARK)
    }

    /** Update the theme and persist the new choice. */
    fun setTheme(context: Context, mode: ThemeMode) {
        _themeMode.value = mode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, mode.name)
            .apply()
    }
}
