package com.martdev.features.movies.domain.repository

import com.martdev.features.movies.domain.model.Movie
import com.martdev.shared.domain.model.DataResult

interface MovieRepository {
    suspend fun saveMovieData(movie: Movie): DataResult<Long>
    suspend fun getMovies(limit: Int, offset: Long): DataResult<List<Movie>>
    suspend fun getMovieById(movieId: Long): DataResult<Movie>
    suspend fun updateMovie(movie: Movie): DataResult<Long>
    suspend fun deleteMovie(id: Long): DataResult<Int>
    suspend fun getMoviesByGenre(genreId: Long, limit: Int, offset: Long): DataResult<List<Movie>>
}