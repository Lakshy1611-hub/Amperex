package com.amperex.batteryintelligence.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.amperex.batteryintelligence.data.local.AmpereXDatabase
import com.amperex.batteryintelligence.data.local.entities.DailyBatterySummaryEntity
import com.amperex.batteryintelligence.domain.calculator.DailyReportCalculator
import java.time.LocalDate

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AmpereXDatabase.getInstance(applicationContext)
            val yesterdayEpochDay = LocalDate.now().minusDays(1).toEpochDay()
            val dayStartTs = yesterdayEpochDay * 86_400_000L
            val dayEndTs = dayStartTs + 86_400_000L

            val samples = db.batteryDao().getSamplesBetween(dayStartTs, dayEndTs).map { it.toDomain() }

            if (samples.isNotEmpty()) {
                val summary = DailyReportCalculator.computeDailySummary(
                    dateEpochDay = yesterdayEpochDay,
                    daySamples = samples,
                    trackedScreenOnMinutes = null
                )
                db.dailySummaryDao().insertSummary(DailyBatterySummaryEntity.fromDomain(summary))
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
