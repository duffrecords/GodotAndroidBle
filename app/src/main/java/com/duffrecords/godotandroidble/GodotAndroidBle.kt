package com.duffrecords.godotandroidble

import android.Manifest
import android.Manifest.permission
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.app.Instrumentation.ActivityResult
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot
import timber.log.Timber

interface SignalEmitter {
    fun emitToGodot(signalName: String, vararg args: Any?)
}
class GodotAndroidBle(godot: Godot): GodotPlugin(godot), SignalEmitter {

    companion object {

        private const val PLUGIN_NAME = "GodotAndroidBle"

        // List of constants for results of permission request
        const val PERMISSION_RESULT_GRANTED = 0
        const val PERMISSION_RESULT_DENIED = 1
        const val PERMISSION_RESULT_DENIED_SHOW_RATIONALE = 2

        // List of return values
        const val PERMISSION_CODE_UNAVAILABLE = -1
        const val PERMISSION_CODE_OK = 0

        const val BLUETOOTH_NOT_SUPPORTED = -1
        const val BLUETOOTH_ENABLED = 0
        const val BLUETOOTH_DISABLED = 1

        const val SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED = "bt_enable_request_completed"
        const val SIGNAL_PERMISSION_REQUEST_COMPLETED = "permission_request_completed"
        const val SIGNAL_BLUETOOTH_DEVICE_CONNECTED = "bluetooth_device_connected"
        const val SIGNAL_BLUETOOTH_DEVICE_DISCONNECTED = "bluetooth_device_disconnected"
        const val SIGNAL_BLUETOOTH_DEVICE_CONNECTION_FAILED = "bluetooth_device_connection_failed"
        const val SIGNAL_BLUETOOTH_STATE_CHANGED = "bluetooth_state_changed"
        const val SIGNAL_BLUETOOTH_DEVICE_FOUND = "bluetooth_device_found"
        const val SIGNAL_CURRENT_TIME_RECEIVED = "current_time_received"
        const val SIGNAL_BATTERY_LEVEL_RECEIVED = "battery_level_received"
        const val SIGNAL_MANUFACTURER_NAME_RECEIVED = "manufacturer_name_received"
        const val SIGNAL_MODEL_NUMBER_RECEIVED = "model_number_received"
        const val SIGNAL_BLOOD_PRESSURE_MEASUREMENT_RECEIVED = "blood_pressure_measurement_received"
        const val SIGNAL_CYCLING_CADENCE_MEASUREMENT_RECEIVED = "cycling_cadence_measurement_received"
        const val SIGNAL_CYCLING_POWER_MEASUREMENT_RECEIVED = "cycling_power_measurement_received"
        const val SIGNAL_RUNNING_CADENCE_MEASUREMENT_RECEIVED = "running_cadence_measurement_received"
        const val SIGNAL_GLUCOSE_MEASUREMENT_RECEIVED = "glucose_measurement_received"
        const val SIGNAL_HEART_RATE_MEASUREMENT_RECEIVED = "heart_rate_measurement_received"
        const val SIGNAL_PULSE_OXIMETER_CONTINUOUS_MEASUREMENT_RECEIVED = "pulse_oximeter_continuous_measurement_received"
        const val SIGNAL_PULSE_OXIMETER_SPOT_MEASUREMENT_RECEIVED = "pulse_oximeter_spot_measurement_received"
        const val SIGNAL_TEMPERATURE_MEASUREMENT_RECEIVED = "temperature_measurement_received"
        const val SIGNAL_WEIGHT_MEASUREMENT_RECEIVED = "weight_measurement_received"

        private const val REQUEST_ENABLE_BT = 1001
        private const val REQ_BLE_PERMISSIONS = 2001
    }

    private val currentActivity: Activity = activity ?: throw IllegalStateException()
    private val mPermissions = mutableMapOf<Int, String>()

    override fun getPluginName() = "GodotAndroidBle"

    override fun getPluginSignals(): MutableSet<SignalInfo> {
        return mutableSetOf(
            SignalInfo(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, Int::class.java),
            SignalInfo(SIGNAL_PERMISSION_REQUEST_COMPLETED, Any::class.java, String::class.java, Any::class.java),
            SignalInfo(SIGNAL_BLUETOOTH_STATE_CHANGED, Int::class.java),
            SignalInfo(SIGNAL_BLUETOOTH_DEVICE_CONNECTED, String::class.java, String::class.java),
            SignalInfo(SIGNAL_BLUETOOTH_DEVICE_CONNECTION_FAILED, String::class.java, String::class.java, String::class.java),
            SignalInfo(SIGNAL_BLUETOOTH_DEVICE_DISCONNECTED, String::class.java, String::class.java, String::class.java),
            SignalInfo(SIGNAL_BLUETOOTH_DEVICE_FOUND, String::class.java, String::class.java, Int::class.java),
            SignalInfo(SIGNAL_CURRENT_TIME_RECEIVED, String::class.java),
            SignalInfo(SIGNAL_BATTERY_LEVEL_RECEIVED, UInt::class.java),
            SignalInfo(SIGNAL_MANUFACTURER_NAME_RECEIVED, String::class.java),
            SignalInfo(SIGNAL_MODEL_NUMBER_RECEIVED, String::class.java),
            SignalInfo(SIGNAL_BLOOD_PRESSURE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_CYCLING_CADENCE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_CYCLING_POWER_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_RUNNING_CADENCE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_GLUCOSE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_HEART_RATE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_PULSE_OXIMETER_CONTINUOUS_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_PULSE_OXIMETER_SPOT_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_TEMPERATURE_MEASUREMENT_RECEIVED, Dictionary::class.java),
            SignalInfo(SIGNAL_WEIGHT_MEASUREMENT_RECEIVED, Dictionary::class.java)
        )
    }

    // Public bridge that BluetoothHandler can call
    override fun emitToGodot(signalName: String, vararg args: Any?) {
        emitSignal(signalName, *args)
    }

    init {
        mPermissions[1] = permission.ACCESS_COARSE_LOCATION
        mPermissions[2] = permission.ACCESS_FINE_LOCATION
        mPermissions[3] = permission.BLUETOOTH_SCAN
        mPermissions[4] = permission.BLUETOOTH_CONNECT
        BluetoothHandler.initialize(context, this)
    }

    @UsedByGodot
    fun startScanning() {
        BluetoothHandler.startScanning()
    }

    @UsedByGodot
    fun stopScanning() {
        BluetoothHandler.stopScanning()
    }

    @UsedByGodot
    fun scanForBlpService() {
        BluetoothHandler.scanForBlpService()
    }

    @UsedByGodot
    fun scanForGlucoseService() {
        BluetoothHandler.scanForGlucoseService()
    }

    @UsedByGodot
    fun scanForHrsService() {
        BluetoothHandler.scanForHrsService()
    }

    @UsedByGodot
    fun scanForHtsService() {
        BluetoothHandler.scanForHtsService()
    }

    @UsedByGodot
    fun scanForPlxService() {
        BluetoothHandler.scanForPlxService()
    }

    @UsedByGodot
    fun scanForWssService() {
        BluetoothHandler.scanForWssService()
    }

    @UsedByGodot
    fun scanForCscService() {
        BluetoothHandler.scanForCscService()
    }

    @UsedByGodot
    fun scanForCpService() {
        BluetoothHandler.scanForCpService()
    }

    @UsedByGodot
    fun scanForRscService() {
        BluetoothHandler.scanForRscService()
    }

    @UsedByGodot
    fun scanForAddress(address: String) {
        BluetoothHandler.scanForAddress(address)
    }

    /**
     * Checks if Bluetooth is enabled and makes a request to enable it if not
     */
    @UsedByGodot
    fun requestEnableBluetooth() {
        val act = activity ?: return  // or godot.getActivity()

        if (BluetoothHandler.centralManager.isBleSupported) {
            emitSignal(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, BLUETOOTH_NOT_SUPPORTED)
            return
        }

        // Already enabled, just notify Godot
        if (BluetoothHandler.centralManager.isBluetoothEnabled) {
            emitSignal(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, BLUETOOTH_ENABLED)
            return
        }

        // API 31+ requires BLUETOOTH_CONNECT before touching the adapter / request flow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasConnect = ActivityCompat.checkSelfPermission(
                act,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasConnect) {
                ActivityCompat.requestPermissions(
                    act,
                    arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT),
                    REQ_BLE_PERMISSIONS
                )
                // Wait for onMainRequestPermissionsResult to continue
                return
            }
        }
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

        // Make sure this runs on the UI thread
        act.runOnUiThread {
            try {
                act.startActivityForResult(intent, REQUEST_ENABLE_BT)
            } catch (se: SecurityException) {
                Timber.e("Security exception when requesting Bluetooth enable: ${se.message}")
                emitSignal(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, BLUETOOTH_DISABLED)
            }
        }
    }

    /**
     * Callback for the Bluetooth request launcher
     */
    override fun onMainActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                emitSignal(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, BLUETOOTH_ENABLED)
            } else {
                emitSignal(SIGNAL_BLUETOOTH_ENABLE_REQUEST_COMPLETED, BLUETOOTH_DISABLED)
            }
        }
    }

    /**
     * Checks if required Bluetooth permissions are granted and makes a request if not
     */
    @UsedByGodot
    fun ensureBlePermissions(): Boolean {
        val act = activity ?: return false

        // If we're below runtime permission APIs, nothing to do
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        if (BluetoothHandler.centralManager.permissionsGranted()) {
            return true
        }

        val missing = BluetoothHandler.centralManager.getMissingPermissions()
        if (missing.isEmpty()) {
            return true
        }

        // Request missing permissions on UI thread
        act.runOnUiThread {
            ActivityCompat.requestPermissions(
                act,
                missing,
                REQ_BLE_PERMISSIONS
            )
        }

        // Not granted yet – wait for callback
        return false
    }

    /**
     * Checks if a permission has been provided for the current activity or not.
     * @param permissionCode : Integer code for the permissions
     */
    @UsedByGodot
    fun checkPermission(permissionCode: Int) : Int {
        val permissionString = mPermissions[permissionCode] ?: return PERMISSION_CODE_UNAVAILABLE
        return checkPermissionString(permissionString)
    }

    /**
     * Checks if a permission has been provided for the current activity or not.
     * @param permission : String name of the permission. See [Manifest.permission]
     */
    @UsedByGodot
    fun checkPermissionString(permission: String) : Int {
        return when (currentActivity.checkSelfPermission(permission)) {
            PackageManager.PERMISSION_GRANTED -> {

                PERMISSION_RESULT_GRANTED
            }
            else -> {
                val showRationale = currentActivity.shouldShowRequestPermissionRationale(permission)
                if (showRationale)
                    PERMISSION_RESULT_DENIED_SHOW_RATIONALE
                else
                    PERMISSION_RESULT_DENIED
            }
        }
    }

    /**
     * Launches the permission request launcher for the given permission code
     * @param permissionCode one of the defined code for permission
     */
    @UsedByGodot
    fun requestPermission(permissionCode: Int):Int {
        val permissionString = mPermissions[permissionCode] ?: return PERMISSION_CODE_UNAVAILABLE
        requestPermissionString(permissionString)
        return PERMISSION_CODE_OK
    }

    /**
     * Launches the permission request launcher for the given permission string
     * @param permission one of string value as specified in [Manifest.permission]
     */
    @UsedByGodot
    fun requestPermissionString(permission: String) {
        currentActivity.requestPermissions(arrayOf(permission), REQ_BLE_PERMISSIONS)
    }


    /**
     * Callback for the Permission request launcher
     */
    override fun onMainRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        super.onMainRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_BLE_PERMISSIONS && permissions != null && permissions.isNotEmpty()){

            // iterate through all permissions requested
            for (i in permissions.indices) {
                val requestedPermission = permissions[i]
                val permissionCode = if (mPermissions.containsValue(requestedPermission)){
                    mPermissions.keys.first { requestedPermission == mPermissions[it]}
                }else 0
                if (grantResults?.get(i) == PackageManager.PERMISSION_GRANTED)

                    emitSignal(SIGNAL_PERMISSION_REQUEST_COMPLETED,
                        permissionCode, requestedPermission, PERMISSION_RESULT_GRANTED)
                else

                    emitSignal(SIGNAL_PERMISSION_REQUEST_COMPLETED,
                        permissionCode, requestedPermission, PERMISSION_RESULT_DENIED)
            }
        }
    }
}
