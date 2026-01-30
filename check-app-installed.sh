#!/bin/bash

# Script to check if the capture app is installed on a connected Android device

PACKAGE_NAME="com.chats.capture"
LAUNCHER_COMPONENT="com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity"

echo "=========================================="
echo "App Installation Check"
echo "=========================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected or device not authorized"
    echo "Run: adb devices"
    exit 1
fi

echo "📱 Device connected"
echo ""

# Check if package is installed
echo "Checking if package is installed..."
if adb shell pm list packages | grep -q "^package:$PACKAGE_NAME$"; then
    echo "✅ Package is installed: $PACKAGE_NAME"
    echo ""
    
    # Get package info
    echo "Package Information:"
    adb shell dumpsys package $PACKAGE_NAME | grep -E "versionName|versionCode|userId" | head -5
    echo ""
    
    # Check component state
    echo "Checking launcher component state..."
    COMPONENT_STATE=$(adb shell pm get-component-enabled-setting $LAUNCHER_COMPONENT 2>/dev/null | grep "newState" | awk '{print $2}')
    
    if [ -z "$COMPONENT_STATE" ]; then
        echo "⚠️  Could not get component state"
    else
        case $COMPONENT_STATE in
            0)
                echo "Component State: DEFAULT (0) - Enabled by default (VISIBLE)"
                ;;
            1)
                echo "Component State: ENABLED (1) - Explicitly enabled (VISIBLE)"
                ;;
            2)
                echo "✅ Component State: DISABLED (2) - Explicitly disabled (HIDDEN)"
                ;;
            3)
                echo "Component State: DISABLED_USER (3) - Disabled by user"
                ;;
            4)
                echo "Component State: DISABLED_UNTIL_USED (4) - Disabled until used"
                ;;
            *)
                echo "Component State: UNKNOWN ($COMPONENT_STATE)"
                ;;
        esac
        
        if [ "$COMPONENT_STATE" = "2" ]; then
            echo ""
            echo "✅ App is HIDDEN from launcher"
        else
            echo ""
            echo "❌ App is VISIBLE in launcher"
            echo ""
            echo "To hide it, run:"
            echo "  adb shell pm disable-user $LAUNCHER_COMPONENT"
        fi
    fi
    
    echo ""
    echo "Checking Device Owner status..."
    DEVICE_OWNER=$(adb shell dpm list-owners 2>/dev/null | grep "$PACKAGE_NAME")
    if [ -n "$DEVICE_OWNER" ]; then
        echo "✅ Device Owner: Active"
    else
        echo "⚠️  Device Owner: Not active"
    fi
    
    echo ""
    echo "Checking app visibility in launcher..."
    LAUNCHER_APPS=$(adb shell pm list packages | grep "$PACKAGE_NAME")
    if [ -n "$LAUNCHER_APPS" ]; then
        echo "Package found in package list"
    fi
    
else
    echo "❌ Package is NOT installed: $PACKAGE_NAME"
    echo ""
    echo "Install the app first, then run this script again."
    exit 1
fi

echo ""
echo "=========================================="
echo "Check complete!"
echo "=========================================="
echo ""
echo "To view detailed logs:"
echo "  adb logcat -s APP_INSTALL_CHECK:D APP_HIDER:D"
echo ""
echo "To manually hide the app:"
echo "  adb shell pm disable-user $LAUNCHER_COMPONENT"
echo ""
echo "To show the app again (for testing):"
echo "  adb shell pm enable $LAUNCHER_COMPONENT"
