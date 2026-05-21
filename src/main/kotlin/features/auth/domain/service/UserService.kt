package com.martdev.features.auth.domain.service

import com.martdev.features.auth.domain.model.Credentials
import com.martdev.features.auth.domain.model.LoginResult
import com.martdev.features.auth.domain.model.OtpResendResult
import com.martdev.features.auth.domain.model.RefreshResult
import com.martdev.features.auth.domain.model.RegistrationResult
import com.martdev.features.auth.domain.model.VerificationInput

interface UserService {
    suspend fun registerUser(credentials: Credentials): RegistrationResult
    suspend fun verifyUser(input: VerificationInput)
    suspend fun loginUser(credentials: Credentials): LoginResult
    suspend fun refreshToken(refreshToken: String): RefreshResult
    suspend fun deleteExpiredRefreshToken()
    suspend fun resendOTP(email: String): OtpResendResult
}
