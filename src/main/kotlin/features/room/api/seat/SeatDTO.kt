package com.martdev.features.room.api.seat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SeatDTO(
    val id: Long = 0,
    @SerialName("room_id")
    val roomId: Long = 0,
    @SerialName("row_label")
    val rowLabel: String = "",
    @SerialName("seat_number")
    val seatNumber: Int = 0
)
