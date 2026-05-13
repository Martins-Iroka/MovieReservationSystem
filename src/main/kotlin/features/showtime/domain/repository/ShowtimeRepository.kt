package com.martdev.features.showtime.domain.repository

import com.martdev.features.showtime.domain.model.Showtime
import com.martdev.features.showtime.domain.model.ShowtimeStatus
import com.martdev.shared.domain.model.DataResult
import kotlin.time.Instant

interface ShowtimeRepository {
    suspend fun createShowtime(showtime: Showtime): DataResult<Showtime>

    suspend fun getShowtimes(limit: Int, offset: Long): DataResult<List<Showtime>>

    suspend fun getShowtimesByMovieId(movieId: Long): DataResult<List<Showtime>>

    suspend fun getShowtimeById(id: Long): DataResult<Showtime>

    suspend fun updateShowtime(showtime: Showtime): DataResult<Showtime>

    suspend fun deleteShowtime(id: Long): DataResult<Int>

    suspend fun updateShowtimeStatus(id: Long, status: ShowtimeStatus): DataResult<Showtime>

    suspend fun hasOverlappingShowtime(
        roomId: Long,
        startsAt: Instant,
        endsAt: Instant,
        excludeId: Long = -1
    ): DataResult<Boolean>
}
