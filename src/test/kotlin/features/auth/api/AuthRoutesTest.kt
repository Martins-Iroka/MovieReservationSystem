package features.auth.api

import com.martdev.config.JWTConfig
import com.martdev.features.auth.api.authRoutes
import com.martdev.features.auth.api.request.CreateUserRequest
import com.martdev.features.auth.api.request.RefreshTokenRequest
import com.martdev.features.auth.api.request.ResendOTPRequest
import com.martdev.features.auth.api.request.UserLoginRequest
import com.martdev.features.auth.api.request.UserVerificationRequest
import com.martdev.features.auth.domain.model.LoginResult
import com.martdev.features.auth.domain.model.OtpResendResult
import com.martdev.features.auth.domain.model.RefreshResult
import com.martdev.features.auth.domain.model.RegistrationResult
import com.martdev.features.auth.domain.service.UserService
import com.martdev.shared.domain.exception.BadRequestException
import com.martdev.shared.domain.exception.InternalServerException
import com.martdev.shared.domain.exception.NotFoundException
import com.martdev.shared.domain.exception.UnauthorizedException
import features.utils.clientConfiguration
import features.utils.testAppConfiguration
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coJustRun
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.dsl.module
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AuthRoutesTest {

    @MockK
    private lateinit var service: UserService

    private val jwtConfig = JWTConfig("test", 15, "iss", "aud")

    private val testModule by lazy {
        module {
            single<UserService> { service }
            single { jwtConfig }
        }
    }

    private fun Application.configure() = testAppConfiguration(testModule) { authRoutes() }

    @Test
    fun `POST register returns 201 with emailId and token`() = testApplication {
        coEvery { service.registerUser(any()) } returns RegistrationResult(
            emailId = "stytch-email-id",
            registrationToken = "registration-token",
        )
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/register") {
            setBody(CreateUserRequest(email = "new@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        coVerify { service.registerUser(any()) }
    }

    @Test
    fun `POST register returns 400 for invalid email format`() = testApplication {
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/register") {
            setBody(CreateUserRequest(email = "not-an-email", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST register returns 400 for weak password`() = testApplication {
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/register") {
            setBody(CreateUserRequest(email = "user@example.com", password = "short"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST register propagates duplicate-email BadRequestException as 400`() = testApplication {
        coEvery { service.registerUser(any()) } throws BadRequestException("Duplicate email")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/register") {
            setBody(CreateUserRequest(email = "dupe@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST register propagates OTP failure as 500`() = testApplication {
        coEvery { service.registerUser(any()) } throws InternalServerException("Failed to send OTP")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/register") {
            setBody(CreateUserRequest(email = "user@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `POST verify-user returns 200 on successful verification`() = testApplication {
        coJustRun { service.verifyUser(any()) }
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/verify-user") {
            setBody(UserVerificationRequest(code = "123456", emailId = "stytch-email-id", token = "tok"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST verify-user returns 400 for invalid OTP`() = testApplication {
        coEvery { service.verifyUser(any()) } throws BadRequestException("Invalid or expired OTP")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/verify-user") {
            setBody(UserVerificationRequest(code = "000000", emailId = "stytch-email-id", token = "tok"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST verify-user returns 404 for unknown registration token`() = testApplication {
        coEvery { service.verifyUser(any()) } throws NotFoundException("Invalid or expired verification token")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/verify-user") {
            setBody(UserVerificationRequest(code = "123456", emailId = "stytch-email-id", token = "tok"))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST verify-user returns 400 for invalid code length`() = testApplication {
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/verify-user") {
            setBody(UserVerificationRequest(code = "12", emailId = "stytch-email-id", token = "tok"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST login returns 200 with tokens on success`() = testApplication {
        coEvery { service.loginUser(any()) } returns LoginResult(
            userId = 1L,
            accessToken = "access",
            refreshToken = "refresh",
        )
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/login") {
            setBody(UserLoginRequest(email = "user@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST login returns 400 for invalid credentials (generic message)`() = testApplication {
        coEvery { service.loginUser(any()) } throws BadRequestException("Invalid email or password")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/login") {
            setBody(UserLoginRequest(email = "user@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST login rate-limit returns 429 after threshold`() = testApplication {
        coEvery { service.loginUser(any()) } throws BadRequestException("Invalid email or password")
        application { configure() }
        val client = clientConfiguration()

        // First 5 should be allowed (and return 400 due to invalid creds), 6th should be rate-limited
        repeat(5) {
            client.post("/authentication/login") {
                setBody(UserLoginRequest(email = "user@example.com", password = "Password123!"))
            }
        }
        val sixth = client.post("/authentication/login") {
            setBody(UserLoginRequest(email = "user@example.com", password = "Password123!"))
        }

        assertEquals(HttpStatusCode.TooManyRequests, sixth.status)
    }

    @Test
    fun `POST refresh-token returns 200 with new tokens`() = testApplication {
        coEvery { service.refreshToken("valid-refresh") } returns RefreshResult(
            accessToken = "new-access",
            refreshToken = "new-refresh",
        )
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/refresh-token") {
            setBody(RefreshTokenRequest(refreshToken = "valid-refresh"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST refresh-token returns 401 for invalid token`() = testApplication {
        coEvery { service.refreshToken(any()) } throws UnauthorizedException()
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/refresh-token") {
            setBody(RefreshTokenRequest(refreshToken = "invalid-refresh"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST refresh-token returns 400 for empty token`() = testApplication {
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/refresh-token") {
            setBody(RefreshTokenRequest(refreshToken = ""))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST resend-otp returns 200 on success`() = testApplication {
        coEvery { service.resendOTP(any()) } returns OtpResendResult(
            emailId = "stytch-email-id",
            verificationToken = "tok",
        )
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/resend-otp") {
            setBody(ResendOTPRequest(email = "user@example.com"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST resend-otp returns 200 with empty body for unknown email (no enumeration)`() = testApplication {
        coEvery { service.resendOTP(any()) } returns OtpResendResult(emailId = "", verificationToken = "")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/resend-otp") {
            setBody(ResendOTPRequest(email = "ghost@example.com"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `POST resend-otp returns 400 for already-verified user`() = testApplication {
        coEvery { service.resendOTP(any()) } throws BadRequestException("User is already verified")
        application { configure() }
        val client = clientConfiguration()

        val response = client.post("/authentication/resend-otp") {
            setBody(ResendOTPRequest(email = "user@example.com"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST resend-otp rate-limit returns 429 on second request within window`() = testApplication {
        coEvery { service.resendOTP(any()) } returns OtpResendResult(
            emailId = "stytch-email-id",
            verificationToken = "tok",
        )
        application { configure() }
        val client = clientConfiguration()

        val first = client.post("/authentication/resend-otp") {
            setBody(ResendOTPRequest(email = "user@example.com"))
        }
        val second = client.post("/authentication/resend-otp") {
            setBody(ResendOTPRequest(email = "user@example.com"))
        }

        assertEquals(HttpStatusCode.OK, first.status)
        assertEquals(HttpStatusCode.TooManyRequests, second.status)
    }
}
