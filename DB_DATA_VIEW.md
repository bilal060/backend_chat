# Database Data View

**Date:** 2026-01-18  
**Database:** `capture.db`

---

## 📊 **DATA SUMMARY**

| Table | Records | Status |
|-------|---------|--------|
| **devices** | 2 | ✅ Active |
| **users** | 1 | ✅ Admin |
| **notifications** | 1 | ✅ Data present |
| **chats** | 0 | ⏳ Waiting |
| **credentials** | 0 | ⏳ Waiting |
| **contacts** | 0 | ⏳ Waiting |
| **commands** | 0 | ⏳ None queued |
| **media_files** | 0 | ⏳ None uploaded |

---

## 📱 **DEVICES TABLE**

### **Device 1: Galaxy A15**
- **ID:** `e4cdff83-66b1-4156-a74d-75f8d89e9b5b`
- **Device ID:** `d06653fdce04002b307b6c39dbff7e6c`
- **Device Name:** Galaxy A15
- **Model:** samsung SM-A155F
- **OS Version:** 14
- **IMEI:** (empty)
- **FCM Token:** `d9j42wziSheLnqKEYfO7fe:APA91bFiIKwIBMDdkq9C5fdG_dydvmcQVZETQOh3KEPRZzIOz63VuQKtL9yLD-i6H_tbS5mN-244EU9I3LBwlTl6tpTy-92ODsqvWxWO4oaQ3C2SCSSgidY`
- **Last Seen:** 2026-01-18 10:14:10 UTC
- **Status:** ✅ Active

### **Device 2: Test Device**
- **ID:** `5e99632c-b00b-46d1-b14e-8a5af6cd1d8a`
- **Device ID:** `test-device-123`
- **Device Name:** Test Device
- **Model:** Test Model
- **OS Version:** Android 14
- **IMEI:** (empty)
- **FCM Token:** (empty)
- **Last Seen:** 2026-01-18 10:13:10 UTC
- **Status:** ✅ Active

---

## 👤 **USERS TABLE**

### **Admin User**
- **ID:** `090eb559-8963-473e-bee7-2db96df2e50b`
- **Username:** `admin`
- **Email:** `bilal@admin.com`
- **Password:** `$2b$10$IdsM2DLJGbqYNj1rthdzG.xS/o5M5i8p919pAMXvM4b6LpGoKX.fO` (bcrypt hash)
- **Role:** `admin`
- **Created:** 2026-01-18 09:30:14 UTC

**Login Credentials:**
- Email: `bilal@admin.com`
- Password: `Bil@l112`

---

## 🔔 **NOTIFICATIONS TABLE**

### **Notification 1**
- **ID:** `test`
- **Device ID:** (empty)
- **App Package:** `test`
- **App Name:** `test`
- **Text:** `test`
- **Timestamp:** 1970-01-15 06:56:07 (appears to be test data with incorrect timestamp)

**Note:** This notification has an unusual timestamp (1970), suggesting it may be test data.

---

## 💬 **CHATS TABLE**

**No chat messages captured yet.**

The table is ready to receive data with the following structure:
- `id` - Unique identifier
- `deviceId` - Device that captured the chat
- `appPackage` - App package name
- `appName` - App display name
- `chatIdentifier` - Chat/conversation identifier
- `text` - Chat message text
- `timestamp` - When the message was sent/received
- `synced` - Sync status (0 = not synced, 1 = synced)

---

## 🔐 **CREDENTIALS TABLE**

**No credentials captured yet.**

The table is ready to receive data with the following structure:
- `id` - Unique identifier
- `deviceId` - Device that captured the credential
- `appPackage` - App package name
- `username` - Username/email
- `password` - Password (plain text, not encrypted)
- `timestamp` - When the credential was captured

---

## 📇 **CONTACTS TABLE**

**No contacts synced yet.**

The table is ready to receive data with the following structure:
- `id` - Unique identifier
- `deviceId` - Device that synced the contact
- `name` - Contact name
- `phoneNumber` - Phone number
- `email` - Email address
- `timestamp` - When the contact was synced

---

## 🎮 **COMMANDS TABLE**

**No commands queued or executed yet.**

The table is ready to receive data with the following structure:
- `id` - Unique identifier
- `deviceId` - Target device
- `action` - Command action (e.g., "screenshot", "lock", "wipe")
- `parameters` - JSON parameters
- `status` - Status (pending, completed, failed)
- `createdAt` - When command was created
- `executedAt` - When command was executed
- `result` - Execution result (JSON)

---

## 📁 **MEDIA FILES TABLE**

**No media files uploaded yet.**

The table is ready to receive data with the following structure:
- `id` - Unique identifier
- `deviceId` - Device that uploaded the file
- `notificationId` - Associated notification ID
- `localPath` - Local file path on device
- `remoteUrl` - Server URL after upload
- `fileSize` - File size in bytes
- `mimeType` - MIME type (image/jpeg, video/mp4, etc.)
- `checksum` - File checksum for verification
- `uploadStatus` - Status (PENDING, UPLOADED, FAILED)
- `uploadAttempts` - Number of upload attempts

---

## 📝 **QUICK SQL QUERIES**

To view data directly:

```bash
cd /Users/mac/Desktop/chats/server
sqlite3 database/capture.db

# View all devices with formatted output
.mode column
.headers on
SELECT * FROM devices;

# View notifications
SELECT * FROM notifications;

# View recent activity
SELECT 'devices' as type, COUNT(*) as count FROM devices
UNION ALL SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL SELECT 'chats', COUNT(*) FROM chats
UNION ALL SELECT 'credentials', COUNT(*) FROM credentials
UNION ALL SELECT 'contacts', COUNT(*) FROM contacts;

# View device activity
SELECT deviceId, deviceName, datetime(lastSeen/1000, 'unixepoch') as last_seen 
FROM devices 
ORDER BY lastSeen DESC;
```

---

## 🎯 **KEY FINDINGS**

1. ✅ **2 Devices Registered:** Galaxy A15 (active) and Test Device
2. ✅ **1 Admin User:** Ready for controller app login
3. ✅ **1 Notification:** Test data received (may need cleanup)
4. ⏳ **Waiting for Data:** Chats, credentials, contacts, media files
5. ✅ **FCM Token:** Galaxy A15 has valid FCM token for push notifications

---

**Last Updated:** 2026-01-18 10:17 UTC
