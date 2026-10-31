package com.amperex.batteryintelligence.data.local

import androidx.room.TypeConverter
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.PlugType

class Converters {
    @TypeConverter
    fun fromChargingStatus(status: ChargingStatus): String = status.name

    @TypeConverter
    fun toChargingStatus(value: String): ChargingStatus = try {
        ChargingStatus.valueOf(value)
    } catch (e: Exception) {
        ChargingStatus.UNKNOWN
    }

    @TypeConverter
    fun fromPlugType(plug: PlugType): String = plug.name

    @TypeConverter
    fun toPlugType(value: String): PlugType = try {
        PlugType.valueOf(value)
    } catch (e: Exception) {
        PlugType.UNKNOWN
    }
}
