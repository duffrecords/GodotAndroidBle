package com.duffrecords.godotandroidble

import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class GodotAndroidBle(godot: Godot): GodotPlugin(godot) {

    override fun getPluginName() = "GodotAndroidBle"

    val cscMeasurementSignalInfo = SignalInfo("csc_measurement", UInt::class.javaObjectType, UShort::class.javaObjectType, UShort::class.javaObjectType, UShort::class.javaObjectType)

    override fun getPluginSignals(): Set<SignalInfo> {
        return setOf(cscMeasurementSignalInfo)
    }

    @UsedByGodot
    fun initialize() {
        BluetoothHandler.initialize(context)
    }

}