package com.martdev.features.movies.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class Movie(
    val id: Long = 0L,
    val title: String = "",
    val description: String = "",
    val posterUrl: String = "",
    val duration: Int = 0,
    val releasedDate: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC)
)
