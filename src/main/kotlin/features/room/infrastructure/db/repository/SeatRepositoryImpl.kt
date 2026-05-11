package com.martdev.features.room.infrastructure.db.repository

import com.martdev.features.room.domain.model.Seat
import com.martdev.features.room.domain.repository.SeatRepository
import com.martdev.features.room.infrastructure.db.tables.RoomTable
import com.martdev.features.room.infrastructure.db.tables.SeatEntity
import com.martdev.features.room.infrastructure.db.tables.SeatTable
import com.martdev.features.room.infrastructure.db.tables.toSeat
import com.martdev.shared.domain.model.DataResult
import com.martdev.shared.infrastruce.db.withSuspendTransaction
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.koin.core.annotation.Single

@Single
class SeatRepositoryImpl : SeatRepository {
    override suspend fun createSeats(seat: List<Seat>): DataResult<List<Seat>> {
        return withSuspendTransaction {
            val savedSeats = SeatTable.batchInsert(seat) {
                this[SeatTable.roomId] = EntityID(it.roomId, RoomTable)
                this[SeatTable.rowLabel] = it.rowLabel
                this[SeatTable.seatNumber] = it.seatNumber
            }.map {
                SeatEntity.wrapRow(it).toSeat()
            }
            DataResult.Success(savedSeats)
        }
    }

    override suspend fun getSeatsByRoomId(roomId: Long): DataResult<List<Seat>> {
        return withSuspendTransaction {
            val seats = SeatEntity.find { SeatTable.roomId eq roomId }.map {
                it.toSeat()
            }
            DataResult.Success(seats)
        }
    }

    override suspend fun getSeatById(seatId: Long): DataResult<Seat> {
        return withSuspendTransaction {
            val seat = SeatEntity.findById(seatId)?.toSeat()
                ?: return@withSuspendTransaction DataResult.Failure.NotFound("Seat not found")

            DataResult.Success(seat)
        }
    }
}