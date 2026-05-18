package com.martdev.features.showtime.api

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.showtime.domain.model.ShowtimeStatus
import com.martdev.features.showtime.domain.service.ShowtimeService
import com.martdev.shared.api.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val showtimePath = "/showtime"
const val adminShowtimePath = "/admin$showtimePath"
const val createShowtimePath = "/create-showtime"
const val getShowtimesPath = "/get-showtimes"
const val getShowtimesByMovieIdPath = "/get-showtimes-by-movie-id/{movie_id}"
const val getShowtimeByIdPath = "/get-showtime-by-id/{showtime_id}"
const val updateShowtimePath = "/update-showtime/{showtime_id}"
const val updateShowtimeStatusPath = "/update-showtime-status/{showtime_id}"
const val deleteShowtimePath = "/delete-showtime/{showtime_id}"

fun Route.showtimeRoute() {
    val service by inject<ShowtimeService>()
    adminShowtime(service)
    showtimePublicRoute(service)
}

private fun Route.adminShowtime(service: ShowtimeService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminShowtimePath) {
                post(createShowtimePath) {
                    val showtime = call.receive<ShowtimeDTO>().toShowtime()
                    val result = service.createShowtime(showtime).toShowtimeDTO()
                    val response = DataResponse(result)
                    call.respond(status = HttpStatusCode.Created, response)
                }

                put(updateShowtimePath) {
                    val showtimeId = getParameterFromPath("showtime_id")
                    val showtime = call.receive<ShowtimeDTO>().toShowtime(showtimeId)
                    val result = service.updateShowtime(showtime).toShowtimeDTO()
                    val response = DataResponse(result)
                    call.respond(status = HttpStatusCode.OK, response)
                }

                delete(deleteShowtimePath) {
                    val showtimeId = getParameterFromPath("showtime_id")
                    service.deleteShowtime(showtimeId)
                    call.respond(HttpStatusCode.NoContent)
                }

                patch(updateShowtimeStatusPath) {
                    val showtimeId = getParameterFromPath("showtime_id")
                    val showtimeStatus = call.receive<UpdateShowtimeStatusRequest>().status
                    val result =
                        service.updateShowtimeStatus(showtimeId, ShowtimeStatus.valueOf(showtimeStatus.uppercase()))
                            .toShowtimeDTO()
                    val response = DataResponse(result)
                    call.respond(status = HttpStatusCode.OK, response)
                }

                get(getShowtimesPath) {
                    val (limit, offset) = getLimitAndOffset()
                    val result = service.getShowtimes(limit, offset).map {
                        it.toShowtimeDTO()
                    }
                    val response = DataResponse(result)
                    call.respond(HttpStatusCode.OK, response)
                }
            }
        }
    }
}

private fun Route.showtimePublicRoute(service: ShowtimeService) {
    route(showtimePath) {
        get(getShowtimesByMovieIdPath) {
            val movieId = getParameterFromPath("movie_id")
            val result = service.getShowtimesByMovieId(movieId).map {
                it.toShowtimeDTO()
            }
            val response = DataResponse(result)
            call.respond(HttpStatusCode.OK, response)
        }

        get(getShowtimeByIdPath) {
            val showtimeId = getParameterFromPath("showtime_id")
            val result = service.getShowtimeById(showtimeId).toShowtimeDTO()
            val response = DataResponse(result)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}