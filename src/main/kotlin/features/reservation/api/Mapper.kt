package com.martdev.features.reservation.api

import com.martdev.features.reservation.domain.model.Reservation
import com.martdev.features.reservation.domain.model.ShowtimeSeat

fun Reservation.toReservationDTO() = ReservationDTO(
    id = id,
    userId = userId,
    showtimeId = showtimeId,
    status = status.name,
    totalAmount = totalAmount,
    seats = seats.map { it.toShowtimeSeatDTO() },
    createdAt = createdAt,
    expiresAt = expiresAt
)

fun ShowtimeSeat.toShowtimeSeatDTO() = ShowtimeSeatDTO(
    id = id,
    showtimeId = showtimeId,
    seatId = seatId,
    reservationId = reservationId,
    status = status.name
)