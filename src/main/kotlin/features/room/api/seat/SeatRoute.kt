package com.martdev.features.room.api.seat

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.room.api.toSeat
import com.martdev.features.room.api.toSeatDTO
import com.martdev.features.room.domain.service.SeatService
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

const val seatPath = "/seat"
const val adminSeatPath = "/admin/$seatPath"
const val createSeatsPath = "/create-seats"
const val getSeatsByRoomIdPath = "/get-seats-by-room-id/{room_id}"
const val getSeatByIdPath = "/get-seat-by-id/{seat_id}"

fun Route.seatRoute() {
    val service by inject<SeatService>()
    adminSeatRoute(service)
    seatPublicRoute(service)
}

private fun Route.adminSeatRoute(service: SeatService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminSeatPath) {
                post(createSeatsPath) {
                    val seats = call.receive<List<SeatDTO>>().map { it.toSeat() }
                    val createdSeats = service.createSeats(seats).map { it.toSeatDTO() }
                    val response = DataResponse(createdSeats)
                    call.respond(HttpStatusCode.Created, response)
                }
            }
        }
    }
}

private fun Route.seatPublicRoute(service: SeatService) {
    route(seatPath) {
        get(getSeatByIdPath) {
            val seatId = getParameterFromPath("seat_id")
            val seat = service.getSeatById(seatId).toSeatDTO()
            val dataResponse = DataResponse(seat)
            call.respond(HttpStatusCode.OK, dataResponse)
        }

        get(getSeatsByRoomIdPath) {
            val roomId = getParameterFromPath("room_id")
            val seats = service.getSeatsByRoomId(roomId).map { it.toSeatDTO() }
            val response = DataResponse(seats)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}