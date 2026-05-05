package com.martdev.features.auth.domain.repository

import com.martdev.shared.domain.model.DataResult
import com.martdev.features.auth.domain.model.UserData
import kotlinx.datetime.LocalDateTime

interface UserRepository {
    suspend fun activateUser(token: String): DataResult<Unit>
    suspend fun saveUserAndVerificationToken(user: UserData, token: String): DataResult<UserData>
    suspend fun saveRefreshToken(userId: Long, tokenHash: String, time: LocalDateTime): DataResult<Unit>
    suspend fun deleteExpiredRefreshToken(): DataResult<Unit>
    suspend fun getUserByEmail(email: String): DataResult<UserData>
    suspend fun getUserById(userId: Long): DataResult<UserData>
    suspend fun getUserIdAndRoleByRefreshToken(tokenHash: String): DataResult<UserData>
    suspend fun revokeRefreshToken(tokenHash: String): DataResult<Unit>
    suspend fun deleteAndCreateVerificationToken(token: String, userId: Long): DataResult<Unit>
    suspend fun deleteUserAndVerificationToken(userId: Long): DataResult<Unit>
}