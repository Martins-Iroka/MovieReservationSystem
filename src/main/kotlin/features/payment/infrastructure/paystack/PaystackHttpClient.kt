package com.martdev.features.payment.infrastructure.paystack

import com.martdev.config.PaystackConfig
import com.martdev.features.payment.infrastructure.paystack.dto.*
import com.martdev.shared.infrastruce.http.KtorHttpClientFactory
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.annotation.Single

@Single
class PaystackHttpClient(
    private val config: PaystackConfig,
    httpClientFactory: KtorHttpClientFactory,
    engine: HttpClientEngine? = null,
) : PaystackClient {

    private val client = httpClientFactory.create(engine) {
        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer ${config.secretKey}")
            contentType(ContentType.Application.Json)
        }
    }

    override suspend fun initializeTransaction(
        email: String,
        amount: Long
    ): InitializeResponse {
        val response = client.post("${config.baseUrl}/transaction/initialize") {
            setBody(
                InitializeRequest(
                    email = email,
                    amount = amount
                )
            )
        }
        return response.body<InitializeResponse>()
    }

    override suspend fun verifyTransaction(reference: String): VerifyResponse {
        val response = client.get("${config.baseUrl}/transaction/verify/$reference")
        return response.body<VerifyResponse>()
    }

    override suspend fun refundTransaction(reference: String, amount: Long?): RefundResponse {
        val response = client.post("${config.baseUrl}/refund") {
            setBody(RefundRequest(transaction = reference, amount = amount))
        }
        return response.body<RefundResponse>()
    }
}
