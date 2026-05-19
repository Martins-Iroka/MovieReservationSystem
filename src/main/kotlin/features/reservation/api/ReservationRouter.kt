package com.martdev.features.reservation.api

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.reservation.domain.service.ReservationService
import com.martdev.features.reservation.domain.service.ShowtimeSeatService
import com.martdev.shared.api.*
import com.martdev.shared.util.extractUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val reservationPath = "/reservation"
const val adminReservationPath = "/admin$reservationPath"

fun Route.reservationRoute() {
    val reservationService by inject<ReservationService>()
    val showtimeSeatService by inject<ShowtimeSeatService>()
    adminReservationRoutes(reservationService, showtimeSeatService)
    userReservationRoutes(reservationService, showtimeSeatService)
}

private fun Route.adminReservationRoutes(
    reservationService: ReservationService,
    showtimeSeatService: ShowtimeSeatService
) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminReservationPath) {

                // Must be called after creating a showtime to populate its seat inventory
                post("/populate-seats/{showtime_id}") {
                    val showtimeId = getParameterFromPath("showtime_id")
                    showtimeSeatService.populateShowtimeSeats(showtimeId)
                    call.respond(HttpStatusCode.Created, DataResponse("Seats populated successfully"))
                }

                get("/get-all") {
                    val (limit, offset) = getLimitAndOffset()
                    val result = reservationService.getAllReservations(limit, offset)
                        .map { it.toReservationDTO() }
                    call.respond(HttpStatusCode.OK, DataResponse(result))
                }

                get("/get-by-id/{reservation_id}") {
                    val reservationId = getParameterFromPath("reservation_id")
                    val result = reservationService.getReservationById(reservationId).toReservationDTO()
                    call.respond(HttpStatusCode.OK, DataResponse(result))
                }

                patch("/cancel/{reservation_id}") {
                    val reservationId = getParameterFromPath("reservation_id")
                    val result = reservationService.cancelReservationAdmin(reservationId).toReservationDTO()
                    call.respond(HttpStatusCode.OK, DataResponse(result))
                }
            }
        }
    }
}

private fun Route.userReservationRoutes(
    reservationService: ReservationService,
    showtimeSeatService: ShowtimeSeatService
) {
    authenticate(AUTH_JWT) {
        route(reservationPath) {

            post("/create") {
                val userId = call.extractUserId()
                val request = call.receive<CreateReservationRequest>()
                val result = reservationService
                    .createReservation(userId, request.showtimeId, request.seatIds)
                    .toReservationDTO()
                call.respond(HttpStatusCode.Created, DataResponse(result))
            }

            get("/my-reservations") {
                val userId = call.extractUserId()
                val result = reservationService.getMyReservations(userId)
                    .map { it.toReservationDTO() }
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }

            get("/{reservation_id}") {
                val reservationId = getParameterFromPath("reservation_id")
                val userId = call.extractUserId()
                val result = reservationService
                    .getMyReservationById(reservationId, userId)
                    .toReservationDTO()
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }

            // Public: show available seats for a showtime (no auth needed — move outside authenticate if desired)
            get("/available-seats/{showtime_id}") {
                val showtimeId = getParameterFromPath("showtime_id")
                val result = showtimeSeatService.getAvailableSeats(showtimeId)
                    .map { it.toShowtimeSeatDTO() }
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }

            patch("/confirm/{reservation_id}") {
                val reservationId = getParameterFromPath("reservation_id")
                val userId = call.extractUserId()
                val result = reservationService.confirmReservation(reservationId, userId).toReservationDTO()
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }

            patch("/cancel/{reservation_id}") {
                val reservationId = getParameterFromPath("reservation_id")
                val userId = call.extractUserId()
                val result = reservationService.cancelReservation(reservationId, userId).toReservationDTO()
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }
        }
    }
}