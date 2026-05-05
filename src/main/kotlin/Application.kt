package com.martdev

import com.martdev.plugins.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*

fun main(args: Array<String>) {
    dotenv {
        systemProperties = true
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureCallLogging()
    configureHttp()
    configureDatabase()
    configureSecurity()
    configureSerialization()
    configureStatusPages()
    configureMonitoring()
    configureRateLimiter()
    configureRouting()
    configureRequestValidation()
    configureBackgroundJobs()
}
