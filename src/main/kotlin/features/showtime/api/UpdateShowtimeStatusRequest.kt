package com.martdev.features.showtime.api

import kotlinx.serialization.Serializable

@Serializable
data class UpdateShowtimeStatusRequest(val status: String)