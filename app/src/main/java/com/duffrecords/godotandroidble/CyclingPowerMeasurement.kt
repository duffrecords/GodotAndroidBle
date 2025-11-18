package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import java.nio.ByteOrder
import org.godotengine.godot.Dictionary

data class CyclingPowerMeasurement(
    val instantaneousPower: Short,
    val pedalPowerBalance: UInt?,
    val accumulatedTorque: UInt?,
    val cumulativeWheelRevs: UInt?,
    val lastWheelEventTime: UInt?,
    val cumulativeCrankRevs: UShort?,
    val lastCrankEventTime: UInt?
) {
    override fun toString(): String {
        return "wheel revs: $cumulativeWheelRevs last wheel event: $lastWheelEventTime crank revs: $cumulativeCrankRevs last crank time: $lastCrankEventTime"
    }

    fun toDictionary(): Dictionary {
        val dict = Dictionary()
        dict["instantaneous_power"] = instantaneousPower.toInt()
        dict["pedal_power_balance"] = pedalPowerBalance?.toInt()
        dict["accumulated_torque"] = accumulatedTorque?.toInt()
        dict["cumulative_wheel_revs"] = cumulativeWheelRevs?.toInt()
        dict["last_wheel_event_time"] = lastWheelEventTime?.toInt()
        dict["cumulative_crank_revs"] = cumulativeCrankRevs?.toInt()
        dict["last_crank_event_time"] = lastCrankEventTime?.toInt()
        return dict
    }

    companion object {
        fun fromBytes(value: ByteArray): CyclingPowerMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val pedalPowerBalancePresent = flags and 0x01u > 0u
                //val pedalPowerBalanceReference = flags and 0x02u shr 1
                val accumulatedTorquePresent = flags and 0x04u > 0u
                //val accumulatedTorqueSource = flags and 0x08u shr 3
                val wheelRevDataPresent = flags and 0x10u > 0u
                val crankRevDataPresent = flags and 0x20u > 0u
                val instantaneousPower = parser.getInt16()
                val pedalPowerBalance = if (pedalPowerBalancePresent) parser.getUInt8() else null
                val accumulatedTorque = if (accumulatedTorquePresent) parser.getUInt16() / 32.toUShort() else null
                val cumulativeWheelRevs = if (wheelRevDataPresent) parser.getUInt32() else null
                val lastWheelEventTime = if (wheelRevDataPresent) parser.getUInt16() / 2048.toUShort() else null
                val cumulativeCrankRevs = if (crankRevDataPresent) parser.getUInt16() else null
                val lastCrankEventTime = if (crankRevDataPresent) parser.getUInt16() / 1024.toUShort() else null

                return CyclingPowerMeasurement(
                    instantaneousPower = instantaneousPower,
                    pedalPowerBalance = pedalPowerBalance,
                    accumulatedTorque = accumulatedTorque,
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