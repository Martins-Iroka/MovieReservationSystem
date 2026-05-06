package com.martdev.features.movies.infrastructure.repository

import com.martdev.features.movies.domain.repository.MovieRepository
import com.martdev.features.movies.infrastructure.tables.GenresTable
import com.martdev.features.movies.infrastructure.tables.MovieGenreTable
import com.martdev.features.movies.infrastructure.tables.MoviesTable
import com.martdev.features.utils.PostgresContainer
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.TestMethodOrder
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
class MovieRepositoryImplTest {

    private lateinit var repository: MovieRepository

    companion object {

        @Container
        val postgres = PostgresContainer.initPostgres()

        @JvmStatic
        @AfterAll
        fun clearDb() {
            transaction {
                GenresTable.deleteAll()
                MoviesTable.deleteAll()
                MovieGenreTable.deleteAll()
            }
        }

        @JvmStatic
        @BeforeAll
        fun connectToDBAndMigrate() {
            PostgresContainer.connectToDBAndMigrate(postgres)
        }
    }
}