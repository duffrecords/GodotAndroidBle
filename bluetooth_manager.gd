extends Node
class_name BluetoothManager 

var has_scan_premission
var scan_perm := "android.permission.BLUETOOTH_SCAN"

var has_connect_premission
var connect_perm := "android.permission.BLUETOOTH_CONNECT"


func _ready() -> void:
	get_tree().connect("on_request_permissions_result", Callable($BluetoothManager, "_on_permission_result"))

func has_permissions(ble_plugin) -> bool:
	print("granted permissions: " + str(OS.get_granted_permissions()))

	if not OS.get_granted_permissions().has(scan_perm):
		has_scan_premission = request_scan_permission()
		if not has_connect_premission:
			print("Unable to get scan permission")
			return false
		
	if not OS.get_granted_permissions().has(connect_perm):
		has_connect_premission = request_connect_permission()
		if not has_connect_premission:
			print("Unable to get connect permission")
			return false
		
	if not ble_plugin.isbluetoothReady():
		print("unable to initialize bluetooth")
		return false
	
	# permissions available and bluetooth initialized
	return true


func request_scan_permission():
	if not OS.get_granted_permissions().has(scan_perm):
		print("requesting ", scan_perm)
		OS.request_permission(scan_perm)
		return true


func request_connect_permission():
	if not OS.get_granted_permissions().has(connect_perm):
		print("requesting ", connect_perm)
		OS.request_permission(connect_perm)
		return true


func _on_permission_result(permission: String, granted: bool):
	print("Permission ", permission, " = ", granted)
	return granted
