package com.martdev.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)