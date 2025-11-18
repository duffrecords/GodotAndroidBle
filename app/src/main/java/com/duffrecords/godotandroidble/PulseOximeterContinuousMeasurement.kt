package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.Calendar
import java.util.Date
import org.godotengine.godot.Dictionary
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class PulseOximeterContinuousMeasurement(
    val spO2: Double,
    val pulseRate: Double,
    val spO2Fast: Double?,
    val pulseRateFast: Double?,
    val spO2Slow: Double?,
    val pulseRateSlow: Double?,
    val pulseAmplitudeIndex: Double?,
    val measurementStatus: UShort?,
    val sensorStatus: UShort?,
    val createdAt: Date = Calendar.getInstance().time
) {
    override fun toString(): String {
        return "${"%.1f".format(spO2)} %%"
    }

    fun toDictionary(): Dictionary {
        val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
        val dict = Dictionary()
        dict["spO2"] = spO2.toFloat()
        dict["pulse_rate"] = pulseRate.toFloat()
        dict["spO2_fast"] = spO2Fast?.toFloat()
        dict["pulse_rate_fast"] = pulseRateFast?.toFloat()
        dict["spO2_slow"] = spO2Slow?.toFloat()
        dict["pulse_rate_slow"] = pulseRateSlow?.toFloat()
        dict["measurement_status"] = measurementStatus?.toInt()
        dict["sensor_status"] = sensorStatus?.toInt()
        dict["pulse_amplitude_index"] = pulseAmplitudeIndex?.toFloat()
        dict["created_at"] = dateFormat.format(createdAt)
        return dict
    }

    companion object {
        fun fromBytes(value: ByteArray): PulseOximeterContinuousMeasurement? {
            val parser = BluetoothBytesParser(value, 0, LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt8()
                val spo2FastPresent = flags and 0x01u > 0u
                val spo2SlowPresent = flags and 0x02u > 0u
                val measurementStatusPresent = flags and 0x04u > 0u
                val sensorStatusPresent = flags and 0x08u > 0u
                val pulseAmplitudeIndexPresent = flags and 0x10u > 0u

                val spO2 = parser.getSFloat()
                val pulseRate = parser.getSFloat()
                val spO2Fast = if (spo2FastPresent) parser.getSFloat() else null
                val pulseRateFast = if (spo2FastPresent) parser.getSFloat() else null
                val spO2Slow = if (spo2SlowPresent) parser.getSFloat() else null
                val pulseRateSlow = if (spo2SlowPresent) parser.getSFloat() else null
                val measurementStatus = if (measurementStatusPresent) parser.getUInt16() else null
                val sensorStatus = if (sensorStatusPresent) parser.getUInt16() else null
                if (sensorStatusPresent) parser.getUInt8() // Reserved byte
                val pulseAmplitudeIndex = if (pulseAmplitudeIndexPresent) parser.getSFloat() else null

                return PulseOximeterContinuousMeasurement(
                    spO2 = spO2,
                    pulseRate = pulseRate,
                    spO2Fast = spO2Fast,
                    pulseRateFast = pulseRateFast,
                    spO2Slow = spO2Slow,
                    pulseRateSlow = pulseRateSlow,
                    measurementStatus = measurementStatus,
                    sensorStatus = sensorStatus,
                    pulseAmplitudeIndex = pulseAmplitudeIndex
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}