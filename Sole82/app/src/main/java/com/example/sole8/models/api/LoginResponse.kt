package com.example.sole8.models.api

data class LoginResponse(
    val token: String,
    val user: UserProfileDto
)