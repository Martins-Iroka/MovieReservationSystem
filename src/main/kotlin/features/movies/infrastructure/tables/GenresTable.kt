package com.martdev.features.movies.infrastructure.tables

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object GenresTable : IntIdTable("genres") {
    val name = varchar("name", 50)
}

class GenreEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GenreEntity>(GenresTable)

    var name by GenresTable.name
}