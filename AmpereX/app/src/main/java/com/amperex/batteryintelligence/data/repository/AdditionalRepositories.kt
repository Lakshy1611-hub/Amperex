package com.amperex.batteryintelligence.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Process
import com.amperex.batteryintelligence.data.local.dao.BatteryHealthDao
import com.amperex.batteryintelligence.data.local.dao.ChargingSessionDao
import com.amperex.batteryintelligence.data.local.dao.DailySummaryDao
import com.amperex.batteryintelligence.data.local.entities.ChargingSessionEntity
import com.amperex.batteryintelligence.domain.calculator.BatteryHealthCalculator
import com.amperex.batteryintelligence.domain.model.AppUsageInfo
import com.amperex.batteryintelligence.domain.model.BatteryHealthReport
import com.amperex.batteryintelligence.domain.model.BatteryHealthState
import com.amperex.batteryintelligence.domain.model.ChargingSession
import com.amperex.batteryintelligence.domain.model.PlugType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChargingRepository(
    private val chargingSessionDao: ChargingSessionDao
) {
    fun getRecentSessions(limit: Int): Flow<List<ChargingSession>> {
        return chargingSessionDao.getRecentSessionsFlow(limit)
            .map { list -> list.map { it.toDomain() } }
    }

    suspend fun recordSessionStart(
        startPercentage: Int,
        plugType: PlugType
    ): Long = withContext(Dispatchers.IO) {
        val session = ChargingSessionEntity(
            startTime = System.currentTimeMillis(),
            endTime = null,
            startPercentage = startPercentage,
            currentPercentage = startPercentage,
            endPercentage = null,
            plugType = plugType,
            peakCurrentMa = null,
            averageTemperatureCelsius = null,
            isCompleted = false
        )
        chargingSessionDao.insertSession(session)
    }

    suspend fun updateActiveSession(currentPercentage: Int, currentMa: Int?, temp: Float?) = withContext(Dispatchers.IO) {
        val active = chargingSessionDao.getActiveSession() ?: return@withContext
        val peak = if (currentMa != null && (active.peakCurrentMa == null || currentMa > active.peakCurrentMa)) {
            currentMa
        } else active.peakCurrentMa

        chargingSessionDao.updateSession(
            active.copy(
                currentPercentage = currentPercentage,
                peakCurrentMa = peak,
                averageTemperatureCelsius = temp ?: active.averageTemperatureCelsius
            )
        )
    }

    suspend fun completeActiveSession(endPercentage: Int) = withContext(Dispatchers.IO) {
        val active = chargingSessionDao.getActiveSession() ?: return@withContext
        chargingSessionDao.updateSession(
            active.copy(
                endTime = System.currentTimeMillis(),
                endPercentage = endPercentage,
                currentPercentage = endPercentage,
                isCompleted = true
            )
        )
    }

    suspend fun getActiveSession(): ChargingSession? = withContext(Dispatchers.IO) {
        chargingSessionDao.getActiveSession()?.toDomain()
    }
}

class HealthRepository(
    private val context: Context,
    private val healthDao: BatteryHealthDao
) {
    suspend fun getHealthReport(): BatteryHealthReport = withContext(Dispatchers.IO) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, ifilter)

        val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val androidHealth = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealthState.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealthState.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealthState.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealthState.OVER_VOLTAGE
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> BatteryHealthState.UNSPECIFIED_FAILURE
            BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealthState.COLD
            else -> BatteryHealthState.UNKNOWN
        }

        // Cycle count: Available on Android 14+ (API 34) on supported devices
        val cycleCount: Int? = try {
            val cycles = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CYCLE_COUNT)
            if (cycles > 0) cycles else null
        } catch (e: Exception) {
            null
        }

        // Standard Android design capacity: Generally inaccessible via public API, some vendors expose via sysfs
        // If not accessible, do not invent.
        val designCapacityMah: Int? = null
        val measuredFullCapacityMah: Int? = null
        val count = healthDao.getObservationsCount()

        BatteryHealthCalculator.evaluateHealth(
            androidReportedHealth = androidHealth,
            designCapacityMah = designCapacityMah,
            measuredFullCapacityMah = measuredFullCapacityMah,
            cycleCount = cycleCount,
            historicalObservationsCount = count
        )
    }
}

class UsageStatsRepository(private val context: Context) {

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    suspend fun getAppUsageList(days: Int = 1): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) return@withContext emptyList()

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return@withContext emptyList()

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (days * 24L * 3600_000L)
        val pm = context.packageManager

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        ) ?: return@withContext emptyList()

        stats
            .filter { it.totalTimeInForeground > 60_000L } // More than 1 minute
            .sortedByDescending { it.totalTimeInForeground }
            .take(30)
            .map { stat ->
                val appName = try {
                    val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    stat.packageName.substringAfterLast('.')
                }

                AppUsageInfo(
                    packageName = stat.packageName,
                    appName = appName,
                    foregroundTimeMs = stat.totalTimeInForeground,
                    lastTimeUsedMs = stat.lastTimeUsed,
                    dailyForegroundMinutes = stat.totalTimeInForeground / 60_000L,
                    weeklyForegroundMinutes = (stat.totalTimeInForeground * 7L) / 60_000L
                )
            }
    }
}
