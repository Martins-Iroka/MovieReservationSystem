package com.martdev.features.auth.domain.model

data class RefreshResult(
    val accessToken: String,
    val refreshToken: String
)
