package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import java.nio.ByteOrder
import org.godotengine.godot.Dictionary

data class CyclingCadenceMeasurement(
    val cumulativeWheelRevs: UInt?,
    val lastWheelEventTime: UShort?,
    val cumulativeCrankRevs: UShort?,
    val lastCrankEventTime: UShort?
) {
    override fun toString(): String {
        return "wheel revs: $cumulativeWheelRevs last wheel event: $lastWheelEventTime crank revs: $cumulativeCrankRevs last crank time: $lastCrankEventTime"
    }

    fun toDictionary(): Dictionary {
        val dict = Dictionary()
        dict["cumulative_wheel_revs"] = cumulativeWheelRevs?.toInt()
        dict["last_wheel_event_time"] = lastWheelEventTime?.toInt()
        dict["cumulative_crank_revs"] = cumulativeCrankRevs?.toInt()
        dict["last_crank_event_time"] = lastCrankEventTime?.toInt()
        return dict
    }

    companion object {
        fun fromBytes(value: ByteArray): CyclingCadenceMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val wheelRevDataPresent = flags and 0x01u > 0u
                val crankRevDataPresent = flags and 0x02u > 0u
                val cumulativeWheelRevs = if (wheelRevDataPresent) parser.getUInt32() else null
                val lastWheelEventTime = if (wheelRevDataPresent) parser.getUInt16() else null
                val cumulativeCrankRevs = if (crankRevDataPresent) parser.getUInt16() else null
                val lastCrankEventTime = if (crankRevDataPresent) parser.getUInt16() else null

                return CyclingCadenceMeasurement(
                    cumulativeWheelRevs = cumulativeWheelRevs,
                    lastWheelEventTime = lastWheelEventTime,
                    cumulativeCrankRevs = cumulativeCrankRevs,
                    lastCrankEventTime = lastCrankEventTime
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}