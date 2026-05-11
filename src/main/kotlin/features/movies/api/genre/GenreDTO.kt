package com.martdev.features.movies.api.genre

import kotlinx.serialization.Serializable

@Serializable
data class GenreDTO(
    val id: Long = 0L,
    val name: String
)
