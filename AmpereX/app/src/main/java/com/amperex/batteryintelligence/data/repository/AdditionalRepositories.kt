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

        // Android does not expose a portable public battery-cycle-count API.
        // Keep this unknown instead of reporting a fabricated value.
        val cycleCount: Int? = null

        // Design/full-charge capacity are not available through a universal public API.
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

data class DischargeSessionSnapshot(
    val startedAt: Long?,
    val startBatteryPercent: Int?,
    val currentBatteryPercent: Int,
    val isCharging: Boolean
)

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

    fun getDischargeSessionSnapshot(): DischargeSessionSnapshot {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val currentPercent = if (level >= 0 && scale > 0) (level * 100 / scale).coerceIn(0, 100) else 0
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        // FULL can also be reported while unplugged on some Android builds.
        // Treat it as charging only when a power source is actually connected.
        val isCharging = plugged != 0 && (
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            )

        val prefs = context.getSharedPreferences("battery_usage_session", Context.MODE_PRIVATE)
        var startedAt = prefs.getLong("started_at", 0L).takeIf { it > 0L }
        var startLevel = if (prefs.contains("start_level")) prefs.getInt("start_level", currentPercent) else null

        // If AmpereX is installed/opened during an existing discharge, begin a transparent
        // baseline now rather than pretending we know the earlier part of the session.
        if (!isCharging && startedAt == null) {
            startedAt = System.currentTimeMillis()
            startLevel = currentPercent
            prefs.edit().putLong("started_at", startedAt).putInt("start_level", currentPercent).apply()
        }
        if (isCharging) {
            startedAt = null
            startLevel = null
        }
        return DischargeSessionSnapshot(startedAt, startLevel, currentPercent, isCharging)
    }

    suspend fun getAppUsageList(sinceTimestamp: Long? = null, days: Int = 1): List<AppUsageInfo> =
        withContext(Dispatchers.IO) {
            if (!hasUsageStatsPermission()) return@withContext emptyList()

            val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return@withContext emptyList()
            val endTime = System.currentTimeMillis()
            val startTime = sinceTimestamp?.takeIf { it > 0L }
                ?: endTime - (days * 24L * 3600_000L)
            val foregroundType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED
            else android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND
            val backgroundType = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
                android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED
            else android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND

            val totals = linkedMapOf<String, Long>()
            val lastUsed = linkedMapOf<String, Long>()
            val events = manager.queryEvents(startTime, endTime)
            val event = android.app.usage.UsageEvents.Event()
            var foregroundPackage: String? = null
            var foregroundStartedAt = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                when (event.eventType) {
                    foregroundType -> {
                        if (foregroundPackage != null && foregroundStartedAt > 0L) {
                            val duration = (event.timeStamp - foregroundStartedAt).coerceAtLeast(0L)
                            totals[foregroundPackage!!] = (totals[foregroundPackage!!] ?: 0L) + duration
                        }
                        foregroundPackage = pkg
                        foregroundStartedAt = event.timeStamp
                        lastUsed[pkg] = event.timeStamp
                    }
                    backgroundType -> {
                        if (foregroundPackage == pkg && foregroundStartedAt > 0L) {
                            val duration = (event.timeStamp - foregroundStartedAt).coerceAtLeast(0L)
                            totals[pkg] = (totals[pkg] ?: 0L) + duration
                            foregroundPackage = null
                            foregroundStartedAt = 0L
                        }
                    }
                }
            }
            if (foregroundPackage != null && foregroundStartedAt > 0L) {
                val duration = (endTime - foregroundStartedAt).coerceAtLeast(0L)
                totals[foregroundPackage!!] = (totals[foregroundPackage!!] ?: 0L) + duration
            }

            val pm = context.packageManager
            totals.entries
                .filter { it.value >= 5_000L }
                .sortedByDescending { it.value }
                .take(50)
                .mapNotNull { (packageName, duration) ->
                    try {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        AppUsageInfo(
                            packageName = packageName,
                            appName = appName,
                            foregroundTimeMs = duration,
                            lastTimeUsedMs = lastUsed[packageName] ?: 0L,
                            dailyForegroundMinutes = duration / 60_000L,
                            weeklyForegroundMinutes = duration / 60_000L
                        )
                    } catch (_: PackageManager.NameNotFoundException) {
                        null
                    }
                }
        }
}
