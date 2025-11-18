package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import java.nio.ByteOrder
import org.godotengine.godot.Dictionary

data class RunningCadenceMeasurement(
    val instantaneousSpeed: UShort,
    val instantaneousCadence: UInt,
    val instantaneousStrideLength: UShort?,
    val totalDistance: UInt?
) {
    override fun toString(): String {
        return "speed: $instantaneousSpeed cadence: $instantaneousCadence"
    }

    fun toDictionary(): Dictionary {
        val dict = Dictionary()
        dict["instantaneous_speed"] = instantaneousSpeed.toInt()
        dict["instantaneous_cadence"] = instantaneousCadence.toInt()
        dict["instantaneous_stride_length"] = instantaneousStrideLength?.toInt()
        dict["total_distance"] = totalDistance?.toInt()
        return dict
    }

    companion object {
        fun fromBytes(value: ByteArray): RunningCadenceMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val instantaneousStrideLengthPresent = flags and 0x01u > 0u
                val totalDistancePresent = flags and 0x02u > 0u
                val walkingOrRunningStatus = flags and 0x04u shr 2
                val instantaneousSpeed = parser.getUInt16()
                val instantaneousCadence = parser.getUInt8()
                val instantaneousStrideLength = if (instantaneousStrideLengthPresent) parser.getUInt16() else null
                val totalDistance = if (totalDistancePresent) parser.getUInt32() else null

                return RunningCadenceMeasurement(
                    instantaneousSpeed = instantaneousSpeed,
                    instantaneousCadence = instantaneousCadence,
                    instantaneousStrideLength = instantaneousStrideLength,
                    totalDistance = totalDistance
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}