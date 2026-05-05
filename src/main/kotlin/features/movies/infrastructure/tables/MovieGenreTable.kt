package com.martdev.features.movies.infrastructure.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.CompositeID
import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.CompositeEntity
import org.jetbrains.exposed.v1.dao.CompositeEntityClass

object MovieGenreTable : CompositeIdTable("movie_genre") {
    val movieId = reference(
        "movie_id",
        MoviesTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).entityId()
    val genreId = reference(
        "genre_id",
        GenresTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    ).entityId()

    override val primaryKey: PrimaryKey = PrimaryKey(
        movieId, genreId
    )
}

class MovieGenreEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
    companion object : CompositeEntityClass<MovieGenreEntity>(MovieGenreTable)

    var movie by MoviesEntity referencedOn MovieGenreTable.movieId
    var genre by GenreEntity referencedOn MovieGenreTable.genreId
}