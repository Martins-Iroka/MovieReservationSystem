package com.martdev.features.report.domain.service

import com.martdev.features.report.domain.model.CapacityReport
import com.martdev.features.report.domain.model.ReportBucketGranularity
import com.martdev.features.report.domain.model.RevenueReport
import kotlin.time.Instant

interface ReportService {
    suspend fun getRevenueReport(
        from: Instant,
        to: Instant,
        bucket: ReportBucketGranularity,
    ): RevenueReport

    suspend fun getCapacityReport(
        from: Instant,
        to: Instant,
        limit: Int,
        offset: Long,
        movieId: Long?,
        roomId: Long?,
    ): CapacityReport
}
