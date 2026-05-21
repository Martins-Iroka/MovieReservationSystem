package com.martdev.features.report.api

import com.martdev.features.report.domain.model.CapacityReport
import com.martdev.features.report.domain.model.CapacityRow
import com.martdev.features.report.domain.model.RevenueBucket
import com.martdev.features.report.domain.model.RevenueReport

fun RevenueBucket.toDTO() = RevenueBucketDTO(
    bucketStart = bucketStart,
    gross = gross,
    refunds = refunds,
    net = net,
    ticketsSold = ticketsSold,
)

fun RevenueReport.toDTO() = RevenueReportDTO(
    from = from,
    to = to,
    bucket = bucket.name,
    currency = currency,
    buckets = buckets.map { it.toDTO() },
    totalGross = totalGross,
    totalRefunds = totalRefunds,
    totalNet = totalNet,
    totalTicketsSold = totalTicketsSold,
)

fun CapacityRow.toDTO() = CapacityRowDTO(
    showtimeId = showtimeId,
    movieId = movieId,
    movieTitle = movieTitle,
    roomId = roomId,
    roomName = roomName,
    startsAt = startsAt,
    endsAt = endsAt,
    seatsTotal = seatsTotal,
    seatsBooked = seatsBooked,
    seatsHeld = seatsHeld,
    seatsAvailable = seatsAvailable,
    occupancyRate = occupancyRate,
)

fun CapacityReport.toDTO() = CapacityReportDTO(
    from = from,
    to = to,
    rows = rows.map { it.toDTO() },
    totalShowtimes = totalShowtimes,
    avgOccupancyRate = avgOccupancyRate,
    totalSeatsBooked = totalSeatsBooked,
    totalSeatsTotal = totalSeatsTotal,
)
