package com.martdev.features.reservation.infrastructure.db.repository

import com.martdev.features.auth.infrastructure.db.tables.UserTable
import com.martdev.features.reservation.domain.model.Reservation
import com.martdev.features.reservation.domain.model.ReservationStatus
import com.martdev.features.reservation.domain.model.SeatStatus
import com.martdev.features.reservation.domain.repository.ReservationRepository
import com.martdev.features.reservation.infrastructure.db.table.ReservationEntity
import com.martdev.features.reservation.infrastructure.db.table.ReservationTable
import com.martdev.features.reservation.infrastructure.db.table.ShowtimeSeatTable
import com.martdev.features.reservation.infrastructure.db.table.toReservation
import com.martdev.features.showtime.infrastructure.db.table.ShowtimeTable
import com.martdev.shared.domain.model.DataResult
import com.martdev.shared.infrastruce.db.withSuspendTransaction
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.koin.core.annotation.Single
import kotlin.time.Clock

@Single
class ReservationRepositoryImpl : ReservationRepository {
    override suspend fun createReservation(
        reservation: Reservation,
        seatIds: List<Long>
    ): DataResult<Reservation> = withSuspendTransaction {
        // Lock rows to prevent concurrent booking (SELECT FOR UPDATE)
        val lockedSeats = ShowtimeSeatTable
            .selectAll()
            .where {
                (ShowtimeSeatTable.showtimeId eq reservation.showtimeId) and
                        (ShowtimeSeatTable.seatId inList seatIds)
            }
            .forUpdate()
            .toList()

        // All requested seats must exist for this showtime
        if (lockedSeats.size != seatIds.size) {
            rollback()
            return@withSuspendTransaction DataResult.Failure.NotFound(
                "Some seats do not exist for this showtime. Ensure showtime seats have been populated."
            )
        }

        // All request seats must be AVAILABLE
        val unavailableSeats = lockedSeats.filter { it[ShowtimeSeatTable.status] != SeatStatus.AVAILABLE }
        if (unavailableSeats.isNotEmpty()) {
            rollback()
            return@withSuspendTransaction DataResult.Failure.Conflict(
                "One or more selected seats are no longer available"
            )
        }

        // Create reservation
        val entity = ReservationEntity.new {
            userId = EntityID(reservation.userId, UserTable)
            showtimeId = EntityID(reservation.showtimeId, ShowtimeTable)
            status = ReservationStatus.PENDING
            totalAmount = reservation.totalAmount
            expiresAt = reservation.expiresAt
        }

        // Hold the seats
        ShowtimeSeatTable.update({
            (ShowtimeSeatTable.showtimeId eq reservation.showtimeId) and
                    (ShowtimeSeatTable.seatId inList seatIds)
        }) {
            it[ShowtimeSeatTable.status] = SeatStatus.HELD
            it[ShowtimeSeatTable.reservationId] = entity.id
        }

        DataResult.Success(entity.toReservation())
    }

    override suspend fun getReservationById(id: Long): DataResult<Reservation> = withSuspendTransaction {
        val entity = ReservationEntity.findById(id)
            ?: return@withSuspendTransaction DataResult.Failure.NotFound("Reservation not found.")
        DataResult.Success(entity.toReservation())
    }

    override suspend fun getReservationsByUserId(userId: Long): DataResult<List<Reservation>> = withSuspendTransaction {
        val reservations = ReservationEntity.find {
            ReservationTable.userId eq userId
        }.orderBy(ReservationTable.createdAt to SortOrder.DESC)
            .map { it.toReservation() }

        DataResult.Success(reservations)
    }

    override suspend fun getAllReservations(limit: Int, offset: Long): DataResult<List<Reservation>> =
        withSuspendTransaction {
            val reservations = ReservationEntity.all()
                .limit(limit)
                .offset(offset)
                .orderBy(ReservationTable.createdAt to SortOrder.DESC)
                .map { it.toReservation() }
            DataResult.Success(reservations)
        }

    override suspend fun updateReservationStatus(
        id: Long,
        status: ReservationStatus
    ): DataResult<Reservation> = withSuspendTransaction {
        val entity = ReservationEntity.findById(id)
            ?: return@withSuspendTransaction DataResult.Failure.NotFound("Reservation not found.")

        entity.status = status

        when (status) {
            ReservationStatus.CONFIRMED -> {
                ShowtimeSeatTable.update({ ShowtimeSeatTable.reservationId eq entity.id }) {
                    it[ShowtimeSeatTable.status] = SeatStatus.BOOKED
                }
            }

            ReservationStatus.CANCELLED -> {
                ShowtimeSeatTable.update({ ShowtimeSeatTable.reservationId eq entity.id }) {
                    it[ShowtimeSeatTable.status] = SeatStatus.AVAILABLE
                    it[ShowtimeSeatTable.reservationId] = null
                }
            }

            else -> {}
        }

        DataResult.Success(entity.toReservation())
    }

    override suspend fun cancelExpiredReservation(): DataResult<Unit> = withSuspendTransaction {
        val now = Clock.System.now()

        val expired = ReservationEntity.find {
            (ReservationTable.status eq ReservationStatus.PENDING) and
                    (ReservationTable.expiresAt less now)
        }.toList()

        expired.forEach { reservation ->
            reservation.status = ReservationStatus.CANCELLED
            ShowtimeSeatTable.update({ ShowtimeSeatTable.reservationId eq reservation.id }) {
                it[ShowtimeSeatTable.status] = SeatStatus.AVAILABLE
                it[ShowtimeSeatTable.reservationId] = null
            }
        }

        DataResult.Success(Unit)
    }
}