package com.example.onlinequizapp.domain.model

enum class QuestionType {
    SINGLE,
    MULTIPLE,
    MIXED
}

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

data class Option(
    val id: String,
    val text: String
)

data class Question(
    val id: String,
    val type: QuestionType,
    val prompt: String,
    val options: List<Option>,
    val correctAnswers: Set<String>,
    val explanation: String,
    val hint: String? = null,
    val difficulty: Difficulty = Difficulty.MEDIUM
) {
    init {
        require(options.size >= 2) { "Question must contain at least 2 options" }
        require(correctAnswers.isNotEmpty()) { "Question must have at least one correct answer" }
        val optionIds = options.map { it.id }.toSet()
        require(optionIds.containsAll(correctAnswers)) { "All correctAnswers must match valid option IDs" }
    }
}
