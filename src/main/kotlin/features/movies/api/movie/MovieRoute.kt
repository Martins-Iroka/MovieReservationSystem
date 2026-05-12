package com.martdev.features.movies.api.movie

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.movies.api.toMovie
import com.martdev.features.movies.api.toMovieDto
import com.martdev.features.movies.api.toMovieItemDto
import com.martdev.features.movies.domain.service.movie.MovieService
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.DataResponse
import com.martdev.shared.api.getParameterFromPath
import com.martdev.shared.api.withRole
import com.martdev.shared.domain.exception.BadRequestException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val moviePath = "/movie"
const val adminMoviePath = "/admin/$moviePath"
const val createMoviePath = "/create-movie"
const val movieListPath = "/get-movies"
const val movieByIdPath = "/get-movie-by-id/{movie_id}"
const val updateMoviePath = "/update-movie/{movie_id}"
const val deleteMoviePath = "/delete-movie/{movie_id}"
const val moviesByGenrePath = "/get-movies-by-genre/{genre-id}"

fun Route.movieRoute() {
    val service by inject<MovieService>()
    adminMovieRoute(service)
    moviePublicRoute(service)
}

private fun Route.adminMovieRoute(service: MovieService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminMoviePath) {
                post(createMoviePath) {
                    val movie = call.receive<MovieDTO>().toMovie()
                    service.createMovie(movie)
                    call.respond(HttpStatusCode.Created)
                }

                put(updateMoviePath) {
                    val movieId = getParameterFromPath("movie_id")
                    val movie = call.receive<MovieDTO>().toMovie().copy(id = movieId)
                    val updatedMovie = service.updateMovie(movie).toMovieDto()
                    val dataResponse = DataResponse(updatedMovie)
                    call.respond(HttpStatusCode.OK, dataResponse)
                }

                delete(deleteMoviePath) {
                    val movieId = getParameterFromPath("movie_id")
                    service.deleteMovie(movieId)
                    call.respond(HttpStatusCode.NoContent)
                }
            }
        }
    }
}

private fun Route.moviePublicRoute(service: MovieService) {
    route(moviePath) {
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
            val movie = service.getMovieById(movieId)
            val response = movie.toMovieDto()
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
    }
}

private fun RoutingContext.getLimitAndOffset(): Pair<Int, Long> {
    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
    val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0L
    if (limit <= 0 || offset < 0) {
        throw BadRequestException("'limit' must be positive and 'offset' must be non-negative.")
    }

    return Pair(limit, offset)
}