package com.martdev.features.payment.domain.model

enum class PaymentStatus {
    INITIATED,
    PENDING,
    SUCCESS,
    FAILED,
    ABANDONED,
    REFUND_PENDING,
    REFUNDED,
    REFUND_FAILED,
}
