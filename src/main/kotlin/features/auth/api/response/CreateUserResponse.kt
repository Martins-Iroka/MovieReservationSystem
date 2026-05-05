package com.martdev.features.auth.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserResponse(
    @SerialName("email_id")
    val emailId: String,
    val token: String
)
