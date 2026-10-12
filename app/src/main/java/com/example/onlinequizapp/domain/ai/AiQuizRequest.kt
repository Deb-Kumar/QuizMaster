package com.example.onlinequizapp.domain.ai

import com.example.onlinequizapp.domain.model.Difficulty
import com.example.onlinequizapp.domain.model.QuestionType

/**
 * Request payload for automated AI quiz question generation.
 */
data class AiQuizRequest(
    val topicOrTitle: String,
    val category: String,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val questionCount: Int = 5,
    val questionType: QuestionType = QuestionType.SINGLE,
    val additionalInstructions: String = ""
)
