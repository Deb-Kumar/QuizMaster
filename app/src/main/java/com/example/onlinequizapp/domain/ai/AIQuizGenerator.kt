package com.example.onlinequizapp.domain.ai

import com.example.onlinequizapp.domain.model.QuestionDraft

/**
 * Common, provider-independent interface for AI-assisted quiz and question generation.
 */
interface AIQuizGenerator {
    val provider: AiProvider

    /**
     * Generates a validated list of question drafts based on teacher parameters.
     */
    suspend fun generateQuestions(
        request: AiQuizRequest,
        apiKey: String
    ): Result<List<QuestionDraft>>
}
