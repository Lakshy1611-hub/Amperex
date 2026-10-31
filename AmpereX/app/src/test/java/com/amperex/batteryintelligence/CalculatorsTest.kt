package com.amperex.batteryintelligence

import com.amperex.batteryintelligence.domain.calculator.BackupPredictionCalculator
import com.amperex.batteryintelligence.domain.calculator.BatteryHealthCalculator
import com.amperex.batteryintelligence.domain.calculator.ChargingAnalyticsCalculator
import com.amperex.batteryintelligence.domain.calculator.DailyReportCalculator
import com.amperex.batteryintelligence.domain.calculator.DischargeRateCalculator
import com.amperex.batteryintelligence.domain.model.BatteryHealthState
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.EstimationConfidence
import com.amperex.batteryintelligence.domain.model.PlugType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorsTest {

    @Test
    fun testGrossDischargeAndChargingGainSeparation() {
        // Starts at 80% -> drops to 40% (drop = 40)
        // Charges to 70% (gain = 30)
        // Drops to 50% (drop = 20)
        // Gross discharge must be 40 + 20 = 60%
        // Charging gain must be 30%
        // Net change must be 50 - 80 = -30%
        val baseTime = 1700000000000L
        val samples = listOf(
            BatterySample(1, baseTime + 0, 80, ChargingStatus.DISCHARGING, PlugType.NONE, 28f, 4100, -350),
            BatterySample(2, baseTime + 3600_000L * 4, 40, ChargingStatus.DISCHARGING, PlugType.NONE, 29f, 3750, -350),
            BatterySample(3, baseTime + 3600_000L * 6, 70, ChargingStatus.CHARGING, PlugType.AC, 32f, 4250, 1800),
            BatterySample(4, baseTime + 3600_000L * 8, 50, ChargingStatus.DISCHARGING, PlugType.NONE, 27f, 3850, -320)
        )

        val summary = DailyReportCalculator.computeDailySummary(
            dateEpochDay = 19680L,
            daySamples = samples
        )

        assertEquals("Gross discharge must be 60%", 60, summary.grossDischarge)
        assertEquals("Gross charging gain must be 30%", 30, summary.grossChargingGain)
        assertEquals("Net change must be -30%", -30, summary.netChange)
        assertEquals("Starting % should be 80%", 80, summary.startingPercentage)
        assertEquals("Ending % should be 50%", 50, summary.endingPercentage)
        assertEquals("Should identify 1 charging session", 1, summary.chargingSessionCount)
    }

    @Test
    fun testBatteryHealthCalculationsHonesty() {
        // Valid hardware values: 4500mAh design, 4050mAh measured => 90.0%
        val validReport = BatteryHealthCalculator.evaluateHealth(
            androidReportedHealth = BatteryHealthState.GOOD,
            designCapacityMah = 4500,
            measuredFullCapacityMah = 4050,
            cycleCount = 180,
            historicalObservationsCount = 25
        )
        assertTrue(validReport.isReliableCapacityAvailable)
        assertEquals(90.0f, validReport.capacityHealthPercentage ?: 0f, 0.1f)
        assertEquals(180, validReport.cycleCount)

        // Unavailable hardware capacity values (null)
        val unavailableReport = BatteryHealthCalculator.evaluateHealth(
            androidReportedHealth = BatteryHealthState.GOOD,
            designCapacityMah = null,
            measuredFullCapacityMah = null,
            cycleCount = null,
            historicalObservationsCount = 10
        )
        assertFalse(unavailableReport.isReliableCapacityAvailable)
        assertNull(validReport.capacityHealthPercentage == null, unavailableReport.capacityHealthPercentage)
        assertTrue(unavailableReport.explanation.contains("Battery capacity health cannot be measured reliably"))

        // Glitched values: 0 or out-of-bounds (e.g. 50000mAh)
        val glitchedReport = BatteryHealthCalculator.evaluateHealth(
            androidReportedHealth = BatteryHealthState.GOOD,
            designCapacityMah = 0,
            measuredFullCapacityMah = 50000,
            cycleCount = 0,
            historicalObservationsCount = 5
        )
        assertFalse(glitchedReport.isReliableCapacityAvailable)
        assertNull(glitchedReport.capacityHealthPercentage)
    }

    @Test
    fun testBackupPredictionDischargeRate() {
        val baseTime = 1700000000000L
        // 60% discharging at 10% per hour -> 6 hours remaining (360 minutes)
        val samples = listOf(
            BatterySample(1, baseTime + 0, 70, ChargingStatus.DISCHARGING, PlugType.NONE, 26f, 4000, -300),
            BatterySample(2, baseTime + 3600_000L, 60, ChargingStatus.DISCHARGING, PlugType.NONE, 26f, 3900, -300),
            BatterySample(3, baseTime + 3600_000L * 2, 50, ChargingStatus.DISCHARGING, PlugType.NONE, 26f, 3800, -300)
        )

        val pred = BackupPredictionCalculator.predictRemainingRuntime(
            currentPercentage = 60,
            isCharging = false,
            recentSamples = samples
        )

        assertNotNull(pred.estimatedMinutesRemaining)
        // Rate is ~10%/hr, 60% remaining / 10%/hr = 6.0 hours = 360 minutes
        assertTrue("Estimated minutes should be close to 360", pred.estimatedMinutesRemaining in 340L..380L)

        // When charging, remaining runtime estimate should pause
        val chargingPred = BackupPredictionCalculator.predictRemainingRuntime(
            currentPercentage = 60,
            isCharging = true,
            recentSamples = samples
        )
        assertNull(chargingPred.estimatedMinutesRemaining)
        assertEquals(EstimationConfidence.INSUFFICIENT_DATA, chargingPred.confidence)
    }

    @Test
    fun testChargingEtaWithConstantVoltageTaper() {
        val baseTime = 1700000000000L
        val samples = listOf(
            BatterySample(1, baseTime + 0, 30, ChargingStatus.CHARGING, PlugType.AC, 30f, 4100, 2500),
            BatterySample(2, baseTime + 1800_000L, 50, ChargingStatus.CHARGING, PlugType.AC, 32f, 4200, 2500)
        )

        val pred = ChargingAnalyticsCalculator.calculateChargingPrediction(
            currentPercentage = 50,
            isCharging = true,
            sessionSamples = samples
        )

        assertTrue(pred.isEstimated)
        assertNotNull(pred.estimatedMinutesTo80)
        assertNotNull(pred.estimatedMinutesToFull)
        // Rate is 20% in 0.5hr = 40%/hr
        // To 80%: 30% needed / 40%/hr = 0.75h = 45 min
        assertEquals(45L, pred.estimatedMinutesTo80 ?: 0L)
        // To 100%: 45 min + (20% at tapered 28%/hr = 0.71h = ~43 min) => ~88 min
        assertTrue("Time to full should include taper factor", (pred.estimatedMinutesToFull ?: 0L) > (pred.estimatedMinutesTo80 ?: 0L))
    }

    @Test
    fun testEmptyDataAndPartialFlagging() {
        val emptySummary = DailyReportCalculator.computeDailySummary(
            dateEpochDay = 19680L,
            daySamples = emptyList()
        )
        assertEquals(0, emptySummary.grossDischarge)
        assertEquals(0, emptySummary.grossChargingGain)
        assertEquals(0, emptySummary.netChange)
        assertTrue(emptySummary.isPartialData)
    }
}
