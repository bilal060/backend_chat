# Immediate File Upload Feature (< 10MB)

## Overview
This feature automatically monitors downloads and immediately uploads files less than 10MB to the server as soon as they are downloaded through any app.

## Implementation

### Components Created

#### 1. **DownloadCompleteReceiver** (`receivers/DownloadCompleteReceiver.kt`)
- **Purpose**: Monitors DownloadManager for completed downloads
- **Trigger**: `ACTION_DOWNLOAD_COMPLETE` broadcast
- **Functionality**:
  - Detects when downloads complete via DownloadManager
  - Checks file size (< 10MB)
  - Extracts file path from URI
  - Immediately uploads to server

#### 2. **DownloadMonitorService** (`services/DownloadMonitorService.kt`)
- **Purpose**: Monitors download directories for files that may not go through DownloadManager
- **Functionality**:
  - Uses ContentObserver to watch Downloads directory
  - Periodically scans download directories (every 30 seconds)
  - Detects new files (modified in last 5 minutes)
  - Checks file size (< 10MB)
  - Immediately uploads to server
  - Tracks processed files to avoid duplicates

#### 3. **FileUtils** (`utils/FileUtils.kt`)
- **Purpose**: Utility functions for file operations
- **Functions**:
  - `getPathFromUri()`: Converts URI to file path (handles various URI schemes)
  - `calculateChecksum()`: Calculates SHA-256 checksum
  - `getMimeTypeFromFile()`: Determines MIME type from file extension
  - `shouldUploadImmediately()`: Checks if file should be uploaded (< 10MB)

### Registration

#### AndroidManifest.xml
- **DownloadCompleteReceiver**: Registered to receive `ACTION_DOWNLOAD_COMPLETE`
- **DownloadMonitorService**: Registered as foreground service with `dataSync` type

#### Service Starter
- `ServiceStarter.startDownloadMonitorService()`: Starts the monitoring service
- Automatically started via `ensureServicesRunning()` which is called:
  - On app startup (CaptureApplication)
  - On boot (BootReceiver)
  - On app install/update (PackageInstallReceiver)

## How It Works

### Download Flow

1. **User downloads file** through any app
2. **DownloadManager completes** download
3. **DownloadCompleteReceiver** receives `ACTION_DOWNLOAD_COMPLETE` broadcast
4. **File is checked**:
   - File size < 10MB? ✅
   - File exists? ✅
   - Already uploaded? ❌ (skip if already uploaded)
5. **File is uploaded immediately** to server
6. **Status logged** in logcat with `DOWNLOAD_MONITOR` tag

### Directory Monitoring Flow

1. **DownloadMonitorService** runs continuously
2. **ContentObserver** watches Downloads directory
3. **Periodic scan** (every 30 seconds) checks download directories:
   - `/storage/emulated/0/Download`
   - `/storage/emulated/0/Downloads`
   - Other common download locations
4. **New files detected** (modified in last 5 minutes)
5. **File is checked**:
   - File size < 10MB? ✅
   - Not already processed? ✅
6. **File is uploaded immediately** to server
7. **File is marked as processed** to avoid duplicates

## File Size Limit

- **Maximum size**: 10MB (10 * 1024 * 1024 bytes)
- **Rationale**: 
  - Immediate upload should be fast
  - Avoids battery drain from large uploads
  - Prevents network congestion
  - Files > 10MB are still captured but uploaded during regular sync

## Supported File Types

All file types are supported, including:
- **Images**: JPG, PNG, GIF, WebP, BMP, SVG
- **Videos**: MP4, WebM, MOV, AVI, MKV, 3GP
- **Audio**: MP3, OGG, WAV, M4A, AAC, FLAC
- **Documents**: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX
- **Archives**: ZIP, RAR, 7Z, TAR, GZ
- **Text**: TXT, JSON, XML, HTML, CSS, JS
- **Other**: Any file type (defaults to `application/octet-stream`)

## Database Storage

Files are stored in `media_files` table with:
- `notificationId`: `download_<timestamp>_<filename>` or `download_<timestamp>`
- `appPackage`: `download_manager` or `file_download`
- `uploadStatus`: `PENDING` → `UPLOADING` → `SUCCESS` or `FAILED`
- `checksum`: SHA-256 hash (prevents duplicate uploads)

## Logging

All operations are logged with `DOWNLOAD_MONITOR` tag:
- `📥 Download completed`: DownloadManager download finished
- `📤 File < 10MB detected`: File eligible for immediate upload
- `✅ File uploaded immediately`: Upload successful
- `⚠️ File upload queued`: Upload failed, will retry later
- `📥 New file detected`: New file found in directory scan

## Monitoring

### Check Service Status
```bash
adb shell dumpsys activity services com.chats.capture | grep DownloadMonitor
```

### View Logs
```bash
adb logcat -s DOWNLOAD_MONITOR
```

### Check Uploaded Files
```bash
# Check database for uploaded files
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT id, notificationId, fileSize, uploadStatus FROM media_files WHERE appPackage LIKE \"%download%\" ORDER BY createdAt DESC LIMIT 10;'"
```

## Performance Considerations

1. **Battery**: 
   - Periodic scans limited to 30 seconds
   - Only processes files modified in last 5 minutes
   - Immediate uploads use existing upload infrastructure

2. **Storage**:
   - Tracks processed files in memory (cleared on service restart)
   - Duplicate detection via checksum prevents re-uploading

3. **Network**:
   - Only files < 10MB are uploaded immediately
   - Larger files uploaded during regular sync
   - Uses existing retry mechanism for failed uploads

## Error Handling

- **File not found**: Logged and skipped
- **Permission denied**: Logged and skipped
- **Upload failure**: File marked as `FAILED`, will retry during regular sync
- **Network unavailable**: File marked as `PENDING`, will retry when network available

## Future Enhancements

1. **Configurable size limit**: Make 10MB limit configurable
2. **File type filtering**: Allow filtering by file type
3. **App-specific monitoring**: Monitor specific app download directories
4. **Upload priority**: Prioritize certain file types or apps
5. **Background upload queue**: Better handling of upload queue

## Testing

### Test Download via Browser
1. Download a small file (< 10MB) from browser
2. Check logcat: `adb logcat -s DOWNLOAD_MONITOR`
3. Verify file appears in database
4. Verify file is uploaded to server

### Test Download via App
1. Download a file from any app (WhatsApp, Telegram, etc.)
2. Check logcat for immediate upload
3. Verify file is on server

### Test Large File
1. Download a file > 10MB
2. Verify it's NOT uploaded immediately
3. Verify it's captured and uploaded during regular sync

## Troubleshooting

### Files Not Uploading Immediately
1. Check service is running: `adb shell dumpsys activity services | grep DownloadMonitor`
2. Check file size: Must be < 10MB
3. Check network: Must be connected
4. Check logs: `adb logcat -s DOWNLOAD_MONITOR`

### Duplicate Uploads
- Checksum prevents duplicates
- If duplicates occur, check database for existing entries

### Service Not Starting
- Check AndroidManifest.xml registration
- Check ServiceStarter.ensureServicesRunning() is called
- Check boot receiver starts service on boot
