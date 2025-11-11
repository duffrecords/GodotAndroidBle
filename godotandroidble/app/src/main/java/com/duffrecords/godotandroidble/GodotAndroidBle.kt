package com.duffrecords.godotandroidble

import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class GodotAndroidBle(godot: Godot): GodotPlugin(godot) {

    override fun getPluginName() = "GodotAndroidBle"

    override fun getPluginSignals(): MutableSet<SignalInfo> {
        return mutableSetOf(
            SignalInfo("connected", String::class.java, String::class.java),
            SignalInfo("connection_failed", String::class.java),
            SignalInfo("disconnected", String::class.java, String::class.java),
            SignalInfo("bluetooth_state_changed", Int::class.java),
            SignalInfo("device_found", String::class.java, String::class.java),
            SignalInfo("current_time_received", String::class.java),
            SignalInfo("battery_level_received", UInt::class.java),
            SignalInfo("manufacturer_name_received", String::class.java),
            SignalInfo("model_number_received", String::class.java),
            SignalInfo("blood_pressure_measurement_received", Dictionary::class.java),
            SignalInfo("cycling_cadence_measurement_received", Dictionary::class.java),
            SignalInfo("glucose_measurement_received", Dictionary::class.java),
            SignalInfo("heart_rate_measurement_received", UInt::class.java),
            SignalInfo("pulse_oximeter_continuous_measurement_received", Dictionary::class.java),
            SignalInfo("pulse_oximeter_spot_measurement_received", Dictionary::class.java),
            SignalInfo("temperature_measurement_received", Dictionary::class.java),
            SignalInfo("weight_measurement_received", Dictionary::class.java)
        )
    }

    @UsedByGodot
    fun initialize() {
        BluetoothHandler.initialize(context)
    }

}