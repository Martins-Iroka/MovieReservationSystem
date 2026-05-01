package com.martdev

import com.martdev.plugins.configureBackgroundJobs
import com.martdev.plugins.configureCallLogging
import com.martdev.plugins.configureDatabase
import com.martdev.plugins.configureHttp
import com.martdev.plugins.configureKoin
import com.martdev.plugins.configureMonitoring
import com.martdev.plugins.configureRateLimiter
import com.martdev.plugins.configureRequestValidation
import com.martdev.plugins.configureRouting
import com.martdev.plugins.configureSecurity
import com.martdev.plugins.configureSerialization
import com.martdev.plugins.configureStatusPages
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    dotenv {
        systemProperties = true
    }
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureBackgroundJobs()
    configureCallLogging()
    configureDatabase()
    configureSecurity()
    configureSerialization()
    configureStatusPages()
    configureHttp()
    configureMonitoring()
    configureRateLimiter()
    configureRouting()
    configureRequestValidation()
}
