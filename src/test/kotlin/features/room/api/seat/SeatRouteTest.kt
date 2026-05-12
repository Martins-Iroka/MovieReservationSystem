package features.room.api.seat

import com.martdev.config.JWTConfig
import com.martdev.features.auth.domain.model.Role
import com.martdev.features.auth.infrastructure.security.JWTAuthImpl
import com.martdev.features.room.api.seat.SeatDTO
import com.martdev.features.room.api.seat.seatRoute
import com.martdev.features.room.domain.model.Seat
import com.martdev.features.room.domain.service.SeatService
import features.utils.clientConfiguration
import features.utils.testAppConfiguration
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class SeatRouteTest {

    @MockK
    private lateinit var service: SeatService

    private val jwtConfig = JWTConfig("test", 15, "iss", "aud")

    private val module = module {
        single { service }
        single { jwtConfig }
    }

    private val adminToken = JWTAuthImpl(jwtConfig).generateAccessToken("1", Role.ADMIN.name)

    @Test
    fun testPost_AdminSeat_CreateSeats() = testApplication {
        coEvery {
            service.createSeats(any())
        } returns listOf(Seat())

        application {
            appConfig()
        }
        val client = clientConfiguration(adminToken)

        client.post("/admin/seat/create-seats") {
            setBody(listOf(SeatDTO()))
        }.apply {
            assertEquals(HttpStatusCode.Created, status)
        }
    }

    @Test
    fun testGetSeat_GetSeatById() = testApplication {
        coEvery {
            service.getSeatById(any())
        } returns Seat()

        application {
            appConfig()
        }
        val client = clientConfiguration(adminToken)
        client.get("/seat/get-seat-by-id/1").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testGet_GetSeatsByRoomId() = testApplication {
        coEvery {
            service.getSeatsByRoomId(any())
        } returns listOf(Seat())

        application {
            appConfig()
        }
        val client = clientConfiguration(adminToken)

        client.get("/seat/get-seats-by-room-id/1").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    private fun Application.appConfig() = testAppConfiguration(module) {
        seatRoute()
    }
}