package com.amperex.batteryintelligence

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.amperex.batteryintelligence.data.local.AmpereXDatabase
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.data.repository.ChargingRepository
import com.amperex.batteryintelligence.data.repository.HealthRepository
import com.amperex.batteryintelligence.data.repository.UsageStatsRepository
import com.amperex.batteryintelligence.export.DataExportManager
import com.amperex.batteryintelligence.worker.DailySummaryWorker
import java.util.concurrent.TimeUnit

class AmpereXApplication : Application() {

    lateinit var database: AmpereXDatabase
        private set

    lateinit var batteryRepository: BatteryRepository
        private set

    lateinit var chargingRepository: ChargingRepository
        private set

    lateinit var healthRepository: HealthRepository
        private set

    lateinit var usageStatsRepository: UsageStatsRepository
        private set

    lateinit var exportManager: DataExportManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AmpereXDatabase.getInstance(this)
        batteryRepository = BatteryRepository(this, database.batteryDao())
        chargingRepository = ChargingRepository(database.chargingSessionDao())
        healthRepository = HealthRepository(this, database.batteryHealthDao())
        usageStatsRepository = UsageStatsRepository(this)
        exportManager = DataExportManager(this, database.batteryDao())

        scheduleDailySummaryWork()
    }

    private fun scheduleDailySummaryWork() {
        val workRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(2, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "AmpereX_DailySummaryWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        lateinit var instance: AmpereXApplication
            private set
    }
}
