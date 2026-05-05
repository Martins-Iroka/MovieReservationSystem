package com.martdev.plugins

import com.martdev.features.auth.domain.service.UserService
import io.ktor.server.application.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.hours

fun Application.configureBackgroundJobs() {
    val userService by inject<UserService>()
    launch {
        while (isActive) {
            delay(24.hours)
            userService.deleteExpiredRefreshToken()
        }
    }
    monitor.subscribe(ApplicationStopping) {
        coroutineContext[Job]?.cancel()
    }
}