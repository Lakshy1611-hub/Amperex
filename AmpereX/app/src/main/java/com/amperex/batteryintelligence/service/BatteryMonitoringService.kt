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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Opt-in user-initiated foreground service for high-resolution charging monitoring.
 * Runs only when the user explicitly engages active session benchmarking.
 */
class BatteryMonitoringService : Service() {

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)
    private val CHANNEL_ID = "amperex_active_monitoring_channel"
    private val NOTIFICATION_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification("Monitoring active battery telemetry...")
        startForeground(NOTIFICATION_ID, notification)

        scope.launch {
            val db = AmpereXDatabase.getInstance(applicationContext)
            val repo = BatteryRepository(applicationContext, db.batteryDao())

            while (isActive) {
                val sample = repo.captureCurrentSample(forceRecord = false)
                val currentText = if (sample.currentMa != null) "${sample.currentMa} mA" else "Current N/A"
                val updatedNotif = buildNotification("${sample.percentage}% • $currentText • ${sample.temperatureCelsius ?: "--"}°C")
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, updatedNotif)

                delay(15_000L) // Battery-conscious 15-second interval during active benchmark
            }
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AmpereX • Active Session")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AmpereX Active Benchmark",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time battery status while benchmark is running"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
