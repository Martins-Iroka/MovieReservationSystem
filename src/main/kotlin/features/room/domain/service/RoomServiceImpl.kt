package com.martdev.features.room.domain.service

import com.martdev.features.room.domain.model.Room
import com.martdev.features.room.domain.repository.RoomRepository
import com.martdev.shared.util.returnValue
import org.koin.core.annotation.Single

@Single
class RoomServiceImpl(
    private val repository: RoomRepository
) : RoomService {
    override suspend fun createRoom(room: Room) {
        repository.createRoom(room).returnValue()
    }

    override suspend fun getAllRooms(): List<Room> {
        return repository.getAllRooms().returnValue()
    }

    override suspend fun getRoomById(roomId: Long): Room {
        return repository.getRoomById(roomId).returnValue()
    }

    override suspend fun updateRoom(room: Room): Room {
        return repository.updateRoom(room).returnValue()
    }

    override suspend fun deleteRoom(roomId: Long) {
        repository.deleteRoom(roomId).returnValue()
    }
}