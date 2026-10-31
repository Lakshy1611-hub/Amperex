package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatteryHealthReport
import com.amperex.batteryintelligence.domain.model.BatteryHealthState

object BatteryHealthCalculator {

    /**
     * Determines battery capacity health accurately without inventing numbers.
     * Evaluates Android reported health, design capacity, and measured full capacity.
     */
    fun evaluateHealth(
        androidReportedHealth: BatteryHealthState,
        designCapacityMah: Int?,
        measuredFullCapacityMah: Int?,
        cycleCount: Int?,
        historicalObservationsCount: Int
    ): BatteryHealthReport {
        // Validation check for genuine capacity values
        val isValidDesign = designCapacityMah != null && designCapacityMah in 500..15000
        val isValidMeasured = measuredFullCapacityMah != null && measuredFullCapacityMah in 400..15000

        val isReliable = isValidDesign && isValidMeasured

        val capacityHealthPercentage: Float? = if (isReliable) {
            val ratio = (measuredFullCapacityMah!!.toFloat() / designCapacityMah!!.toFloat()) * 100f
            // Valid realistic battery health bounds (30% to 110%)
            if (ratio in 30f..115f) {
                // Round to 1 decimal place
                Math.round(ratio * 10f) / 10f
            } else {
                null
            }
        } else {
            null
        }

        val explanation = when {
            capacityHealthPercentage != null -> {
                "Measured via hardware full-charge capacity ($measuredFullCapacityMah mAh) relative to factory design capacity ($designCapacityMah mAh). Android reported status: ${androidReportedHealth.name}."
            }
            !isReliable -> {
                "Battery capacity health cannot be measured reliably on this device. Standard Android public APIs on this hardware do not expose verified factory design capacity or full-charge Coulomb counters. System reports status as ${androidReportedHealth.name}."
            }
            else -> {
                "Capacity readings from system counters were out of expected operational bounds and have been excluded to prevent inaccurate reporting."
            }
        }

        return BatteryHealthReport(
            androidReportedHealth = androidReportedHealth,
            designCapacityMah = if (isValidDesign) designCapacityMah else null,
            measuredFullCapacityMah = if (isValidMeasured) measuredFullCapacityMah else null,
            capacityHealthPercentage = capacityHealthPercentage,
            cycleCount = if (cycleCount != null && cycleCount in 0..10000) cycleCount else null,
            isReliableCapacityAvailable = capacityHealthPercentage != null,
            historicalReadingsCount = historicalObservationsCount,
            explanation = explanation
        )
    }
}
