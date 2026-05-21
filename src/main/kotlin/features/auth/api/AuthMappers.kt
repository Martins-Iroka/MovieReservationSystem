package com.martdev.features.auth.api

import com.martdev.features.auth.api.request.CreateUserRequest
import com.martdev.features.auth.api.request.UserLoginRequest
import com.martdev.features.auth.api.request.UserVerificationRequest
import com.martdev.features.auth.api.response.CreateUserResponse
import com.martdev.features.auth.api.response.RefreshTokenResponse
import com.martdev.features.auth.api.response.ResendOTPResponse
import com.martdev.features.auth.api.response.UserLoginResponse
import com.martdev.features.auth.domain.model.Credentials
import com.martdev.features.auth.domain.model.LoginResult
import com.martdev.features.auth.domain.model.OtpResendResult
import com.martdev.features.auth.domain.model.RefreshResult
import com.martdev.features.auth.domain.model.RegistrationResult
import com.martdev.features.auth.domain.model.VerificationInput

fun CreateUserRequest.toCredentials() = Credentials(
    email = email,
    password = password
)

fun RegistrationResult.toCreateUserResponse() = CreateUserResponse(
    emailId = emailId,
    token = registrationToken
)

fun UserVerificationRequest.toVerificationInput() = VerificationInput(
    code = code,
    emailId = emailId,
    registrationToken = token
)

fun UserLoginRequest.toCredentials() = Credentials(
    email = email,
    password = password
)

fun LoginResult.toUserLoginResponse() = UserLoginResponse(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun RefreshResult.toRefreshTokenResponse() = RefreshTokenResponse(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun OtpResendResult.toResendOTPResponse() = ResendOTPResponse(
    emailId = emailId,
    verificationToken = verificationToken
)
