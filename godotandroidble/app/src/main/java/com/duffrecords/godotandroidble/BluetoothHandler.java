package com.duffrecords.godotandroidble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.BondState;
import com.welie.blessed.ConnectionPriority;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import com.welie.blessed.ScanFailure;
import com.welie.blessed.WriteType;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

// import timber.log.Timber;

import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_WRITE;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_SINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT16;
import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;
import static com.welie.blessed.BluetoothBytesParser.bytes2String;

import static java.lang.Math.abs;

class BluetoothHandler {

    // Intent constants
    public static final String MEASUREMENT_CYCLING = "blessed.measurement.cycling";
    public static final String MEASUREMENT_CYCLING_EXTRA = "blessed.measurement.cycling.extra";
    // public static final String MEASUREMENT_RUNNING = "blessed.measurement.running";
    public static final String MEASUREMENT_BLOODPRESSURE = "blessed.measurement.bloodpressure";
    public static final String MEASUREMENT_BLOODPRESSURE_EXTRA = "blessed.measurement.bloodpressure.extra";
    public static final String MEASUREMENT_TEMPERATURE = "blessed.measurement.temperature";
    public static final String MEASUREMENT_TEMPERATURE_EXTRA = "blessed.measurement.temperature.extra";
    public static final String MEASUREMENT_HEARTRATE = "blessed.measurement.heartrate";
    public static final String MEASUREMENT_HEARTRATE_EXTRA = "blessed.measurement.heartrate.extra";
    public static final String MEASUREMENT_GLUCOSE = "blessed.measurement.glucose";
    public static final String MEASUREMENT_GLUCOSE_EXTRA = "blessed.measurement.glucose.extra";
    public static final String MEASUREMENT_PULSE_OX = "blessed.measurement.pulseox";
    public static final String MEASUREMENT_PULSE_OX_EXTRA_CONTINUOUS = "blessed.measurement.pulseox.extra.continuous";
    public static final String MEASUREMENT_PULSE_OX_EXTRA_SPOT = "blessed.measurement.pulseox.extra.spot";
    public static final String MEASUREMENT_WEIGHT = "blessed.measurement.weight";
    public static final String MEASUREMENT_WEIGHT_EXTRA = "blessed.measurement.weight.extra";
    public static final String MEASUREMENT_EXTRA_PERIPHERAL = "blessed.measurement.peripheral";

    // UUIDs for the Cadence service (HRS)
    private static final UUID CSC_SERVICE_UUID = UUID.fromString("00001816-0000-1000-8000-00805f9b34fb");
    // private static final UUID RSC_SERVICE_UUID = UUID.fromString("00001814-0000-1000-8000-00805f9b34fb");
    private static final UUID CYCLING_SPEED_CADENCE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A5B-0000-1000-8000-00805f9b34fb");
    // private static final UUID CYCLING_SPEED_CADENCE_FEATURE_CHARACTERISTIC_UUID = UUID.fromString("00002A5C-0000-1000-8000-00805f9b34fb");
    // private static final UUID CYCLING_SPEED_CADENCE_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A5D-0000-1000-8000-00805f9b34fb");
    // private static final UUID RUNNING_SPEED_CADENCE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A53-0000-1000-8000-00805f9b34fb");
    // private static final UUID RUNNING_SPEED_CADENCE_FEATURE_CHARACTERISTIC_UUID = UUID.fromString("00002A54-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Blood Pressure service (BLP)
    private static final UUID BLP_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
    private static final UUID BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A35-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Health Thermometer service (HTS)
    private static final UUID HTS_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
    private static final UUID TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");
    private static final UUID PNP_ID_CHARACTERISTIC_UUID = UUID.fromString("00002A50-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Heart Rate service (HRS)
    private static final UUID HRS_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
    private static final UUID HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Device Information service (DIS)
    private static final UUID DIS_SERVICE_UUID = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    private static final UUID MANUFACTURER_NAME_CHARACTERISTIC_UUID = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    private static final UUID MODEL_NUMBER_CHARACTERISTIC_UUID = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Current Time service (CTS)
    private static final UUID CTS_SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    private static final UUID CURRENT_TIME_CHARACTERISTIC_UUID = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Battery Service (BAS)
    private static final UUID BTS_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Pulse Oximeter Service (PLX)
    public static final UUID PLX_SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    private static final UUID PLX_SPOT_MEASUREMENT_CHAR_UUID = UUID.fromString("00002a5e-0000-1000-8000-00805f9b34fb");
    private static final UUID PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID = UUID.fromString("00002a5f-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Weight Scale Service (WSS)
    public static final UUID WSS_SERVICE_UUID = UUID.fromString("0000181D-0000-1000-8000-00805f9b34fb");
    private static final UUID WSS_MEASUREMENT_CHAR_UUID = UUID.fromString("00002A9D-0000-1000-8000-00805f9b34fb");

    public static final UUID GLUCOSE_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
    public static final UUID GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
    public static final UUID GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID = UUID.fromString("00002A52-0000-1000-8000-00805f9b34fb");
    public static final UUID GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");

    // Contour Glucose Service
    public static final UUID CONTOUR_SERVICE_UUID = UUID.fromString("00000000-0002-11E2-9E96-0800200C9A66");
    private static final UUID CONTOUR_CLOCK = UUID.fromString("00001026-0002-11E2-9E96-0800200C9A66");

    // Local variables
    public BluetoothCentralManager central;
    private static BluetoothHandler instance = null;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentTimeCounter = 0;
    private static final String TAG = "BluetoothHandler";

    // Callback for peripherals
    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            // Request a higher MTU, iOS always asks for 185
            peripheral.requestMtu(185);

            // Request a new connection priority
            peripheral.requestConnectionPriority(ConnectionPriority.HIGH);

            // Read manufacturer and model number from the Device Information Service
            peripheral.readCharacteristic(DIS_SERVICE_UUID, MANUFACTURER_NAME_CHARACTERISTIC_UUID);
            peripheral.readCharacteristic(DIS_SERVICE_UUID, MODEL_NUMBER_CHARACTERISTIC_UUID);

            // Turn on notifications for Current Time Service and write it if possible
            BluetoothGattCharacteristic currentTimeCharacteristic = peripheral.getCharacteristic(CTS_SERVICE_UUID, CURRENT_TIME_CHARACTERISTIC_UUID);
            if (currentTimeCharacteristic != null) {
                peripheral.setNotify(currentTimeCharacteristic, true);

                // If it has the write property we write the current time
                if ((currentTimeCharacteristic.getProperties() & PROPERTY_WRITE) > 0) {
                    // Write the current time unless it is an Omron device
                    if (!isOmronBPM(peripheral.getName())) {
                        BluetoothBytesParser parser = new BluetoothBytesParser();
                        parser.setCurrentTime(Calendar.getInstance());
                        peripheral.writeCharacteristic(currentTimeCharacteristic, parser.getValue(), WriteType.WITH_RESPONSE);
                    }
                }
            }

            // Try to turn on notifications for other characteristics
            peripheral.readCharacteristic(BTS_SERVICE_UUID, BATTERY_LEVEL_CHARACTERISTIC_UUID);
            peripheral.setNotify(CSC_SERVICE_UUID, CYCLING_SPEED_CADENCE_MEASUREMENT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(BLP_SERVICE_UUID, BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(HTS_SERVICE_UUID, TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(HRS_SERVICE_UUID, HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(PLX_SERVICE_UUID, PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID, true);
            peripheral.setNotify(PLX_SERVICE_UUID, PLX_SPOT_MEASUREMENT_CHAR_UUID, true);
            peripheral.setNotify(WSS_SERVICE_UUID, WSS_MEASUREMENT_CHAR_UUID, true);
            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_MEASUREMENT_CONTEXT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, true);
            peripheral.setNotify(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK, true);
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                final boolean isNotifying = peripheral.isNotifying(characteristic);
                Log.i(TAG, String.format("SUCCESS: Notify set to '%s' for %s", isNotifying, characteristic.getUuid()));
                if (characteristic.getUuid().equals(CONTOUR_CLOCK)) {
                    writeContourClock(peripheral);
                } else if (characteristic.getUuid().equals(GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID)) {
                    writeGetAllGlucoseMeasurements(peripheral);
                }
            } else {
                Log.e(TAG, String.format("ERROR: Changing notification state failed for %s (%s)", characteristic.getUuid(), status));
            }
        }

        @Override
        public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                Log.i(TAG, String.format("SUCCESS: Writing <%s> to <%s>", bytes2String(value), characteristic.getUuid()));
            } else {
                Log.i(TAG, String.format("ERROR: Failed writing <%s> to <%s> (%s)", bytes2String(value), characteristic.getUuid(), status));
            }
        }

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status != GattStatus.SUCCESS) return;

            UUID characteristicUUID = characteristic.getUuid();
            BluetoothBytesParser parser = new BluetoothBytesParser(value);

            if (characteristicUUID.equals(CYCLING_SPEED_CADENCE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                CyclingCadenceMeasurement measurement = new CyclingCadenceMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_CYCLING);
                intent.putExtra(MEASUREMENT_CYCLING_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                BloodPressureMeasurement measurement = new BloodPressureMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_BLOODPRESSURE);
                intent.putExtra(MEASUREMENT_BLOODPRESSURE_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(TEMPERATURE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                TemperatureMeasurement measurement = new TemperatureMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_TEMPERATURE);
                intent.putExtra(MEASUREMENT_TEMPERATURE_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(HEARTRATE_MEASUREMENT_CHARACTERISTIC_UUID)) {
                HeartRateMeasurement measurement = new HeartRateMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_HEARTRATE);
                intent.putExtra(MEASUREMENT_HEARTRATE_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(PLX_CONTINUOUS_MEASUREMENT_CHAR_UUID)) {
                PulseOximeterContinuousMeasurement measurement = new PulseOximeterContinuousMeasurement(value);
                if (measurement.getSpO2() <= 100 && measurement.getPulseRate() <= 220) {
                    Intent intent = new Intent(MEASUREMENT_PULSE_OX);
                    intent.putExtra(MEASUREMENT_PULSE_OX_EXTRA_CONTINUOUS, measurement);
                    sendMeasurement(intent, peripheral);
                }
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(PLX_SPOT_MEASUREMENT_CHAR_UUID)) {
                PulseOximeterSpotMeasurement measurement = new PulseOximeterSpotMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_PULSE_OX);
                intent.putExtra(MEASUREMENT_PULSE_OX_EXTRA_SPOT, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(WSS_MEASUREMENT_CHAR_UUID)) {
                WeightMeasurement measurement = new WeightMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_WEIGHT);
                intent.putExtra(MEASUREMENT_WEIGHT_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals((GLUCOSE_MEASUREMENT_CHARACTERISTIC_UUID))) {
                GlucoseMeasurement measurement = new GlucoseMeasurement(value);
                Intent intent = new Intent(MEASUREMENT_GLUCOSE);
                intent.putExtra(MEASUREMENT_GLUCOSE_EXTRA, measurement);
                sendMeasurement(intent, peripheral);
                Log.d(TAG, String.format("%s", measurement));
            } else if (characteristicUUID.equals(CURRENT_TIME_CHARACTERISTIC_UUID)) {
                Date currentTime = parser.getDateTime();
                Log.i(TAG, String.format("Received device time: %s", currentTime));

                // Deal with Omron devices where we can only write currentTime under specific conditions
                if (isOmronBPM(peripheral.getName())) {
                    BluetoothGattCharacteristic bloodpressureMeasurement = peripheral.getCharacteristic(BLP_SERVICE_UUID, BLOOD_PRESSURE_MEASUREMENT_CHARACTERISTIC_UUID);
                    if (bloodpressureMeasurement == null) return;

                    boolean isNotifying = peripheral.isNotifying(bloodpressureMeasurement);
                    if (isNotifying) currentTimeCounter++;

                    // We can set device time for Omron devices only if it is the first notification and currentTime is more than 10 min from now
                    long interval = abs(Calendar.getInstance().getTimeInMillis() - currentTime.getTime());
                    if (currentTimeCounter == 1 && interval > 10 * 60 * 1000) {
                        parser.setCurrentTime(Calendar.getInstance());
                        peripheral.writeCharacteristic(characteristic, parser.getValue(), WriteType.WITH_RESPONSE);
                    }
                }
            } else if (characteristicUUID.equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
                int batteryLevel = parser.getIntValue(FORMAT_UINT8);
                Log.i(TAG, String.format("Received battery level %d%%", batteryLevel));
            } else if (characteristicUUID.equals(MANUFACTURER_NAME_CHARACTERISTIC_UUID)) {
                String manufacturer = parser.getStringValue(0);
                Log.i(TAG, String.format("Received manufacturer: %s", manufacturer));
            } else if (characteristicUUID.equals(MODEL_NUMBER_CHARACTERISTIC_UUID)) {
                String modelNumber = parser.getStringValue(0);
                Log.i(TAG, String.format("Received modelnumber: %s", modelNumber));
            } else if (characteristicUUID.equals(PNP_ID_CHARACTERISTIC_UUID)) {
                String modelNumber = parser.getStringValue(0);
                Log.i(TAG, String.format("Received pnp: %s", modelNumber));
            }
        }

        @Override
        public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
            Log.i(TAG, String.format("new MTU set: %d", mtu));
        }

        private void sendMeasurement(@NotNull Intent intent, @NotNull BluetoothPeripheral peripheral ) {
            intent.putExtra(MEASUREMENT_EXTRA_PERIPHERAL, peripheral.getAddress());
            context.sendBroadcast(intent);
            Log.d(TAG, "sent measurement broadcast");
        }

        private void writeContourClock(@NotNull BluetoothPeripheral peripheral) {
            Calendar calendar = Calendar.getInstance();
            int offsetInMinutes = calendar.getTimeZone().getRawOffset() / 60000;
            int dstSavingsInMinutes = calendar.getTimeZone().getDSTSavings() / 60000;
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
            BluetoothBytesParser parser = new BluetoothBytesParser(ByteOrder.LITTLE_ENDIAN);
            parser.setIntValue(1, FORMAT_UINT8);
            parser.setIntValue(calendar.get(Calendar.YEAR), FORMAT_UINT16);
            parser.setIntValue(calendar.get(Calendar.MONTH) + 1, FORMAT_UINT8);
            parser.setIntValue(calendar.get(Calendar.DAY_OF_MONTH), FORMAT_UINT8);
            parser.setIntValue(calendar.get(Calendar.HOUR_OF_DAY), FORMAT_UINT8);
            parser.setIntValue(calendar.get(Calendar.MINUTE), FORMAT_UINT8);
            parser.setIntValue(calendar.get(Calendar.SECOND), FORMAT_UINT8);
            parser.setIntValue(offsetInMinutes + dstSavingsInMinutes, FORMAT_SINT16);
            peripheral.writeCharacteristic(CONTOUR_SERVICE_UUID, CONTOUR_CLOCK, parser.getValue(), WriteType.WITH_RESPONSE);
        }

        private void writeGetAllGlucoseMeasurements(@NotNull BluetoothPeripheral peripheral) {
            byte OP_CODE_REPORT_STORED_RECORDS = 1;
            byte OPERATOR_ALL_RECORDS = 1;
            final byte[] command = new byte[] {OP_CODE_REPORT_STORED_RECORDS, OPERATOR_ALL_RECORDS};
            peripheral.writeCharacteristic(GLUCOSE_SERVICE_UUID, GLUCOSE_RECORD_ACCESS_POINT_CHARACTERISTIC_UUID, command, WriteType.WITH_RESPONSE);
        }
    };

    // Callback for central
    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {

        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Log.i(TAG, String.format("connected to '%s'", peripheral.getName()));
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Log.e(TAG, String.format("connection '%s' failed with status %s", peripheral.getName(), status));
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull final BluetoothPeripheral peripheral, final @NotNull HciStatus status) {
            Log.i(TAG, String.format("disconnected '%s' with status %s", peripheral.getName(), status));

            // Reconnect to this device when it becomes available again
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    central.autoConnectPeripheral(peripheral, peripheralCallback);
                }
            }, 5000);
        }

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            Log.i(TAG, String.format("Found peripheral '%s'", peripheral.getName()));
            central.stopScan();

            if (peripheral.getName().contains("Contour") && peripheral.getBondState() == BondState.NONE) {
                // Create a bond immediately to avoid double pairing popups
                central.createBond(peripheral, peripheralCallback);
            } else {
                central.connectPeripheral(peripheral, peripheralCallback);
            }
        }

        @Override
        public void onBluetoothAdapterStateChanged(int state) {
            Log.i(TAG, String.format("bluetooth adapter changed state to %d", state));
            if (state == BluetoothAdapter.STATE_ON) {
                // Bluetooth is on now, start scanning again
                // Scan for peripherals with a certain service UUIDs
                central.startPairingPopupHack();
                startScan();
            }
        }

        @Override
        public void onScanFailed(@NotNull ScanFailure scanFailure) {
            Log.i(TAG, String.format("scanning failed with error %s", scanFailure));
        }
    };

    public static synchronized BluetoothHandler getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothHandler(context.getApplicationContext());
        }
        return instance;
    }

    private BluetoothHandler(Context context) {
        this.context = context;

        // Plant a tree
        // Timber.plant(new Timber.DebugTree());

        // Create BluetoothCentral
        central = new BluetoothCentralManager(context, bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

        // Scan for peripherals with a certain service UUIDs
        central.startPairingPopupHack();
        startScan();
    }

    private void startScan() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // central.scanForPeripheralsWithServices(new UUID[]{CSC_SERVICE_UUID, BLP_SERVICE_UUID, HTS_SERVICE_UUID, HRS_SERVICE_UUID, PLX_SERVICE_UUID, WSS_SERVICE_UUID, GLUCOSE_SERVICE_UUID});
                central.scanForPeripheralsWithServices(new UUID[]{CSC_SERVICE_UUID});
            }
        },1000);
    }

    private boolean isOmronBPM(final String name) {
        return name.contains("BLESmart_") || name.contains("BLEsmart_");
    }
}
