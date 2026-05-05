package com.martdev.features.auth.domain.model

data class UserData(
    val id: Long = 0,
    val email: String = "",
    val password: String = "",
    val isVerified: Boolean = false,
    val role: Role = Role.USER,
    val emailId: String = "",
    val status: String = "",
    val code: String = "",
    val token: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
    val verificationToken: String = "",
)
