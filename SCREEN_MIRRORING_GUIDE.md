# Screen Mirroring Setup Guide for Samsung Galaxy A15

## Device Information
- **Model**: Samsung Galaxy A15 (SM-A155F)
- **Android Version**: 14 (API Level 34)
- **Screen Mirroring Support**: ✅ YES

## Confirmed Capabilities

### ✅ WiFi Display (Miracast) Support
- WifiDisplayAdapter: Present
- WiFi Direct: Supported
- Can mirror to compatible displays/TVs

### ✅ Samsung Smart Mirroring App
- Package: `com.samsung.android.smartmirroring`
- Version: 8.2.23.16
- Installed and ready to use

### ✅ Media Projection API
- Service available for app-based screen recording/mirroring

## How to Enable and Test Screen Mirroring

### Method 1: Enable WiFi Display via ADB

```bash
# 1. Connect device via USB and enable USB debugging
adb devices

# 2. Enable WiFi Display
adb shell settings put global wifi_display_on 1

# 3. Verify it's enabled
adb shell settings get global wifi_display_on
# Should return: 1

# 4. Check WiFi Display status
adb shell dumpsys display | grep -A 5 "WifiDisplayAdapter"
```

### Method 2: Start Samsung Smart Mirroring App

```bash
# Launch Smart Mirroring app
adb shell am start -n com.samsung.android.smartmirroring/.MainActivity

# Or try alternative activity names
adb shell monkey -p com.samsung.android.smartmirroring -c android.intent.category.LAUNCHER 1
```

### Method 3: Via Device Settings (Manual)

1. Open **Settings** on the device
2. Go to **Connections** → **More connection settings**
3. Enable **Screen mirroring** or **Smart View**
4. Select your TV or display device

## Testing Screen Mirroring

### Check Current Status

```bash
# Check WiFi Display status
adb shell dumpsys display | grep -i "wifi\|mirror"

# Check active displays
adb shell cmd display get-displays

# Check Media Projection
adb shell dumpsys media_projection
```

### Test Screen Recording (Alternative)

```bash
# Start screen recording (tests Media Projection)
adb shell screenrecord /sdcard/test_mirror.mp4
# Press Ctrl+C to stop after a few seconds

# Pull the video to verify
adb pull /sdcard/test_mirror.mp4
```

## Troubleshooting

### If WiFi Display is not working:

1. **Enable WiFi P2P**:
   ```bash
   adb shell svc wifi enable
   ```

2. **Check WiFi Display scan**:
   ```bash
   adb shell dumpsys display | grep -i "scan\|peer"
   ```

3. **Restart WiFi Display service**:
   ```bash
   adb shell stop wfd
   adb shell start wfd
   ```

### If Smart Mirroring app won't start:

1. **Clear app data**:
   ```bash
   adb shell pm clear com.samsung.android.smartmirroring
   ```

2. **Check app permissions**:
   ```bash
   adb shell dumpsys package com.samsung.android.smartmirroring | grep permission
   ```

## Quick Test Commands

```bash
# Enable WiFi Display
adb shell settings put global wifi_display_on 1

# Launch Smart Mirroring
adb shell am start -n com.samsung.android.smartmirroring/.MainActivity

# Check status
adb shell dumpsys display | grep -A 10 "WifiDisplayAdapter"
```

## Notes

- **Current Status**: WiFi Display is disabled by default (`wifi_display_on=0`)
- **Requires**: Both device and target display must be on the same WiFi network
- **Alternative**: Use USB cable for wired screen mirroring via ADB
- **OnePlus 11 Pro**: If you have a OnePlus device, it uses different mirroring methods (OnePlus Screen Mirroring or Cast)

## Next Steps

1. Reconnect your device via USB
2. Run the enable command: `adb shell settings put global wifi_display_on 1`
3. Launch Smart Mirroring app
4. Select your TV/display from the list
5. Start mirroring!
