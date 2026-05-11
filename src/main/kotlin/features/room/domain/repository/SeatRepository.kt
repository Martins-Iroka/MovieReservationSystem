package com.martdev.features.room.domain.repository

import com.martdev.features.room.domain.model.Seat
import com.martdev.shared.domain.model.DataResult

interface SeatRepository {
    suspend fun createSeats(seat: List<Seat>): DataResult<List<Seat>>
    suspend fun getSeatsByRoomId(roomId: Long): DataResult<List<Seat>>
    suspend fun getSeatById(seatId: Long): DataResult<Seat>
}