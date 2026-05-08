package com.martdev.features.movies.api

import com.martdev.features.movies.domain.service.movie.MovieService
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.DataResponse
import com.martdev.shared.domain.exception.BadRequestException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val moviePath = "/movie"
const val adminPath = "/admin/$moviePath"
const val createMoviePath = "/create-movie"
const val movieListPath = "/get-movies"
const val movieByIdPath = "/get-movie-by-id/{movie-id}"
const val updateMoviePath = "/update-movie"
const val deleteMoviePath = "/delete-movie"
const val moviesByGenrePath = "/get-movies-by-genre/{genre-id}"

fun Route.movieRoute() {
    val service by inject<MovieService>()
    authenticate(AUTH_JWT) {
        get(movieListPath) {
            val (limit, offset) = getLimitAndOffset()
            val response = service.getMovies(limit, offset).map {
                it.toMovieItemDto()
            }
            val dataResponse = DataResponse(response)
            call.respond(HttpStatusCode.OK, dataResponse)
        }

        get(movieByIdPath) {
            val movieId = getParameterFromPath("movie_id")
            val response = service.getMovieById(movieId).toMovieDto()
            val dataResponse = DataResponse(response)
            call.respond(HttpStatusCode.OK, dataResponse)
        }

        get(moviesByGenrePath) {
            val genreId = getParameterFromPath("genre-id")
            val (limit, offset) = getLimitAndOffset()
            val response = service.getMoviesByGenre(genreId, limit, offset).map {
                it.toMovieItemDto()
            }
            val dataResponse = DataResponse(response)
            call.respond(HttpStatusCode.OK, dataResponse)
        }
        route(adminPath) {

        }
    }
}

private fun RoutingContext.getParameterFromPath(parameter: String): Long {
    return call.parameters[parameter]?.toLongOrNull() ?: throw BadRequestException("Invalid id")
}

private fun RoutingContext.getLimitAndOffset(): Pair<Int, Long> {
    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
    val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
    if (limit <= 0 || offset < 0) {
        throw BadRequestException("'limit' must be positive and 'offset' must be non-negative.")
    }

    return Pair(limit, offset)
}