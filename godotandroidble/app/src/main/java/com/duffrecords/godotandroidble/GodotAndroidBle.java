package com.duffrecords.godotandroidble;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.SignalInfo;
import org.godotengine.godot.plugin.UsedByGodot;
import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.collection.ArraySet;
import android.widget.TextView;

import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothPeripheral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

// import timber.log.Timber;

public class GodotAndroidBle extends GodotPlugin {

    private boolean initialized = false;
    protected Activity activity = null;
    private Handler localHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;
    private static final String TAG = "GodotAndroidBle";

    private LocationServiceStateReceiver locationServiceStateReceiver;
    private CyclingSpeedCadenceDataReceiver cyclingSpeedCadenceDataReceiver;
    private BloodPressureDataReceiver bloodPressureDataReceiver;
    private TemperatureDataReceiver temperatureDataReceiver;
    private HeartRateDataReceiver heartRateDataReceiver;
    private PulseOxDataReceiver pulseOxDataReceiver;
    private WeightDataReceiver weightDataReceiver;
    private GlucoseDataReceiver glucoseDataReceiver;

    public GodotAndroidBle(Godot godot) {
        super(godot);
        activity = getActivity();
        localHandler = null;
    }

    @Override
    public View onMainCreate(Activity activity) {
        Log.i(TAG, "in onMainCreate");
        // if (GodotAndroidBleUtil.shouldSetupBle(activity)) {
        //     GodotAndroidBleUtil.setupBle(activity, activity.getIntent());
        // }
        activity.registerReceiver(locationServiceStateReceiver, new IntentFilter((LocationManager.MODE_CHANGED_ACTION)));
        activity.registerReceiver(cyclingSpeedCadenceDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_CYCLING));
        activity.registerReceiver(bloodPressureDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_BLOODPRESSURE));
        activity.registerReceiver(temperatureDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_TEMPERATURE));
        activity.registerReceiver(heartRateDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_HEARTRATE));
        activity.registerReceiver(pulseOxDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_PULSE_OX));
        activity.registerReceiver(weightDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_WEIGHT));
        activity.registerReceiver(glucoseDataReceiver, new IntentFilter(BluetoothHandler.MEASUREMENT_GLUCOSE));
        Log.i(TAG, "registered receivers");
        return null;
    }

    @NonNull
    @Override
    public String getPluginName() {
        return "GodotAndroidBle";
    }

    @NonNull
    @Override
    public Set<SignalInfo> getPluginSignals() {
        Log.i(TAG, "in getPluginSignals");
        Set<SignalInfo> signals = new ArraySet<>();

        // signals.add(new SignalInfo("on_disconnected", String.class, String.class));
        // signals.add(new SignalInfo("on_data_received_string", Object.class));
        signals.add(new SignalInfo("status_logged", String.class));
        signals.add(new SignalInfo("on_cycling_measurement", Integer.class, Integer.class));
        signals.add(new SignalInfo("on_blood_pressure_measurement", Object.class));
        signals.add(new SignalInfo("on_temperature_measurement", Object.class));
        signals.add(new SignalInfo("on_heart_rate_measurement", Object.class));
        signals.add(new SignalInfo("on_pulse_ox_continuous_measurement", Object.class));
        signals.add(new SignalInfo("on_pulse_ox_spot_measurement", Object.class));
        signals.add(new SignalInfo("on_weight_measurement", Object.class));
        signals.add(new SignalInfo("on_glucose_measurement", Object.class));
        // signals.add(new SignalInfo("on_single_device_found", String.class, String.class, String.class));
//        signals.add(new SignalInfo("on_disconnected_from_pair"));
        // signals.add(new SignalInfo("on_connected", String.class, String.class));
        // signals.add(new SignalInfo("on_connected_error"));
        // signals.add(new SignalInfo("on_received_connection", String.class, String.class));
        // signals.add(new SignalInfo("on_getting_uuid", String.class));
        // signals.add(new SignalInfo("on_devices_found", Object.class, Object.class));

        return signals;
    }

    @Override
    public void onMainResume() {
        super.onMainResume();

        if (getBluetoothManager().getAdapter() != null) {
            Log.i(TAG, "in onMainResume");
            // emitSignal("status_logged", "in onMainResume");
            if (!isBluetoothEnabled()) {
                Log.i(TAG, "onMainResume: bluetooth is not enabled");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int targetSdkVersion = activity.getApplicationInfo().targetSdkVersion;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "onMainResume: BLUETOOTH_CONNECT permission not granted");
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG, "onMainResume: BLUETOOTH_ADMIN permission not granted");
                        return;
                    }
                    // activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            } else {
                checkPermissions();
            }
        } else {
            Log.e(TAG, "This device has no Bluetooth hardware");
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = getBluetoothManager().getAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }

    private void initBluetoothHandler()
    {
        Log.i(TAG, "in initBluetoothHandler");
        BluetoothHandler.getInstance(activity.getApplicationContext());
    }

    @NotNull
    private BluetoothManager getBluetoothManager() {
        return Objects.requireNonNull((BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE),"cannot get BluetoothManager");
    }

    private BluetoothPeripheral getPeripheral(String peripheralAddress) {
        BluetoothCentralManager central = BluetoothHandler.getInstance(activity.getApplicationContext()).central;
        return central.getPeripheral(peripheralAddress);
    }

    @Override
    public void onMainDestroy() {
        super.onMainDestroy();
        activity.unregisterReceiver(locationServiceStateReceiver);
        activity.unregisterReceiver(cyclingSpeedCadenceDataReceiver);
        activity.unregisterReceiver(bloodPressureDataReceiver);
        activity.unregisterReceiver(temperatureDataReceiver);
        activity.unregisterReceiver(heartRateDataReceiver);
        activity.unregisterReceiver(pulseOxDataReceiver);
        activity.unregisterReceiver(weightDataReceiver);
        activity.unregisterReceiver(glucoseDataReceiver);
    }

    private class LocationServiceStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                boolean isEnabled = areLocationServicesEnabled();
                // Timber.i("Location service state changed to: %s", isEnabled ? "on" : "off");
                if (isEnabled) {
                    Log.i(TAG, "Location service state changed to: on");
                } else {
                    Log.i(TAG, "Location service state changed to: off");
                }
                checkPermissions();
            }
        }
    };

    private class CyclingSpeedCadenceDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in CyclingSpeedCadenceDataReceiver");
            CyclingCadenceMeasurement measurement = (CyclingCadenceMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_CYCLING_EXTRA);
            if (measurement == null) return;

            emitSignal("on_cycling_measurement", measurement.revs, measurement.last);
            // Timber.i(String.format(Locale.ENGLISH, "%d revolutions", measurement.revs));
            Log.i(TAG, String.format(Locale.ENGLISH, "%d revolutions", measurement.revs));
            // measurementValue.setText(String.format(Locale.ENGLISH, "%d revolutions", measurement.revs));
        }
    };

    private class BloodPressureDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in BloodPressureDataReceiver");
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            BloodPressureMeasurement measurement = (BloodPressureMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_BLOODPRESSURE_EXTRA);
            if (measurement == null) return;

            emitSignal("on_blood_pressure_measurement", new Object[]{measurement});
            // measurementValue.setText(String.format(Locale.ENGLISH, "%.0f/%.0f %s, %.0f bpm\n%s\n\nfrom %s", measurement.systolic, measurement.diastolic, measurement.isMMHG ? "mmHg" : "kpa", measurement.pulseRate, dateFormat.format(measurement.timestamp), peripheral.getName()));
        }
    };

    private class TemperatureDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in TemperatureDataReceiver");
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            TemperatureMeasurement measurement = (TemperatureMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_TEMPERATURE_EXTRA);
            if (measurement == null) return;

            emitSignal("on_temperature_measurement", new Object[]{measurement});
            // measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s (%s)\n%s\n\nfrom %s", measurement.temperatureValue, measurement.unit == TemperatureUnit.Celsius ? "celsius" : "fahrenheit", measurement.type, dateFormat.format(measurement.timestamp), peripheral.getName()));
        }
    };

    private class HeartRateDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in HeartRateDataReceiver");
            HeartRateMeasurement measurement = (HeartRateMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_HEARTRATE_EXTRA);
            if (measurement == null) return;

            emitSignal("on_heart_rate_measurement", new Object[]{measurement});
            // measurementValue.setText(String.format(Locale.ENGLISH, "%d bpm", measurement.pulse));
        }
    };

    private class PulseOxDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in PulseOxDataReceiver");
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            PulseOximeterContinuousMeasurement measurement = (PulseOximeterContinuousMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_PULSE_OX_EXTRA_CONTINUOUS);
            if (measurement != null) {
                emitSignal("on_pulse_ox_continuous_measurement", new Object[]{measurement});
                // measurementValue.setText(String.format(Locale.ENGLISH, "SpO2 %d%%,  Pulse %d bpm\n\nfrom %s", measurement.getSpO2(), measurement.getPulseRate(), peripheral.getName()));
            }
            PulseOximeterSpotMeasurement spotMeasurement = (PulseOximeterSpotMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_PULSE_OX_EXTRA_SPOT);
            if (spotMeasurement != null) {
                emitSignal("on_pulse_ox_spot_measurement", new Object[]{measurement});
                // measurementValue.setText(String.format(Locale.ENGLISH, "SpO2 %d%%,  Pulse %d bpm\n%s\n\nfrom %s", spotMeasurement.getSpO2(), spotMeasurement.getPulseRate(), dateFormat.format(spotMeasurement.getTimestamp()), peripheral.getName()));
            }
        }
    };

    private class WeightDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in WeightDataReceiver");
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            WeightMeasurement measurement = (WeightMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_WEIGHT_EXTRA);
            if (measurement != null) {
                emitSignal("on_weight_measurement", new Object[]{measurement});
                // measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s\n%s\n\nfrom %s", measurement.weight, measurement.unit.toString(), dateFormat.format(measurement.timestamp), peripheral.getName()));
            }
        }
    };

    private class GlucoseDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in GlucoseDataReceiver");
            BluetoothPeripheral peripheral = getPeripheral(intent.getStringExtra(BluetoothHandler.MEASUREMENT_EXTRA_PERIPHERAL));
            GlucoseMeasurement measurement = (GlucoseMeasurement) intent.getSerializableExtra(BluetoothHandler.MEASUREMENT_GLUCOSE_EXTRA);
            if (measurement != null) {
                emitSignal("on_glucose_measurement", new Object[]{measurement});
                // measurementValue.setText(String.format(Locale.ENGLISH, "%.1f %s\n%s\n\nfrom %s", measurement.value, measurement.unit == GlucoseMeasurementUnit.MmolPerLiter ? "mmol/L" : "mg/dL", dateFormat.format(measurement.timestamp), peripheral.getName()));
            }
        }
    };

    protected void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "checking permissions");
            emitSignal("status_logged", "checking permissions");
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                Log.i(TAG, "missing permissions: " + String.join(", ", missingPermissions));
                activity.requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);
            } else {
                permissionsGranted();
            }
        }
    }

    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (activity.getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = activity.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && targetSdkVersion >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q) {
            return new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    // Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else return new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work for SDK < 31
        int targetSdkVersion = activity.getApplicationInfo().targetSdkVersion;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && targetSdkVersion < Build.VERSION_CODES.S) {
            if (checkLocationServices()) {
                initBluetoothHandler();
            }
        } else {
            initBluetoothHandler();
        }
    }

    protected boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) activity.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG, "could not get location manager");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return locationManager.isLocationEnabled();
        } else {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            return isGpsEnabled || isNetworkEnabled;
        }
    }

    private boolean checkLocationServices() {
        Log.i(TAG, "checking location services");
        if (!areLocationServicesEnabled()) {
            Log.i(TAG, "location services are not enabled");
            new AlertDialog.Builder(activity.getApplicationContext())
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            activity.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

}
