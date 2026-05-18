package com.martdev.features.reservation.domain.repository

import com.martdev.features.reservation.domain.model.ShowtimeSeat
import com.martdev.shared.domain.model.DataResult

interface ShowtimeSeatRepository {
    suspend fun populateShowtimeSeats(showtimeId: Long, seatIds: List<Long>): DataResult<Unit>
    suspend fun getAvailableSeats(showtimeId: Long): DataResult<List<ShowtimeSeat>>
    suspend fun getAllSeatsByShowtime(showtimeId: Long): DataResult<List<ShowtimeSeat>>
}