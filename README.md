# Godot Android BLE

A Godot plugin for communicating with Bluetooth Low Engergy devices on Android.

Since there is no cross-platform BLE plugin available for Godot, I thought I would take a shot at writing an Android one. There are a handful of cycling apps available for the Quest 2 but most of them require subscriptions and the one freemium one I tried was basically a VR video that plays back at a varying speed. I think there's room for improvement. 360 degree video on low-end VR hardware still doesn't look very impressive and moving along a fixed track feels very limiting. Why not make a 3D-rendered world where you can actually turn the handlebars and go wherever you want? Movement should be fairly simple to extrapolate by reading the revolutions from a simple BLE cadence sensor but I've yet to find a good example for this particular platform. Sometimes you have to do it yourself.

After a few fits and starts, I found [this very helpful set of articles](https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02) so I'm going to use the [BLESSED](https://github.com/weliem/blessed-android) library as a starting point.