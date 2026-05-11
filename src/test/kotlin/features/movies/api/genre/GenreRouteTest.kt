package features.movies.api.genre

import com.martdev.config.JWTConfig
import com.martdev.features.auth.infrastructure.security.JWTAuthImpl
import com.martdev.features.movies.api.genre.GenreDTO
import com.martdev.features.movies.api.genre.genreRoute
import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.service.genre.GenreService
import features.utils.clientConfiguration
import features.utils.testAppConfiguration
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class GenreRouteTest {

    @MockK
    private lateinit var service: GenreService

    val jwtConfig = JWTConfig("test", 15, "iss", "audience")

    val module = module {
        single { service }
        single { jwtConfig }
    }

    val adminToken = JWTAuthImpl(jwtConfig).generateAccessToken("5", "ADMIN")
    val userToken = JWTAuthImpl(jwtConfig).generateAccessToken("7", "USER")

    @Test
    fun testPostAdminGenre() = testApplication {
        coJustRun {
            service.createGenre(any())
        }
        application {
            configure()
        }
        val client = clientConfiguration(adminToken)
        client.post("/admin/genre/create-genre") {
            setBody(GenreDTO(name = "Action"))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
    }

    @Test
    fun testDeleteAdminGenreById() = testApplication {
        coJustRun {
            service.deleteGenre(any())
        }
        application {
            configure()
        }
        val client = clientConfiguration(adminToken)
        client.delete("/admin/genre/delete-genre/50").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGetGenres() = testApplication {
        coEvery {
            service.getGenres()
        } returns listOf(Genre())

        application {
            configure()
        }
        val client = clientConfiguration(userToken)
        client.get("/genres").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    private fun Application.configure() = testAppConfiguration(module) {
        genreRoute()
    }
}