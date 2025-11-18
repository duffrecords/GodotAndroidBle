#!/bin/sh

if [ $# -ne 1 ]; then
    printf "Usage: deploy.sh /path/to/godot/project\n"
    exit 1
fi

found=false
for variant in debug release; do
    if [ -f app/build/outputs/aar/godotandroidble-${variant}.aar ] && [ -f blessed/build/outputs/aar/blessed-${variant}.aar ]; then
        found=true
        mkdir -p "$1/addons/godotandroidble/bin/${variant}/"
        cp app/build/outputs/aar/godotandroidble-${variant}.aar "$1/addons/godotandroidble/bin/${variant}/"
        cp blessed/build/outputs/aar/blessed-${variant}.aar "$1/addons/godotandroidble/bin/${variant}/"
    fi
done
if [ "$found" = false ]; then
    printf "Error: Could not find built AAR files. Please build the project first.\n"
    rm -rf "$1/addons/godotandroidble"
    exit 1
fi
mkdir -p "$1/addons/godotandroidble"
cp export_scripts/export_plugin.gd "$1/addons/godotandroidble/"
cp export_scripts/plugin.cfg "$1/addons/godotandroidble/"
printf "GodotAndroidBLE deployed to %s/addons/godotandroidble/\n" "$1"
exit 0
