#!/bin/bash
# Quick check for data capture logs

echo "🔍 Quick Data Capture Status Check"
echo "=================================="
echo ""

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected"
    echo "   Connect your Android device via USB and enable USB debugging"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Check recent logs (last 30 seconds)
echo "📊 Recent capture logs (last 30 seconds):"
echo "----------------------------------------"
adb logcat -d -t 300 | grep -i -E "(com.chats.capture|Notification captured|Chat captured|Credential|Contact|Location|Upload|API)" | tail -20

echo ""
echo "📈 Capture Statistics:"
echo "---------------------"
NOTIFICATIONS=$(adb logcat -d | grep -i "Notification captured" | wc -l | tr -d ' ')
CHATS=$(adb logcat -d | grep -i "Chat captured" | wc -l | tr -d ' ')
UPLOADS=$(adb logcat -d | grep -i -E "(Upload.*success|POST.*api)" | w-d ' ')
ERRORS=$(adb logcat -d -s "Timber:E" "com.chats.capture:E" | wc -l | tr -d ' ')

echo "  Notifications captured: $NOTIFICATIONS"
echo "  Chats captured: $CHATS"
echo "  Uploads attempted: $UPLOADS"
echo "  Errors: $ERRORS"

echo ""
echo "💡 To monitor live: ./monitor-capture.sh"
