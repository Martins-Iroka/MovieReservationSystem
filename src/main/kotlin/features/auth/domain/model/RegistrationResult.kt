package com.martdev.features.auth.domain.model

data class RegistrationResult(
    val emailId: String,
    val registrationToken: String
)
