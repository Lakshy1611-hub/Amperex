package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import kotlin.math.abs

object DischargeRateCalculator {

    /**
     * Calculates the smoothed discharge rate in percentage points per hour
     * using consecutive discharging samples within the observation window.
     * Returns 0f if insufficient data or currently charging.
     */
    fun calculateDischargeRatePerHour(samples: List<BatterySample>): Float {
        val dischargingSamples = samples
            .filter { it.status == ChargingStatus.DISCHARGING }
            .sortedBy { it.timestamp }

        if (dischargingSamples.size < 2) return 0f

        val rates = mutableListOf<Float>()
        for (i in 1 until dischargingSamples.size) {
            val prev = dischargingSamples[i - 1]
            val curr = dischargingSamples[i]

            val deltaHours = (curr.timestamp - prev.timestamp) / 3_600_000f
            val drop = prev.percentage - curr.percentage

            // Only consider meaningful positive drops and reasonable time intervals (10s to 12h)
            if (deltaHours in 0.003f..12.0f && drop >= 0) {
                val rate = drop / deltaHours
                // Filter unreasonable hardware glitches (>60% per hour)
                if (rate in 0.1f..60f) {
                    rates.add(rate)
                }
            }
        }

        if (rates.isEmpty()) return 0f

        // Exponential smoothing / weighted towards recent samples
        var weightedSum = 0f
        var weightTotal = 0f
        rates.forEachIndexed { index, rate ->
            val weight = 1.0f + (index.toFloat() / rates.size.toFloat())
            weightedSum += rate * weight
            weightTotal += weight
        }

        return if (weightTotal > 0f) weightedSum / weightTotal else 0f
    }
}
