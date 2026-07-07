# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

A Godot Engine Android plugin for Bluetooth Low Energy (BLE) communication. It exposes a GDScript API for scanning and connecting to BLE health/fitness devices (heart rate monitors, blood pressure cuffs, glucose meters, weight scales, etc.) using the [BLESSED Kotlin library](https://github.com/weliem/blessed-android-coroutines).

## Build Commands

```bash
# Build the plugin (requires kotlinVersion parameter)
./gradlew --no-daemon -PkotlinVersion=2.2.21 \
  :app:assembleDebug :app:assembleRelease \
  :blessed:assembleDebug :blessed:assembleRelease

# Run unit tests
./gradlew :app:test

# Run Android instrumented tests (requires connected device/emulator)
./gradlew :app:connectedAndroidTest

# Deploy built AARs to a Godot project
./deploy.sh /path/to/your/godot/project
```

The `-PkotlinVersion` parameter selects which Kotlin version to build with; supported pairings with Godot versions are in `.github/versions.json`.

## Architecture

### Module Structure

- **`app/`** — The Godot plugin itself. Produces AARs that get bundled into exported APKs.
- **`blessed/`** — Wraps the BLESSED BLE Kotlin library as a separate AAR dependency.
- **`peripheral/`** — Example peripheral (BLE server) app using Jetpack Compose.
- **`godot-lib/`** — Houses the Godot Engine AAR (downloaded per version during CI).

### Plugin Entry Point

`app/src/main/java/com/duffrecords/godotandroidble/GodotAndroidBle.kt` is the main class. It:
- Implements Godot's `GodotPlugin` interface
- Exposes scanning/connection methods via `@UsedByGodot` annotations (callable from GDScript)
- Runs BLE operations on a dedicated `HandlerThread` ("Blessed")
- Emits 22 Godot signals for device events and measurement data

**Scan/connect flow:** Scanning emits a `bluetooth_device_found` signal for every discovered device and continues until stopped. GDScript collects these into a device list for the user to choose from, then calls `connectToDevice(address)` which stops the scan and initiates the connection.

### Measurement Parsing Pattern

Each supported BLE profile has a corresponding `*Measurement.kt` data class:
- `fromBytes(ByteArray)` — parses raw BLE characteristic bytes using BLESSED's `BluetoothBytesParser`
- `toDictionary()` — converts to a `Dictionary` for GDScript consumption

Adding a new measurement type means creating this data class and wiring it into `GodotAndroidBle.kt`.

### GDScript Side

- **`ble_permissions_manager.gd`** — AutoLoad singleton that handles runtime `BLUETOOTH_SCAN`/`BLUETOOTH_CONNECT` permissions (Android 12+) with a queue-based flow.
- **`export_scripts/export_plugin.gd`** — `EditorExportPlugin` that tells Godot to include the plugin AARs on Android export.

### Supported BLE Services

Blood Pressure (BLP), Heart Rate (HRS), Health Thermometer (HTS), Glucose, Pulse Oximeter (PLX), Weight Scale (WSS), Cycling Speed & Cadence (CSC), Cycling Power (CPS), Running Speed & Cadence (RSC), plus Battery and Device Information services.

### CI/CD

`.github/workflows/build-and-release.yml` triggers on `v*.*.*` tags. It reads `.github/versions.json` to build against each supported Godot version, then creates a GitHub Release with a ZIP per Godot version (e.g., `godotandroidble-godot-4.6.2.zip`).

## Key Constraints

- **Minimum Android API 28** (Android 9). Bluetooth permissions differ significantly below API 31.
- **One active scan at a time** — the plugin enforces this; queue or cancel before starting a new scan.
- **Automatic bonding** is triggered only for devices whose names match `"Contour*"` or `"A&D*"`.
- The `godot-lib` module does not vendor the Godot AAR — it is downloaded during the CI build for the target Godot version.
