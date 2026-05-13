package com.martdev.features.showtime.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val defaultLocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)

data class Showtime(
    val id: Long = 0,
    val movieId: Long = 0,
    val roomId: Long = 0,
    val startsAt: LocalDateTime = defaultLocalDateTime,
    val endsAt: LocalDateTime = defaultLocalDateTime,
    val price: Int = 0,
    val status: ShowtimeStatus = ShowtimeStatus.SCHEDULED
)
