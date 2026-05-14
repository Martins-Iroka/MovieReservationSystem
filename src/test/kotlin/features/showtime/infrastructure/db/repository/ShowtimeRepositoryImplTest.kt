package features.showtime.infrastructure.db.repository

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.model.Movie
import com.martdev.features.movies.domain.repository.GenreRepository
import com.martdev.features.movies.domain.repository.MovieRepository
import com.martdev.features.movies.infrastructure.repository.GenreRepositoryImpl
import com.martdev.features.movies.infrastructure.repository.MovieRepositoryImpl
import com.martdev.features.movies.infrastructure.tables.MoviesTable
import com.martdev.features.room.domain.model.Room
import com.martdev.features.room.domain.repository.RoomRepository
import com.martdev.features.room.infrastructure.db.repository.RoomRepositoryImpl
import com.martdev.features.room.infrastructure.db.tables.RoomTable
import com.martdev.features.showtime.domain.model.Showtime
import com.martdev.features.showtime.domain.repository.ShowtimeRepository
import com.martdev.features.showtime.infrastructure.db.repository.ShowtimeRepositoryImpl
import com.martdev.features.showtime.infrastructure.db.table.ShowtimeTable
import com.martdev.features.utils.PostgresContainer
import com.martdev.shared.domain.model.DataResult
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Testcontainers
class ShowtimeRepositoryImplTest {

    companion object {
        private lateinit var repo: ShowtimeRepository
        private lateinit var movieRepo: MovieRepository
        private lateinit var roomRepo: RoomRepository
        private lateinit var genreRepo: GenreRepository

        @Container
        private val postgres = PostgresContainer.initPostgres()

        @BeforeAll
        @JvmStatic
        fun connectDBAndMigrate() {
            PostgresContainer.connectToDBAndMigrate(postgres)
            repo = ShowtimeRepositoryImpl()
            movieRepo = MovieRepositoryImpl()
            roomRepo = RoomRepositoryImpl()
            genreRepo = GenreRepositoryImpl()
        }
    }

    @BeforeEach
    fun cleanDb() {
        transaction {
            ShowtimeTable.deleteAll()
            RoomTable.deleteAll()
            MoviesTable.deleteAll()
        }
    }

    @Test
    fun `create show time successfully`() = runTest {
        val (movieId, roomId) = createMovieAndRoom()

        val showtime = Showtime(
            movieId = movieId,
            roomId = roomId,
            startsAt = Clock.System.now().minus(2.hours),
            endsAt = Clock.System.now(),
            price = 5000
        )
        val createdShowtime = repo.createShowtime(showtime)

        assertTrue(createdShowtime is DataResult.Success, createdShowtime.toString())
        assertEquals(showtime.roomId, createdShowtime.value.roomId)
        assertEquals(showtime.movieId, createdShowtime.value.movieId)
    }

    private suspend fun createMovieAndRoom(): Pair<Long, Long> {
        val genre = Genre(name = "Action")
        val savedGenre = genreRepo.saveGenre(genre) as DataResult.Success
        val movie = Movie(
            title = "Inception",
            description = "Inception description",
            posterUrl = "poster_url",
            genres = listOf(
                savedGenre.value
            )
        )

        val movieId = (movieRepo.createMovie(movie) as DataResult.Success).value

        val room = Room(name = "Room 1", rows = 5, columns = 10)

        val roomId = (roomRepo.createRoom(room) as DataResult.Success).value.id

        return movieId to roomId
    }
}