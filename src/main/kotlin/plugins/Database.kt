package com.martdev.plugins

import com.martdev.config.DatabaseConfig
import com.martdev.shared.infrastruce.db.DatabaseFactory
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val config by inject<DatabaseConfig>()

    DatabaseFactory.setupDatabase(config)
}