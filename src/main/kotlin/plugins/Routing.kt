package com.martdev.plugins

import com.martdev.features.auth.api.authRoutes
import com.martdev.features.movies.api.genre.genreRoute
import com.martdev.features.movies.api.movie.movieRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

const val apiV1Path = "/v1/api"
fun Application.configureRouting() {
    routing {
        route(apiV1Path) {
            authRoutes()
            movieRoute()
            genreRoute()
        }
    }
}