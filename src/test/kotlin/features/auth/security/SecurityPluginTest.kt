package features.auth.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.martdev.config.JWTConfig
import com.martdev.features.auth.domain.model.Role
import com.martdev.features.auth.infrastructure.security.JWTAuthImpl
import com.martdev.shared.api.AUTH_JWT
import com.martdev.shared.api.withRole
import features.utils.clientConfiguration
import features.utils.testAppConfiguration
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import java.util.Date
import kotlin.test.assertEquals

class SecurityPluginTest {

    private val jwtConfig = JWTConfig("test-secret", 15, "iss", "aud")
    private val auth = JWTAuthImpl(jwtConfig)
    private val module = module { single { jwtConfig } }

    private fun adminRoute(): Route.() -> Unit = {
        authenticate(AUTH_JWT) {
            get("/any-user") {
                call.respond(HttpStatusCode.OK, "ok")
            }
            route("/admin-only") {
                withRole(Role.ADMIN) {
                    get {
                        call.respond(HttpStatusCode.OK, "ok")
                    }
                }
            }
        }
    }

    private fun Application.configure() = testAppConfiguration(module, adminRoute())

    @Test
    fun `unauthenticated request to protected route returns 401`() = testApplication {
        application { configure() }
        val client = clientConfiguration()

        val response = client.get("/any-user")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `valid USER token reaches protected route`() = testApplication {
        application { configure() }
        val token = auth.generateAccessToken("2", "USER")
        val client = clientConfiguration(token)

        assertEquals(HttpStatusCode.OK, client.get("/any-user").status)
    }

    @Test
    fun `USER token on ADMIN route returns 403`() = testApplication {
        application { configure() }
        val token = auth.generateAccessToken("2", "USER")
        val client = clientConfiguration(token)

        assertEquals(HttpStatusCode.Forbidden, client.get("/admin-only").status)
    }

    @Test
    fun `ADMIN token on ADMIN route returns 200`() = testApplication {
        application { configure() }
        val token = auth.generateAccessToken("1", "ADMIN")
        val client = clientConfiguration(token)

        assertEquals(HttpStatusCode.OK, client.get("/admin-only").status)
    }

    @Test
    fun `expired access token returns 401`() = testApplication {
        application { configure() }
        val expired = JWT.create()
            .withClaim("userId", "1")
            .withClaim("role", "USER")
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withIssuedAt(Date(System.currentTimeMillis() - 60_000))
            .withExpiresAt(Date(System.currentTimeMillis() - 30_000))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
        val client = clientConfiguration(expired)

        assertEquals(HttpStatusCode.Unauthorized, client.get("/any-user").status)
    }

    @Test
    fun `token signed with a different secret returns 401`() = testApplication {
        application { configure() }
        val tampered = JWT.create()
            .withClaim("userId", "1")
            .withClaim("role", "USER")
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
            .sign(Algorithm.HMAC256("wrong-secret"))
        val client = clientConfiguration(tampered)

        assertEquals(HttpStatusCode.Unauthorized, client.get("/any-user").status)
    }

    @Test
    fun `token with wrong issuer returns 401`() = testApplication {
        application { configure() }
        val wrongIssuer = JWT.create()
            .withClaim("userId", "1")
            .withClaim("role", "USER")
            .withAudience(jwtConfig.audience)
            .withIssuer("not-the-right-issuer")
            .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
        val client = clientConfiguration(wrongIssuer)

        val response = client.get("/any-user")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        // sanity check: the body uses our configured challenge handler
        assertEquals("unauthorized", response.bodyAsText())
    }

    @Test
    fun `token with empty userId claim fails validation and returns 401`() = testApplication {
        application { configure() }
        val empty = JWT.create()
            .withClaim("userId", "")
            .withClaim("role", "USER")
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + 60_000))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
        val client = clientConfiguration(empty)

        assertEquals(HttpStatusCode.Unauthorized, client.get("/any-user").status)
    }
}
