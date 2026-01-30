# ADB Commands to Check if App is Installed

## Quick Check Commands

### 1. Check if App is Installed (Basic)
```bash
adb shell pm list packages | grep com.chats.capture
```

**Expected Output if Installed:**
```
package:com.chats.capture
```

**Output if NOT Installed:**
```
(no output)
```

---

### 2. Check All User Apps (Third-Party)
```bash
adb shell pm list packages -3 | grep -i capture
```

This lists all third-party (user-installed) apps and filters for "capture".

---

### 3. Get Detailed App Information
```bash
adb shell dumpsys package com.chats.capture
```

This shows comprehensive information including:
- Package name
- Version code and version name
- App label (display name)
- Installation path
- Permissions
- Activities and services

---

### 4. Get App Label/Name Only
```bash
adb shell dumpsys package com.chats.capture | grep -i "label" | head -1
```

**Expected Output:**
```
label=Chat Capture
```

---

### 5. Get App Version
```bash
adb shell dumpsys package com.chats.capture | grep -i "versionName"
```

**Expected Output:**
```
versionName=1.0.0
```

---

### 6. Check Installation Path
```bash
adb shell pm path com.chats.capture
```

**Expected Output if Installed:**
```
package:/data/app/~~XXXXX==/com.chats.capture-XXXXX==/base.apk
```

---

### 7. Get App Info (Short)
```bash
adb shell pm dump com.chats.capture | grep -A 5 "ApplicationInfo"
```

---

## Complete Verification Script

Run this to get all app information:

```bash
#!/bin/bash

APP_PACKAGE="com.chats.capture"

echo "Checking if app is installed..."
echo ""

# Check installation
if adb shell pm list packages | grep -q "$APP_PACKAGE"; then
    echo "✅ App IS INSTALLED"
    echo ""
    
    # Get package path
    echo "📦 Installation Path:"
    adb shell pm path $APP_PACKAGE
    echo ""
    
    # Get app label
    echo "📱 App Name:"
    adb shell dumpsys package $APP_PACKAGE | grep -i "label" | head -1
    echo ""
    
    # Get version
    echo "🔢 Version:"
    adb shell dumpsys package $APP_PACKAGE | grep -i "versionName" | head -1
    echo ""
    
    # Get version code
    echo "🔢 Version Code:"
    adb shell dumpsys package $APP_PACKAGE | grep -i "versionCode" | head -1
    echo ""
    
else
    echo "❌ App is NOT INSTALLED"
    echo ""
    echo "To install:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
fi
```

---

## One-Line Commands

### Quick Check (Returns 0 if installed, 1 if not)
```bash
adb shell pm list packages | grep -q com.chats.capture && echo "INSTALLED" || echo "NOT INSTALLED"
```

### Check and Show Details
```bash
adb shell pm list packages | grep com.chats.capture && adb shell dumpsys package com.chats.capture | grep -E "label|versionName|versionCode" | head -3
```

### Check Installation Path Only
```bash
adb shell pm path com.chats.capture 2>/dev/null || echo "App not installed"
```

---

## Troubleshooting

### If "device not found"
```bash
# Check if device is connected
adb devices

# If no device, connect via USB and enable USB debugging
```

### If "package not found"
The app is not installed. Install it with:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### If you get permission errors
```bash
# Try with root (if device is rooted)
adb root
adb shell pm list packages | grep com.chats.capture
```

---

## Useful ADB Commands for App Management

### Install App
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Uninstall App
```bash
adb uninstall com.chats.capture
```

### Open App
```bash
adb shell am start -n com.chats.capture/.ui.MainActivity
```

### Open App Settings
```bash
adb shell am start -a android.settings.APPLICATION_DETAILS_SETTINGS -d package:com.chats.capture
```

### Clear App Data
```bash
adb shell pm clear com.chats.capture
```

### Force Stop App
```bash
adb shell am force-stop com.chats.capture
```

---

## Summary

**Most Common Command:**
```bash
adb shell pm list packages | grep com.chats.capture
```

**If you see:** `package:com.chats.capture` → App is installed ✅  
**If you see:** (nothing) → App is NOT installed ❌
