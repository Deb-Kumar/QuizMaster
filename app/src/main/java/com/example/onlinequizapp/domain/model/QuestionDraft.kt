package com.example.onlinequizapp.domain.model

import java.util.UUID

/**
 * Mutable draft presentation model representing a question under construction or review.
 */
data class QuestionDraft(
    val id: String = UUID.randomUUID().toString().take(8),
    val prompt: String = "",
    val type: QuestionType = QuestionType.SINGLE,
    val options: List<String> = listOf("", "", "", ""),
    val correctAnswers: Set<String> = setOf("A"),
    val hint: String = "",
    val explanation: String = ""
)
