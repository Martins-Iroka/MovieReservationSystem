package com.martdev.features.movies.infrastructure.repository

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.model.Movie
import com.martdev.features.movies.domain.repository.GenreRepository
import com.martdev.features.movies.domain.repository.MovieRepository
import com.martdev.features.movies.infrastructure.tables.GenresTable
import com.martdev.features.movies.infrastructure.tables.MovieGenreTable
import com.martdev.features.movies.infrastructure.tables.MoviesTable
import com.martdev.features.utils.PostgresContainer
import com.martdev.shared.domain.model.DataResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
class MovieRepositoryImplTest {

    companion object {
        private lateinit var movieRepo: MovieRepository
        private lateinit var genreRepo: GenreRepository
        val genres = mutableListOf<Genre>()
        var movie = Movie(
            title = "movie_title",
            description = "movie_description",
            posterUrl = "poster_url",
            duration = 90,
            releasedDate = LocalDate(year = 2026, month = Month.MAY, 6)
        )
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
            genreRepo = GenreRepositoryImpl()
            movieRepo = MovieRepositoryImpl()
        }
    }

    @Test
    @Order(1)
    fun `should save movie genre`() = runTest {
        val genreList = listOf(
            Genre(
                name = "Action"
            ),
            Genre(
                name = "Adventure"
            ),
            Genre(
                name = "Drama"
            ),
            Genre(
                name = "Comedy"
            )
        )

        genreList.forEach {
            val result = genreRepo.saveGenre(it)
            assertTrue(result is DataResult.Success)
            genres.add(result.value)
        }

        assertTrue(genres.isNotEmpty())
        assertEquals(4, genres.size)
    }

    @Test
    @Order(2)
    fun `should save movie`() = runTest {
        val genre = genres.find {
            it.name == "Adventure"
        }!!

        movie = movie.copy(genres = listOf(genre))

        val result = movieRepo.saveMovieData(movie)
        assertTrue(result is DataResult.Success, result.toString())

        movie = movie.copy(id = result.value)
    }
}