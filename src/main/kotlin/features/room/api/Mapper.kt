package com.martdev.features.room.api

import com.martdev.features.room.api.room.RoomDTO
import com.martdev.features.room.api.seat.SeatDTO
import com.martdev.features.room.domain.model.Room
import com.martdev.features.room.domain.model.Seat

fun RoomDTO.toRoom() = Room(
    name = name,
    rows = rows,
    columns = columns
)

fun Room.toRoomDTO() = RoomDTO(
    id, name, rows, columns
)

fun SeatDTO.toSeat() = Seat(
    roomId = roomId,
    rowLabel = rowLabel,
    seatNumber = seatNumber
)

fun Seat.toSeatDTO() = SeatDTO(
    id, roomId, rowLabel, seatNumber
)