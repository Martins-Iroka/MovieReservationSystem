package com.martdev.features.reservation.infrastructure.db.table

import com.martdev.features.auth.infrastructure.db.tables.UserTable
import com.martdev.features.reservation.domain.model.ReservationStatus
import com.martdev.features.showtime.infrastructure.db.table.ShowtimeTable
import com.martdev.shared.infrastruce.db.setEnumeration
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.dao.LongEntity
import org.jetbrains.exposed.v1.dao.LongEntityClass
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object ReservationTable : LongIdTable("reservations") {
    val userId = reference("user_id", UserTable)
    val showtimeId = reference("showtime_id", ShowtimeTable)
    val status = setEnumeration<ReservationStatus>("status", "status_type")
    val totalAmount = long("total_amount")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at")
}

class ReservationEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ReservationEntity>(ReservationTable)

    var userId by ReservationTable.userId
    var showtimeId by ReservationTable.showtimeId
    var status by ReservationTable.status
    var totalAmount by ReservationTable.totalAmount
    var createdAt by ReservationTable.createdAt
    var expiresAt by ReservationTable.expiresAt
}
