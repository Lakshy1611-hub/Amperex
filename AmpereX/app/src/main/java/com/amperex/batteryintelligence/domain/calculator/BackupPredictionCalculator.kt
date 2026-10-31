package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BackupPrediction
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.EstimationConfidence

object BackupPredictionCalculator {

    /**
     * Estimates remaining runtime in minutes based on real recent battery discharge rate.
     */
    fun predictRemainingRuntime(
        currentPercentage: Int,
        isCharging: Boolean,
        recentSamples: List<BatterySample>
    ): BackupPrediction {
        if (isCharging) {
            return BackupPrediction(
                estimatedMinutesRemaining = null,
                confidence = EstimationConfidence.INSUFFICIENT_DATA,
                dischargeRatePerHour = 0f,
                recentSampleCount = recentSamples.size,
                methodologyNote = "Device is currently charging. Runtime on battery prediction pauses during power input."
            )
        }

        if (currentPercentage <= 0) {
            return BackupPrediction(
                estimatedMinutesRemaining = 0L,
                confidence = EstimationConfidence.HIGH,
                dischargeRatePerHour = 0f,
                recentSampleCount = recentSamples.size,
                methodologyNote = "Battery level is depleted."
            )
        }

        val dischargingSamples = recentSamples
            .filter { it.status == ChargingStatus.DISCHARGING }
            .sortedBy { it.timestamp }

        // Require at least 2 distinct observations covering at least 3 minutes
        if (dischargingSamples.size < 2) {
            return BackupPrediction(
                estimatedMinutesRemaining = null,
                confidence = EstimationConfidence.INSUFFICIENT_DATA,
                dischargeRatePerHour = 0f,
                recentSampleCount = dischargingSamples.size,
                methodologyNote = "Collecting data... At least two recorded battery drop events are required for honest estimation."
            )
        }

        val firstSample = dischargingSamples.first()
        val lastSample = dischargingSamples.last()
        val durationSpanHours = (lastSample.timestamp - firstSample.timestamp) / 3_600_000f

        val smoothedRate = DischargeRateCalculator.calculateDischargeRatePerHour(dischargingSamples)

        if (smoothedRate <= 0.1f || durationSpanHours < 0.05f) {
            return BackupPrediction(
                estimatedMinutesRemaining = null,
                confidence = EstimationConfidence.INSUFFICIENT_DATA,
                dischargeRatePerHour = smoothedRate,
                recentSampleCount = dischargingSamples.size,
                methodologyNote = "Collecting data... Observed discharge duration (${Math.round(durationSpanHours * 60)} min) is currently too short for stable prediction."
            )
        }

        // Hours remaining = currentPercentage / ratePerHour
        val hoursRemaining = currentPercentage / smoothedRate
        val minutesRemaining = (hoursRemaining * 60f).toLong().coerceIn(1L, 7200L) // max 5 days

        val confidence = when {
            dischargingSamples.size >= 10 && durationSpanHours >= 2.0f -> EstimationConfidence.HIGH
            dischargingSamples.size >= 4 && durationSpanHours >= 0.5f -> EstimationConfidence.MEDIUM
            else -> EstimationConfidence.LOW
        }

        val note = "Estimated based on observed discharge of ${String.format("%.1f", smoothedRate)}%/hr across ${dischargingSamples.size} observations ($confidence confidence). Actual runtime varies with screen brightness, network signals, and active workloads."

        return BackupPrediction(
            estimatedMinutesRemaining = minutesRemaining,
            confidence = confidence,
            dischargeRatePerHour = smoothedRate,
            recentSampleCount = dischargingSamples.size,
            methodologyNote = note
        )
    }
}
