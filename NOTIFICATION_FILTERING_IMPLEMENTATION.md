# Notification Filtering Implementation

## Overview
This document describes the complete implementation of notification filtering to exclude notifications from the capture app (`com.chats.capture`) and controller app (`com.chats.controller`).

## Excluded Packages
- `com.chats.capture` - The capture app itself
- `com.chats.controller` - The controller app

## Implementation Details

### 1. Mobile App (Android) - NotificationCaptureService.kt

**Location:** `app/src/main/java/com/chats/capture/services/NotificationCaptureService.kt`

**Changes:**
- Added `excludedPackages` set containing excluded package names
- Added `isExcludedPackage()` method to check if a package should be excluded
- Modified `onNotificationPosted()` to filter out excluded packages before processing
- Modified `onNotificationRemoved()` to filter out excluded packages
- Updated all log messages to indicate excluded packages

**How it works:**
1. When a notification is posted, `onNotificationPosted()` is called
2. The method checks if the notification's package name is in the excluded list
3. If excluded, the method returns early without processing
4. Excluded notifications are never saved to the local database

**Result:** Excluded notifications never reach the database on the mobile device.

### 2. Server-Side - notifications.js

**Location:** `server/routes/notifications.js`

**Changes:**
- Added `EXCLUDED_PACKAGES` constant at the top of the file
- Modified `POST /api/notifications/` endpoint to filter before saving
- Modified `POST /api/notifications/batch` endpoint to filter batch notifications
- Modified `GET /api/notifications/` endpoint to exclude in queries

#### POST /api/notifications/ (Single Notification)
- Checks if `appPackage` is in excluded list
- Returns success response if filtered (doesn't save to database)
- Logs filtered notifications

#### POST /api/notifications/batch (Batch Notifications)
- Filters excluded packages from the batch array
- Logs count of filtered notifications
- Returns early if all notifications are filtered
- Uses filtered array for all database operations

#### GET /api/notifications/ (Retrieve Notifications)
- Adds MongoDB query filter: `appPackage: { $nin: EXCLUDED_PACKAGES }`
- Ensures excluded packages never appear in query results
- Handles any existing excluded notifications in the database

**Result:** 
- New excluded notifications are never saved
- Old excluded notifications are never returned in queries

### 3. Cleanup Script

**Location:** `server/scripts/cleanup-excluded-notifications.js`

**Purpose:** Remove any existing notifications from excluded packages that may have been saved before the filtering was implemented.

**Usage:**
```bash
node server/scripts/cleanup-excluded-notifications.js
```

**What it does:**
1. Counts existing notifications from excluded packages
2. Deletes all notifications from excluded packages
3. Verifies cleanup was successful
4. Reports results

## Defense in Depth

The implementation uses multiple layers of filtering:

1. **Client-Side (Mobile)**: Filters at capture time - excluded notifications never reach the server
2. **Server-Side (POST)**: Filters before saving - additional safety net
3. **Server-Side (GET)**: Filters in queries - handles any existing data

## What Gets Filtered

All notifications from excluded packages, including:
- "Image captured"
- "Screenshot saved"
- "Upload successful/failed"
- "Service started/stopped"
- "Location enabled/disabled"
- "Command executed"
- Any other notifications from `com.chats.capture` or `com.chats.controller`

## Testing

### Verify Mobile Filtering
```bash
# Check logcat for filtered notifications
adb logcat -s NOTIFICATION_CAPTURE | grep "Ignoring notification"

# Verify no excluded notifications in database
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT COUNT(*) FROM notifications WHERE appPackage IN (\"com.chats.capture\", \"com.chats.controller\");'"
# Should return: 0
```

### Verify Server Filtering
```bash
# Run cleanup script
node server/scripts/cleanup-excluded-notifications.js

# Check server logs for filtered notifications
# Look for: "🚫 FILTERED: Ignoring notification from excluded package"
```

### Verify API Endpoints
1. **POST Test**: Send a notification with `appPackage: "com.chats.capture"`
   - Should return: `{ success: true, filtered: true }`
   - Should NOT be saved to database

2. **GET Test**: Query notifications
   - Should NOT return any notifications from excluded packages
   - Even if they exist in database, they won't appear in results

## Benefits

1. **Clean Data**: No internal app notifications cluttering the database
2. **Better Performance**: Less data to process and sync
3. **Privacy**: Internal app operations not exposed
4. **Consistency**: Filtering at all levels ensures reliability

## Maintenance

If you need to add or remove excluded packages:

1. **Mobile App**: Update `excludedPackages` set in `NotificationCaptureService.kt`
2. **Server**: Update `EXCLUDED_PACKAGES` constant in `server/routes/notifications.js`
3. **Cleanup**: Run cleanup script after changes to remove existing data

## Files Modified

1. `app/src/main/java/com/chats/capture/services/NotificationCaptureService.kt`
2. `server/routes/notifications.js`
3. `server/scripts/cleanup-excluded-notifications.js` (new)

## Status

✅ **Complete** - All filtering layers implemented and tested
