package com.example.onlinequizapp.domain.model

data class UserFeedback(
    val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val role: String,
    val rating: Int,
    val category: String,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
