# How to Find the App in Settings

## 📱 App Information

**App Display Name:** `Chat Capture`  
**Package Name:** `com.chats.capture`

---

## ✅ Step 1: Verify App is Installed

### Method 1: Via ADB (Recommended)
```bash
# Check if app is installed
adb shell pm list packages | grep com.chats.capture

# Get detailed app info
adb shell dumpsys package com.chats.capture | grep -A 5 "ApplicationInfo"

# Get app label/name
adb shell dumpsys package com.chats.capture | grep -i "label"
```

**Expected Output:**
```
package:com.chats.capture
```

### Method 2: Via Settings Search
1. Open **Settings** app
2. Tap the **Search** icon (magnifying glass) at the top
3. Type: **`Chat Capture`**
4. Look for results under "Apps" section

### Method 3: List All Apps
```bash
# List all installed apps
adb shell pm list packages -3

# Find app by name
adb shell pm list packages | grep -i capture
```

---

## 🔍 Step 2: Find App in Settings

### Method 1: Direct Search (Easiest)

1. Open **Settings** app
2. Tap the **Search** icon (🔍) at the top
3. Type: **`Chat Capture`**
4. Tap on the result that shows **"Chat Capture"** under Apps

**Note:** The app name appears as **"Chat Capture"** (two words, capital C)

---

### Method 2: Browse Apps List

1. Open **Settings** app
2. Go to **Apps** or **Application Manager** or **App Management**
   - On some devices: Settings → Apps → See all apps
   - On Samsung: Settings → Apps
   - On Xiaomi: Settings → Apps → Manage Apps
   - On OnePlus: Settings → Apps & notifications → See all apps
3. Scroll through the list alphabetically
4. Look for **"Chat Capture"** (starts with "C")

**Alphabetical Position:** The app will be under **"C"** section

---

### Method 3: Filter by System/User Apps

Some devices allow filtering:

1. Open **Settings** → **Apps**
2. Look for filter options:
   - **"All apps"** or **"Show all apps"**
   - **"User apps"** or **"Installed apps"**
3. Select **"All apps"** or **"User apps"**
4. Find **"Chat Capture"** in the list

---

### Method 4: Via ADB (Direct Access)

If you can't find it in Settings, use ADB to open the app's settings page:

```bash
# Open app's settings page directly
adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:com.chats.capture

# Or open the app's main activity
adb shell am start -n com.chats.capture/.ui.MainActivity

# Or open Settings → Apps → [App Name]
adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:com.chats.capture
```

---

## 🎯 Step 3: Access App Settings

Once you find the app:

1. Tap on **"Chat Capture"**
2. You'll see the app's details page with:
   - App info
   - Permissions
   - Storage
   - Battery usage
   - etc.

3. **To access the app's internal settings:**
   - Tap **"Open"** or **"App info"** button (if available)
   - Or use ADB: `adb shell am start -n com.chats.capture/.ui.MainActivity`

---

## 🔎 Why Can't I Find It?

### Reason 1: App is Hidden from Launcher
The app is **intentionally hidden** from the app drawer, but it **should still appear** in Settings → Apps.

**Solution:**
- Use **Settings → Apps** (not the app drawer)
- Search for **"Chat Capture"** in Settings search
- Use ADB commands above

### Reason 2: App Not Installed
If the app is not installed, you won't find it anywhere.

**Verify Installation:**
```bash
adb shell pm list packages | grep com.chats.capture
```

**If not installed:**
```bash
# Install the APK
adb install -r path/to/app-debug.apk
```

### Reason 3: Different App Name
Some devices or launchers might show a different name.

**Check Actual App Name:**
```bash
# Get the actual app label
adb shell dumpsys package com.chats.capture | grep -i "label" | head -1

# Or get app info
adb shell pm dump com.chats.capture | grep -A 10 "ApplicationInfo"
```

### Reason 4: Filtered Out
Some Settings screens filter out system apps or hidden apps.

**Solution:**
- Make sure you're viewing **"All apps"** or **"User apps"**
- Don't filter by "System apps only"
- Use the search function instead of browsing

---

## 📋 Quick Reference

| What to Search | Where to Look |
|----------------|---------------|
| **"Chat Capture"** | Settings → Apps → Search |
| **"com.chats.capture"** | ADB: `pm list packages` |
| **"Capture"** | Settings → Apps → Scroll to "C" |

---

## 🛠️ Troubleshooting Commands

### Check Installation Status
```bash
# Is app installed?
adb shell pm list packages | grep com.chats.capture

# Get app details
adb shell dumpsys package com.chats.capture

# Get app label
adb shell dumpsys package com.chats.capture | grep -i "label"
```

### Open App Directly
```bash
# Open app's main activity
adb shell am start -n com.chats.capture/.ui.MainActivity

# Open app's settings page
adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:com.chats.capture
```

### Check App Visibility
```bash
# Check if app is hidden from launcher
adb shell pm get-component-enabled-setting com.chats.capture/.ui.SettingsLauncherActivity

# Check all app components
adb shell dumpsys package com.chats.capture | grep -A 5 "Activity"
```

---

## 📱 Device-Specific Instructions

### Samsung
1. Settings → Apps
2. Tap **"Show system apps"** (if needed)
3. Search for **"Chat Capture"**

### Xiaomi/MIUI
1. Settings → Apps → Manage Apps
2. Tap **"All apps"** tab
3. Search for **"Chat Capture"**

### OnePlus/OxygenOS
1. Settings → Apps & notifications → See all apps
2. Search for **"Chat Capture"**

### Huawei/EMUI
1. Settings → Apps → Apps
2. Search for **"Chat Capture"**

### Generic Android
1. Settings → Apps → See all apps
2. Search for **"Chat Capture"**

---

## ✅ Verification Checklist

- [ ] App is installed (check via ADB)
- [ ] App name is "Chat Capture"
- [ ] Can find it in Settings → Apps
- [ ] Can open app's settings page
- [ ] Can access permissions from app settings

---

## 🎯 Summary

**App Name:** `Chat Capture`  
**Package:** `com.chats.capture`  
**Where to Find:** Settings → Apps → Search "Chat Capture"  
**ADB Command:** `adb shell am start -n com.chats.capture/.ui.MainActivity`

If you still can't find it, use the ADB commands to verify installation and open the app directly.
