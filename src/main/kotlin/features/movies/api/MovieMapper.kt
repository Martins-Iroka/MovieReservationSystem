package com.martdev.features.movies.api

import com.martdev.features.movies.domain.model.Genre
import com.martdev.features.movies.domain.model.Movie

fun Movie.toMovieItemDto() = MovieListItemDTO(
    id, title, posterUrl
)

fun Movie.toMovieDto() = MovieDTO(
    id, title, description, posterUrl, duration, releasedDate.toString(), genres.map { it.toGenreDto() }
)

fun Genre.toGenreDto() = GenreDTO(
    id, name
)