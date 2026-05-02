package com.martdev.infrastructure.db.repository

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.testcontainers.postgresql.PostgreSQLContainer

val postgres: PostgreSQLContainer = PostgreSQLContainer(
    "postgres:16-alpine"
).apply {
    withDatabaseName("mrs")
    withUsername("test")
    withPassword("test")
}

fun connectAndMigrate(): Database {
    Flyway.configure()
        .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
        .load().migrate()

    return Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )
}