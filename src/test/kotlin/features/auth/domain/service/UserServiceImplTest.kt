package com.martdev.features.auth.domain.service

import com.martdev.features.auth.domain.model.Role
import com.martdev.features.auth.domain.model.UserData
import com.martdev.features.auth.domain.repository.UserRepository
import com.martdev.features.auth.domain.security.Auth
import com.martdev.features.auth.domain.security.OTPProvider
import com.martdev.features.auth.domain.security.PasswordHasher
import com.martdev.shared.domain.exception.BadRequestException
import com.martdev.shared.domain.exception.InternalServerException
import com.martdev.shared.domain.exception.NotFoundException
import com.martdev.shared.domain.exception.UnauthorizedException
import com.martdev.shared.domain.model.DataResult
import io.ktor.client.request.request
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class UserServiceImplTest {

    @MockK
    private lateinit var repository: UserRepository

    @MockK
    private lateinit var otpProvider: OTPProvider

    @MockK
    private lateinit var auth: Auth

    @MockK
    private lateinit var passwordHasher: PasswordHasher

    private lateinit var service: UserService

    @BeforeEach
    fun setup() {
        service = UserServiceImpl(repository, otpProvider, auth, passwordHasher)
    }

    private val user = UserData(
        id = 1,
        email = "test@email.com",
        password = "password",
        role = Role.USER,
        code = "123456",
        emailId = "emailId",
        token = "token",
        isVerified = true,
        refreshToken = "refresh_token"
    )

    private val hashedPassword = "hashed_password"

    @Test
    fun `should register user successfully`() = runTest {

        val userSlot = slot<UserData>()
        val otpSlot = slot<String>()

        every {
            passwordHasher.hashPassword(any())
        } returns hashedPassword

        coEvery {
            repository.saveUserAndVerificationToken(capture(userSlot), any())
        } answers {
            assertEquals(user.email, userSlot.captured.email)
            assertEquals(hashedPassword, userSlot.captured.password)
            DataResult.Success(
                user
            )
        }

        coEvery {
            otpProvider.sendVerificationCode(capture(otpSlot))
        } answers {
            assertEquals(user.email, otpSlot.captured)
            Pair("emailId", "")
        }

        val result = service.registerUser(user)

        assertEquals("emailId", result.emailId)
        assertTrue(result.token.isNotEmpty())
    }

    @Test
    fun `should throw bad request exception for duplicate email or username`() = runTest {
        every {
            passwordHasher.hashPassword(any())
        } returns hashedPassword

        coEvery {
            repository.saveUserAndVerificationToken(any(), any())
        } returns DataResult.Failure.UniqueViolation

        val exception = assertFailsWith<BadRequestException> {
            service.registerUser(user)
        }

        assertEquals("Duplicate email", exception.error)
    }

    @Test
    fun `should throw internal server exception for unknown db error`() = runTest {
        every {
            passwordHasher.hashPassword(any())
        } returns hashedPassword

        coEvery {
            repository.saveUserAndVerificationToken(any(), any())
        } returns DataResult.Failure.UnknownError("error")

        val internalServerException = assertFailsWith<InternalServerException> {
            service.registerUser(user)
        }

        assertEquals("The server encountered a problem", internalServerException.error)
    }

    @Test
    fun `should throw internal server exception for otp error`() = runTest {
        every {
            passwordHasher.hashPassword(any())
        } returns hashedPassword

        coEvery {
            repository.saveUserAndVerificationToken(any(), any())
        } returns DataResult.Success(
            user
        )

        coEvery {
            otpProvider.sendVerificationCode(any())
        } returns Pair("", "error")

        val internalServerException = assertFailsWith<InternalServerException> {
            service.registerUser(user)
        }

        assertEquals("Failed to send OTP", internalServerException.error)
    }

    @Test
    fun `should request user verification then returns user verification response`() = runTest {

        val emailIdSlot = slot<String>()
        val codeSlot = slot<String>()
        val tokenSlot = slot<String>()

        coEvery {
            otpProvider.verifyCode(capture(emailIdSlot), capture(codeSlot))
        } answers {
            assertEquals(user.code, codeSlot.captured)
            assertEquals(user.emailId, emailIdSlot.captured)
            Pair(true, "")
        }

        coEvery {
            repository.activateUser(capture(tokenSlot))
        } answers {
            assertEquals(user.token, tokenSlot.captured)
            DataResult.Success(Unit)
        }

        service.verifyUser(user)
    }

    @Test
    fun `should throw bad request exception for failed otp verification`() = runTest {

        coEvery {
            otpProvider.verifyCode(any(), any())
        } returns Pair(false, "error")

        val exception = assertFailsWith<BadRequestException> {
            service.verifyUser(user)
        }

        assertEquals("Invalid or expired OTP", exception.error)
    }

    @Test
    fun `should throw not found exception when activating user`() = runTest {

        coEvery {
            otpProvider.verifyCode(any(), any())
        } returns Pair(true, "")

        coEvery {
            repository.activateUser(any())
        } returns DataResult.Failure.NotFound

        val exception = assertFailsWith<NotFoundException> {
            service.verifyUser(user)
        }

        assertEquals("Invalid or expired verification token", exception.error)
    }

    @Test
    fun `should throw internal server exception when activating user`() = runTest {
        coEvery {
            otpProvider.verifyCode(any(), any())
        } returns Pair(true, "")

        coEvery {
            repository.activateUser(any())
        } returns DataResult.Failure.UnknownError("error")

        val exception = assertFailsWith<InternalServerException> {
            service.verifyUser(user)
        }

        assertEquals("An error occurred during verification", exception.error)
    }

    private val accessToken = "accessToken"
    private val refreshToken = "refreshToken"

    @Test
    fun `should login user successfully`() = runTest {
        val emailSlot = slot<String>()
        val userIdSlot = slot<String>()

        coEvery {
            repository.getUserByEmail(capture(emailSlot))
        } answers {
            assertEquals(user.email, emailSlot.captured)
            DataResult.Success(
                user
            )
        }

        coEvery {
            passwordHasher.verifyPassword(any(), any())
        } returns true

        every {
            auth.generateAccessToken(capture(userIdSlot), any())
        } answers {
            assertEquals("1", userIdSlot.captured)
            accessToken
        }

        every {
            auth.generateRefreshToken()
        } returns refreshToken

        coEvery {
            repository.saveRefreshToken(any(), any(), any())
        } returns DataResult.Success(Unit)

        val response = service.loginUser(user)

        assertEquals(accessToken, response.accessToken)
        assertEquals(refreshToken, response.refreshToken)
    }


    @Test
    fun `should throw bad request exception for get user by email`() = runTest {
        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Failure.NotFound

        val exception = assertFailsWith<BadRequestException> { service.loginUser(user) }

        assertEquals("Invalid email or password", exception.error)

    }

    @Test
    fun `should throw internal server exception for get user by email`() = runTest {
        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Failure.UnknownError("error")

        assertFailsWith<InternalServerException> { service.loginUser(user) }
    }

    @Test
    fun `should throw bad request exception for incorrect password`() = runTest {
        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user
        )

        every {
            passwordHasher.verifyPassword(any(), any())
        } returns false

        val exception = assertFailsWith<BadRequestException> {
            service.loginUser(user)
        }

        assertEquals("Invalid email or password", exception.error)
    }

    @Test
    fun `should throw unauthorized exception for unverified user`() = runTest {

        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user.copy(isVerified = false)
        )

        coEvery {
            passwordHasher.verifyPassword(any(), any())
        } returns true

        val exception = assertFailsWith<UnauthorizedException> {
            service.loginUser(user)
        }

        assertEquals("Please verify your email before logging in", exception.error)
    }

    @Test
    fun `should throw not found exception when saving refresh token`() = runTest {

        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user
        )

        coEvery {
            passwordHasher.verifyPassword(any(), any())
        } returns true

        every {
            auth.generateAccessToken(any(), any())
        } returns accessToken

        every {
            auth.generateRefreshToken()
        } returns refreshToken

        coEvery {
            repository.saveRefreshToken(any(), any(), any())
        } returns DataResult.Failure.NotFound

        assertFailsWith<NotFoundException> {
            service.loginUser(user)
        }
    }

    @Test
    fun `should throw internal server exception when saving refresh token`() = runTest {

        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user
        )

        coEvery {
            passwordHasher.verifyPassword(any(), any())
        } returns true

        every {
            auth.generateAccessToken(any(), any())
        } returns accessToken

        every {
            auth.generateRefreshToken()
        } returns refreshToken

        coEvery {
            repository.saveRefreshToken(any(), any(), any())
        } returns DataResult.Failure.UnknownError("error")

        assertFailsWith<InternalServerException> {
            service.loginUser(user)
        }
    }

    @Test
    fun `should refresh token successfully`() = runTest {
        coEvery {
            repository.getUserIdAndRoleByRefreshToken(any())
        } returns DataResult.Success(user)

        coEvery {
            repository.revokeRefreshToken(any())
        } returns DataResult.Success(Unit)

        every {
            auth.generateAccessToken(any(), any())
        } returns accessToken

        every {
            auth.generateRefreshToken()
        } returns refreshToken

        coEvery {
            repository.saveRefreshToken(any(), any(), any())
        } returns DataResult.Success(Unit)

        val response = service.refreshToken("")

        assertEquals(accessToken, response.accessToken)
        assertEquals(refreshToken, response.refreshToken)
    }

    @Test
    fun `should throw unauthorized exception when getting user id by refresh token`() = runTest {

        coEvery {
            repository.getUserIdAndRoleByRefreshToken(any())
        } returns DataResult.Failure.NotFound

        assertFailsWith<UnauthorizedException> {
            service.refreshToken("")
        }
    }

    @Test
    fun `should delete expired refresh token`() = runTest {
        coEvery {
            repository.deleteExpiredRefreshToken()
        } returns DataResult.Success(Unit)

        repository.deleteExpiredRefreshToken()

        coVerify {
            repository.deleteExpiredRefreshToken()
        }
    }

    @Test
    fun `should resend otp successfully`() = runTest {

        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user.copy(isVerified = false)
        )

        coEvery {
            otpProvider.sendVerificationCode(any())
        } returns Pair("emailId", "")

        coEvery {
            repository.deleteAndCreateVerificationToken(any(), any())
        } returns DataResult.Success(Unit)

        val response = service.resendOTP("")

        assertEquals("emailId", response.emailId)
        assertTrue(response.verificationToken.isNotEmpty())
    }

    @Test
    fun `should return na parameter for db failed request to get user by email`() = runTest {

        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Failure.UnknownError("error")

        val response = service.resendOTP("")

        assertEquals("N/A", response.emailId)
        assertEquals("N/A", response.verificationToken)
    }

    @Test
    fun `should throw bad request exception when a verified user request for otp`() = runTest {
        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user
        )

        val exception = assertFailsWith<BadRequestException> {
            service.resendOTP("")
        }

        assertEquals("User is already verified", exception.error)
    }

    @Test
    fun `should throw internal server exception when otp provider fails to send code`() = runTest {
        coEvery {
            repository.getUserByEmail(any())
        } returns DataResult.Success(
            user.copy(isVerified = false)
        )

        coEvery {
            otpProvider.sendVerificationCode(any())
        } returns Pair("", "error")

        val exception = assertFailsWith<InternalServerException> {
            service.resendOTP("")
        }

        assertEquals("Failed to resend OTP", exception.error)
    }
}