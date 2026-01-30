#!/bin/bash

echo "=========================================="
echo "App Installation & Name Verification"
echo "=========================================="
echo ""

# App Information
APP_PACKAGE="com.chats.capture"
APP_NAME="Chat Capture"

echo "📱 App Information:"
echo "   Package Name: $APP_PACKAGE"
echo "   Display Name: $APP_NAME"
echo ""

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK Platform Tools."
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected."
    echo "   Please connect your device via USB and enable USB debugging."
    exit 1
fi

echo "✅ Device connected"
echo ""

# Check if app is installed
echo "🔍 Checking if app is installed..."
if adb shell pm list packages | grep -q "$APP_PACKAGE"; then
    echo "✅ App is INSTALLED"
    echo ""
    
    # Get app details
    echo "📋 App Details:"
    echo "   Package: $APP_PACKAGE"
    
    # Get app label/name
    APP_LABEL=$(adb shell dumpsys package $APP_PACKAGE | grep -i "label" | head -1 | sed 's/.*label=//' | sed 's/ .*//' | tr -d '\r')
    if [ -n "$APP_LABEL" ]; then
        echo "   Display Name: $APP_LABEL"
    else
        echo "   Display Name: $APP_NAME [default]"
    fi
    
    # Get version
    VERSION=$(adb shell dumpsys package $APP_PACKAGE | grep -i "versionName" | head -1 | sed 's/.*versionName=//' | tr -d '\r')
    if [ -n "$VERSION" ]; then
        echo "   Version: $VERSION"
    fi
    
    echo ""
    echo "🔍 How to Find in Settings:"
    echo "   1. Open Settings app"
    echo "   2. Go to Apps or Application Manager"
    echo "   3. Search for: $APP_NAME"
    echo "   4. Or scroll to C section and find Chat Capture"
    echo ""
    echo "🚀 Quick Access Commands:"
    echo "   Open app: adb shell am start -n $APP_PACKAGE/.ui.MainActivity"
    echo "   Open settings: adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:$APP_PACKAGE"
    echo ""
    
    # Check component states
    echo "📊 Component Status:"
    SETTINGS_ACTIVITY=$(adb shell pm get-component-enabled-setting $APP_PACKAGE/.ui.SettingsLauncherActivity 2>/dev/null | grep -i "enabled" | head -1)
    if [ -n "$SETTINGS_ACTIVITY" ]; then
        echo "   SettingsLauncherActivity: $SETTINGS_ACTIVITY"
    fi
    
else
    echo "❌ App is NOT INSTALLED"
    echo ""
    echo "📥 To install:"
    echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
    echo ""
fi

echo ""
echo "=========================================="
