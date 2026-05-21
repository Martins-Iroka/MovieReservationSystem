package com.martdev.features.auth.domain.model

data class LoginResult(
    val userId: Long,
    val accessToken: String,
    val refreshToken: String
)
