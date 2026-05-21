package com.martdev.features.report.api

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.report.domain.model.ReportBucketGranularity
import com.martdev.features.report.domain.service.ReportService
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.DataResponse
import com.martdev.shared.api.getLimitAndOffset
import com.martdev.shared.api.withRole
import com.martdev.shared.domain.exception.BadRequestException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.time.Instant

const val adminReportPath = "/admin/reports"

fun Route.reportRoute() {
    val service by inject<ReportService>()
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminReportPath) {
                get("/revenue") {
                    val from = requireInstant("from")
                    val to = requireInstant("to")
                    val bucket = parseBucket(call.request.queryParameters["bucket"])
                    val report = service.getRevenueReport(from, to, bucket)
                    call.respond(HttpStatusCode.OK, DataResponse(report.toDTO()))
                }
                get("/capacity") {
                    val from = requireInstant("from")
                    val to = requireInstant("to")
                    val (limit, offset) = getLimitAndOffset()
                    val movieId = call.request.queryParameters["movieId"]?.toLongOrNull()
                    val roomId = call.request.queryParameters["roomId"]?.toLongOrNull()
                    val report = service.getCapacityReport(from, to, limit, offset, movieId, roomId)
                    call.respond(HttpStatusCode.OK, DataResponse(report.toDTO()))
                }
            }
        }
    }
}

private fun RoutingContext.requireInstant(name: String): Instant =
    call.request.queryParameters[name]
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }
        ?: throw BadRequestException("Invalid or missing '$name' (expected ISO-8601 instant)")

private fun parseBucket(raw: String?): ReportBucketGranularity = when (raw?.uppercase()) {
    null, "DAY" -> ReportBucketGranularity.DAY
    "WEEK" -> ReportBucketGranularity.WEEK
    "MONTH" -> ReportBucketGranularity.MONTH
    else -> throw BadRequestException("Invalid bucket: $raw")
}
