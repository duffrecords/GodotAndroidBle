# Godot Android BLE

A Godot plugin for communicating with Bluetooth Low Engergy devices on Android.

Since there is no cross-platform BLE plugin available for Godot, I thought I would take a shot at writing an Android one. There are a handful of cycling apps available for the Quest 2 but most of them require subscriptions and the one freemium one I tried was basically a VR video that plays back at a varying speed. I think there's room for improvement. 360 degree video on low-end VR hardware still doesn't look very impressive and moving along a fixed track feels very limiting. Why not make a 3D-rendered world where you can actually turn the handlebars and go wherever you want? Movement should be fairly simple to extrapolate by reading the revolutions from a simple BLE cadence sensor but I've yet to find a good example for this particular platform. Sometimes you have to do it yourself.

After a few fits and starts, I found [this very helpful set of articles](https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02) so I'm going to use the [BLESSED](https://github.com/weliem/blessed-android) library as a starting point.

## Installation
Go to the [Releases](https://github.com/duffrecords/GodotAndroidBle/releases) page and download the latest one that corresponds with your Godot version. The v2 Android plugin format changed in Godot 4.2, so I'm focusing on the most recent Godot 4 releases. Extract the archive into your project's `addons/` folder (create it if it doesn't exist). The folder structure should look like this:

```bash
res://
├── addons
│   └── godotandroidble
│       ├── bin
│       ├── export_plugin.gd
│       └── plugin.cfg
```

## Building the plugin
Optionally, you can build the plugin yourself by downloading the appropriate Godot library to the godot-lib folder and running the following command:
```bash
./gradlew --no-daemon \
  -PkotlinVersion="${KOTLIN_VERSION}" \
  :app:assembleDebug :app:assembleRelease \
  :blessed:assembleDebug :blessed:assembleRelease
```
Then run the installation script to copy the plugin to your project:
```bash
./deploy.sh /path/to/your/project
```

## Usage
Enable the plugin in your project settings and copy the `bluetooth_manager.gd` script to your project. Now you can access the plugin via GDScript like this:
```python
@onready var bluetooth_manager = BluetoothManager.new()

var _plugin_singleton: JNISingleton
var _plugin_name: String = "godotandroidble"
var bt_permissions_granted = false

func _ready() -> void:
	if Engine.has_singleton(_plugin_name):
		_plugin_singleton = Engine.get_singleton(_plugin_name)
		_plugin_singleton.initPlugin()
    	_plugin_singleton.connect("plugin_message", _on_plugin_message_received)
    	_plugin_singleton.connect("permission_required", _on_permission_required)
    	_plugin_singleton.connect("bluetooth_device_found", _on_device_found)
    	_plugin_singleton.connect("bluetooth_device_connected", _on_device_connected)
    	_plugin_singleton.connect("bluetooth_device_disconnected", _on_device_disconnected)
		print("checking Bluetooth permissions")
		bt_permissions_granted = bluetooth_manager.has_permissions(_plugin_singleton)
		print("Bluetooth permissions are set: " + str(bt_permissions_granted))
	elif OS.has_feature("template"):
		printerr(_plugin_name, " singleton not found!")
```

The BluetoothManager object can request `android.permission.BLUETOOTH_SCAN` and `android.permission.BLUETOOTH_CONNECT` at runtime (these are required in Android API 31 and higher) and this will create a popup in the UI to prompt the user to accept. It should send a `on_request_permissions_result` signal once this is complete but this doesn't seem to be working yet. One workaround is to create a button in your UI that connects to a function that checks and requests these permissions
```python
bt_permissions_granted = bluetooth_manager.has_permissions(_plugin_singleton)
```
and then make the button invisible once `bt_permissions_granted` is true.

Once you have permissions sorted out, you can scan for one of these BLE service types:
```python
_plugin_singleton.scanForBlpService()     # blood pressure measurement
_plugin_singleton.scanForGlucoseService() # glucose measurement
_plugin_singleton.scanForHrsService()     # heart rate measurement
_plugin_singleton.scanForPlxService()     # pulse oximeter measurement
_plugin_singleton.scanForWssService()     # weight measurement
_plugin_singleton.scanForCscService()     # cycling speed and cadence measurement
_plugin_singleton.scanForCpService()      # cycling power measurement
_plugin_singleton.scanForRscService()     # running speed and cadence measurement
```
The scan will time out eventually but only one type of scan can run at a time so run `_plugin_singleton().stopScanning()` before attempting another one. If a BLE device is found, a `bluetooth_device_found` signal will be sent and you can handle that in your script. This signal includes a dictionary with the following fields:
```python
{
    "name": string,    # device name
    "address": string, # MAC address
    "rssi": int        # signal strength
}
```

The plugin will automatically attempt to connect to the discovered device and, if successful, send a `bluetooth_device_connected` signal, along with the name and MAC address of the device. It will request notifications from the device, which will send an associated signal containing a dictionary of measurement data. Here is a list of the measurement signals:

* blood_pressure_measurement_received
* glucose_measurement_received
* heart_rate_measurement_received
* pulse_oximeter_continuous_measurement_received
* pulse_oximeter_spot_measurement_received
* temperature_measurement_received
* weight_measurement_received
* cycling_cadence_measurement_received
* cycling_power_measurement_received
* running_cadence_measurement_received

Each device type is different and not all fields are required, so you'll have to examine the dictionary or take a look at the source code in `app/src/main/java/com/duffrecords/godotandroidble` for more details on the measurement data. For example, the cycling speed and cadence measurement might look like this:
```python
{
    "cumulative_crank_revs": int,  # number of crank revolutions since the device started
    "last_crank_event_time": int   # free-running count of 1/1024 second units since the last event
}
```
