package com.duffrecords.godotandroidble

import com.welie.blessed.BluetoothBytesParser
import org.godotengine.godot.Dictionary
import java.nio.ByteOrder

/**
 * Parsed FTMS Rower Data (0x2AD1)
 */
data class FtmsRowerParsedData(
    val flags: Int,
    val strokeRate: Int?,
    val strokeCount: Int?,
    val averageStrokeRate: Int?,
    val totalDistanceMeters: Int?,
    val instantaneousPaceRaw: Int?,
    val averagePaceRaw: Int?,
    val instantaneousPowerWatts: Int?,
    val averagePowerWatts: Int?,
    val resistanceLevelRaw: Int?,
    val totalEnergy: Int?,
    val energyPerHour: Int?,
    val energyPerMinute: Int?,
    val heartRateBpm: Int?,
    val metabolicEquivalentRaw: Int?,
    val elapsedTimeSeconds: Int?,
    val remainingTimeSeconds: Int?
) {
    fun toDictionary(): Dictionary {
        val d = Dictionary()
        d["flags"] = flags
        d["stroke_rate"] = strokeRate
        d["stroke_count"] = strokeCount
        d["average_stroke_rate"] = averageStrokeRate
        d["total_distance_m"] = totalDistanceMeters
        d["instantaneous_pace_raw"] = instantaneousPaceRaw
        d["average_pace_raw"] = averagePaceRaw
        d["instantaneous_power_w"] = instantaneousPowerWatts
        d["average_power_w"] = averagePowerWatts
        d["resistance_raw"] = resistanceLevelRaw
        d["total_energy"] = totalEnergy
        d["energy_per_hour"] = energyPerHour
        d["energy_per_minute"] = energyPerMinute
        d["heart_rate_bpm"] = heartRateBpm
        d["metabolic_equivalent_raw"] = metabolicEquivalentRaw
        d["elapsed_time_s"] = elapsedTimeSeconds
        d["remaining_time_s"] = remainingTimeSeconds
        return d
    }
}

/**
 * Raw wrapper (kept for compatibility)
 */
data class FtmsRowerData(
    val raw: ByteArray
) {
    companion object {

        fun fromBytes(bytes: ByteArray): FtmsRowerData {
            return FtmsRowerData(bytes)
        }

        fun parse(bytes: ByteArray): FtmsRowerParsedData? {
            return try {
                val parser = BluetoothBytesParser(bytes, 0, ByteOrder.LITTLE_ENDIAN)

                val flags = parser.getUInt16().toInt() and 0xFFFF
                fun bitSet(f: Int, b: Int) = ((f shr b) and 0x1) == 1

                val strokeRate = parser.getUInt8().toInt()
                val strokeCount = parser.getUInt16().toInt()

                val averageStrokeRate = if (bitSet(flags, 1)) parser.getUInt8().toInt() else null

                val totalDistanceMeters = if (bitSet(flags, 2)) {
                    val b0 = parser.getUInt8().toInt()
                    val b1 = parser.getUInt8().toInt()
                    val b2 = parser.getUInt8().toInt()
                    b0 or (b1 shl 8) or (b2 shl 16)
                } else null

                val instantaneousPaceRaw = if (bitSet(flags, 3)) parser.getUInt16().toInt() else null
                val averagePaceRaw = if (bitSet(flags, 4)) parser.getUInt16().toInt() else null

                val instantaneousPower = if (bitSet(flags, 5)) parser.getUInt16().toInt() else null
                val averagePower = if (bitSet(flags, 6)) parser.getUInt16().toInt() else null

                val resistance = if (bitSet(flags, 7)) parser.getInt16().toInt() else null

                val totalEnergy = if (bitSet(flags, 8)) parser.getUInt16().toInt() else null
                val energyPerHour = if (bitSet(flags, 9)) parser.getUInt16().toInt() else null
                val energyPerMinute = if (bitSet(flags, 10)) parser.getUInt8().toInt() else null

                val heartRate = if (bitSet(flags, 11)) parser.getUInt8().toInt() else null
                val met = if (bitSet(flags, 12)) parser.getUInt8().toInt() else null

                val elapsed = if (bitSet(flags, 13)) {
                    val b0 = parser.getUInt8().toInt()
                    val b1 = parser.getUInt8().toInt()
                    val b2 = parser.getUInt8().toInt()
                    b0 or (b1 shl 8) or (b2 shl 16)
                } else null

                val remaining = if (bitSet(flags, 14)) {
                    val b0 = parser.getUInt8().toInt()
                    val b1 = parser.getUInt8().toInt()
                    val b2 = parser.getUInt8().toInt()
                    b0 or (b1 shl 8) or (b2 shl 16)
                } else null

                FtmsRowerParsedData(
                    flags = flags,
                    strokeRate = strokeRate,
                    strokeCount = strokeCount,
                    averageStrokeRate = averageStrokeRate,
                    totalDistanceMeters = totalDistanceMeters,
                    instantaneousPaceRaw = instantaneousPaceRaw,
                    averagePaceRaw = averagePaceRaw,
                    instantaneousPowerWatts = instantaneousPower,
                    averagePowerWatts = averagePower,
                    resistanceLevelRaw = resistance,
                    totalEnergy = totalEnergy,
                    energyPerHour = energyPerHour,
                    energyPerMinute = energyPerMinute,
                    heartRateBpm = heartRate,
                    metabolicEquivalentRaw = met,
                    elapsedTimeSeconds = elapsed,
                    remainingTimeSeconds = remaining
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
