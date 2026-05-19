package com.martdev.features.reservation.domain.repository

import com.martdev.features.reservation.domain.model.Reservation
import com.martdev.features.reservation.domain.model.ReservationStatus
import com.martdev.shared.domain.model.DataResult

interface ReservationRepository {
    suspend fun createReservation(reservation: Reservation, seatIds: List<Long>): DataResult<Reservation>
    suspend fun getReservationById(id: Long): DataResult<Reservation>
    suspend fun getReservationsByUserId(userId: Long): DataResult<List<Reservation>>
    suspend fun getAllReservations(limit: Int, offset: Long): DataResult<List<Reservation>>
    suspend fun updateReservationStatus(id: Long, status: ReservationStatus): DataResult<Reservation>
    suspend fun cancelExpiredReservation(): DataResult<Unit>
}