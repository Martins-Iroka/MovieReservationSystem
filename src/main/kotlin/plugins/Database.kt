package com.martdev.plugins

import com.martdev.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject

fun Application.configureDatabase() {
    val config by inject<DatabaseConfig>()

    val flyway = Flyway.configure()
        .dataSource(
            config.address,
            config.user,
            config.password
        ).load()
    flyway.migrate()

    val hConfig = HikariConfig().apply {
        jdbcUrl = config.address
        driverClassName = "org.postgresql.Driver"
        username = config.user
        password = config.password
        maximumPoolSize = config.maxOpenCon
        minimumIdle = config.maxIdleCon
        connectionTimeout = config.maxIdleTime
    }

    val dataSource = HikariDataSource(hConfig)

    Database.connect(
        dataSource
    )
}