package com.martdev.features.auth.api.request

import kotlinx.serialization.Serializable

@Serializable
data class ResendOTPRequest(
    val email: String
)
