package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import org.godotengine.godot.Dictionary
import java.nio.ByteOrder

/**
 * Parsed FTMS Rower Data (0x2AD1)
 */
data class FtmsRowerMeasurement(
    val flags: UShort,
    val strokeRate: UInt?,
    val strokeCount: UShort?,
    val averageStrokeRate: UInt?,
    val totalDistanceMeters: Int?,
    val instantaneousPaceRaw: UShort?,
    val averagePaceRaw: UShort?,
    val instantaneousPowerWatts: UShort?,
    val averagePowerWatts: UShort?,
    val resistanceLevelRaw: Short?,
    val totalEnergy: UShort?,
    val energyPerHour: UShort?,
    val energyPerMinute: UInt?,
    val heartRateBpm: UInt?,
    val metabolicEquivalentRaw: UInt?,
    val elapsedTimeSeconds: Int?,
    val remainingTimeSeconds: Int?
) {

    fun toDictionary(): Dictionary {
        val dict = Dictionary()

        dict["flags"] = flags.toInt()
        dict["stroke_rate"] = strokeRate?.toInt()
        dict["stroke_count"] = strokeCount?.toInt()
        dict["average_stroke_rate"] = averageStrokeRate?.toInt()
        dict["total_distance_m"] = totalDistanceMeters
        dict["instantaneous_pace_raw"] = instantaneousPaceRaw?.toInt()
        dict["average_pace_raw"] = averagePaceRaw?.toInt()
        dict["instantaneous_power_w"] = instantaneousPowerWatts?.toInt()
        dict["average_power_w"] = averagePowerWatts?.toInt()
        dict["resistance_raw"] = resistanceLevelRaw?.toInt()
        dict["total_energy"] = totalEnergy?.toInt()
        dict["energy_per_hour"] = energyPerHour?.toInt()
        dict["energy_per_minute"] = energyPerMinute?.toInt()
        dict["heart_rate_bpm"] = heartRateBpm?.toInt()
        dict["metabolic_equivalent_raw"] = metabolicEquivalentRaw?.toInt()
        dict["elapsed_time_s"] = elapsedTimeSeconds
        dict["remaining_time_s"] = remainingTimeSeconds

        return dict
    }

    companion object {

        fun fromBytes(value: ByteArray): FtmsRowerMeasurement? {
            val parser = BluetoothBytesParser(value, 0, ByteOrder.LITTLE_ENDIAN)

            try {
                val flags = parser.getUInt16()
                val moreData = (flags.toUInt() and 0x0001u) != 0u
                var strokeRate: UInt? = null
                var strokeCount: UShort? = null
                val averageStrokeRatePresent = flags.toUInt() and 0x0002u > 0u
                val totalDistancePresent = flags.toUInt() and 0x0004u > 0u
                val instantaneousPacePresent = flags.toUInt() and 0x0008u > 0u
                val averagePacePresent = flags.toUInt() and 0x0010u > 0u
                val instantaneousPowerPresent = flags.toUInt() and 0x0020u > 0u
                val averagePowerPresent = flags.toUInt() and 0x0040u > 0u
                val resistancePresent = flags.toUInt() and 0x0080u > 0u
                val totalEnergyPresent = flags.toUInt() and 0x0100u > 0u
                val energyPerHourPresent = flags.toUInt() and 0x0200u > 0u
                val energyPerMinutePresent = flags.toUInt() and 0x0400u > 0u
                val heartRatePresent = flags.toUInt() and 0x0800u > 0u
                val metabolicEquivalentPresent = flags.toUInt() and 0x1000u > 0u
                val elapsedTimePresent = flags.toUInt() and 0x2000u > 0u
                val remainingTimePresent = flags.toUInt() and 0x4000u > 0u

                if (!moreData) {
                    strokeRate = parser.getUInt8()
                    strokeCount = parser.getUInt16()
                }

                val averageStrokeRate =
                    if (averageStrokeRatePresent) parser.getUInt8() else null

                val totalDistanceMeters =
                    if (totalDistancePresent) {
                        val b0 = parser.getUInt8().toInt()
                        val b1 = parser.getUInt8().toInt()
                        val b2 = parser.getUInt8().toInt()
                        b0 or (b1 shl 8) or (b2 shl 16)
                    } else null

                val instantaneousPaceRaw =
                    if (instantaneousPacePresent) parser.getUInt16() else null

                val averagePaceRaw =
                    if (averagePacePresent) parser.getUInt16() else null

                val instantaneousPowerWatts =
                    if (instantaneousPowerPresent) parser.getUInt16() else null

                val averagePowerWatts =
                    if (averagePowerPresent) parser.getUInt16() else null

                val resistanceLevelRaw =
                    if (resistancePresent) parser.getInt16() else null

                val totalEnergy =
                    if (totalEnergyPresent) parser.getUInt16() else null

                val energyPerHour =
                    if (energyPerHourPresent) parser.getUInt16() else null

                val energyPerMinute =
                    if (energyPerMinutePresent) parser.getUInt8() else null

                val heartRateBpm =
                    if (heartRatePresent) parser.getUInt8() else null

                val metabolicEquivalentRaw =
                    if (metabolicEquivalentPresent) parser.getUInt8() else null

                val elapsedTimeSeconds =
                    if (elapsedTimePresent) parser.getUInt16().toInt() else null

                val remainingTimeSeconds =
                    if (remainingTimePresent) parser.getUInt16().toInt() else null

                return FtmsRowerMeasurement(
                    flags = flags,
                    strokeRate = strokeRate,
                    strokeCount = strokeCount,
                    averageStrokeRate = averageStrokeRate,
                    totalDistanceMeters = totalDistanceMeters,
                    instantaneousPaceRaw = instantaneousPaceRaw,
                    averagePaceRaw = averagePaceRaw,
                    instantaneousPowerWatts = instantaneousPowerWatts,
                    averagePowerWatts = averagePowerWatts,
                    resistanceLevelRaw = resistanceLevelRaw,
                    totalEnergy = totalEnergy,
                    energyPerHour = energyPerHour,
                    energyPerMinute = energyPerMinute,
                    heartRateBpm = heartRateBpm,
                    metabolicEquivalentRaw = metabolicEquivalentRaw,
                    elapsedTimeSeconds = elapsedTimeSeconds,
                    remainingTimeSeconds = remainingTimeSeconds
                )
            } catch (_: Exception) {
                return null
            }
        }
    }
}