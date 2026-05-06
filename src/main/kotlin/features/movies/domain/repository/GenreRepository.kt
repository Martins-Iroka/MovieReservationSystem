package com.martdev.features.movies.domain.repository

import com.martdev.features.movies.domain.model.Genre
import com.martdev.shared.domain.model.DataResult

interface GenreRepository {
    suspend fun saveGenre(genre: Genre): DataResult<Genre>
    suspend fun getGenres(): DataResult<List<Genre>>
    suspend fun deleteGenre(id: Long): DataResult<Int>
}