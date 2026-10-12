package com.example.onlinequizapp.data.ai

import com.example.onlinequizapp.domain.ai.AiQuizRequest
import com.example.onlinequizapp.domain.model.QuestionType

object AIQuizPromptHelper {

    fun buildPrompt(request: AiQuizRequest): String {
        val qTypeDesc = when (request.questionType) {
            QuestionType.SINGLE -> "Single-choice questions where each question has EXACTLY 1 correct answer (e.g. \"A\")"
            QuestionType.MULTIPLE -> "Multiple-choice questions where each question has 2 or more correct answers (e.g. [\"A\", \"C\"] or \"A, C\")"
            QuestionType.MIXED -> "Mixed format (Single choice and Multiple choice): Generate a realistic blend of BOTH single-choice questions (with exactly 1 correct answer) AND multiple-choice questions (with 2 or more correct answers)"
        }

        return """
            You are an expert academic educator and examiner.
            Generate exactly ${request.questionCount} high quality questions for an online quiz.

            Configuration:
            - Topic/Title: ${request.topicOrTitle}
            - Category: ${request.category}
            - Difficulty Level: ${request.difficulty.label}
            - Question Type: $qTypeDesc
            ${if (request.additionalInstructions.isNotBlank()) "- Additional Teacher Notes: " + request.additionalInstructions else ""}

            CRITICAL OUTPUT FORMAT:
            You MUST return ONLY a valid JSON array of objects. Do NOT include any markdown introduction, explanations, or commentary outside the JSON array.
            Each object in the array MUST strictly have these exact keys:
            [
              {
                "question": "Clear and concise question text",
                "options": ["Option A text", "Option B text", "Option C text", "Option D text"],
                "correctAnswer": "A",
                "correctAnswers": ["A"],
                "explanation": "Detailed explanation of why this answer is correct",
                "hint": "Short helpful clue for students"
              }
            ]

            Ensure options are distinct, plausible, and the correct option letter(s) (A, B, C, or D) accurately correspond to the correct option in the options array. For questions with multiple correct answers, include all valid option letters in "correctAnswers" (e.g. ["A", "C"]).
        """.trimIndent()
    }
}
