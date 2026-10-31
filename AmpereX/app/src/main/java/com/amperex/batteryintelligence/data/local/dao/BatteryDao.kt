package com.amperex.batteryintelligence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.amperex.batteryintelligence.data.local.entities.BatterySampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSample(sample: BatterySampleEntity): Long

    @Query("SELECT * FROM battery_samples ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSampleFlow(): Flow<BatterySampleEntity?>

    @Query("SELECT * FROM battery_samples ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSample(): BatterySampleEntity?

    @Query("SELECT * FROM battery_samples WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getSamplesSinceFlow(sinceTimestamp: Long): Flow<List<BatterySampleEntity>>

    @Query("SELECT * FROM battery_samples WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getSamplesSince(sinceTimestamp: Long): List<BatterySampleEntity>

    @Query("SELECT * FROM battery_samples WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getSamplesBetween(startTime: Long, endTime: Long): List<BatterySampleEntity>

    @Query("SELECT * FROM battery_samples ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSamples(limit: Int): List<BatterySampleEntity>

    @Query("SELECT COUNT(*) FROM battery_samples")
    suspend fun getTotalSampleCount(): Int

    @Query("DELETE FROM battery_samples")
    suspend fun clearAll()
}
