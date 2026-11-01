package com.amperex.batteryintelligence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.amperex.batteryintelligence.data.local.AmpereXDatabase
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.data.repository.ChargingRepository
import com.amperex.batteryintelligence.domain.model.PlugType
import com.amperex.batteryintelligence.service.BatteryMonitoringService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            try {
                val serviceIntent = Intent(context, BatteryMonitoringService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(serviceIntent)
                else context.startService(serviceIntent)
            } catch (_: Exception) { /* OS background-start restrictions may defer restart. */ }
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AmpereXDatabase.getInstance(context)
                val batteryRepo = BatteryRepository(context, db.batteryDao())
                val chargingRepo = ChargingRepository(db.chargingSessionDao())
                when (action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = true)
                        chargingRepo.recordSessionStart(sample.percentage, sample.plugType)
                        context.getSharedPreferences("battery_usage_session", Context.MODE_PRIVATE).edit().clear().apply()
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = true)
                        chargingRepo.completeActiveSession(sample.percentage)
                        context.getSharedPreferences("battery_usage_session", Context.MODE_PRIVATE).edit()
                            .putLong("started_at", System.currentTimeMillis())
                            .putInt("start_level", sample.percentage).apply()
                    }
                    Intent.ACTION_BATTERY_LOW, Intent.ACTION_BATTERY_OKAY -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = true)
                        if (sample.plugType != PlugType.NONE) chargingRepo.updateActiveSession(sample.percentage, sample.currentMa, sample.temperatureCelsius)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally { pendingResult.finish() }
        }
    }
}
