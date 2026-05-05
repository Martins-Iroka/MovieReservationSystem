package com.martdev.features.auth.api

import com.martdev.features.auth.api.request.CreateUserRequest
import com.martdev.features.auth.api.request.RefreshTokenRequest
import com.martdev.features.auth.api.request.ResendOTPRequest
import com.martdev.features.auth.api.request.UserLoginRequest
import com.martdev.features.auth.api.request.UserVerificationRequest
import com.martdev.features.auth.api.response.RefreshTokenResponse
import com.martdev.features.auth.domain.service.UserService
import com.martdev.shared.api.DataResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

const val authenticationPath = "/authentication"
const val registerPath = "/register"
const val verifyUserPath = "/verify-user"
const val loginUserPath = "/login"
const val refreshTokenPath = "/refresh-token"
const val resendOTPPath = "/resend-otp"

//todo write test
fun Route.authRoutes() {
    val service by inject<UserService>()
    route(authenticationPath) {

        /**
         * Tag: authentication
         *
         * Registers a user
         *
         * Responses:
         *      - 201 [com.martdev.features.auth.api.response.CreateUserResponse]
         *      - 400 [com.martdev.shared.api.ErrorResponse] duplicate email
         *      - 500 [com.martdev.shared.api.ErrorResponse]
         */
        post(registerPath) {
            val userRequest = call.receive<CreateUserRequest>()
            val response = service.registerUser(userRequest.toUserData()).toCreateUserResponse()
            val dataResponse = DataResponse(response)
            call.respond(status = HttpStatusCode.Created, dataResponse)
        }

        /**
         * Tag: authentication
         *
         * Verifies a user
         *
         * Responses:
         *      - 200 the user is verified
         *      - 400 [com.martdev.shared.api.ErrorResponse] Invalid or expired OTP
         *      - 404 [com.martdev.shared.api.ErrorResponse] Invalid or expired verification token
         *      - 500 [com.martdev.shared.api.ErrorResponse] An error occurred during verification
         */
        post(verifyUserPath) {
            val request = call.receive<UserVerificationRequest>()
            service.verifyUser(request.toUserData())
            call.respond(HttpStatusCode.OK)
        }

        /**
         * Tag: authentication
         *
         * Login a user
         *
         * Responses:
         *      - 200 [com.martdev.features.auth.api.response.UserLoginResponse]
         *      - 400 [com.martdev.shared.api.ErrorResponse] Invalid email or password
         *      - 401 [com.martdev.shared.api.ErrorResponse] Please verify your email before login
         *      - 404 [com.martdev.shared.api.ErrorResponse]
         *      - 500 [com.martdev.shared.api.ErrorResponse]
         */
        post(loginUserPath) {
            val request = call.receive<UserLoginRequest>().toUserData()
            val response = service.loginUser(request).toUserLoginResponse()
            val dataResponse = DataResponse(response)
            call.respond(status = HttpStatusCode.OK, dataResponse)
        }

        /**
         * Tag: authentication
         *
         * Refresh access token
         *
         * Responses:
         *      - 200 [com.martdev.features.auth.api.response.RefreshTokenResponse]
         *      - 401 [com.martdev.shared.api.ErrorResponse] unauthorized
         *      - 500 [com.martdev.shared.api.ErrorResponse]
         */
        post(refreshTokenPath) {
            val request = call.receive<RefreshTokenRequest>()
            val response = service.refreshToken(request.refreshToken).toRefreshTokenResponse()
            val dataResponse = DataResponse(response)
            call.respond(status = HttpStatusCode.OK, dataResponse)
        }

        /**
         * Tag: authentication
         *
         * Resend verification code
         *
         * Responses:
         *      - 200 [com.martdev.features.auth.api.response.ResendOTPResponse]
         *      - 400 [com.martdev.shared.api.ErrorResponse] User is already verified
         *      - 500 [com.martdev.shared.api.ErrorResponse] Failed to resend OTP
         */
        rateLimit(RateLimitName("resend-otp")) {
            post(resendOTPPath) {
                val request = call.receive<ResendOTPRequest>()
                val response = service.resendOTP(request.email).toResendOTPResponse()
                val dataResponse = DataResponse(response)
                call.respond(status = HttpStatusCode.OK, dataResponse)
            }
        }
    }
}