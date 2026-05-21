package com.martdev.features.auth.api

import com.martdev.features.auth.api.request.*
import com.martdev.features.auth.domain.service.UserService
import com.martdev.shared.api.DataResponse
import io.ktor.http.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val authenticationPath = "/authentication"
const val registerPath = "/register"
const val verifyUserPath = "/verify-user"
const val loginUserPath = "/login"
const val refreshTokenPath = "/refresh-token"
const val resendOTPPath = "/resend-otp"

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
            val response = service.registerUser(userRequest.toCredentials()).toCreateUserResponse()
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
            service.verifyUser(request.toVerificationInput())
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
         *      - 404 [com.martdev.shared.api.ErrorResponse]
         *      - 429 [com.martdev.shared.api.ErrorResponse] too many requests
         *      - 500 [com.martdev.shared.api.ErrorResponse]
         */
        rateLimit(RateLimitName("login")) {
            post(loginUserPath) {
                val request = call.receive<UserLoginRequest>().toCredentials()
                val response = service.loginUser(request).toUserLoginResponse()
                val dataResponse = DataResponse(response)
                call.respond(status = HttpStatusCode.OK, dataResponse)
            }
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
