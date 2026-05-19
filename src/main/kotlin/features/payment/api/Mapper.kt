package com.martdev.features.payment.api

import com.martdev.features.payment.domain.model.Payment
import com.martdev.features.payment.domain.service.InitializePaymentResult

fun Payment.toPaymentDTO() = PaymentDTO(
    id = id,
    reservationId = reservationId,
    userId = userId,
    reference = reference,
    amount = amount,
    currency = currency,
    status = status.name,
    authorizationUrl = authorizationUrl,
    paidAt = paidAt,
    refundedAt = refundedAt,
    gatewayResponse = gatewayResponse,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun InitializePaymentResult.toInitializeResponse() = InitializePaymentResponse(
    authorizationUrl = authorizationUrl,
    reference = reference,
    reservationId = reservationId,
)
