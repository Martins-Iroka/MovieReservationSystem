package com.martdev.features.movies.infrastructure.repository

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.model.Movie
import com.martdev.features.movies.domain.repository.MovieRepository
import com.martdev.features.movies.infrastructure.tables.*
import com.martdev.shared.domain.model.DataResult
import com.martdev.shared.infrastruce.db.withSuspendTransaction
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.koin.core.annotation.Single

@Single
class MovieRepositoryImpl : MovieRepository {
    override suspend fun saveMovieData(movie: Movie): DataResult<Long> {
        return withSuspendTransaction {
            val movieEntity = MoviesEntity.new {
                title = movie.title
                description = movie.description
                posterUrl = movie.posterUrl
                duration = movie.duration
                releasedDate = movie.releasedDate
            }
            val result = linkGenres(movieEntity, movie.genres)
            if (result is DataResult.Failure.NotFound) {
                return@withSuspendTransaction DataResult.Failure.NotFound
            }
            DataResult.Success(movieEntity.id.value)
        }
    }

    override suspend fun getMovies(
        limit: Int,
        offset: Long
    ): DataResult<List<Movie>> {
        return withSuspendTransaction {
            val result = MoviesTable
                .select(
                    MoviesTable.id,
                    MoviesTable.title,
                    MoviesTable.posterUrl
                ).limit(limit).offset(offset)
                .orderBy(MoviesTable.createdAt, SortOrder.DESC)
                .map {
                    val id = it[MoviesTable.id].value
                    val title = it[MoviesTable.title]
                    val posterUrl = it[MoviesTable.posterUrl]
                    Movie(id = id, title = title, posterUrl = posterUrl)
                }

            DataResult.Success(result)
        }
    }

    override suspend fun getMovieById(movieId: Long): DataResult<Movie> {
        return withSuspendTransaction {
            val entity = MoviesEntity
                .findById(id = movieId) ?: return@withSuspendTransaction DataResult.Failure.NotFound
            val movie = entity.toMovie()
            DataResult.Success(movie)
        }
    }

    override suspend fun updateMovie(movie: Movie): DataResult<Long> {
        return withSuspendTransaction {
            val movieId = MoviesEntity.findByIdAndUpdate(movie.id) {
                it.title = movie.title
                it.description = movie.description
                it.posterUrl = movie.posterUrl
                it.duration = movie.duration
                it.releasedDate = movie.releasedDate
            }?.id?.value ?: return@withSuspendTransaction DataResult.Failure.NotFound

            DataResult.Success(movieId)
        }
    }

    override suspend fun deleteMovie(id: Long): DataResult<Int> {
        return withSuspendTransaction {
            val deletedRow = MoviesTable.deleteWhere {
                MoviesTable.id eq id
            }
            if (deletedRow == 0) {
                DataResult.Failure.UnknownError("Failed to delete movie with id $id")
            } else DataResult.Success(deletedRow)
        }
    }

    override suspend fun getMoviesByGenre(
        genreId: Long,
        limit: Int,
        offset: Long
    ): DataResult<List<Movie>> {
        return withSuspendTransaction {
            val genreEntity = GenreEntity.findById(genreId) ?: return@withSuspendTransaction DataResult.Failure.NotFound

            val movies = genreEntity.movies
                .limit(limit).offset(offset)
                .map {
                    Movie(it.id.value, it.title, posterUrl = it.posterUrl)
                }

            DataResult.Success(movies)
        }
    }

    private fun linkGenres(m: MoviesEntity, genres: List<Genre>): DataResult<Unit> {
        genres.forEach { g ->
            val genreEntity = GenreEntity.findById(g.id) ?: return DataResult.Failure.NotFound
            MovieGenreTable.insert {
                it[movieId] = m.id
                it[genreId] = genreEntity.id
            }
        }
        return DataResult.Success(Unit)
    }
}