package com.martdev.features.auth.domain.model

data class User(
    val id: Long = 0,
    val email: String = "",
    val password: String = "",
    val isVerified: Boolean = false,
    val role: Role = Role.USER
)
