package com.martdev.features.auth.domain.model

data class VerificationInput(
    val code: String,
    val emailId: String,
    val registrationToken: String
)
