package com.amperex.batteryintelligence.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.amperex.batteryintelligence.MainActivity
import com.amperex.batteryintelligence.R
import com.amperex.batteryintelligence.data.local.AmpereXDatabase
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Persistent, user-visible battery sampler. Android/OEM policies can still stop it. */
class BatteryMonitoringService : Service() {
    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val channelId = "amperex_background_monitoring"
    private val notificationId = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(notificationId, buildNotification("Starting battery monitoring…"))
        scope.launch {
            val repo = BatteryRepository(applicationContext, AmpereXDatabase.getInstance(applicationContext).batteryDao())
            while (isActive) {
                try {
                    val sample = repo.captureCurrentSample(forceRecord = true)
                    val current = sample.currentMa?.let { "${it} mA" } ?: "Current unavailable"
                    val temp = sample.temperatureCelsius?.let { "${it}°C" } ?: "Temp unavailable"
                    val status = "${sample.percentage}% • $current • $temp"
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .notify(notificationId, buildNotification(status))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Sensor/database errors are transient; keep the monitor alive.
                }
                delay(60_000L)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    private fun buildNotification(contentText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AmpereX • Background monitoring")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "AmpereX Background Monitoring", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Ongoing battery sampling status" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
