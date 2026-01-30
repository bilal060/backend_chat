# Social Media Random Screenshot Feature

## Overview
This feature automatically monitors social media apps and takes random, low-resolution screenshots when these apps are in use. Screenshots are captured at readable quality but optimized for storage and bandwidth.

## Supported Apps

The service monitors the following social media apps:
- **WhatsApp** (`com.whatsapp`)
- **WhatsApp Business** (`com.whatsapp.w4b`)
- **Viber** (`com.viber.voip`)
- **Telegram** (`org.telegram.messenger`)
- **Facebook** (`com.facebook.katana`)
- **Facebook Messenger** (`com.facebook.orca`)
- **Instagram** (`com.instagram.android`)
- **Snapchat** (`com.snapchat.android`)
- **Twitter** (`com.twitter.android`)
- **LinkedIn** (`com.linkedin.android`)
- **Skype** (`com.skype.raider`)
- **Discord** (`com.discord`)
- **WeChat** (`com.tencent.mm`)
- **LINE** (`com.linecorp.line`)
- **Signal** (`com.signal.org`)

## Features

### Random Capture
- **Probability**: 15% chance to take screenshot when app is active
- **Randomness**: Prevents predictable patterns
- **Cooldown**: Minimum 30 seconds between screenshots

### Low Resolution but Readable
- **Maximum Width**: 1080px (maintains aspect ratio)
- **Quality**: 75% JPEG compression
- **Format**: JPEG (smaller than PNG)
- **Result**: Readable screenshots with reduced file size

### Smart Timing
- **App Detection**: Checks foreground app every 8 seconds
- **Minimum Active Time**: Waits 5 seconds after app comes to foreground
- **Cooldown Period**: 30 seconds minimum between screenshots

## Implementation

### Components

#### 1. **SocialMediaScreenshotService** (`services/SocialMediaScreenshotService.kt`)
- **Purpose**: Monitors social media apps and captures random screenshots
- **Functionality**:
  - Uses UsageStatsManager to detect foreground apps
  - Randomly decides to take screenshot (15% probability)
  - Captures screenshot using AccessibilityService
  - Resizes and compresses to low resolution
  - Uploads to server immediately
  - Tracks last screenshot time to enforce cooldown

### How It Works

1. **Service Starts**: SocialMediaScreenshotService runs continuously
2. **App Monitoring**: Checks foreground app every 8 seconds
3. **Social Media Detection**: Identifies if current app is a social media app
4. **Timing Check**:
   - App must be active for at least 5 seconds
   - At least 30 seconds since last screenshot
5. **Random Decision**: 15% chance to take screenshot
6. **Screenshot Capture**: Uses AccessibilityService.takeScreenshot()
7. **Resize & Compress**: Reduces to max 1080px width, 75% JPEG quality
8. **Upload**: Immediately uploads to server
9. **Cleanup**: Deletes local file after successful upload

### Screenshot Processing

#### Original Capture
- Full resolution screenshot captured via AccessibilityService
- Saved temporarily as PNG

#### Resize & Compress
- **Resize**: Maintains aspect ratio, max width 1080px
- **Compress**: JPEG format at 75% quality
- **Result**: Typically 50-200KB (vs 2-5MB for full resolution)

#### Example Sizes
- **Original**: 1080x2340 (2.5MB PNG)
- **Resized**: 1080x2340 (150KB JPEG) - No resize needed
- **Original**: 1440x3200 (4MB PNG)
- **Resized**: 1080x2400 (180KB JPEG) - Scaled down

### Upload

- **Endpoint**: `POST /api/devices/{deviceId}/screenshots`
- **Format**: Multipart form data
- **Content-Type**: `image/jpeg`
- **Immediate Upload**: Screenshots uploaded as soon as captured
- **Retry**: Failed uploads keep file for retry

## Configuration

### Adjustable Parameters

```kotlin
// Screenshot probability (0.0 to 1.0)
private val screenshotProbability = 0.15f // 15% chance

// Check interval (seconds)
private val checkIntervalSeconds = 8L // Check every 8 seconds

// Minimum time between screenshots (milliseconds)
private val minTimeBetweenScreenshots = 30000L // 30 seconds

// Maximum width for resized screenshots
private val maxWidth = 1080 // pixels

// JPEG quality (0-100)
private val quality = 75 // 75% quality
```

## Registration

### AndroidManifest.xml
- **SocialMediaScreenshotService**: Registered as foreground service with `dataSync` type

### Service Starter
- `ServiceStarter.startSocialMediaScreenshotService()`: Starts the service
- Automatically started via `ensureServicesRunning()` which is called:
  - On app startup (CaptureApplication)
  - On boot (BootReceiver)
  - On app install/update (PackageInstallReceiver)

## Permissions Required

- **Usage Stats Permission**: Required to detect foreground apps
  - `android.permission.PACKAGE_USAGE_STATS`
  - User must grant in Settings → Apps → Special access → Usage access

- **Accessibility Service**: Required for screenshot capture
  - Already configured for other features
  - Uses `AccessibilityService.takeScreenshot()` (Android 11+)

## Logging

All operations are logged with `SOCIAL_SCREENSHOT` tag:
- `📸 Taking random screenshot of {app}`: Screenshot triggered
- `Screenshot captured`: Screenshot file created
- `Screenshot resized`: Resize and compression completed
- `✅ Screenshot uploaded successfully`: Upload successful
- `⚠️ Screenshot upload failed`: Upload failed, will retry

## Monitoring

### Check Service Status
```bash
adb shell dumpsys activity services com.chats.capture | grep SocialMediaScreenshot
```

### View Logs
```bash
adb logcat -s SOCIAL_SCREENSHOT
```

### Check Screenshot Files (Before Upload)
```bash
adb shell "run-as com.chats.capture ls -la files/screenshots/social_media/"
```

## Performance Considerations

### Battery
- **Low Impact**: Checks every 8 seconds (not continuous)
- **Random Capture**: Only 15% chance reduces frequency
- **Cooldown**: 30-second minimum prevents rapid captures

### Storage
- **Temporary Files**: Screenshots deleted after upload
- **Low Resolution**: 50-200KB per screenshot (vs 2-5MB full res)
- **JPEG Compression**: Further reduces file size

### Network
- **Immediate Upload**: Screenshots uploaded as soon as captured
- **Small Files**: Low resolution means fast uploads
- **Retry Logic**: Failed uploads retry automatically

## Error Handling

- **AccessibilityService Not Available**: Logged and skipped
- **Screenshot Capture Failed**: Logged and skipped
- **Resize Failed**: Original deleted, no upload
- **Upload Failed**: File kept for retry
- **Network Unavailable**: File kept, will retry when network available

## Testing

### Test Screenshot Capture
1. Open WhatsApp (or any social media app)
2. Wait 5+ seconds
3. Check logcat: `adb logcat -s SOCIAL_SCREENSHOT`
4. Should see screenshot capture within 30-60 seconds (random)
5. Verify screenshot uploaded to server

### Test Randomness
1. Open social media app
2. Monitor logs for 5 minutes
3. Should see screenshots at random intervals
4. Should not capture more than once per 30 seconds

### Test Low Resolution
1. Capture screenshot on high-resolution device
2. Check file size: Should be 50-200KB
3. Verify image is readable but compressed

## Troubleshooting

### Screenshots Not Being Captured
1. Check service is running: `adb shell dumpsys activity services | grep SocialMediaScreenshot`
2. Check Usage Stats permission: Settings → Apps → Special access → Usage access
3. Check Accessibility Service: Settings → Accessibility
4. Check logs: `adb logcat -s SOCIAL_SCREENSHOT`
5. Verify app is in monitored list

### Screenshots Too Frequent/Infrequent
- Adjust `screenshotProbability` (0.15 = 15% chance)
- Adjust `minTimeBetweenScreenshots` (30000 = 30 seconds)
- Adjust `checkIntervalSeconds` (8 = check every 8 seconds)

### Screenshots Too Large
- Reduce `maxWidth` (1080 = max width in pixels)
- Reduce `quality` (75 = JPEG quality 0-100)

### Upload Failures
- Check network connectivity
- Check server endpoint: `/api/devices/{deviceId}/screenshots`
- Check logs for error messages
- Files are kept for retry if upload fails

## Privacy & Security

- **Random Capture**: Prevents predictable patterns
- **Low Resolution**: Reduces storage and bandwidth
- **Immediate Upload**: Screenshots not stored locally long-term
- **Automatic Cleanup**: Files deleted after successful upload
- **Cooldown Period**: Prevents excessive captures

## Future Enhancements

1. **Configurable Probability**: Make screenshot probability configurable
2. **Time-Based Rules**: Capture more during certain hours
3. **App-Specific Settings**: Different settings per app
4. **Smart Compression**: Adaptive quality based on content
5. **Batch Upload**: Upload multiple screenshots together
6. **Thumbnail Generation**: Generate thumbnails for quick preview
