package com.martdev.features.showtime.domain.service

import com.martdev.features.showtime.domain.model.Showtime
import com.martdev.features.showtime.domain.model.ShowtimeStatus
import com.martdev.features.showtime.domain.repository.ShowtimeRepository
import com.martdev.shared.domain.exception.ConflictException
import com.martdev.shared.domain.exception.InternalServerException
import com.martdev.shared.domain.exception.NotFoundException
import com.martdev.shared.domain.model.DataResult
import com.martdev.shared.util.returnValue
import org.koin.core.annotation.Single

@Single
class ShowtimeServiceImpl(
    private val repo: ShowtimeRepository
) : ShowtimeService {
    override suspend fun createShowtime(showtime: Showtime): Showtime {
        return when (val result = repo.createShowtime(showtime)) {
            is DataResult.Failure.Conflict -> throw ConflictException("Room is already booked for this time slot")
            is DataResult.Failure.ForeignKeyViolation -> throw NotFoundException("Movie or room not found")
            is DataResult.Success -> result.value
            else -> throw InternalServerException()
        }
    }

    override suspend fun getShowtimes(
        limit: Int,
        offset: Long
    ): List<Showtime> {
        return repo.getShowtimes(limit, offset).returnValue()
    }

    override suspend fun getShowtimesByMovieId(movieId: Long): List<Showtime> {
        return repo.getShowtimesByMovieId(movieId).returnValue()
    }

    override suspend fun getShowtimeById(id: Long): Showtime {
        return repo.getShowtimeById(id).returnValue()
    }

    override suspend fun updateShowtime(showtime: Showtime): Showtime {
        return when (val result = repo.updateShowtime(showtime)) {
            is DataResult.Success -> result.value
            is DataResult.Failure.Conflict -> throw ConflictException("Room is already booked for this time slot")
            is DataResult.Failure.NotFound -> throw NotFoundException("Showtime not found")
            else -> throw InternalServerException()
        }
    }

    override suspend fun deleteShowtime(id: Long) {
        repo.deleteShowtime(id).returnValue()
    }

    override suspend fun updateShowtimeStatus(
        id: Long,
        status: ShowtimeStatus
    ): Showtime {
        return repo.updateShowtimeStatus(id, status).returnValue()
    }
}