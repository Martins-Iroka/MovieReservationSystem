package com.martdev.features.auth.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserVerificationRequest(
    val code: String,
    @SerialName("email_id")
    val emailId: String,
    val token: String
)
