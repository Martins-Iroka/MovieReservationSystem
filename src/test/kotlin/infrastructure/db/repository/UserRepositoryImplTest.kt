package com.martdev.infrastructure.db.repository

import com.martdev.domain.DataResult
import com.martdev.domain.model.User
import com.martdev.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

var user = User(
    email = "testEmail@gmail.com",
    password = "password",
)
class UserRepositoryImplTest {

    private lateinit var repository: UserRepository

    companion object {
        const val VERIFICATION_TOKEN = "verification_token"
        const val REFRESH_TOKEN = "refresh_token"
        val userFlow = MutableStateFlow(User())
        @BeforeClass
        @JvmStatic
        fun startContainer() {
            postgres.start()
            connectAndMigrate()
        }

        @AfterClass
        @JvmStatic
        fun stopContainer() {
            postgres.stop()
        }
    }

    @Before
    fun setup() {
        repository = UserRepositoryImpl()
    }

    @Test
    fun `save user and verification token should return data result user`() = runTest {
        val savedUser = repository.saveUserAndVerificationToken(user, VERIFICATION_TOKEN)

        assertTrue(savedUser is DataResult.Success, savedUser.toString())
        user = user.copy(id = savedUser.value.id)
    }

    @Test
    fun `get user by id`() = runTest {
        val userResult = repository.getUserById(user.id)
        assertTrue(userResult is DataResult.Success)
        user = userResult.value
    }

    @Test
    fun `test user activation`() = runTest {
        val result = repository.activateUser(VERIFICATION_TOKEN)
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `test save refresh token`() = runTest {
        println(user)
        val result = repository.saveRefreshToken(
            user.id,
            REFRESH_TOKEN,
            Clock.System.now().plus(5.milliseconds).toLocalDateTime(TimeZone.currentSystemDefault())
        )

        assertTrue(result is DataResult.Success, result.toString())
    }

    @Test
    fun `test get user id and role by refresh token`() = runTest {
        val result = repository.getUserIdAndRoleByRefreshToken(REFRESH_TOKEN)
        assertTrue(result is DataResult.Success)
        assertEquals(user.id, result.value.id)
    }

    @Test
    fun `should get user by email`() = runTest {
        val result = repository.getUserByEmail(user.email)
        assertTrue(result is DataResult.Success)
        assertEquals(user.id, result.value.id)
    }

    @Test
    fun `should revoke refresh token`() = runTest {
        val result = repository.revokeRefreshToken(REFRESH_TOKEN)
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `should delete expired refresh token`() = runTest {
        val result = repository.deleteExpiredRefreshToken()
        assertTrue(result is DataResult.Success)
    }

    @Test
    fun `should delete and create verification token`() = runTest {
        val result = repository.deleteAndCreateVerificationToken(VERIFICATION_TOKEN, user.id)
        assertTrue(result is DataResult.Success)
    }
}