package com.martdev.features.movies.domain.service.genre

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.repository.GenreRepository
import com.martdev.shared.util.returnValue
import org.koin.core.annotation.Single

@Single
class GenreServiceImpl(
    private val repository: GenreRepository
) : GenreService {
    override suspend fun createGenre(genre: Genre) {
        repository.saveGenre(genre).returnValue()
    }

    override suspend fun getGenres(): List<Genre> {
        return repository.getGenres().returnValue()
    }

    override suspend fun deleteGenre(id: Long) {
        repository.deleteGenre(id).returnValue()
    }
}