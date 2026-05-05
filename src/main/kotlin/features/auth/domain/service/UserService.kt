package com.martdev.features.auth.domain.service

import com.martdev.features.auth.domain.model.UserData

interface UserService {
    suspend fun registerUser(userData: UserData): UserData
    suspend fun verifyUser(userData: UserData)
    suspend fun loginUser(userData: UserData): UserData
    suspend fun refreshToken(refreshToken: String): UserData
    suspend fun deleteExpiredRefreshToken()
    suspend fun resendOTP(email: String): UserData
}