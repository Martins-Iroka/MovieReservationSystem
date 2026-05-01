package com.martdev.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimiter() {
    install(RateLimit) {
        register(name = RateLimitName("resend-otp")) {
            rateLimiter(limit = 1, refillPeriod = 60.seconds)
        }
    }
}