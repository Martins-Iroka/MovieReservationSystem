package com.martdev.features.room.domain.repository

import com.martdev.features.room.domain.model.Room
import com.martdev.shared.domain.model.DataResult

interface RoomRepository {
    suspend fun createRoom(room: Room): DataResult<Room>
    suspend fun getAllRooms(): DataResult<List<Room>>
    suspend fun getRoomById(roomId: Long): DataResult<Room>
    suspend fun updateRoom(room: Room): DataResult<Room>
    suspend fun deleteRoom(roomId: Long): DataResult<Int>
}
