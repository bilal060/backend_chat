# OnePlus Device Troubleshooting Guide

## Current Status
- **Device**: OnePlus (Model: Unknown - needs authorization)
- **Connection**: Connected but **UNAUTHORIZED**
- **Device ID**: 7f7e113e
- **Status**: USB Debugging enabled but not authorized

## Issue: Device Unauthorized

The device is detected but requires authorization to allow ADB access.

### Step 1: Authorize the Device

**On the OnePlus device:**
1. Look for a popup dialog asking "Allow USB debugging?"
2. Check the box "Always allow from this computer" (if available)
3. Tap **"Allow"** or **"OK"**

**If no popup appears:**
1. Go to **Settings** → **About phone**
2. Tap **Build number** 7 times to enable Developer options
3. Go to **Settings** → **System** → **Developer options**
4. Enable **USB debugging**
5. Enable **USB debugging (Security settings)** if available
6. Revoke USB debugging authorizations
7. Disconnect and reconnect the USB cable
8. The authorization popup should appear

### Step 2: Verify Authorization

```bash
# Check device status
adb devices

# Should show: "7f7e113e    device" (not "unauthorized")
```

## If Device Screen is Broken/Not Working

### Option 1: Use Recovery Mode

If the screen is broken but device boots:

```bash
# Boot into recovery mode
adb reboot recovery

# Or use hardware keys:
# Power + Volume Down (hold for 10 seconds)
```

### Option 2: Use Fastboot Mode

```bash
# Boot into fastboot
adb reboot bootloader

# Or hardware keys:
# Power + Volume Up (hold for 10 seconds)

# Check fastboot connection
fastboot devices
```

### Option 3: Try Wireless ADB (if WiFi works)

```bash
# Enable wireless debugging (if screen works partially)
# Settings → Developer options → Wireless debugging

# Or via ADB if you can get one authorized connection
adb tcpip 5555
adb connect <device-ip>:5555
```

## Diagnosing "Broken" Issues

### Check What's Actually Broken

```bash
# Once authorized, check device info
adb shell getprop ro.product.model
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.name

# Check if device boots
adb shell getprop sys.boot_completed

# Check battery status
adb shell dumpsys battery

# Check screen state
adb shell dumpsys display | grep "mScreenState"

# Check if device is responsive
adb shell input keyevent KEYCODE_WAKEUP
```

### Common "Broken" Scenarios

1. **Screen broken but device works**:
   - Device can still be controlled via ADB
   - Can enable screen mirroring via ADB
   - Can backup data

2. **Device won't boot**:
   - Try recovery mode
   - Try fastboot mode
   - May need factory reset or firmware flash

3. **Touch screen broken**:
   - Can use ADB for input
   - Can enable screen mirroring to see screen
   - Can use mouse via USB OTG

4. **Software issues**:
   - Can use ADB to diagnose
   - Can clear app data
   - Can factory reset if needed

## Screen Mirroring for Broken Screen

Once authorized, you can enable screen mirroring even with a broken screen:

```bash
# Enable WiFi Display
adb shell settings put global wifi_display_on 1

# Launch OnePlus Screen Mirroring (if available)
adb shell am start -n com.oneplus.screenrecord/.MainActivity

# Or use Media Projection
adb shell screenrecord /sdcard/mirror_test.mp4
```

## Quick Fix Commands

```bash
# 1. Restart ADB server
adb kill-server
adb start-server

# 2. Check connection
adb devices

# 3. If still unauthorized, try:
adb reconnect

# 4. Check device in recovery
adb reboot recovery

# 5. Check device in fastboot
adb reboot bootloader
fastboot devices
```

## Next Steps

1. **Authorize the device** on the phone screen (if visible)
2. **Check what's broken** - screen, touch, software, hardware?
3. **Determine recovery options** based on what works
4. **Enable screen mirroring** if screen is broken but device works

## OnePlus-Specific Screen Mirroring

OnePlus devices typically support:
- **OnePlus Screen Mirroring** app
- **Cast** feature (Google Cast)
- **Miracast** (WiFi Display)
- **USB Screen Mirroring** via ADB

Once authorized, we can check which methods are available.
