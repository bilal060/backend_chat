# System Architecture - Dual App MDM System

**Date:** 2026-01-18  
**Status:** ✅ **CONFIRMED**

---

## 🎯 **SYSTEM OVERVIEW**

### **Two Separate Apps:**

1. **Receiver App** (`/app/`) - **Chat Capture**
   - **Purpose:** Runs silently on managed devices
   - **Login:** ❌ **NO LOGIN REQUIRED**
   - **Function:** Captures data automatically
   - **Visibility:** Hidden from launcher
   - **Operation:** Silent background operation

2. **Controller App** (`/controller-app/`) - **MDM Controller**
   - **Purpose:** Management interface for viewing captured data
   - **Login:** ✅ **REQUIRES LOGIN**
   - **Function:** View and manage captured data
   - **Usage:** Used on **separate device** (admin's device)
   - **Operation:** Full UI for device management

---

## 📱 **RECEIVER APP (Chat Capture)**

### **Installation:**
- Install on **target/managed devices**
- Runs automatically after installation
- No user interaction required
- Hidden from launcher

### **Operation:**
- ✅ **No Login** - Runs silently
- ✅ **Auto-Registration** - Registers with server automatically
- ✅ **Data Capture** - Captures:
  - Notifications
  - Chats
  - Credentials/Passwords
  - Contacts (daily sync)
  - Keylogs
  - Location
  - Media files
  - Screenshots (on command)

### **Communication:**
- Connects to backend server automatically
- Receives commands via FCM push notifications
- Sends captured data to server
- No user interface visible

---

## 🎮 **CONTROLLER APP (MDM Controller)**

### **Installation:**
- Install on **separate device** (admin's device)
- Used to monitor and manage devices
- Full user interface

### **Login:**
- ✅ **Admin Login:**
  - Email: `bilal@admin.com`
  - Password: `Bil@l112`
  - Access: All devices, full control

- ✅ **Device Owner Login:**
  - Username: 6-digit alphanumeric (e.g., `A1B2C3`)
  - Password: 6-digit alphanumeric (e.g., `X9Y8Z7`)
  - Access: Assigned device only, view-only

### **Features:**
- View all registered devices (Admin) or assigned device (Device Owner)
- View captured data:
  - Notifications
  - Chats
  - Credentials
  - Contacts
  - Screenshots
- Send MDM commands:
  - Capture Screenshot
  - Sync Data
  - Update App
  - Restart Service
- Real-time updates via WebSocket

---

## 🔄 **DATA FLOW**

```
┌─────────────────────────────────┐
│   Managed Device (Target)        │
│   ┌───────────────────────────┐ │
│   │  Receiver App              │ │
│   │  (Chat Capture)            │ │
│   │  - No Login                │ │
│   │  - Auto-captures data     │ │
│   │  - Hidden from launcher   │ │
│   └───────────────────────────┘ │
│            │                      │
│            │ Captured Data        │
│            ▼                      │
└─────────────────────────────────┘
            │
            │ HTTP/WebSocket
            │
            ▼
┌─────────────────────────────────┐
│   Backend Server                 │
│   (Port 3000)                    │
│   - Stores captured data         │
│   - Manages devices              │
│   - Handles commands             │
└─────────────────────────────────┘
            │
            │ API/WebSocket
            │
            ▼
┌─────────────────────────────────┐
│   Admin Device (Separate)        │
│   ┌───────────────────────────┐ │
│   │  Controller App           │ │
│   │  (MDM Controller)         │ │
│   │  - Requires Login         │ │
│   │  - Views captured data   │ │
│   │  - Sends commands         │ │
│   └───────────────────────────┘ │
└─────────────────────────────────┘
```

---

## ✅ **KEY POINTS**

### **Receiver App:**
- ❌ **NO LOGIN** - Runs automatically
- ✅ **AUTOMATIC** - Captures data without user interaction
- ✅ **HIDDEN** - Not visible in launcher
- ✅ **SILENT** - No notifications or UI

### **Controller App:**
- ✅ **REQUIRES LOGIN** - Admin or Device Owner credentials
- ✅ **SEPARATE DEVICE** - Used on admin's device (not target device)
- ✅ **VIEW DATA** - See all captured data from managed devices
- ✅ **MANAGE DEVICES** - Send commands and manage devices

---

## 🎯 **USAGE SCENARIO**

### **Setup:**
1. Install **Receiver App** on target/managed device
2. App runs automatically, captures data
3. Install **Controller App** on admin's device (separate device)
4. Login to Controller App with admin credentials
5. View captured data from managed devices

### **Operation:**
- **Target Device:** Receiver App runs silently, captures data automatically
- **Admin Device:** Controller App shows captured data, allows management

---

## 📋 **SUMMARY**

| Feature | Receiver App | Controller App |
|---------|-------------|----------------|
| **Login Required** | ❌ No | ✅ Yes |
| **Device** | Target/Managed Device | Separate Admin Device |
| **Purpose** | Capture Data | View & Manage Data |
| **Visibility** | Hidden | Visible |
| **User Interaction** | None | Full UI |
| **Operation** | Automatic | Manual (after login) |

---

**Status:** ✅ **ARCHITECTURE CONFIRMED**

**Last Updated:** 2026-01-18
