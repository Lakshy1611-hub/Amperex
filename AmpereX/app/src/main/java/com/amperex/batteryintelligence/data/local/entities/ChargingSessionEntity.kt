package com.amperex.batteryintelligence.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amperex.batteryintelligence.domain.model.ChargingSession
import com.amperex.batteryintelligence.domain.model.PlugType

@Entity(
    tableName = "charging_sessions",
    indices = [Index(value = ["startTime"])]
)
data class ChargingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val startPercentage: Int,
    val currentPercentage: Int,
    val endPercentage: Int?,
    val plugType: PlugType,
    val peakCurrentMa: Int?,
    val averageTemperatureCelsius: Float?,
    val isCompleted: Boolean
) {
    fun toDomain(): ChargingSession = ChargingSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        startPercentage = startPercentage,
        currentPercentage = currentPercentage,
        endPercentage = endPercentage,
        plugType = plugType,
        peakCurrentMa = peakCurrentMa,
        averageTemperatureCelsius = averageTemperatureCelsius,
        isCompleted = isCompleted
    )

    companion object {
        fun fromDomain(session: ChargingSession): ChargingSessionEntity = ChargingSessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            startPercentage = session.startPercentage,
            currentPercentage = session.currentPercentage,
            endPercentage = session.endPercentage,
            plugType = session.plugType,
            peakCurrentMa = session.peakCurrentMa,
            averageTemperatureCelsius = session.averageTemperatureCelsius,
            isCompleted = session.isCompleted
        )
    }
}
