package com.martdev.config

data class JWTConfig(
    val secret: String = "",
    val exp: Long = 0,
    val issuer: String = "",
    val audience: String = ""
)
