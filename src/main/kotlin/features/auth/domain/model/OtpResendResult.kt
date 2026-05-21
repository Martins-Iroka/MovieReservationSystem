package com.martdev.features.auth.domain.model

data class OtpResendResult(
    val emailId: String,
    val verificationToken: String
)
