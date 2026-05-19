package com.martdev.features.payment.api

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.payment.domain.service.PaymentService
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.DataResponse
import com.martdev.shared.api.getParameterFromPath
import com.martdev.shared.api.withRole
import com.martdev.shared.domain.exception.BadRequestException
import com.martdev.shared.util.extractUserId
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val paymentPath = "/payment"
const val adminPaymentPath = "/admin$paymentPath"
const val paystackSignatureHeader = "x-paystack-signature"

fun Route.paymentRoute() {
    val paymentService by inject<PaymentService>()
    publicPaymentRoutes(paymentService)
    userPaymentRoutes(paymentService)
    adminPaymentRoutes(paymentService)
}

private fun Route.publicPaymentRoutes(paymentService: PaymentService) {
    route(paymentPath) {
        // Paystack webhook — auth via HMAC signature header, NOT JWT.
        post("/webhook") {
            val rawBody = call.receiveText()
            val signature = call.request.headers[paystackSignatureHeader]
            paymentService.handleWebhook(rawBody, signature)
            call.respond(HttpStatusCode.OK)
        }

        // Public redirect target from Paystack-hosted checkout.
        get("/callback") {
            val reference = call.request.queryParameters["reference"]
                ?: call.request.queryParameters["trxref"]
                ?: throw BadRequestException("Missing reference")
            val payment = paymentService.verifyPayment(reference, requestingUserId = null)
            call.respond(
                HttpStatusCode.OK,
                DataResponse(
                    PaymentCallbackResponse(
                        status = payment.status.name,
                        reference = payment.reference,
                        reservationId = payment.reservationId,
                    )
                )
            )
        }
    }
}

private fun Route.userPaymentRoutes(paymentService: PaymentService) {
    authenticate(AUTH_JWT) {
        route(paymentPath) {
            post("/initialize") {
                val userId = call.extractUserId()
                val request = call.receive<InitializePaymentRequest>()
                val result = paymentService.initializePayment(request.reservationId, userId)
                    .toInitializeResponse()
                call.respond(HttpStatusCode.Created, DataResponse(result))
            }

            get("/verify/{reference}") {
                val userId = call.extractUserId()
                val reference = call.parameters["reference"]
                    ?: throw BadRequestException("Missing reference")
                val payment = paymentService.verifyPayment(reference, requestingUserId = userId)
                call.respond(HttpStatusCode.OK, DataResponse(payment.toPaymentDTO()))
            }

            get("/my-payments") {
                val userId = call.extractUserId()
                val result = paymentService.getMyPayments(userId).map { it.toPaymentDTO() }
                call.respond(HttpStatusCode.OK, DataResponse(result))
            }
        }
    }
}

private fun Route.adminPaymentRoutes(paymentService: PaymentService) {
    authenticate(AUTH_JWT) {
        withRole(Role.ADMIN) {
            route(adminPaymentPath) {
                get("/by-reservation/{reservation_id}") {
                    val reservationId = getParameterFromPath("reservation_id")
                    val result = paymentService.getPaymentsByReservationId(reservationId)
                        .map { it.toPaymentDTO() }
                    call.respond(HttpStatusCode.OK, DataResponse(result))
                }
            }
        }
    }
}
