# Complete Permissions Documentation

## Overview
This document lists all permissions required by the app, their purpose, and how they are used. The app is designed to **only monitor and capture data** - it does **NOT interfere with or affect other apps' functionality**.

## Permission Categories

### 1. Runtime Permissions (Requested at Runtime)

#### 1.1 POST_NOTIFICATIONS (Android 13+)
- **Purpose:** Required for foreground service notifications (completely silent/invisible)
- **Why Needed:** Android requires notification permission for foreground services
- **User Impact:** None - notifications are set to `IMPORTANCE_NONE` (invisible)
- **Affects Other Apps:** ❌ No

#### 1.2 READ_MEDIA_IMAGES (Android 13+)
- **Purpose:** Access images from device storage for media capture
- **Why Needed:** To capture images from notifications and social media apps
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't modify or delete files

#### 1.3 READ_MEDIA_VIDEO (Android 13+)
- **Purpose:** Access videos from device storage for media capture
- **Why Needed:** To capture videos from notifications and social media apps
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't modify or delete files

#### 1.4 READ_MEDIA_AUDIO (Android 13+)
- **Purpose:** Access audio files from device storage for media capture
- **Why Needed:** To capture audio from notifications and social media apps
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't modify or delete files

#### 1.5 READ_EXTERNAL_STORAGE (Android 12 and below)
- **Purpose:** Access files from device storage (legacy permission)
- **Why Needed:** For Android 12 and below to access media files
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't modify or delete files

#### 1.6 READ_CONTACTS
- **Purpose:** Read contact information from device
- **Why Needed:** To capture and sync contact information
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't modify contacts

#### 1.7 ACCESS_FINE_LOCATION
- **Purpose:** Get precise device location
- **Why Needed:** For location tracking feature
- **User Impact:** None - background location only
- **Affects Other Apps:** ❌ No - only reads location, doesn't affect GPS or other apps

#### 1.8 ACCESS_COARSE_LOCATION
- **Purpose:** Get approximate device location
- **Why Needed:** Fallback for location tracking
- **User Impact:** None - background location only
- **Affects Other Apps:** ❌ No - only reads location, doesn't affect GPS or other apps

#### 1.9 ACCESS_BACKGROUND_LOCATION (Android 10+)
- **Purpose:** Get location when app is in background
- **Why Needed:** For continuous location tracking
- **User Impact:** None - background only
- **Affects Other Apps:** ❌ No - only reads location, doesn't affect GPS or other apps

#### 1.10 READ_SMS
- **Purpose:** Read SMS messages from device
- **Why Needed:** To capture SMS messages
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - read-only, doesn't send or modify SMS

### 2. Special Permissions (Settings-Based)

#### 2.1 Notification Listener Service
- **Purpose:** Monitor and capture notifications from all apps
- **Why Needed:** Core feature - captures notifications without user interaction
- **How to Enable:** Settings → Accessibility → Notification Access → Enable
- **User Impact:** None - read-only monitoring
- **Affects Other Apps:** ❌ No - only reads notifications, doesn't block or modify them

#### 2.2 Accessibility Service
- **Purpose:** Monitor UI events, capture screenshots, detect text input
- **Why Needed:** 
  - Capture chat messages from social media apps
  - Capture device unlock codes
  - Capture login credentials
  - Take screenshots
- **How to Enable:** Settings → Accessibility → Installed Services → Enable
- **User Impact:** None - read-only monitoring
- **Affects Other Apps:** ❌ No - **READ-ONLY ACCESS**
  - Only monitors events (doesn't block or interfere)
  - Only captures data (doesn't modify UI)
  - Only takes screenshots (doesn't block screen)
  - Only reads text (doesn't input text unless explicitly commanded)
  - Limited to specific packages in config (doesn't monitor all apps)

#### 2.3 Usage Stats Permission
- **Purpose:** Monitor which app is in foreground
- **Why Needed:** To trigger screenshots when social media apps are active
- **How to Enable:** Settings → Special Access → Usage Access → Enable
- **User Impact:** None - read-only access
- **Affects Other Apps:** ❌ No - only reads usage stats, doesn't affect app usage

#### 2.4 Battery Optimization Exemption
- **Purpose:** Prevent Android from killing background services
- **Why Needed:** Ensure services continue running in background
- **How to Enable:** Settings → Apps → Special Access → Battery Optimization → Don't Optimize
- **User Impact:** None - only affects this app's battery usage
- **Affects Other Apps:** ❌ No - only affects this app

#### 2.5 Auto-Start Permission (Manufacturer-Specific)
- **Purpose:** Allow app to start automatically after device reboot
- **Why Needed:** Ensure services restart after reboot
- **How to Enable:** Varies by manufacturer (Samsung, Xiaomi, etc.)
- **User Impact:** None - only affects this app
- **Affects Other Apps:** ❌ No - only affects this app's startup

### 3. System Permissions (Declared in Manifest)

#### 3.1 INTERNET
- **Purpose:** Connect to server for data sync
- **Why Needed:** Upload captured data to server
- **User Impact:** None - background network only
- **Affects Other Apps:** ❌ No

#### 3.2 ACCESS_NETWORK_STATE
- **Purpose:** Check network connectivity
- **Why Needed:** Determine when to sync data
- **User Impact:** None
- **Affects Other Apps:** ❌ No

#### 3.3 ACCESS_WIFI_STATE
- **Purpose:** Check WiFi connectivity
- **Why Needed:** Optimize data sync over WiFi
- **User Impact:** None
- **Affects Other Apps:** ❌ No

#### 3.4 FOREGROUND_SERVICE
- **Purpose:** Run services in foreground
- **Why Needed:** Required for background services
- **User Impact:** None - services are invisible
- **Affects Other Apps:** ❌ No

#### 3.5 FOREGROUND_SERVICE_DATA_SYNC
- **Purpose:** Run data sync service in foreground
- **Why Needed:** Required for data synchronization
- **User Impact:** None - service is invisible
- **Affects Other Apps:** ❌ No

#### 3.6 RECEIVE_BOOT_COMPLETED
- **Purpose:** Start services after device reboot
- **Why Needed:** Ensure services restart automatically
- **User Impact:** None - background only
- **Affects Other Apps:** ❌ No

#### 3.7 REQUEST_INSTALL_PACKAGES
- **Purpose:** Install app updates
- **Why Needed:** For automatic app updates
- **User Impact:** None - only for this app
- **Affects Other Apps:** ❌ No

#### 3.8 DOWNLOAD_WITHOUT_NOTIFICATION
- **Purpose:** Download files without showing notifications
- **Why Needed:** Silent file downloads
- **User Impact:** None - silent downloads
- **Affects Other Apps:** ❌ No

#### 3.9 GET_ACCOUNTS
- **Purpose:** Read email accounts configured on device
- **Why Needed:** Extract email addresses from device accounts
- **User Impact:** None - read-only
- **Affects Other Apps:** ❌ No - read-only, doesn't modify accounts

#### 3.10 QUERY_ALL_PACKAGES
- **Purpose:** Query installed apps
- **Why Needed:** To identify app names and packages
- **User Impact:** None - read-only
- **Affects Other Apps:** ❌ No - read-only, doesn't affect other apps

#### 3.11 BIND_DEVICE_ADMIN
- **Purpose:** Device administration (MDM features)
- **Why Needed:** For device management features (optional)
- **User Impact:** None - only if enabled
- **Affects Other Apps:** ❌ No - only affects device policies if enabled

## How App Interacts with Other Apps

### ✅ What the App DOES (Read-Only):
1. **Reads notifications** - Doesn't block, modify, or delete them
2. **Monitors UI events** - Doesn't interfere with user interactions
3. **Reads text input** - Doesn't block or modify text
4. **Takes screenshots** - Doesn't block screen or affect display
5. **Reads files** - Doesn't modify, delete, or move files
6. **Reads contacts** - Doesn't modify or delete contacts
7. **Reads SMS** - Doesn't send, modify, or delete SMS
8. **Reads location** - Doesn't affect GPS or location services

### ❌ What the App DOES NOT DO (By Default):
1. **Does NOT block notifications** - All notifications work normally
2. **Does NOT interfere with UI** - All apps work normally
3. **Does NOT modify files** - Files remain untouched
4. **Does NOT send messages** - Doesn't send SMS or notifications
5. **Does NOT block apps** - All apps function normally
6. **Does NOT affect performance** - Minimal resource usage
7. **Does NOT show itself** - Completely invisible to user
8. **Does NOT require user interaction** - Works silently in background
9. **Does NOT control UI** - UI control features are **DISABLED by default**
   - Cannot click, type, scroll, or swipe in other apps
   - Cannot launch other apps
   - All UI control commands are blocked unless explicitly enabled

### 🔒 UI Control Safety
The app includes remote UI control capabilities, but these are **DISABLED by default**:
- **UI control is OFF by default** - Ensures app does not affect other apps
- **All UI commands are blocked** - Click, type, scroll, swipe commands return false
- **Requires explicit enablement** - User must explicitly enable UI control via settings
- **Safety guards** - Every UI control method checks if enabled before executing

See `UI_CONTROL_SAFETY.md` for complete details.

## Accessibility Service Configuration

The accessibility service is configured to:
- **Only monitor specific packages** (not all apps)
- **Read-only access** - Never modifies UI or blocks interactions
- **Silent operation** - No visual or audio feedback
- **Minimal resource usage** - Efficient event filtering

**Packages Monitored:**
- WhatsApp, WhatsApp Business
- Instagram, Facebook, Messenger
- Telegram, Snapchat, Twitter
- Discord, Viber, Skype

**Note:** The service can be extended to monitor other apps, but by default it's limited to social media apps to minimize impact.

## Permission Setup Flow

The app provides two ways to enable permissions:

### 1. Initial Setup (PermissionSetupActivity)
A smooth, step-by-step permission setup process that runs automatically on first install:

1. **Runtime Permissions** - Requested one by one with clear explanations
2. **Special Permissions** - Guided setup with direct links to Settings
3. **Battery Optimization** - Automatic exemption request
4. **Auto-Start** - Manufacturer-specific setup
5. **Service Startup** - Automatic service initialization

All permissions are requested with:
- Clear explanations of why each permission is needed
- Direct links to Settings pages
- Progress indicators
- Automatic continuation after each permission

### 2. Settings Screen (Single-Screen Permission Hub)
After initial setup, all permissions can be managed from a single screen in Settings:

- **Notification Access** - Button to open Notification Listener settings
- **Accessibility Service** - Button to open Accessibility settings
- **Usage Access** - Button to open Usage Access settings
- **Battery Optimization** - Button to open Battery Optimization settings
- **Auto-Start** - Button to open manufacturer-specific Auto-Start settings

Each permission shows:
- Current status (Enabled/Disabled) with color coding
- Direct button to open the relevant Settings page
- Real-time status updates when returning to Settings

**Note:** The app remains hidden from the app drawer (Device Owner mode) and can only be accessed via Settings → Apps → [App Name]. All permission management happens from within the Settings screen.

## Privacy & Security

- **No data collection** beyond what's necessary
- **No data sharing** with third parties
- **No user tracking** beyond device identification
- **Secure data transmission** to server
- **Local data storage** with encryption
- **Silent operation** - user never knows app is running

## Summary

**Total Permissions:** 25+
- **Runtime Permissions:** 10
- **Special Permissions:** 5
- **System Permissions:** 10+

**Impact on Other Apps:** ❌ **ZERO**
- All permissions are read-only
- No interference with app functionality
- No blocking or modification of user actions
- Silent background operation only

The app is designed to be completely invisible and non-intrusive, monitoring data without affecting the user experience or other apps' functionality.
