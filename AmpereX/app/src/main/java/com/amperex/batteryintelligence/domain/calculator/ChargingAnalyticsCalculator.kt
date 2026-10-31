package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus

data class ChargingPrediction(
    val estimatedMinutesTo80: Long?,
    val estimatedMinutesToFull: Long?,
    val observedRatePerHour: Float, // percentage per hour
    val isEstimated: Boolean,
    val explanation: String
)

object ChargingAnalyticsCalculator {

    /**
     * Calculates charging speed and estimates time to 80% and 100% full.
     * Uses real charging rate from the active session.
     * Applies standard Li-ion taper model above 80% (saturation phase charging slows down).
     */
    fun calculateChargingPrediction(
        currentPercentage: Int,
        isCharging: Boolean,
        sessionSamples: List<BatterySample>
    ): ChargingPrediction {
        if (!isCharging || currentPercentage >= 100) {
            return ChargingPrediction(
                estimatedMinutesTo80 = if (currentPercentage >= 80) 0L else null,
                estimatedMinutesToFull = if (currentPercentage >= 100) 0L else null,
                observedRatePerHour = 0f,
                isEstimated = false,
                explanation = if (currentPercentage >= 100) "Battery is fully charged." else "Device is not currently connected to a charger."
            )
        }

        val chargingSamples = sessionSamples
            .filter { it.status == ChargingStatus.CHARGING }
            .sortedBy { it.timestamp }

        if (chargingSamples.size < 2) {
            return ChargingPrediction(
                estimatedMinutesTo80 = null,
                estimatedMinutesToFull = null,
                observedRatePerHour = 0f,
                isEstimated = true,
                explanation = "Measuring charging current and initial progress... Please wait 2-3 minutes for rate stabilization."
            )
        }

        val first = chargingSamples.first()
        val last = chargingSamples.last()
        val deltaHours = (last.timestamp - first.timestamp) / 3_600_000f
        val deltaPct = last.percentage - first.percentage

        // Rate = delta % / delta hours
        val observedRate = if (deltaHours > 0.01f && deltaPct > 0) {
            (deltaPct / deltaHours).coerceIn(2f, 150f)
        } else {
            // Fallback to recent instantaneous current if available
            val currentMa = last.currentMa
            if (currentMa != null && currentMa > 100) {
                // Estimate roughly from current assuming a typical 4500mAh battery: 1000mA is ~22%/hr
                (currentMa / 4500f * 100f).coerceIn(2f, 120f)
            } else {
                0f
            }
        }

        if (observedRate < 1f) {
            return ChargingPrediction(
                estimatedMinutesTo80 = null,
                estimatedMinutesToFull = null,
                observedRatePerHour = 0f,
                isEstimated = true,
                explanation = "Charging speed is slow or still stabilizing. Insufficient gain observed to calculate ETA."
            )
        }

        // Time to 80%
        val minTo80: Long? = if (currentPercentage < 80) {
            val remainingTo80 = 80 - currentPercentage
            val hours = remainingTo80 / observedRate
            (hours * 60f).toLong().coerceAtLeast(1L)
        } else {
            0L
        }

        // Time to full: Li-ion batteries slow down above 80% due to constant-voltage (CV) phase.
        // Taper factor: 1.4x slower on average between 80% and 100%
        val minToFull: Long = if (currentPercentage < 80) {
            val hoursUnder80 = (80 - currentPercentage) / observedRate
            val hoursOver80 = (20f / (observedRate * 0.70f))
            ((hoursUnder80 + hoursOver80) * 60f).toLong().coerceAtLeast(1L)
        } else {
            val remainingPct = 100 - currentPercentage
            val taperedRate = observedRate * 0.70f
            val hours = remainingPct / taperedRate
            (hours * 60f).toLong().coerceAtLeast(1L)
        }

        val explanation = "Estimated based on observed charging rate of ${String.format("%.1f", observedRate)}%/hr across ${chargingSamples.size} points. Accounts for standard Li-ion constant-voltage taper above 80%."

        return ChargingPrediction(
            estimatedMinutesTo80 = minTo80,
            estimatedMinutesToFull = minToFull,
            observedRatePerHour = observedRate,
            isEstimated = true,
            explanation = explanation
        )
    }
}
