package com.martdev.features.movies.api.genre

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.movies.api.toGenre
import com.martdev.features.movies.api.toGenreDto
import com.martdev.features.movies.domain.service.genre.GenreService
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

const val genrePath = "/genre"
const val adminGenrePath = "/admin/$genrePath"
const val createGenrePath = "/create-genre"
const val getGenresPath = "/genres"
const val deleteGenrePath = "/delete-genre/{genre_id}"

fun Route.genreRoute() {
    val service by inject<GenreService>()
    adminGenreRoute(service)
    genrePublicRoute(service)
}

private fun Route.adminGenreRoute(service: GenreService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminGenrePath) {

                post(createGenrePath) {
                    val genre = call.receive<GenreDTO>().toGenre()
                    service.createGenre(genre)
                    call.respond(HttpStatusCode.Created)
                }

                delete(deleteGenrePath) {
                    val genreId = getParameterFromPath("genre_id")
                    service.deleteGenre(genreId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun Route.genrePublicRoute(service: GenreService) {
    route(genrePath) {
        get(getGenresPath) {
            val genres = service.getGenres().map {
                it.toGenreDto()
            }
            val dataResponse = DataResponse(genres)
            call.respond(HttpStatusCode.OK, dataResponse)
        }
    }
}