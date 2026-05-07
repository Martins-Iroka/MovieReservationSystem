package com.martdev.features.movies.domain.service.genre

import com.martdev.features.movies.domain.model.Genre

interface GenreService {
    suspend fun createGenre(genre: Genre): Genre
    suspend fun getGenres(): List<Genre>
    suspend fun deleteGenre(id: Long)
}