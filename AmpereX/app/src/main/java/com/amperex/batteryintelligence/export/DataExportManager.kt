package com.amperex.batteryintelligence.export

import android.content.Context
import com.amperex.batteryintelligence.data.local.dao.BatteryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class DataExportManager(
    private val context: Context,
    private val batteryDao: BatteryDao
) {
    private val json = Json { prettyPrint = true }

    suspend fun exportAsJson(): File = withContext(Dispatchers.IO) {
        val samples = batteryDao.getRecentSamples(1000).map { it.toDomain() }
        val jsonString = json.encodeToString(samples)
        val file = File(context.cacheDir, "amperex_battery_data_${System.currentTimeMillis()}.json")
        file.writeText(jsonString)
        file
    }

    suspend fun exportAsCsv(): File = withContext(Dispatchers.IO) {
        val samples = batteryDao.getRecentSamples(1000).map { it.toDomain() }
        val sb = StringBuilder()
        sb.append("Timestamp,Percentage,Status,PlugType,TemperatureCelsius,VoltageMv,CurrentMa\n")
        samples.forEach { s ->
            sb.append("${s.timestamp},${s.percentage},${s.status.name},${s.plugType.name},${s.temperatureCelsius ?: ""},${s.voltageMv ?: ""},${s.currentMa ?: ""}\n")
        }
        val file = File(context.cacheDir, "amperex_battery_data_${System.currentTimeMillis()}.csv")
        file.writeText(sb.toString())
        file
    }
}
