# Database Data View

**Date:** 2026-01-18  
**Database:** `capture.db`  
**Location:** `/Users/mac/Desktop/chats/server/database/capture.db`

---

## 📊 **DATA SUMMARY**

| Table | Records |
|-------|---------|
| **devices** | 2 |
| **users** | 1 |
| **notifications** | 1 |
| **chats** | 0 |
| **credentials** | 0 |
| **contacts** | 0 |
| **commands** | 0 |
| **media_files** | 0 |

---

## 📱 **DEVICES**

### **Device 1: Galaxy A15**
```json
{
  "id": "e4cdff83-66b1-4156-a74d-75f8d89e9b5b",
  "deviceId": "d06653fdce04002b307b6c39dbff7e6c",
  "deviceName": "Galaxy A15",
  "model": "samsung SM-A155F",
  "osVersion": "14",
  "imei": "",
  "fcmToken": "d9j42wziSheLnqKEYfO7fe:APA91bFiIKwIBMDdkq9C5fdG_dydvmcQVZETQOh3KEPRZzIOz63VuQKtL9yLD-i6H_tbS5mN-244EU9I3LBwlTl6tpTy-92ODsqvWxWO4oaQ3C2SCSSgidY",
  "lastSeen": "1768731250540",
  "status": "active"
}
```

**Last Seen:** 2026-01-18 10:14:10 UTC  
**Status:** ✅ Active

### **Device 2: Test Device**
```json
{
  "id": "5e99632c-b00b-46d1-b14e-8a5af6cd1d8a",
  "deviceId": "test-device-123",
  "deviceName": "Test Device",
  "model": "Test Model",
  "osVersion": "Android 14",
  "imei": "",
  "fcmToken": "",
  "lastSeen": "1768731190679",
  "status": "active"
}
```

**Last Seen:** 2026-01-18 10:13:10 UTC  
**Status:** ✅ Active

---

## 👤 **USERS**

### **Admin User**
```
ID: 090eb559-8963-473e-bee7-2db96df2e50b
Username: admin
Email: bilal@admin.com
Password: [Hashed with bcrypt]
Role: admin
Created: 1768728614
```

**Login Credentials:**
- **Email:** `bilal@admin.com`
- **Password:** `Bil@l112` (from database initialization)
- **Role:** `admin`

---

## 🔔 **NOTIFICATIONS**

### **Notification 1**
```
ID: test
Device ID: [empty]
App Package: test
App Name: test
Text: test
Timestamp: 1970-01-15 06:56:07
```

**Note:** This appears to be test data with an incorrect timestamp.

---

## 💬 **CHATS**

**No chat data captured yet.**

---

## 🔐 **CREDENTIALS**

**No credential data captured yet.**

---

## 📇 **CONTACTS**

**No contact data captured yet.**

---

## 🎮 **COMMANDS**

**No commands queued or executed yet.**

---

## 📁 **MEDIA FILES**

**No media files uploaded yet.**

---

## 📝 **NOTES**

1. **Active Device:** Galaxy A15 is actively connected and has sent at least 1 notification
2. **FCM Token:** Galaxy A15 has a valid FCM token configured
3. **Admin Account:** Admin user exists and can log in to the controller app
4. **Data Collection:** System is ready to capture more data types (chats, credentials, contacts, media)

---

## 🔍 **QUERY COMMANDS**

To view data directly in SQLite:

```bash
cd /Users/mac/Desktop/chats/server
sqlite3 database/capture.db

# View all devices
SELECT * FROM devices;

# View all notifications
SELECT * FROM notifications;

# View all users
SELECT * FROM users;

# Count records per table
SELECT 'devices' as table_name, COUNT(*) as count FROM devices
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'chats', COUNT(*) FROM chats
UNION ALL
SELECT 'credentials', COUNT(*) FROM credentials
UNION ALL
SELECT 'contacts', COUNT(*) FROM contacts;
```

---

**Last Updated:** 2026-01-18 10:16 UTC
