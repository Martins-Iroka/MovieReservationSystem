package com.martdev.plugins

import com.martdev.features.auth.api.request.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+[a-zA-Z]{2,}$")
    install(RequestValidation) {
        validate<CreateUserRequest> { request ->
            when {
                request.email.isEmpty() || !emailPattern.matches(request.email) -> invalidResponseResult("Invalid email or password")
                request.password.isEmpty() -> invalidResponseResult("Invalid email or password")
                else -> ValidationResult.Valid
            }
        }

        validate<UserVerificationRequest> { request ->
            when {
                request.code.isEmpty() || request.code.length != 6 -> invalidResponseResult("Code is not valid")
                request.emailId.isEmpty() -> invalidResponseResult("Email id is needed")
                request.token.isEmpty() -> invalidResponseResult("Token is needed")
                else -> ValidationResult.Valid
            }
        }

        validate<UserLoginRequest> { request ->
            when {
                request.email.isEmpty() || !emailPattern.matches(request.email) -> invalidResponseResult("Invalid email or password")
                request.password.isEmpty() -> invalidResponseResult("Invalid email or password")
                else -> ValidationResult.Valid
            }
        }

        validate<ResendOTPRequest> { request ->
            if (request.email.isEmpty() || !emailPattern.matches(request.email)) {
                ValidationResult.Invalid("Invalid email")
            } else ValidationResult.Valid
        }

        validate<RefreshTokenRequest> { request ->
            if (request.refreshToken.isEmpty()) {
                ValidationResult.Invalid("Invalid refresh token")
            } else ValidationResult.Valid
        }
    }
}

private fun invalidResponseResult(message: String) = ValidationResult.Invalid(message)
