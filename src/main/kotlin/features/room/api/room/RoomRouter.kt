package com.martdev.features.room.api.room

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.room.api.toRoom
import com.martdev.features.room.api.toRoomDTO
import com.martdev.features.room.domain.service.RoomService
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.DataResponse
import com.martdev.shared.api.getParameterFromPath
import com.martdev.shared.api.withRole
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val roomPath = "/room"
const val adminRoomPath = "/admin/$roomPath"
const val createRoomPath = "/create-room"
const val getAllRoomsPath = "/get-rooms"
const val getRoomByIdPath = "/get-room-by-id/{room_id}"
const val updateRoomPath = "/update-room/{room_id}"
const val deleteRoomPath = "/delete-room/{room_id}"

fun Route.roomRoute() {
    val service by inject<RoomService>()
    adminRoomRoute(service)
    roomPublicRoute(service)
}

private fun Route.adminRoomRoute(service: RoomService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminRoomPath) {
                post(createRoomPath) {
                    val room = call.receive<RoomDTO>().toRoom()
                    val createdRoom = service.createRoom(room).toRoomDTO()
                    val response = DataResponse(createdRoom)
                    call.respond(HttpStatusCode.Created, response)
                }
                put(updateRoomPath) {
                    val roomId = getParameterFromPath("room_id")
                    val room = call.receive<RoomDTO>().toRoom().copy(id = roomId)
                    val updatedRoom = service.updateRoom(room)
                    val dataResponse = DataResponse(updatedRoom)
                    call.respond(HttpStatusCode.OK, dataResponse)
                }
                delete(deleteRoomPath) {
                    val roomId = getParameterFromPath("room_id")
                    service.deleteRoom(roomId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun Route.roomPublicRoute(service: RoomService) {
    route(roomPath) {
        get(getAllRoomsPath) {
            val rooms = service.getAllRooms().map {
                it.toRoomDTO()
            }
            val dataResponse = DataResponse(rooms)
            call.respond(HttpStatusCode.OK, dataResponse)
        }
        get(getRoomByIdPath) {
            val roomId = getParameterFromPath("room_id")
            val room = service.getRoomById(roomId).toRoomDTO()
            val dataResponse = DataResponse(room)
            call.respond(HttpStatusCode.OK, dataResponse)
        }
    }
}