# App Hiding Implementation - Complete Guide

## Overview
This document details the complete implementation of app hiding functionality for Device Owner mode. The app remains hidden from the launcher (app drawer) but is accessible via Settings → Apps → [App Name].

## Implementation Status: ✅ COMPLETE

### Core Components

#### 1. AppVisibilityManager (`utils/AppVisibilityManager.kt`)
- **Purpose**: Manages app visibility in launcher and Settings
- **Key Methods**:
  - `hideFromLauncher(context)` - Disables launcher activity component
  - `showInLauncher(context)` - Enables launcher activity (for testing)
  - `isHiddenFromLauncher(context)` - Checks current visibility state
  - `getSettingsIntent(context)` - Returns intent to open app from Settings

#### 2. AppHider (`utils/AppHider.kt`)
- **Purpose**: Utility to completely hide the app from launcher
- **Key Methods**:
  - `hide(context)` - Hides app from launcher
  - `isHidden(context)` - Checks if app is hidden
  - `ensureHidden(context)` - Ensures app stays hidden (with verification)

### Activities with App Hiding Logic

#### ✅ MainActivity
- **Hides in**: `onCreate()`, `onResume()`, `onPause()`, `onBackPressed()`
- **Purpose**: Main entry point, shows Settings UI when accessed from Settings
- **Special**: Defaults to Settings tab (position 4) for immediate permissions access

#### ✅ SettingsLauncherActivity
- **Hides in**: `onCreate()` with delayed retries (1s, multiple attempts)
- **Purpose**: Hidden launcher activity that redirects to MainActivity
- **Special**: Multiple delayed hide attempts to ensure it sticks

#### ✅ SettingsShortcutActivity
- **Hides in**: `onCreate()`
- **Purpose**: Redirects to system app info page for permission management

#### ✅ PermissionSetupActivity
- **Hides in**: Multiple points via `forceHideAppFromLauncher()`
- **Purpose**: Initial permission setup flow
- **Special**: Hides before and after opening each Settings page

#### ✅ PermissionSettingsActivity
- **Hides in**: `onCreate()`, `onResume()`, after each button click (6 buttons)
- **Purpose**: Alternative permission management screen
- **Special**: Re-hides after opening any Settings page

#### ✅ UpdatePermissionActivity
- **Hides in**: `onCreate()`, `onResume()`, `onActivityResult()`
- **Purpose**: Requests install package permission for updates
- **Special**: Re-hides after returning from Settings

#### ✅ DebugChatInputActivity
- **Hides in**: `onCreate()`
- **Purpose**: Debug-only activity for testing chat capture
- **Note**: Debug activity, but still hides for consistency

#### ✅ HideFromDrawerActivity
- **Hides in**: `onCreate()`
- **Purpose**: Explicitly hides app from drawer
- **Note**: Silent operation (no toast messages)

### Fragments with App Hiding Logic

#### ✅ SettingsFragment
- **Hides in**: `onResume()`, after all user actions:
  - Toggling capture enabled/disabled
  - Saving server URL
  - Opening any permission settings page (6 buttons total)
- **Purpose**: Single-screen permissions hub for managing all permissions
- **Features**:
  - Shows status for 5 special permissions
  - Buttons to open each system settings page
  - Runtime permissions button
  - Real-time status updates

### Receivers with App Hiding Logic

#### ✅ BootReceiver
- **Hides in**: `onReceive()` for `ACTION_BOOT_COMPLETED`
- **Purpose**: Ensures app stays hidden after device reboot

#### ✅ PackageInstallReceiver
- **Hides in**: `handleAppInstalled()` with multiple delayed attempts (2s, 5s, 10s)
- **Purpose**: Ensures app stays hidden after installation/update
- **Special**: Multiple delayed hide attempts to handle launcher caching

### Permissions Hub Features

The SettingsFragment provides a comprehensive single-screen permissions hub:

1. **Notification Access** - Opens Notification Listener settings
2. **Accessibility Service** - Opens Accessibility settings
3. **Usage Access** - Opens Usage Access settings
4. **Battery Optimization** - Opens Battery Optimization settings
5. **Auto-Start** - Opens manufacturer-specific Auto-Start settings
6. **Runtime Permissions** - Opens app's permission settings page

Each permission shows:
- Current status (Enabled/Disabled) with color coding
- Button to open relevant Settings page
- Dynamic button labels ("Enable" vs "Manage")
- Real-time status updates

### App Hiding Enforcement Points

The app re-hides itself automatically in the following scenarios:

1. **Activity Lifecycle**:
   - `onCreate()` - When activity is created
   - `onResume()` - When returning to activity
   - `onPause()` - When activity is paused
   - `onBackPressed()` - When user presses back

2. **User Interactions**:
   - Toggling capture enabled/disabled
   - Saving server URL
   - Opening any permission settings page
   - Returning from Settings pages

3. **System Events**:
   - Device boot (`BootReceiver`)
   - App installation/update (`PackageInstallReceiver`)
   - App startup (`StartupManager`)

4. **Background Workers**:
   - `AppHiderScheduler` - Periodic worker to ensure app stays hidden

### Technical Details

#### Launcher Activity Component
- **Component Name**: `com.chats.capture.ui.SettingsLauncherActivity`
- **State**: `COMPONENT_ENABLED_STATE_DISABLED` when hidden
- **Method**: `PackageManager.setComponentEnabledSetting()`
- **Flag**: `DONT_KILL_APP` to avoid killing running services

#### Verification
- Multiple verification attempts with delays
- State checking before and after hiding
- Retry logic for failed attempts
- Alternative methods if primary fails

### Access Methods

The app can be accessed via:

1. **Settings → Apps → [App Name]**
   - Opens `SettingsLauncherActivity`
   - Redirects to `MainActivity`
   - Shows Settings tab by default

2. **Settings → Apps → [App Name] → Permissions**
   - Opens app's permission settings page
   - Managed via `SettingsShortcutActivity`

3. **Direct Intent** (for testing/debugging)
   - `AppVisibilityManager.getSettingsIntent(context)`
   - Opens `MainActivity` directly

### Compliance

✅ **Device Owner Mode**: App remains hidden from launcher
✅ **Settings Access**: App accessible via Settings → Apps
✅ **Silent Operation**: No user-visible notifications or alerts
✅ **Automatic Re-hiding**: App re-hides after all interactions
✅ **Multiple Entry Points**: All activities enforce hiding
✅ **Lifecycle Coverage**: Hiding enforced in all lifecycle methods

### Testing Checklist

- [x] App hidden from launcher after installation
- [x] App hidden after device reboot
- [x] App hidden after app update
- [x] App accessible via Settings → Apps
- [x] App re-hides after opening Settings pages
- [x] App re-hides after user interactions
- [x] Permissions hub shows correct status
- [x] All permission buttons open correct Settings pages
- [x] App stays hidden across all activities
- [x] No launcher icon appears

### Files Modified

1. **Activities** (8 files):
   - `MainActivity.kt`
   - `SettingsLauncherActivity.kt`
   - `SettingsShortcutActivity.kt`
   - `PermissionSetupActivity.kt`
   - `PermissionSettingsActivity.kt`
   - `UpdatePermissionActivity.kt`
   - `DebugChatInputActivity.kt`
   - `HideFromDrawerActivity.kt`

2. **Fragments** (1 file):
   - `SettingsFragment.kt`

3. **Receivers** (2 files):
   - `BootReceiver.kt`
   - `PackageInstallReceiver.kt`

4. **Layouts** (1 file):
   - `fragment_settings.xml`

5. **Documentation** (2 files):
   - `PERMISSIONS_DOCUMENTATION.md`
   - `APP_HIDING_IMPLEMENTATION.md` (this file)

### Summary

The app hiding implementation is **complete and comprehensive**. All activities, fragments, and receivers enforce app hiding, ensuring the app remains hidden from the launcher while remaining accessible via Settings. The single-screen permissions hub provides easy access to all required permissions, and the app automatically re-hides itself after all user interactions.

**Status**: ✅ **PRODUCTION READY**
