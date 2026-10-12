package com.example.onlinequizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.domain.model.UserRole
import com.example.onlinequizapp.ui.navigation.QuizNavGraph
import com.example.onlinequizapp.ui.navigation.Screen
import com.example.onlinequizapp.ui.theme.QuizMasterTheme
import com.example.onlinequizapp.ui.theme.ThemeManager
import com.example.onlinequizapp.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore the persisted theme preference before composing UI
        ThemeManager.init(this)
        AuthSessionManager.init(this)
        com.example.onlinequizapp.data.session.QuizDraftManager.init(this)
        com.example.onlinequizapp.data.session.QuizResultStorageManager.init(this)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val isSystemDark = isSystemInDarkTheme()
            val isDark = when (themeMode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> isSystemDark
            }
            QuizMasterTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    QuizNavGraph(navController = navController, startDestination = Screen.Splash.route)
                }
            }
        }
    }
}
