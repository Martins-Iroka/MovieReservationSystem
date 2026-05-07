package com.martdev.config

import com.martdev.shared.util.getEnvValue
import io.ktor.server.application.*

data class JWTConfig(
    val secret: String = "",
    val exp: Long = 0,
    val issuer: String = "",
    val audience: String = ""
) {
    companion object {
        fun fromEnvironment(environment: ApplicationEnvironment): JWTConfig {
            return JWTConfig(
                secret = environment.getEnvValue("jwt.secret"),
                exp = environment.getEnvValue("jwt.expirationMinutes").toLongOrNull() ?: 15,
                issuer = environment.getEnvValue("jwt.issuer"),
                audience = environment.getEnvValue("jwt.audience")
            )
        }
    }
}
