package com.martdev.features.room.domain.model

data class Room(
    val id: Long = 0,
    val name: String = "",
    val rows: Int = 0,
    val columns: Int = 0
)
