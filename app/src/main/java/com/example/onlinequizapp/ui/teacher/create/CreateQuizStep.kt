package com.example.onlinequizapp.ui.teacher.create

enum class CreateQuizStep {
    METHOD_SELECT,     // Step 1: Manual vs AI
    BASIC_INFO,        // Step 2: Configure Quiz (Title, Category, Difficulty, Count, Time, Hints)
    PROVIDER_SELECT,   // Step 3 (AI): Model Selection (Gemini, OpenAI, Claude, DeepSeek, OpenRouter, AgentRouter)
    QUESTION_REVIEW    // Step 4: Review, Edit, Delete, Add, Reorder & Publish
}
