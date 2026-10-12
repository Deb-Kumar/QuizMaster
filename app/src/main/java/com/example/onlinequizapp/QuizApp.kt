package com.example.onlinequizapp

import android.app.Application
import com.example.onlinequizapp.data.session.AuthSessionManager
import com.example.onlinequizapp.data.session.QuizDraftManager
import com.example.onlinequizapp.data.session.QuizResultStorageManager
import com.example.onlinequizapp.ui.theme.ThemeManager

class QuizApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ThemeManager.init(this)
        AuthSessionManager.init(this)
        QuizDraftManager.init(this)
        QuizResultStorageManager.init(this)
        com.example.onlinequizapp.data.session.AiConfigManager.init(this)
        com.example.onlinequizapp.data.session.CategoryManager.init(this)
    }
}

