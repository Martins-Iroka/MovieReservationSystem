package com.martdev.features.room.domain.service

import com.martdev.features.room.domain.model.Seat
import com.martdev.features.room.domain.repository.SeatRepository
import com.martdev.shared.util.returnValue
import org.koin.core.annotation.Single

@Single
class SeatServiceImpl(
    private val repository: SeatRepository
) : SeatService {
    override suspend fun createSeats(seats: List<Seat>): List<Seat> {
        return repository.createSeats(seats).returnValue()
    }

    override suspend fun getSeatsByRoomId(roomId: Long): List<Seat> {
        return repository.getSeatsByRoomId(roomId).returnValue()
    }

    override suspend fun getSeatById(seatId: Long): Seat {
        return repository.getSeatById(seatId).returnValue()
    }
}