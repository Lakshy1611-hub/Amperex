package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.DailyBatterySummary
import kotlin.math.max

object DailyReportCalculator {

    /**
     * Accurately calculates daily battery metrics by accumulating step drops and gains.
     * Never calculates gross discharge solely by (ending - starting).
     *
     * Example:
     * Starts at 80% -> drops to 40% (drop = 40)
     * Charges to 70% (gain = 30)
     * Drops to 50% (drop = 20)
     * Gross discharge = 40 + 20 = 60%
     * Gross charging gain = 30%
     * Net change = 50% - 80% = -30%
     */
    fun computeDailySummary(
        dateEpochDay: Long,
        daySamples: List<BatterySample>,
        trackedScreenOnMinutes: Long? = null
    ): DailyBatterySummary {
        if (daySamples.isEmpty()) {
            return DailyBatterySummary(
                dateEpochDay = dateEpochDay,
                startingPercentage = null,
                endingPercentage = null,
                grossDischarge = 0,
                grossChargingGain = 0,
                netChange = 0,
                trackedScreenOnMinutes = trackedScreenOnMinutes,
                chargingSessionCount = 0,
                averageDischargeRatePerHour = null,
                peakTemperatureCelsius = null,
                isPartialData = true,
                totalSamples = 0
            )
        }

        val sorted = daySamples.sortedBy { it.timestamp }
        val startingPct = sorted.first().percentage
        val endingPct = sorted.last().percentage

        var grossDischarge = 0
        var grossChargingGain = 0
        var chargingSessions = 0
        var inChargingSession = false
        var peakTemp: Float? = null

        val dischargeDropsWithTime = mutableListOf<Pair<Float, Float>>() // drop, hours

        for (i in 0 until sorted.size) {
            val curr = sorted[i]

            // Check peak temperature
            if (curr.temperatureCelsius != null) {
                peakTemp = if (peakTemp == null) curr.temperatureCelsius else max(peakTemp, curr.temperatureCelsius)
            }

            // Detect charging session boundary transitions
            val isChargingNow = curr.status == ChargingStatus.CHARGING || curr.status == ChargingStatus.FULL
            if (isChargingNow && !inChargingSession) {
                chargingSessions++
                inChargingSession = true
            } else if (!isChargingNow && inChargingSession) {
                inChargingSession = false
            }

            // Step calculations compared to previous sample
            if (i > 0) {
                val prev = sorted[i - 1]
                val delta = curr.percentage - prev.percentage
                val deltaHours = (curr.timestamp - prev.timestamp) / 3_600_000f

                if (delta < 0) {
                    val drop = -delta
                    grossDischarge += drop
                    if (deltaHours in 0.003f..12.0f) {
                        dischargeDropsWithTime.add(drop.toFloat() to deltaHours)
                    }
                } else if (delta > 0) {
                    grossChargingGain += delta
                }
            }
        }

        // Net change: ending - starting
        val netChange = endingPct - startingPct

        // Average discharge rate
        val avgDischargeRate: Float? = if (dischargeDropsWithTime.isNotEmpty()) {
            val totalDrop = dischargeDropsWithTime.sumOf { it.first.toDouble() }.toFloat()
            val totalDischargeHours = dischargeDropsWithTime.sumOf { it.second.toDouble() }.toFloat()
            if (totalDischargeHours > 0.05f) totalDrop / totalDischargeHours else null
        } else null

        // Coverage assessment: 24h day has 86,400,000 ms. If coverage < 18 hours or < 12 samples, marked as partial
        val firstTs = sorted.first().timestamp
        val lastTs = sorted.last().timestamp
        val coverageHours = (lastTs - firstTs) / 3_600_000f
        val isPartial = coverageHours < 18.0f || sorted.size < 12

        return DailyBatterySummary(
            dateEpochDay = dateEpochDay,
            startingPercentage = startingPct,
            endingPercentage = endingPct,
            grossDischarge = grossDischarge,
            grossChargingGain = grossChargingGain,
            netChange = netChange,
            trackedScreenOnMinutes = trackedScreenOnMinutes,
            chargingSessionCount = chargingSessions,
            averageDischargeRatePerHour = avgDischargeRate,
            peakTemperatureCelsius = peakTemp,
            isPartialData = isPartial,
            totalSamples = sorted.size
        )
    }
}
