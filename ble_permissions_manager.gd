extends Node
class_name BlePermissionsManagerService
## Autoload singleton: Project Settings → Autoload → Path: this script, Name: BlePermissionsManager

signal permissions_done(all_granted: bool, results: Dictionary)

## Configure the exact permissions your app/library needs.
## Example for BLE (Android 12+): SCAN, CONNECT (add ADVERTISE if needed).
## For pre-Android 12 projects that still need scan-by-location, include ACCESS_FINE_LOCATION.
@export var required_permissions: PackedStringArray = [
	"android.permission.BLUETOOTH_SCAN",
    "android.permission.BLUETOOTH_CONNECT"
	# "android.permission.BLUETOOTH_ADVERTISE",
	# "android.permission.ACCESS_FINE_LOCATION",
]

var _in_flight := false
var _queue: Array[String] = []
var _results := {} # permission -> bool
var _waiting_set := {} # permission -> null/true/false

func _ready() -> void:
	if not get_tree().on_request_permissions_result.is_connected(_on_request_permissions_result):
		get_tree().on_request_permissions_result.connect(_on_request_permissions_result)

func has_all_permissions() -> bool:
	if not OS.has_feature("android"):
		return true
	var granted: PackedStringArray = OS.get_granted_permissions()
	for p in required_permissions:
		if not granted.has(p):
			return false
	return true

func missing_permissions() -> PackedStringArray:
	if not OS.has_feature("android"):
		return PackedStringArray()
	var missing := PackedStringArray()
	var granted: PackedStringArray = OS.get_granted_permissions()
	for p in required_permissions:
		if not granted.has(p):
			missing.append(p)
	return missing

## Call and optionally `await`:
##   var ok := await BlePermissionsManager.ensure_permissions()
func ensure_permissions() -> bool:
	if not OS.has_feature("android"):
		return true

	var missing := missing_permissions()
	if missing.is_empty():
		_results.clear()
		for p in required_permissions:
			_results[p] = true
		call_deferred("_emit_done", true, _results.duplicate())
		print("all permissions are granted")
		return true

	var result: Array
	var ok: bool
	if _in_flight:
		result = await permissions_done
		ok = result[0]
		return ok

	_in_flight = true
	_results.clear()

	# Request only the ones we care about (multiple dialogs, Android limitation).
	_queue = []
	for p in missing:
		_queue.append(p)
	_waiting_set.clear()
	for p in _queue:
		_waiting_set[p] = null
	_request_next_in_queue()

	result = await permissions_done
	ok = result[0]
	return ok

func _request_next_in_queue() -> void:
	if _queue.is_empty():
		_finish_if_done()
		return
	var next = _queue.pop_front()
	if next:
		print("requesting " + next + " permission")
		OS.request_permission(next) # shows a dialog for this specific permission

func _on_request_permissions_result(permission: String, granted: bool) -> void:
	# We’ll only track results for permissions we care about.
	print("permission " + permission + " = " + str(granted))
	if not _waiting_set.has(permission):
		return

	_waiting_set[permission] = granted
	_results[permission] = granted

	_request_next_in_queue()

func _finish_if_done() -> void:
	# Check if any of our tracked permissions are still pending.
	for k in _waiting_set.keys():
		if _waiting_set[k] == null:
			return
	var all_ok := true
	for k in _waiting_set.keys():
		if not bool(_waiting_set[k]):
			all_ok = false
			break
	_in_flight = false
	print("sending permissions_done signal")
	call_deferred("_emit_done", all_ok, _results.duplicate())
	# Disconnect signal since it is no longer needed
	get_tree().on_request_permissions_result.disconnect(_on_request_permissions_result)

func _emit_done(all_ok: bool, results: Dictionary) -> void:
	emit_signal("permissions_done", all_ok, results)
