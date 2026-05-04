package com.martdev.plugins

import com.martdev.config.DatabaseConfig
import com.martdev.shared.infrastruce.db.DatabaseFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val config by inject<DatabaseConfig>()

    DatabaseFactory.setupDatabase(config)
}