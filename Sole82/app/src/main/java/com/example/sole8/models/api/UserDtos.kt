package com.example.sole8.models.api

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserRegisterRequest(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val gender: String,
    val username: String,
    val email: String,
    val password: String
)

data class UserProfileDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val gender: String,
    val username: String,
    val email: String
)

data class UserProfileUpdate(
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val gender: String,
    val username: String,
    val email: String
)

