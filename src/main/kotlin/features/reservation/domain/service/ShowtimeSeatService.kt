package com.martdev.features.reservation.domain.service

import com.martdev.features.reservation.domain.model.ShowtimeSeat

interface ShowtimeSeatService {
    suspend fun populateShowtimeSeats(showtimeId: Long)
    suspend fun getAvailableSeats(showtimeId: Long): List<ShowtimeSeat>
    suspend fun getAllSeatsByShowtime(showtimeId: Long): List<ShowtimeSeat>
}