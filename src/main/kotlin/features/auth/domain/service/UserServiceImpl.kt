package com.martdev.features.auth.domain.service

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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Single
import java.security.MessageDigest
import java.util.HexFormat
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@Single
class UserServiceImpl(
    private val repository: UserRepository,
    private val otpProvider: OTPProvider,
    private val auth: Auth,
    private val passwordHasher: PasswordHasher
) : UserService {
    override suspend fun registerUser(userData: UserData): UserData {
        val hashedPassword = passwordHasher.hashPassword(userData.password)
        val token = UUID.randomUUID().toString()

        return when (val result =
            repository.saveUserAndVerificationToken(userData.copy(password = hashedPassword), token)) {
            is DataResult.Failure.UniqueViolation -> throw BadRequestException("Duplicate email")

            is DataResult.Success -> {
                val entity = result.value
                val (emailId, error) = otpProvider.sendVerificationCode(entity.email)
                if (error.isNotEmpty()) {
                    throw InternalServerException("Failed to send OTP")
                }
                UserData(
                    emailId = emailId,
                    registrationToken = token
                )
            }

            else -> throw InternalServerException()
        }
    }

    override suspend fun verifyUser(userData: UserData) {
        val (isSuccess, errorMessage) = otpProvider.verifyCode(userData.emailId, userData.code)
        if (!isSuccess) {
            println(errorMessage) //replace with a logger
            throw BadRequestException("Invalid or expired OTP")
        }
        val result = repository.activateUser(userData.registrationToken)
        if (result is DataResult.Failure) {
            if (result is DataResult.Failure.NotFound) {
                throw NotFoundException("Invalid or expired verification token")
            } else throw InternalServerException("An error occurred during verification")
        }
    }

    override suspend fun loginUser(userData: UserData): UserData {
        val errorMessage = "Invalid email or password"
        return when (val result = repository.getUserByEmail(userData.email)) {
            is DataResult.Failure.NotFound -> throw BadRequestException(errorMessage)

            is DataResult.Success -> {
                val savedUser = result.value
                val isValid = passwordHasher.verifyPassword(userData.password, savedUser.password)
                if (!isValid) throw BadRequestException(errorMessage)

                if (!savedUser.isVerified) throw UnauthorizedException("Please verify your email before logging in")

                val accessToken = auth.generateAccessToken(savedUser.id.toString(), savedUser.role.name)
                val refreshToken = auth.generateRefreshToken()
                val refreshTokenInHex = generateHexValueFromToken(refreshToken)
                val refreshExpiry = Clock.System.now().plus(24.hours).toLocalDateTime(TimeZone.UTC)

                when (repository.saveRefreshToken(savedUser.id, refreshTokenInHex, refreshExpiry)) {
                    is DataResult.Failure.NotFound -> throw NotFoundException()

                    is DataResult.Success -> UserData(
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        id = savedUser.id
                    )

                    else -> throw InternalServerException()
                }
            }

            else -> throw InternalServerException()
        }
    }

    override suspend fun refreshToken(refreshToken: String): UserData {
        val hexToken = generateHexValueFromToken(refreshToken)

        return when (val result = repository.getUserIdAndRoleByRefreshToken(hexToken)) {
            is DataResult.Failure.NotFound -> {
                throw UnauthorizedException()
            }

            is DataResult.Success -> {
                val savedUser = result.value
                repository.revokeRefreshToken(hexToken)

                val newAccessToken = auth.generateAccessToken(savedUser.id.toString(), savedUser.role.name)
                val newRefreshToken = auth.generateRefreshToken()
                val newRefreshTokenInHex = generateHexValueFromToken(newRefreshToken)
                val newExpiryDate = Clock.System.now().plus(24.hours).toLocalDateTime(TimeZone.UTC)
                repository.saveRefreshToken(savedUser.id, newRefreshTokenInHex, newExpiryDate)

                UserData(accessToken = newAccessToken, refreshToken = newRefreshToken)
            }

            else -> throw InternalServerException()
        }
    }

    override suspend fun deleteExpiredRefreshToken() {
        repository.deleteExpiredRefreshToken()
    }

    override suspend fun resendOTP(email: String): UserData {
        return when (val result = repository.getUserByEmail(email)) {
            is DataResult.Failure -> UserData(emailId = "N/A", verificationToken = "N/A")
            is DataResult.Success -> {
                val savedUser = result.value
                if (savedUser.isVerified) throw BadRequestException("User is already verified")

                val (emailId, error) = otpProvider.sendVerificationCode(savedUser.email)
                if (error.isNotEmpty()) throw InternalServerException("Failed to resend OTP")

                val token = UUID.randomUUID().toString()
                repository.deleteAndCreateVerificationToken(token, savedUser.id)

                UserData(emailId = emailId, verificationToken = token)
            }
        }
    }

    private fun generateHexValueFromToken(token: String): String {
        val hashedToken = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
        return HexFormat.of().formatHex(hashedToken)
    }
}