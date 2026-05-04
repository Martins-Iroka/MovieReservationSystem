package com.martdev.infrastructure.db.repository

import com.martdev.domain.DataResult
import com.martdev.domain.model.User
import com.martdev.domain.repository.UserRepository
import com.martdev.infrastructure.db.tables.user.UserRefreshTokenTable
import com.martdev.infrastructure.db.tables.user.UserTable
import com.martdev.infrastructure.db.tables.user.UserVerificationTable
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.*
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
class UserRepositoryImplTest {

    private lateinit var repository: UserRepository


    companion object {
        const val VERIFICATION_TOKEN = "verification_token"
        const val REFRESH_TOKEN = "refresh_token"
        var user = User(
            email = "testEmail@gmail.com",
            password = "password",
        )
        @Container
        val postgres: PostgreSQLContainer = PostgreSQLContainer(
            "postgres:16-alpine"
        ).apply {
            withDatabaseName("mrs")
            withUsername("test")
            withPassword("test")
        }
        fun connectAndMigrate(){
            Flyway.configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .load().migrate()

            Database.connect(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )
        }
        @JvmStatic
        @AfterAll
        fun clearDb() {
            transaction {
                UserRefreshTokenTable.deleteAll()
                UserVerificationTable.deleteAll()
                UserTable.deleteAll()
            }
        }

        @JvmStatic
        @BeforeAll
        fun setupContainer() {
            connectAndMigrate()
        }
    }

    @BeforeEach
    fun setup() {
        repository = UserRepositoryImpl()
    }

    @Test
    @Order(1)
    fun `save user and verification token should return data result user`() = runTest {
        val savedUser = repository.saveUserAndVerificationToken(user, VERIFICATION_TOKEN)

        assertTrue(savedUser is DataResult.Success)
        user = user.copy(id = savedUser.value.id)
    }

    @Test
    @Order(2)
    fun `save user with same email should fail`() = runTest {
        val savedUser = repository.saveUserAndVerificationToken(user, VERIFICATION_TOKEN)

        assertTrue(savedUser is DataResult.Failure.UniqueViolation)
    }

    @Test
    @Order(3)
    fun `save user verification with same token should fail`() = runTest {
        val savedUser = repository.saveUserAndVerificationToken(user.copy(email = "test2@gmail.com"), VERIFICATION_TOKEN)

        assertTrue(savedUser is DataResult.Failure.UniqueViolation)
    }

    @Test
    @Order(4)
    fun `get user by id`() = runTest {
        val userResult = repository.getUserById(user.id)
        assertTrue(userResult is DataResult.Success)
    }

    @Test
    @Order(5)
    fun `get user with a wrong id should fail`() = runTest {
        val userResult = repository.getUserById(Random.nextLong())
        assertTrue(userResult is DataResult.Failure.NotFound)
    }

    @Test
    @Order(6)
    fun `test user activation`() = runTest {
        val result = repository.activateUser(VERIFICATION_TOKEN)
        assertTrue(result is DataResult.Success)
    }
    
    @Test
    @Order(7)
    fun `test activate user should fail as a result of get user id by verification token`() = runTest { 
        val result = repository.activateUser("invalid_token")
        assertTrue(result is DataResult.Failure.NotFound)
    }

    @Test
    @Order(8)
    fun `test save refresh token`() = runTest {
        val result = repository.saveRefreshToken(
            user.id,
            REFRESH_TOKEN,
            Clock.System.now().plus(1.hours).toLocalDateTime(TimeZone.currentSystemDefault())
        )

        assertTrue(result is DataResult.Success)
    }
    
    @Test
    @Order(9)
    fun `test save refresh token should fail with not found`() = runTest { 
        val result = repository.saveRefreshToken(
            Random.nextLong(),
            REFRESH_TOKEN.plus("2"),
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
        assertTrue(result is DataResult.Failure.NotFound)
    }

    @Test
    @Order(10)
    fun `test get user id and role by refresh token`() = runTest {
        val result = repository.getUserIdAndRoleByRefreshToken(REFRESH_TOKEN)
        assertTrue(result is DataResult.Success, result.toString())
        assertEquals(user.id, result.value.id)
    }

    @Test
    @Order(11)
    fun `test get user id and role by refresh token should fail with not found`() = runTest {
        val result = repository.getUserIdAndRoleByRefreshToken("invalid_token")
        assertTrue(result is DataResult.Failure.NotFound)
    }

    @Test
    @Order(12)
    fun `should get user by email`() = runTest {
        val result = repository.getUserByEmail(user.email)
        assertTrue(result is DataResult.Success)
        assertEquals(user.id, result.value.id)
    }

    @Test
    @Order(13)
    fun `get user by email should fail with not found`() = runTest {
        val result = repository.getUserByEmail(user.email.plus("mm"))
        assertTrue(result is DataResult.Failure.NotFound)
    }

    @Test
    @Order(14)
    fun `should revoke refresh token`() = runTest {
        val result = repository.revokeRefreshToken(REFRESH_TOKEN)
        assertTrue(result is DataResult.Success)
    }

    @Test
    @Order(15)
    fun `test get user id and role by refresh token should fail with not found after token has been revoked`() = runTest {
        val result = repository.getUserIdAndRoleByRefreshToken(REFRESH_TOKEN)
        assertTrue(result is DataResult.Failure.NotFound)
    }

    @Test
    @Order(16)
    fun `should delete expired refresh token`() = runTest {
        val result = repository.deleteExpiredRefreshToken()
        assertTrue(result is DataResult.Success)
    }

    @Test
    @Order(17)
    fun `should delete and create verification token`() = runTest {
        val result = repository.deleteAndCreateVerificationToken(VERIFICATION_TOKEN, user.id)
        assertTrue(result is DataResult.Success)
    }
}
