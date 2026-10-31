package com.amperex.batteryintelligence.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.amperex.batteryintelligence.data.local.dao.BatteryDao
import com.amperex.batteryintelligence.data.local.entities.BatterySampleEntity
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.PlugType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BatteryRepository(
    private val context: Context,
    private val batteryDao: BatteryDao
) {
    val latestSampleFlow: Flow<BatterySample?> = batteryDao.getLatestSampleFlow()
        .map { it?.toDomain() }

    fun getSamplesSinceFlow(sinceTimestamp: Long): Flow<List<BatterySample>> {
        return batteryDao.getSamplesSinceFlow(sinceTimestamp)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun getSamplesSince(sinceTimestamp: Long): List<BatterySample> = withContext(Dispatchers.IO) {
        batteryDao.getSamplesSince(sinceTimestamp).map { it.toDomain() }
    }

    suspend fun getRecentSamples(limit: Int): List<BatterySample> = withContext(Dispatchers.IO) {
        batteryDao.getRecentSamples(limit).map { it.toDomain() }
    }

    /**
     * Reads current hardware battery state using Android BatteryManager and sticky intent.
     * Prevents duplicate entries if the readings haven't changed meaningfully.
     */
    suspend fun captureCurrentSample(forceRecord: Boolean = false): BatterySample = withContext(Dispatchers.IO) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val rawLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val rawScale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (rawLevel >= 0 && rawScale > 0) {
            (rawLevel * 100) / rawScale
        } else {
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }.coerceIn(0, 100)

        val statusInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val status = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> ChargingStatus.CHARGING
            BatteryManager.BATTERY_STATUS_DISCHARGING -> ChargingStatus.DISCHARGING
            BatteryManager.BATTERY_STATUS_FULL -> ChargingStatus.FULL
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> ChargingStatus.NOT_CHARGING
            else -> ChargingStatus.UNKNOWN
        }

        val pluggedInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val plugType = when (pluggedInt) {
            BatteryManager.BATTERY_PLUGGED_AC -> PlugType.AC
            BatteryManager.BATTERY_PLUGGED_USB -> PlugType.USB
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.WIRELESS
            4 -> PlugType.DOCK // BATTERY_PLUGGED_DOCK
            else -> if (pluggedInt > 0) PlugType.UNKNOWN else PlugType.NONE
        }

        // Temperature: Tenths of a degree Celsius (e.g. 290 = 29.0°C)
        val rawTemp = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        val temperatureCelsius = if (rawTemp > -100 && rawTemp < 1000) rawTemp / 10f else null

        // Voltage: millivolts
        val rawVolt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val voltageMv = if (rawVolt in 1500..5500) rawVolt else null

        // Current: microamperes (convert to mA)
        val currentUa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentMa = if (currentUa != Int.MIN_VALUE && currentUa != 0) currentUa / 1000 else null

        // Charge counter: uAh
        val chargeCounter = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val chargeCounterUah = if (chargeCounter > 0L) chargeCounter else null

        val sample = BatterySample(
            timestamp = System.currentTimeMillis(),
            percentage = percentage,
            status = status,
            plugType = plugType,
            temperatureCelsius = temperatureCelsius,
            voltageMv = voltageMv,
            currentMa = currentMa,
            chargeCounterUah = chargeCounterUah
        )

        // Deduplication check: compare with latest in DB
        val latest = batteryDao.getLatestSample()
        val isDuplicate = latest != null &&
                latest.percentage == sample.percentage &&
                latest.status == sample.status &&
                latest.plugType == sample.plugType &&
                (sample.timestamp - latest.timestamp < 60_000L) // within 1 minute

        if (forceRecord || !isDuplicate) {
            batteryDao.insertSample(BatterySampleEntity.fromDomain(sample))
        }

        sample
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        batteryDao.clearAll()
    }
}
