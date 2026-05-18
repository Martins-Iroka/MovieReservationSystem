package com.martdev.plugins

import com.martdev.shared.api.ErrorResponse
import com.martdev.shared.domain.exception.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Bad Request")
            call.respond(status = HttpStatusCode.BadRequest, errorResponse)
        }

        exception<io.ktor.server.plugins.BadRequestException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Bad Request")
            call.respond(status = HttpStatusCode.BadRequest, errorResponse)
        }

        exception<NotFoundException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Not found")
            call.respond(status = HttpStatusCode.NotFound, errorResponse)
        }

        exception<io.ktor.server.plugins.NotFoundException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Not found")
            call.respond(status = HttpStatusCode.NotFound, errorResponse)
        }

        exception<InternalServerException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Internal server error")
            call.respond(status = HttpStatusCode.InternalServerError, errorResponse)
        }

        exception<UnauthorizedException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Unauthorized")
            call.respond(status = HttpStatusCode.Unauthorized, errorResponse)
        }

        exception<SerializationException> { call, _ ->
            val errorResponse = ErrorResponse("Invalid request body format")
            call.respond(status = HttpStatusCode.BadRequest, errorResponse)
        }

        exception<Exception> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Internal server error")
            call.respond(status = HttpStatusCode.InternalServerError, errorResponse)
        }

        exception<ForbiddenException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Forbidden")
            call.respond(status = HttpStatusCode.Forbidden, errorResponse)
        }

        exception<RequestValidationException> { call, cause ->
            val errorResponse = ErrorResponse(cause.reasons.joinToString())
            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<ConflictException> { call, cause ->
            val errorResponse = ErrorResponse(cause.message ?: "Conflict")
            call.respond(status = HttpStatusCode.Conflict, errorResponse)
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            val errorResponse = ErrorResponse("too many request")
            call.respond(status = status, errorResponse)
        }
    }
}
