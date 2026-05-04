package com.martdev.features.auth.domain.security

interface PasswordHasher {
    fun hashPassword(password: String): String
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean
}