package com.martdev.infrastructure.db.repository

import com.martdev.domain.DataResult
import com.martdev.domain.model.User
import com.martdev.domain.repository.UserRepository
import kotlinx.datetime.LocalDateTime

class UserRepositoryImpl : UserRepository {
    override suspend fun activateUser(token: String): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun saveUserAndVerificationToken(
        user: User,
        token: String
    ): DataResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun saveRefreshToken(
        userId: Long,
        tokenHash: String,
        time: LocalDateTime
    ): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteExpiredRefreshToken(): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserByEmail(email: String): DataResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserById(userId: Long): DataResult<User> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserIdAndRoleByRefreshToken(tokenHash: String): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun revokeRefreshToken(tokenHash: String): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAndCreateVerificationToken(token: String, userId: Long) {
        TODO("Not yet implemented")
    }
}