package com.martdev.plugins

import com.martdev.config.DatabaseConfig
import com.martdev.config.JWTConfig
import com.martdev.config.StytchConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    val configModule = module {
        single { JWTConfig.fromEnvironment(environment) }
        single { DatabaseConfig.fromEnvironment(environment) }
        single { StytchConfig.fromEnvironment(environment) }
    }

    install(Koin) {
        slf4jLogger()
        modules(configModule)
    }
}