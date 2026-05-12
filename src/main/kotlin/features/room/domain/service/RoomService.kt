package com.martdev.features.room.domain.service

import com.martdev.features.room.domain.model.Room

interface RoomService {
    suspend fun createRoom(room: Room): Room
    suspend fun getAllRooms(): List<Room>
    suspend fun getRoomById(roomId: Long): Room
    suspend fun updateRoom(room: Room): Room
    suspend fun deleteRoom(roomId: Long)
}