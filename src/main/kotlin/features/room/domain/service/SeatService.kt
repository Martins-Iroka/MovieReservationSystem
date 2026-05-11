package com.martdev.features.room.domain.service

import com.martdev.features.room.domain.model.Seat

interface SeatService {
    suspend fun createSeats(seats: List<Seat>)
    suspend fun getSeatsByRoomId(roomId: Long): List<Seat>
    suspend fun getSeatById(seatId: Long): Seat
}