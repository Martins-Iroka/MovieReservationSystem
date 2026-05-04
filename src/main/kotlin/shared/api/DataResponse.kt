package com.martdev.shared.api

import kotlinx.serialization.Serializable

@Serializable
data class DataResponse<T>(
    val data: T
)
