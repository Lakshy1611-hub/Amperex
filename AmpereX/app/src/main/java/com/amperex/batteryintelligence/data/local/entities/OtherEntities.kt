package com.amperex.batteryintelligence.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amperex.batteryintelligence.domain.model.DailyBatterySummary

@Entity(tableName = "daily_summaries")
data class DailyBatterySummaryEntity(
    @PrimaryKey
    val dateEpochDay: Long,
    val startingPercentage: Int?,
    val endingPercentage: Int?,
    val grossDischarge: Int,
    val grossChargingGain: Int,
    val netChange: Int,
    val trackedScreenOnMinutes: Long?,
    val chargingSessionCount: Int,
    val averageDischargeRatePerHour: Float?,
    val peakTemperatureCelsius: Float?,
    val isPartialData: Boolean,
    val totalSamples: Int
) {
    fun toDomain(): DailyBatterySummary = DailyBatterySummary(
        dateEpochDay = dateEpochDay,
        startingPercentage = startingPercentage,
        endingPercentage = endingPercentage,
        grossDischarge = grossDischarge,
        grossChargingGain = grossChargingGain,
        netChange = netChange,
        trackedScreenOnMinutes = trackedScreenOnMinutes,
        chargingSessionCount = chargingSessionCount,
        averageDischargeRatePerHour = averageDischargeRatePerHour,
        peakTemperatureCelsius = peakTemperatureCelsius,
        isPartialData = isPartialData,
        totalSamples = totalSamples
    )

    companion object {
        fun fromDomain(d: DailyBatterySummary): DailyBatterySummaryEntity = DailyBatterySummaryEntity(
            dateEpochDay = d.dateEpochDay,
            startingPercentage = d.startingPercentage,
            endingPercentage = d.endingPercentage,
            grossDischarge = d.grossDischarge,
            grossChargingGain = d.grossChargingGain,
            netChange = d.netChange,
            trackedScreenOnMinutes = d.trackedScreenOnMinutes,
            chargingSessionCount = d.chargingSessionCount,
            averageDischargeRatePerHour = d.averageDischargeRatePerHour,
            peakTemperatureCelsius = d.peakTemperatureCelsius,
            isPartialData = d.isPartialData,
            totalSamples = d.totalSamples
        )
    }
}

@Entity(tableName = "health_observations")
data class BatteryHealthObservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val androidStatus: String,
    val measuredCapacityMah: Int?,
    val designCapacityMah: Int?,
    val calculatedHealthPct: Float?,
    val cycleCount: Int?
)

@Entity(tableName = "app_usage_snapshots")
data class AppUsageSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val lastTimeUsedMs: Long
)
