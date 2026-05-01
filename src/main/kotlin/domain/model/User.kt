package com.martdev.domain.model

enum class Role {
    ADMIN, USER
}

data class User(
    val id: Long = 0,
    val email: String = "",
    val password: String = "",
    val isVerified: Boolean = false,
    val role: Role = Role.USER
)
