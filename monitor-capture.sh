#!/bin/bash

# Monitor logcat for data capture logs
# Usage: ./monitor-capture.sh

echo "📱 Monitoring Android App Data Capture Logs"
echo "============================================"
echo ""
echo "Waiting for device..."
adb wait-for-device
echo "✅ Device connected!"
echo ""
echo "Clearing logcat..."
adb logcat -c
echo ""
echo "Monitoring for data capture events..."
echo ""
echo "🔍 Looking for:"
echo "  - Notification captured"
echo "  - Chat captured"
echo "  - Credential captured"
echo "  - Contact captured"
echo "  - Location captured"
echo "  - Screenshot captured"
echo "  - Upload success/error"
echo "  - API calls"
echo ""
echo "Press Ctrl+C to stop"
echo ""
echo "============================================"
echo ""

# Monitor with filters
adb logcat -s "Timber:*" "com.chats.capture:*" | grep --line-buffered -i -E "(capture|notification|chat|credential|contact|location|screenshot|upload|api|sync|saved|error|success|POST|GET|uploaded)" | while read line; do
    timestamp=$(date +"%H:%M:%S")
    echo "[$timestamp] $line"
done
