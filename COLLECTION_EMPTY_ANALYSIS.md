# MongoDB Collections Empty Analysis

## Overview
This document explains which collections might be empty and why, based on the app's functionality and data flow.

---

## Collection Status & Reasons

### ✅ **Populated Collections** (Should Have Data)

#### 1. **users**
- **Status:** Should have at least 1 document (admin user)
- **Why:** Created automatically on server startup in `mongodb.js`
- **If Empty:** Server initialization failed or admin user was deleted
- **Fix:** Restart server or run `createAdminUser()` manually

#### 2. **devices**
- **Status:** Should have data if app is installed and registered
- **Why:** Created when app first connects to server (`/api/devices/register`)
- **If Empty:** 
  - App is not installed
  - App is not connecting to server
  - Server URL is incorrect in app
- **Fix:** Install app, check server URL, verify network connectivity

#### 3. **logs**
- **Status:** Should have data if server is receiving requests
- **Why:** Created by `requestLogger` middleware for all POST/PUT requests
- **If Empty:** 
  - No API requests being made
  - Request logging middleware is disabled
  - Server is not running
- **Fix:** Check server logs, verify middleware is active

---

### ❌ **Empty Collections** (May Be Empty - Depends on App Activity)

#### 4. **notifications**
- **Why Empty:**
  1. **App not installed** - No app to capture notifications
  2. **Notification Access not granted** - App needs Notification Listener Service permission
  3. **Capture disabled** - "Capture Enabled" toggle is OFF in Settings
  4. **No notifications received** - Device hasn't received any notifications
  5. **Sync not working** - Data captured but not synced to server
- **Requirements:**
  - App installed ✅
  - Notification Access permission ✅
  - Capture Enabled toggle ON ✅
  - App receiving notifications ✅
  - Network connectivity ✅
- **Check:** 
  ```bash
  # Check if app is installed
  adb shell pm list packages | grep com.chats.capture
  
  # Check notification access
  adb shell settings get secure enabled_notification_listeners | grep com.chats.capture
  
  # Check logcat for notification capture
  adb logcat -s NOTIFICATION_CAPTURE:D
  ```

#### 5. **chats**
- **Why Empty:**
  1. **App not installed** - No app to capture chats
  2. **Accessibility Service not enabled** - Required for chat capture
  3. **Capture disabled** - "Capture Enabled" toggle is OFF
  4. **No social media apps used** - Chats only captured from WhatsApp, Instagram, etc.
  5. **No text input detected** - App only captures when user types/pastes text
  6. **Sync not working** - Data captured but not synced
- **Requirements:**
  - App installed ✅
  - Accessibility Service enabled ✅
  - Capture Enabled toggle ON ✅
  - Social media apps installed and used ✅
  - Text input activity ✅
- **Check:**
  ```bash
  # Check accessibility service
  adb shell settings get secure enabled_accessibility_services | grep com.chats.capture
  
  # Check logcat for chat capture
  adb logcat -s CHAT_CAPTURE:D KEYBOARD_CAPTURE:D
  ```

#### 6. **contacts**
- **Why Empty:**
  1. **App not installed** - No app to capture contacts
  2. **READ_CONTACTS permission not granted** - Required for contact access
  3. **Capture disabled** - "Capture Enabled" toggle is OFF
  4. **No contacts on device** - Device has no contacts
  5. **Sync not working** - Contacts captured but not synced
- **Requirements:**
  - App installed ✅
  - READ_CONTACTS permission ✅
  - Capture Enabled toggle ON ✅
  - Contacts exist on device ✅
- **Check:**
  ```bash
  # Check contacts permission
  adb shell dumpsys package com.chats.capture | grep READ_CONTACTS
  
  # Check logcat for contact capture
  adb logcat -s CONTACT_CAPTURE:D
  ```

#### 7. **credentials**
- **Why Empty:**
  1. **App not installed** - No app to capture credentials
  2. **Accessibility Service not enabled** - Required for credential capture
  3. **Capture disabled** - "Capture Enabled" toggle is OFF
  4. **No login forms used** - User hasn't logged into any apps/websites
  5. **Password capture not triggered** - No email/username + password combinations detected
  6. **Sync not working** - Credentials captured but not synced
- **Requirements:**
  - App installed ✅
  - Accessibility Service enabled ✅
  - Capture Enabled toggle ON ✅
  - User logs into apps/websites ✅
  - Login forms detected ✅
- **Check:**
  ```bash
  # Check accessibility service
  adb shell settings get secure enabled_accessibility_services | grep com.chats.capture
  
  # Check logcat for credential capture
  adb logcat -s CREDENTIAL_CAPTURE:D PASSWORD_CAPTURE:D
  ```

#### 8. **media_files**
- **Why Empty:**
  1. **No media in notifications** - Notifications don't contain images/videos
  2. **No files downloaded** - No files <20MB downloaded through apps
  3. **No social media media** - No media received through WhatsApp/Viber/etc.
  4. **File size limit** - Files >20MB are not uploaded
  5. **Upload failed** - Media files exist but upload failed
- **Requirements:**
  - Notifications with media OR
  - Files downloaded (<20MB) OR
  - Social media media received (<20MB) ✅
  - Network connectivity ✅
- **Check:**
  ```bash
  # Check logcat for media upload
  adb logcat -s MEDIA_UPLOAD:D DOWNLOAD_MONITOR:D SOCIAL_MEDIA_MONITOR:D
  ```

#### 9. **commands**
- **Why Empty:**
  1. **No commands sent** - Controller app or admin hasn't sent any commands
  2. **No devices registered** - Can't send commands to non-existent devices
  3. **Admin only** - Only admin users can queue commands
- **Requirements:**
  - Devices registered ✅
  - Admin user logged in ✅
  - Commands sent via `/api/commands` endpoint ✅
- **Check:** Commands are created when admin sends commands via API

#### 10. **device_ownership**
- **Why Empty:**
  1. **No device owners assigned** - Devices haven't been assigned to users
  2. **No device_owner role users** - No users with device_owner role exist
  3. **Feature not used** - Device ownership feature not implemented/used
- **Requirements:**
  - Devices registered ✅
  - Users with device_owner role ✅
  - Devices assigned via `/api/devices/:deviceId/assign-owner` ✅

---

## Quick Diagnostic Checklist

### Step 1: Verify App Installation
```bash
adb shell pm list packages | grep com.chats.capture
```
- ✅ If found: App is installed
- ❌ If not found: Install app first

### Step 2: Check Critical Permissions
```bash
# Notification Access
adb shell settings get secure enabled_notification_listeners | grep com.chats.capture

# Accessibility Service
adb shell settings get secure enabled_accessibility_services | grep com.chats.capture

# Contacts Permission
adb shell dumpsys package com.chats.capture | grep READ_CONTACTS
```
- ✅ All should show the app package name
- ❌ If missing: Grant permissions in Settings

### Step 3: Check Capture Enabled Status
```bash
# Check if capture is enabled (requires app to be running)
adb logcat -s APP_STATE:D | grep "Capture enabled"
```
- ✅ Should show "Capture enabled: true"
- ❌ If false: Enable "Capture Enabled" toggle in Settings

### Step 4: Check Server Connection
```bash
# Check if app is connecting to server
adb logcat -s API_CLIENT:D DEVICE_REGISTRATION:D | grep -i "connected\|registered"
```
- ✅ Should show connection/registration success
- ❌ If not: Check server URL in app settings

### Step 5: Check Data Capture Activity
```bash
# Check for notification capture
adb logcat -s NOTIFICATION_CAPTURE:D | tail -20

# Check for chat capture
adb logcat -s CHAT_CAPTURE:D KEYBOARD_CAPTURE:D | tail -20

# Check for credential capture
adb logcat -s CREDENTIAL_CAPTURE:D PASSWORD_CAPTURE:D | tail -20
```
- ✅ Should show capture activity
- ❌ If empty: No data being captured (check permissions and capture enabled)

### Step 6: Check Sync Activity
```bash
# Check sync worker
adb logcat -s SYNC_WORKER:D | tail -20

# Check API requests
adb logcat -s API_REQUEST_DATA:D API_RESPONSE_DATA:D | tail -20
```
- ✅ Should show sync attempts and API calls
- ❌ If empty: Sync not running (check network, server URL)

---

## Common Issues & Solutions

### Issue 1: All Collections Empty Except `users` and `logs`
**Cause:** App is not installed or not connecting to server
**Solution:**
1. Install app: `./gradlew installDebug`
2. Check server URL in app settings
3. Verify network connectivity
4. Check device registration in `devices` collection

### Issue 2: `notifications` Empty But App Installed
**Cause:** Notification Access not granted or Capture disabled
**Solution:**
1. Grant Notification Access: Settings → Apps → Special Access → Notification Access
2. Enable Capture: Settings → Apps → [App Name] → Toggle "Capture Enabled" ON
3. Test by receiving a notification

### Issue 3: `chats` Empty But App Installed
**Cause:** Accessibility Service not enabled or no social media activity
**Solution:**
1. Enable Accessibility Service: Settings → Accessibility → Downloaded Apps
2. Use social media apps (WhatsApp, Instagram, etc.)
3. Type messages in social media apps
4. Check logcat for capture activity

### Issue 4: `contacts` Empty But App Installed
**Cause:** READ_CONTACTS permission not granted or no contacts on device
**Solution:**
1. Grant READ_CONTACTS permission: Settings → Apps → [App Name] → Permissions
2. Verify device has contacts
3. Trigger contact sync manually or wait for automatic sync

### Issue 5: `credentials` Empty But App Installed
**Cause:** No login activity or Accessibility Service not enabled
**Solution:**
1. Enable Accessibility Service
2. Log into apps/websites (trigger credential capture)
3. Check logcat for credential capture activity

### Issue 6: `media_files` Empty
**Cause:** No media in notifications or files not downloaded
**Solution:**
1. Receive notifications with images/videos
2. Download files <20MB through apps
3. Receive media through social media apps
4. Check logcat for media upload activity

### Issue 7: `commands` Empty
**Cause:** No commands sent (this is normal if not using command feature)
**Solution:**
- This is expected if you're not sending commands via API
- Commands are created when admin sends commands to devices

### Issue 8: `device_ownership` Empty
**Cause:** Device ownership feature not used (this is normal)
**Solution:**
- This is expected if not using device ownership feature
- Only populated when devices are assigned to users with device_owner role

---

## Summary Table

| Collection | Expected Status | Why Empty | Priority |
|------------|----------------|-----------|----------|
| **users** | ✅ Populated | Server init failed | HIGH |
| **devices** | ✅ Populated | App not installed/connected | HIGH |
| **logs** | ✅ Populated | No API requests | MEDIUM |
| **notifications** | ⚠️ May be empty | Permissions/Capture disabled | HIGH |
| **chats** | ⚠️ May be empty | Permissions/No activity | HIGH |
| **contacts** | ⚠️ May be empty | Permissions/No contacts | MEDIUM |
| **credentials** | ⚠️ May be empty | Permissions/No logins | MEDIUM |
| **media_files** | ⚠️ May be empty | No media/downloads | LOW |
| **commands** | ⚠️ May be empty | Feature not used | LOW |
| **device_ownership** | ⚠️ May be empty | Feature not used | LOW |

---

## Next Steps

1. **Run the diagnostic checklist** above to identify issues
2. **Check MongoDB collections** to see which are actually empty
3. **Fix permissions** if collections that should have data are empty
4. **Enable Capture** if app is installed but not capturing
5. **Verify server connection** if no data is syncing
6. **Check logcat** for detailed capture activity logs

For detailed permission setup, see: `PERMISSION_GRANT_STEPS.md`
