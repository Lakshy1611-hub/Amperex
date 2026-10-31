package com.amperex.batteryintelligence.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.PlugType

@Entity(
    tableName = "battery_samples",
    indices = [
        Index(value = ["timestamp"], unique = true),
        Index(value = ["status"])
    ]
)
data class BatterySampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val percentage: Int,
    val status: ChargingStatus,
    val plugType: PlugType,
    val temperatureCelsius: Float?,
    val voltageMv: Int?,
    val currentMa: Int?,
    val chargeCounterUah: Long? = null
) {
    fun toDomain(): BatterySample = BatterySample(
        id = id,
        timestamp = timestamp,
        percentage = percentage,
        status = status,
        plugType = plugType,
        temperatureCelsius = temperatureCelsius,
        voltageMv = voltageMv,
        currentMa = currentMa,
        chargeCounterUah = chargeCounterUah
    )

    companion object {
        fun fromDomain(sample: BatterySample): BatterySampleEntity = BatterySampleEntity(
            id = sample.id,
            timestamp = sample.timestamp,
            percentage = sample.percentage,
            status = sample.status,
            plugType = sample.plugType,
            temperatureCelsius = sample.temperatureCelsius,
            voltageMv = sample.voltageMv,
            currentMa = sample.currentMa,
            chargeCounterUah = sample.chargeCounterUah
        )
    }
}
