package com.martdev.features.reservation.observability

interface ReservationEvents {
    fun checkoutStarted(showtimeId: Long, seatCount: Int)
    fun seatHoldExpired(showtimeId: Long, userId: Long)
    fun reservationCompleted(reservationId: Long)
    fun reservationCancelled(reservationId: Long, reason: String)
}