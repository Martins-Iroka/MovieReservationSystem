package com.martdev.features.showtime.domain.repository

import com.martdev.features.showtime.domain.model.Showtime
import com.martdev.features.showtime.domain.model.ShowtimeStatus
import com.martdev.shared.domain.model.DataResult

interface ShowtimeRepository {
    suspend fun createShowtime(showtime: Showtime): DataResult<Showtime>

    suspend fun getShowtimes(limit: Int, offset: Long): DataResult<List<Showtime>>

    suspend fun getShowtimesByMovieId(movieId: Long): DataResult<List<Showtime>>

    suspend fun getShowtimeById(id: Long): DataResult<Showtime>

    suspend fun updateShowtime(showtime: Showtime): DataResult<Showtime>

    suspend fun deleteShowtime(id: Long): DataResult<Int>

    suspend fun updateShowtimeStatus(id: Long, status: ShowtimeStatus): DataResult<Showtime>
}
