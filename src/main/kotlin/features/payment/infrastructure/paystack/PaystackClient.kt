package com.martdev.features.payment.infrastructure.paystack

import com.martdev.features.payment.infrastructure.paystack.dto.InitializeResponse
import com.martdev.features.payment.infrastructure.paystack.dto.RefundResponse
import com.martdev.features.payment.infrastructure.paystack.dto.VerifyResponse

interface PaystackClient {
    suspend fun initializeTransaction(
        email: String,
        amount: Long,
        reference: String,
        callbackUrl: String,
        currency: String,
        metadata: Map<String, String>,
    ): InitializeResponse

    suspend fun verifyTransaction(reference: String): VerifyResponse

    suspend fun refundTransaction(reference: String, amount: Long?): RefundResponse
}
