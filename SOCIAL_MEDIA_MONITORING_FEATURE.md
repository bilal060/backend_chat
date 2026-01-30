# Social Media Media Monitoring Feature

## Overview
This feature automatically monitors WhatsApp, WhatsApp Business, and Viber directories for new images, videos, and audio files, and immediately uploads them to the server.

## Supported Apps

### ✅ WhatsApp (`com.whatsapp`)
- **Images**: JPG, JPEG, PNG, WebP, GIF
- **Videos**: MP4, WebM, MOV, AVI, MKV, 3GP, M4V
- **Audio**: MP3, OGG, M4A, Opus, AAC, WAV, AMR
- **Directories Monitored**:
  - `WhatsApp Images` (including Sent and Statuses)
  - `WhatsApp Video` (including Sent and Statuses)
  - `WhatsApp Audio` (including Sent and Voice Notes)

### ✅ WhatsApp Business (`com.whatsapp.w4b`)
- **Same media types as WhatsApp**
- **Directories Monitored**:
  - `WhatsApp Business Images` (including Sent and Statuses)
  - `WhatsApp Business Video` (including Sent and Statuses)
  - `WhatsApp Business Audio` (including Sent and Voice Notes)

### ✅ Viber (`com.viber.voip`)
- **Same media types as WhatsApp**
- **Directories Monitored**:
  - `Viber/Images`
  - `Viber/Videos`
  - `Viber/Audio`
  - `Viber/Voice Notes`
  - `ViberMedia` (legacy location)

## Implementation

### Components

#### 1. **SocialMediaMonitorService** (`services/SocialMediaMonitorService.kt`)
- **Purpose**: Monitors social media app directories in real-time
- **Functionality**:
  - Uses ContentObserver to watch MediaStore (Images, Videos, Audio)
  - Periodically scans app-specific directories (every 20 seconds)
  - Detects new files (modified in last 10 minutes)
  - Immediately uploads files < 50MB
  - Saves larger files for regular sync
  - Tracks processed files to avoid duplicates

#### 2. **Enhanced WhatsAppMediaScanner** (`utils/WhatsAppMediaScanner.kt`)
- **Updated**: Now includes video file support
- **Functionality**: Scans WhatsApp directories for all media types

### File Size Limits

- **Immediate Upload**: Files ≤ 50MB
  - Uploaded immediately when detected
  - Higher limit than downloads (10MB) because social media files are typically larger
  
- **Regular Sync**: Files > 50MB
  - Saved to database
  - Uploaded during regular sync cycles
  - Uses chunked upload for large files

### Directory Locations

#### Android 10+ (Scoped Storage)
- **WhatsApp**: `/sdcard/Android/media/com.whatsapp/WhatsApp/Media/`
- **WhatsApp Business**: `/sdcard/Android/media/com.whatsapp.w4b/WhatsApp Business/Media/`
- **Viber**: `/sdcard/Android/media/com.viber.voip/Viber/`

#### Android 9 and Below
- **WhatsApp**: `/sdcard/WhatsApp/Media/`
- **WhatsApp Business**: `/sdcard/WhatsApp Business/Media/`
- **Viber**: `/sdcard/Viber/` or `/sdcard/ViberMedia/`

## How It Works

### Real-Time Monitoring Flow

1. **Service Starts**: SocialMediaMonitorService runs continuously
2. **ContentObserver**: Watches MediaStore for new images, videos, audio
3. **Directory Scanning**: Periodically scans app directories (every 20 seconds)
4. **File Detection**: Detects files modified in last 10 minutes
5. **File Processing**:
   - Check if already processed (avoid duplicates)
   - Check file size:
     - ≤ 50MB: Upload immediately
     - > 50MB: Save for regular sync
6. **Upload**: Files uploaded to server immediately
7. **Status Logging**: All operations logged with `SOCIAL_MEDIA_MONITOR` tag

### Notification-Based Capture

In addition to directory monitoring, media is also captured from notifications:
- When a notification with media is received
- Media is extracted and downloaded immediately
- Works for all apps, including WhatsApp, WhatsApp Business, Viber

## Registration

### AndroidManifest.xml
- **SocialMediaMonitorService**: Registered as foreground service with `dataSync` type

### Service Starter
- `ServiceStarter.startSocialMediaMonitorService()`: Starts the monitoring service
- Automatically started via `ensureServicesRunning()` which is called:
  - On app startup (CaptureApplication)
  - On boot (BootReceiver)
  - On app install/update (PackageInstallReceiver)

## Logging

All operations are logged with `SOCIAL_MEDIA_MONITOR` tag:
- `📱 New {app} media detected`: New media file found
- `✅ {app} media uploaded immediately`: Upload successful
- `⚠️ {app} media upload queued`: Upload failed, will retry
- `File size exceeds 50MB limit`: Large file saved for regular sync

## Monitoring

### Check Service Status
```bash
adb shell dumpsys activity services com.chats.capture | grep SocialMediaMonitor
```

### View Logs
```bash
adb logcat -s SOCIAL_MEDIA_MONITOR
```

### Check Uploaded Files
```bash
# Check database for social media files
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT id, appPackage, notificationId, fileSize, uploadStatus FROM media_files WHERE appPackage IN (\"com.whatsapp\", \"com.whatsapp.w4b\", \"com.viber.voip\") ORDER BY createdAt DESC LIMIT 20;'"
```

## Performance Considerations

1. **Battery**:
   - Periodic scans limited to 20 seconds (more frequent than downloads)
   - Only processes files modified in last 10 minutes
   - Immediate uploads use existing upload infrastructure

2. **Storage**:
   - Tracks processed files in memory (cleared on service restart)
   - Duplicate detection via checksum prevents re-uploading
   - Large files (>50MB) saved for regular sync to avoid blocking

3. **Network**:
   - Files ≤ 50MB uploaded immediately
   - Larger files uploaded during regular sync
   - Uses existing retry mechanism for failed uploads

## Error Handling

- **File not found**: Logged and skipped
- **Permission denied**: Logged and skipped
- **Upload failure**: File marked as `FAILED`, will retry during regular sync
- **Network unavailable**: File marked as `PENDING`, will retry when network available
- **Large files**: Saved for regular sync (not immediate upload)

## Testing

### Test WhatsApp Media
1. Receive an image/video/audio in WhatsApp
2. Check logcat: `adb logcat -s SOCIAL_MEDIA_MONITOR`
3. Verify file appears in database
4. Verify file is uploaded to server

### Test WhatsApp Business Media
1. Receive media in WhatsApp Business
2. Check logcat for immediate upload
3. Verify file is on server

### Test Viber Media
1. Receive media in Viber
2. Check logcat for immediate upload
3. Verify file is on server

### Test Large Files
1. Receive a file > 50MB
2. Verify it's NOT uploaded immediately
3. Verify it's captured and uploaded during regular sync

## Troubleshooting

### Media Not Uploading Immediately
1. Check service is running: `adb shell dumpsys activity services | grep SocialMediaMonitor`
2. Check file size: Must be ≤ 50MB for immediate upload
3. Check network: Must be connected
4. Check logs: `adb logcat -s SOCIAL_MEDIA_MONITOR`
5. Check app directories exist and are accessible

### Duplicate Uploads
- Checksum prevents duplicates
- If duplicates occur, check database for existing entries

### Service Not Starting
- Check AndroidManifest.xml registration
- Check ServiceStarter.ensureServicesRunning() is called
- Check boot receiver starts service on boot

### Files Not Detected
1. Check if app directories exist: `adb shell ls -la /sdcard/WhatsApp/Media/`
2. Check file permissions
3. Check if files are in expected locations (varies by Android version)
4. Check logs for directory scan errors

## Integration with Existing Features

### Notification Capture
- Media from notifications is already captured
- Works alongside directory monitoring
- Provides redundancy (capture from both sources)

### Regular Sync
- Large files (>50MB) are saved for regular sync
- Uses existing SyncWorker infrastructure
- Chunked upload for very large files

### Download Monitor
- Works alongside download monitoring
- Different file size limits (50MB vs 10MB)
- Different monitoring frequencies (20s vs 30s)

## Future Enhancements

1. **More Apps**: Add support for Telegram, Signal, Facebook Messenger
2. **Configurable Limits**: Make 50MB limit configurable per app
3. **Priority Upload**: Prioritize certain file types or contacts
4. **Background Upload Queue**: Better handling of upload queue
5. **Compression**: Compress images before upload to save bandwidth
