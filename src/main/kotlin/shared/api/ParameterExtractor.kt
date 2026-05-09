package com.martdev.shared.api

import com.martdev.shared.domain.exception.BadRequestException
import io.ktor.server.routing.*

fun RoutingContext.getParameterFromPath(parameter: String): Long {
    return call.parameters[parameter]?.toLongOrNull() ?: throw BadRequestException("Invalid id")
}