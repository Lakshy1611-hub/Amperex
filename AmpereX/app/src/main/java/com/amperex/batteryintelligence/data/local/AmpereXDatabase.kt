package com.amperex.batteryintelligence.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.amperex.batteryintelligence.data.local.dao.BatteryDao
import com.amperex.batteryintelligence.data.local.dao.BatteryHealthDao
import com.amperex.batteryintelligence.data.local.dao.ChargingSessionDao
import com.amperex.batteryintelligence.data.local.dao.DailySummaryDao
import com.amperex.batteryintelligence.data.local.entities.AppUsageSnapshotEntity
import com.amperex.batteryintelligence.data.local.entities.BatteryHealthObservationEntity
import com.amperex.batteryintelligence.data.local.entities.BatterySampleEntity
import com.amperex.batteryintelligence.data.local.entities.ChargingSessionEntity
import com.amperex.batteryintelligence.data.local.entities.DailyBatterySummaryEntity

@Database(
    entities = [
        BatterySampleEntity::class,
        ChargingSessionEntity::class,
        DailyBatterySummaryEntity::class,
        BatteryHealthObservationEntity::class,
        AppUsageSnapshotEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AmpereXDatabase : RoomDatabase() {

    abstract fun batteryDao(): BatteryDao
    abstract fun chargingSessionDao(): ChargingSessionDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun batteryHealthDao(): BatteryHealthDao

    companion object {
        @Volatile
        private var INSTANCE: AmpereXDatabase? = null

        fun getInstance(context: Context): AmpereXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmpereXDatabase::class.java,
                    "amperex_battery_intelligence.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
