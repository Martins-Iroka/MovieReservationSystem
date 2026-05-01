package com.martdev.infrastructure.db.tables.user

import com.martdev.domain.model.Role
import com.martdev.domain.model.User
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object UserTable : LongIdTable("users") {
    val email = citext("email").uniqueIndex()
    val password = text("password")
    val isVerified = bool("is_verified").default(false)
    val role = enumeration<Role>("role").default(Role.USER)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class UserEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserEntity>(UserTable)

    var email by UserTable.email
    var password by UserTable.password
    var isVerified by UserTable.isVerified
    var role by UserTable.role
}

fun UserEntity.toUserModel() = User(
    id.value,
    email,
    password,
    isVerified,
    role
)