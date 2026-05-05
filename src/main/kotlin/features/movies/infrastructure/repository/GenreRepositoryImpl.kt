package com.martdev.features.movies.infrastructure.repository

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.repository.GenreRepository
import com.martdev.shared.domain.model.DataResult

class GenreRepositoryImpl : GenreRepository {
    override suspend fun saveGenre(genre: Genre): DataResult<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getGenres(): DataResult<List<Genre>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteGenre(id: Long): DataResult<Unit> {
        TODO("Not yet implemented")
    }
}