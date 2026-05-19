package com.martdev.plugins

import com.martdev.features.auth.domain.service.UserService
import com.martdev.features.payment.domain.service.PaymentService
import com.martdev.features.reservation.domain.service.ReservationService
import io.ktor.server.application.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun Application.configureBackgroundJobs() {
    val userService by inject<UserService>()
    val reservationService by inject<ReservationService>()
    val paymentService by inject<PaymentService>()
    launch {
        while (isActive) {
            delay(24.hours)
            userService.deleteExpiredRefreshToken()
        }
    }
    launch {
        while (isActive) {
            delay(1.minutes)
            reservationService.cancelExpiredReservations()
        }
    }
    launch {
        while (isActive) {
            delay(5.minutes)
            paymentService.reconcilePendingPayments()
        }
    }
    monitor.subscribe(ApplicationStopping) {
        coroutineContext[Job]?.cancel()
    }
}