package com.martdev.features.auth.api

import com.martdev.features.auth.api.request.CreateUserRequest
import com.martdev.features.auth.api.request.UserLoginRequest
import com.martdev.features.auth.api.request.UserVerificationRequest
import com.martdev.features.auth.api.response.CreateUserResponse
import com.martdev.features.auth.api.response.RefreshTokenResponse
import com.martdev.features.auth.api.response.ResendOTPResponse
import com.martdev.features.auth.api.response.UserLoginResponse
import com.martdev.features.auth.domain.model.UserData

fun CreateUserRequest.toUserData() = UserData(
    email = email,
    password = password
)

fun UserData.toCreateUserResponse() = CreateUserResponse(
    emailId = emailId,
    token = registrationToken
)

fun UserVerificationRequest.toUserData() = UserData(
    code = code,
    emailId = emailId,
    verificationToken = token
)

fun UserLoginRequest.toUserData() = UserData(
    email = email,
    password = password
)

fun UserData.toUserLoginResponse() = UserLoginResponse(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun UserData.toRefreshTokenResponse() = RefreshTokenResponse(
    accessToken = accessToken,
    refreshToken = refreshToken
)

fun UserData.toResendOTPResponse() = ResendOTPResponse(
    emailId = emailId,
    verificationToken = verificationToken
)