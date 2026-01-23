# Manual Fastboot Mode Entry for OnePlus Device

## Current Situation
- **Device**: OnePlus (Unauthorized in ADB)
- **Status**: Cannot use `adb reboot bootloader` (requires authorization)
- **Solution**: Use hardware keys to enter fastboot manually

## Method 1: Hardware Keys (Recommended)

### Steps:
1. **Power off the device completely**
   - Hold Power button for 10+ seconds until it shuts down
   - Or remove battery if possible

2. **Enter Fastboot Mode**:
   - **Hold Volume Up + Power button** simultaneously
   - Keep holding for 10-15 seconds
   - Release when you see fastboot logo or OnePlus logo with "Fastboot Mode" text

3. **Alternative method** (if above doesn't work):
   - Hold **Volume Down + Power button**
   - Some OnePlus models use this combination

### Visual Indicators:
- Fastboot mode shows: "Fastboot Mode" text
- Or shows: OnePlus logo with bootloader menu
- Screen may be black with text, or show bootloader interface

## Method 2: Recovery Mode First

If fastboot doesn't work directly:

1. **Enter Recovery Mode**:
   - Power off device
   - Hold **Volume Down + Power button**
   - Release when recovery menu appears

2. **From Recovery, select "Reboot to Bootloader"**:
   - Use volume keys to navigate
   - Power button to select
   - Select "Reboot to bootloader" or "Reboot to fastboot"

## Once in Fastboot Mode

### Verify Connection:
```bash
fastboot devices
```

### Get Device Information:
```bash
# Get device model
fastboot getvar product

# Get all device info
fastboot getvar all

# Get serial number
fastboot getvar serialno

# Check bootloader status
fastboot getvar unlocked
```

### Check Screen Mirroring Support (if device info available):
```bash
# Get Android version
fastboot getvar version-baseband

# Get hardware info
fastboot getvar hw-revision
```

## Fastboot Commands for Broken Screen

If screen is broken but device boots:

### 1. Flash Recovery (if needed):
```bash
fastboot flash recovery recovery.img
```

### 2. Boot Custom Recovery:
```bash
fastboot boot recovery.img
```

### 3. Check Partitions:
```bash
fastboot getvar partition-size:system
fastboot getvar partition-size:userdata
```

### 4. Unlock Bootloader (if needed):
```bash
fastboot oem unlock
# WARNING: This will wipe all data!
```

## Troubleshooting

### If fastboot doesn't detect device:

1. **Check USB connection**:
   ```bash
   lsusb | grep -i "oneplus\|qualcomm"
   # On Mac:
   system_profiler SPUSBDataType | grep -A 5 -i "oneplus"
   ```

2. **Install fastboot drivers** (if needed):
   - OnePlus USB drivers
   - Or generic Android USB drivers

3. **Try different USB port/cable**

4. **Check if device is in correct mode**:
   - Should show "Fastboot Mode" on screen
   - Or show bootloader menu

### If device won't enter fastboot:

1. **Try different key combinations**:
   - Volume Up + Power
   - Volume Down + Power
   - Volume Up + Volume Down + Power

2. **Try with device powered off**:
   - Remove battery (if possible)
   - Wait 10 seconds
   - Insert battery
   - Try key combination

3. **Check if device is charging**:
   - Connect to charger
   - Wait for charging indicator
   - Then try fastboot entry

## OnePlus-Specific Fastboot

OnePlus devices typically support:
- **Fastboot mode**: Standard Android fastboot
- **EDL Mode**: Emergency Download Mode (for deep recovery)
- **Recovery Mode**: Custom OnePlus recovery

### Enter EDL Mode (if fastboot fails):
1. Power off device
2. Hold **Volume Up + Volume Down + Power** (all three)
3. Device enters EDL mode (Qualcomm 9008 mode)
4. Can use Qualcomm tools for recovery

## Next Steps After Fastboot Entry

Once in fastboot and detected:

1. **Identify device model**:
   ```bash
   fastboot getvar product
   ```

2. **Check bootloader status**:
   ```bash
   fastboot getvar unlocked
   ```

3. **Get device info for screen mirroring**:
   - Model number
   - Android version
   - Hardware capabilities

4. **If screen is broken, consider**:
   - Flashing recovery
   - Booting custom recovery
   - Using recovery to enable ADB
   - Then enable screen mirroring via ADB

## Important Notes

⚠️ **WARNING**: 
- Unlocking bootloader will **wipe all data**
- Flashing wrong firmware can **brick device**
- Only proceed if you know what you're doing

✅ **Safe Operations**:
- Getting device info (`fastboot getvar`)
- Checking status
- Booting (not flashing) recovery images

## Quick Reference

```bash
# Check if in fastboot
fastboot devices

# Get device info
fastboot getvar product
fastboot getvar version-baseband

# Boot recovery (safe, doesn't flash)
fastboot boot recovery.img

# Reboot to system
fastboot reboot

# Reboot to recovery
fastboot reboot recovery
```
