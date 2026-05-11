package features.utils

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*

fun ApplicationTestBuilder.clientConfiguration(token: String = ""): HttpClient = createClient {
    install(ContentNegotiation) {
        json()
    }
    defaultRequest {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        bearerAuth(token)
    }
}