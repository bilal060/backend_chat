# App Hiding Troubleshooting Guide

## Issue: App Still Showing in App Drawer

If the app is still visible in the app drawer after installation, follow these steps:

### Step 1: Verify Device Owner Status

Check if Device Owner is active (required for reliable app hiding):

```bash
adb shell dpm list-owners
```

You should see `com.chats.capture` listed. If not, Device Owner provisioning needs to be completed.

### Step 2: Check Component State

Verify the launcher component state:

```bash
adb shell pm get-component-enabled-setting com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

Expected output: `newState=2` (DISABLED)

### Step 3: Manually Disable Component (If Needed)

If the app is still showing, manually disable the component via ADB:

```bash
adb shell pm disable-user com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

Or for system-level (requires root or Device Owner):

```bash
adb shell pm disable com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

### Step 4: Check Logcat

Monitor the app hiding attempts:

```bash
adb logcat -s APP_HIDER:D
```

Look for messages like:
- `✅ App successfully hidden from launcher`
- `⚠️ Component state is X (expected 2=DISABLED)`
- `Security exception - may need Device Owner or system permissions`

### Step 5: Verify Launcher Cache

Some launchers cache the app list. Try:

1. **Restart the launcher:**
   ```bash
   adb shell am force-stop com.android.launcher3
   # Or for Samsung:
   adb shell am force-stop com.sec.android.app.launcher
   ```

2. **Clear launcher data (if needed):**
   ```bash
   adb shell pm clear com.android.launcher3
   ```

3. **Reboot the device:**
   ```bash
   adb reboot
   ```

### Step 6: Alternative - Remove LAUNCHER Category (Permanent Solution)

If component disabling doesn't work, you can permanently remove the LAUNCHER category from the manifest:

**In `AndroidManifest.xml`, remove or comment out:**

```xml
<!-- Remove this intent-filter to permanently hide from launcher -->
<!--
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
-->
```

**Note:** This will make the app inaccessible from the app drawer permanently. The app will still be accessible via:
- Settings → Apps → [App Name]
- Direct intent: `adb shell am start -n com.chats.capture/.ui.MainActivity`

### Step 7: Verify App Hiding Worker

Check if the periodic worker is running:

```bash
adb shell dumpsys jobscheduler | grep -A 10 "app_hider"
```

The worker should run every 15 minutes to ensure the app stays hidden.

### Common Issues

1. **SecurityException**: App doesn't have permission to disable components
   - **Solution**: Ensure Device Owner is active or use ADB command

2. **Component state remains enabled**: Launcher is ignoring disabled state
   - **Solution**: Remove LAUNCHER category from manifest (Step 6)

3. **Launcher cache**: Launcher shows cached app list
   - **Solution**: Restart launcher or reboot device (Step 5)

4. **Component re-enabled**: Something is re-enabling the component
   - **Solution**: Check for other apps or system processes that might be enabling it

### Manual ADB Command (Quick Fix)

To immediately hide the app:

```bash
adb shell pm disable-user com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

To verify it's hidden:

```bash
adb shell pm get-component-enabled-setting com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

Expected: `newState=2`

### Re-enable for Testing

If you need to show the app again for testing:

```bash
adb shell pm enable com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity
```

### Access App After Hiding

Even when hidden from launcher, you can access the app via:

1. **Settings → Apps → [App Name]**
2. **ADB command:**
   ```bash
   adb shell am start -n com.chats.capture/.ui.MainActivity
   ```
3. **Direct intent from another app** (if you have one)

### Diagnostic Commands

Check current state:
```bash
# Component state
adb shell pm get-component-enabled-setting com.chats.capture/com.chats.capture.ui.SettingsLauncherActivity

# Device Owner status
adb shell dpm list-owners

# App visibility
adb shell pm list packages | grep chats
```

### If Nothing Works

If component disabling doesn't work and you need the app permanently hidden:

1. **Remove LAUNCHER category from manifest** (see Step 6)
2. **Rebuild and reinstall the app**
3. **Access via Settings → Apps → [App Name]**

This is the most reliable method but requires a rebuild.
