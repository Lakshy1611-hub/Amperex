package com.amperex.batteryintelligence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.amperex.batteryintelligence.data.local.AmpereXDatabase
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.data.repository.ChargingRepository
import com.amperex.batteryintelligence.domain.model.PlugType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AmpereXDatabase.getInstance(context)
                val batteryRepo = BatteryRepository(context, db.batteryDao())
                val chargingRepo = ChargingRepository(db.chargingSessionDao())

                when (action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = true)
                        chargingRepo.recordSessionStart(
                            startPercentage = sample.percentage,
                            plugType = sample.plugType
                        )
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = true)
                        chargingRepo.completeActiveSession(endPercentage = sample.percentage)
                    }
                    Intent.ACTION_BATTERY_LOW,
                    Intent.ACTION_BATTERY_OKAY,
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val sample = batteryRepo.captureCurrentSample(forceRecord = false)
                        if (sample.plugType != PlugType.NONE) {
                            chargingRepo.updateActiveSession(
                                currentPercentage = sample.percentage,
                                currentMa = sample.currentMa,
                                temp = sample.temperatureCelsius
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
