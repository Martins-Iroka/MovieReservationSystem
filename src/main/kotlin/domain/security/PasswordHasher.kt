package com.martdev.domain.security

interface PasswordHasher {
    fun hashPassword(password: String): String
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean
}