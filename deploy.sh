#!/bin/sh

variant="debug"
if [ "$1" = "release" ]; then
    variant=$1
fi

cp -a blessed/build/outputs/aar/blessed-${variant}.aar ~/Documents/Godot/ble-test/addons/godotandroidble/bin/${variant}/
cp -a app/build/outputs/aar/godotandroidble-${variant}.aar ~/Documents/Godot/ble-test/addons/godotandroidble/bin/${variant}/

