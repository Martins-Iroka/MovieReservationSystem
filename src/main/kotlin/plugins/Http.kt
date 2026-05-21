package com.martdev.plugins

import com.martdev.config.CorsConfig
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import org.koin.ktor.ext.inject

fun Application.configureHttp() {
    val corsConfig by inject<CorsConfig>()
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        if (corsConfig.allowAnyHost) {
            anyHost()
        } else {
            corsConfig.allowedHosts.forEach { host ->
                allowHost(host, schemes = listOf("https", "http"))
            }
        }
    }
    routing {
        swaggerUI(path = "/swaggerUI") {
            info = OpenApiInfo(
                title = "Movie Reservation System",
                version = "1.0.0",
                description = "An application to enable users book movie tickets and admins to add movie for viewing",
                termsOfService = "https://swagger.io/terms/"
            )
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }
    }
}
