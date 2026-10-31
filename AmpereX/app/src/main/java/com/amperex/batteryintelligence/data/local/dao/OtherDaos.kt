package com.amperex.batteryintelligence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.amperex.batteryintelligence.data.local.entities.ChargingSessionEntity
import com.amperex.batteryintelligence.data.local.entities.DailyBatterySummaryEntity
import com.amperex.batteryintelligence.data.local.entities.BatteryHealthObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargingSessionEntity): Long

    @Update
    suspend fun updateSession(session: ChargingSessionEntity)

    @Query("SELECT * FROM charging_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): ChargingSessionEntity?

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int): Flow<List<ChargingSessionEntity>>

    @Query("SELECT * FROM charging_sessions WHERE startTime >= :sinceTimestamp ORDER BY startTime DESC")
    suspend fun getSessionsSince(sinceTimestamp: Long): List<ChargingSessionEntity>

    @Query("DELETE FROM charging_sessions")
    suspend fun clearAll()
}

@Dao
interface DailySummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: DailyBatterySummaryEntity)

    @Query("SELECT * FROM daily_summaries WHERE dateEpochDay = :epochDay")
    suspend fun getSummaryForDay(epochDay: Long): DailyBatterySummaryEntity?

    @Query("SELECT * FROM daily_summaries ORDER BY dateEpochDay DESC LIMIT :limit")
    fun getRecentSummariesFlow(limit: Int): Flow<List<DailyBatterySummaryEntity>>

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAll()
}

@Dao
interface BatteryHealthDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthObservation(observation: BatteryHealthObservationEntity)

    @Query("SELECT * FROM health_observations ORDER BY timestamp DESC")
    fun getAllObservationsFlow(): Flow<List<BatteryHealthObservationEntity>>

    @Query("SELECT COUNT(*) FROM health_observations")
    suspend fun getObservationsCount(): Int

    @Query("DELETE FROM health_observations")
    suspend fun clearAll()
}
