package com.martdev.features.reservation.domain.service

import com.martdev.features.reservation.domain.model.ShowtimeSeat
import com.martdev.features.reservation.domain.repository.ShowtimeSeatRepository
import com.martdev.features.room.domain.service.SeatService
import com.martdev.features.showtime.domain.service.ShowtimeService
import com.martdev.shared.util.returnValue
import org.koin.core.annotation.Single

@Single
class ShowtimeSeatServiceImpl(
    private val repo: ShowtimeSeatRepository,
    private val showtimeService: ShowtimeService,
    private val seatService: SeatService
) : ShowtimeSeatService {
    override suspend fun populateShowtimeSeats(showtimeId: Long) {
        val showtime = showtimeService.getShowtimeById(showtimeId)
        val seatIds = seatService.getSeatsByRoomId(showtime.roomId).map { it.id }
        repo.populateShowtimeSeats(showtimeId, seatIds).returnValue()
    }

    override suspend fun getAvailableSeats(showtimeId: Long): List<ShowtimeSeat> {
        return repo.getAvailableSeats(showtimeId).returnValue()
    }

    override suspend fun getAllSeatsByShowtime(showtimeId: Long): List<ShowtimeSeat> {
        return repo.getAllSeatsByShowtime(showtimeId).returnValue()
    }
}