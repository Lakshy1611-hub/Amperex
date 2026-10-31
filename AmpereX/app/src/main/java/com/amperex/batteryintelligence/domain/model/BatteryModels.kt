package com.amperex.batteryintelligence.domain.model

import kotlinx.serialization.Serializable

enum class ChargingStatus {
    CHARGING,
    DISCHARGING,
    FULL,
    NOT_CHARGING,
    UNKNOWN
}

enum class PlugType {
    NONE,
    AC,
    USB,
    WIRELESS,
    DOCK,
    UNKNOWN
}

enum class BatteryHealthState {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}

enum class EstimationConfidence {
    HIGH,
    MEDIUM,
    LOW,
    INSUFFICIENT_DATA
}

@Serializable
data class BatterySample(
    val id: Long = 0,
    val timestamp: Long,
    val percentage: Int, // 0 - 100
    val status: ChargingStatus,
    val plugType: PlugType,
    val temperatureCelsius: Float?, // null if unavailable
    val voltageMv: Int?, // millivolts
    val currentMa: Int?, // negative for discharge, positive for charge, or null if unsupported
    val chargeCounterUah: Long? = null // microampere-hours if supported
)

@Serializable
data class ChargingSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val startPercentage: Int,
    val currentPercentage: Int,
    val endPercentage: Int? = null,
    val plugType: PlugType,
    val peakCurrentMa: Int? = null,
    val averageTemperatureCelsius: Float? = null,
    val isCompleted: Boolean = false
) {
    val durationMs: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime

    val percentageGain: Int
        get() = (endPercentage ?: currentPercentage) - startPercentage

    val chargeRatePerHour: Float
        get() {
            val hours = durationMs / 3_600_000f
            return if (hours > 0.01f && percentageGain > 0) percentageGain / hours else 0f
        }
}

@Serializable
data class DailyBatterySummary(
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val startingPercentage: Int?,
    val endingPercentage: Int?,
    val grossDischarge: Int, // Sum of all observed percentage drops
    val grossChargingGain: Int, // Sum of all observed percentage increases
    val netChange: Int, // ending - starting
    val trackedScreenOnMinutes: Long?,
    val chargingSessionCount: Int,
    val averageDischargeRatePerHour: Float?,
    val peakTemperatureCelsius: Float?,
    val isPartialData: Boolean, // True if recorded coverage < 18h
    val totalSamples: Int
)

@Serializable
data class BatteryHealthReport(
    val androidReportedHealth: BatteryHealthState,
    val designCapacityMah: Int?, // null if system does not expose
    val measuredFullCapacityMah: Int?, // null if system does not expose
    val capacityHealthPercentage: Float?, // measured / design * 100
    val cycleCount: Int?, // from BatteryManager.BATTERY_PROPERTY_CYCLE_COUNT or null
    val isReliableCapacityAvailable: Boolean,
    val historicalReadingsCount: Int,
    val explanation: String
)

@Serializable
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val foregroundTimeMs: Long,
    val lastTimeUsedMs: Long,
    val dailyForegroundMinutes: Long,
    val weeklyForegroundMinutes: Long
)

@Serializable
data class BackupPrediction(
    val estimatedMinutesRemaining: Long?,
    val confidence: EstimationConfidence,
    val dischargeRatePerHour: Float, // percentage points per hour
    val recentSampleCount: Int,
    val methodologyNote: String
)
