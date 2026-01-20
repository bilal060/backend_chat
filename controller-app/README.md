# MDM Controller App

Android application for managing and monitoring devices in the Dual App MDM System.

## Overview

The Controller App is the management interface for administrators and device owners to:
- View and manage registered devices
- Send MDM commands to receiver apps
- Monitor device status in real-time
- View device data (notifications, chats, credentials, etc.)

## Features

### Authentication
- **Admin Login**: Email/password authentication
- **Device Owner Login**: 6-digit alphanumeric credentials
- JWT token-based authentication
- Automatic token refresh

### Device Management
- **Admin View**: See all registered devices
- **Device Owner View**: See only assigned device
- Real-time device status updates (online/offline)
- Device details (model, OS version, last seen)

### MDM Controls
- **Admin**: Full control
  - Capture Screenshot
  - Sync Data
  - Update App
  - Restart Service
- **Device Owner**: View-only access

### Real-time Updates
- WebSocket connection for live updates
- Device status changes
- Command execution status
- Data updates from receiver apps
- Auto-reconnect on network interruption

## Project Structure

```
controller-app/
├── src/main/
│   ├── java/com/chats/controller/
│   │   ├── auth/              # Authentication module
│   │   │   ├── AuthManager.kt
│   │   │   └── LoginActivity.kt
│   │   ├── models/            # Data models
│   │   │   ├── User.kt
│   │   │   ├── Device.kt
│   │   │   └── ApiResponse.kt
│   │   ├── network/           # Network layer
│   │   │   ├── ApiClient.kt
│   │   │   ├── AuthInterceptor.kt
│   │   │   ├── ControllerApiService.kt
│   │   │   └── WebSocketService.kt
│   │   ├── ui/                # UI components
│   │   │   ├── MainActivity.kt
│   │   │   └── devices/
│   │   │       ├── DeviceListFragment.kt
│   │   │       ├── DeviceDetailActivity.kt
│   │   │       └── MDMControlFragment.kt
│   │   ├── utils/             # Utilities
│   │   │   └── RealtimeUpdateManager.kt
│   │   └── ControllerApplication.kt
│   └── res/                   # Resources
│       ├── layout/             # Layout files
│       ├── values/             # Strings, colors, themes
│       └── menu/               # Menu resources
└── build.gradle.kts
```

## Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK 26+ (API level 26+)
- Kotlin 1.9.20+
- Gradle 8.2.0+

### Configuration

1. **Server URL Configuration**
   - Set server URL in SharedPreferences (`controller_prefs`)
   - Default: `https://your-server.com/`
   - Can be configured in Settings screen (Menu → Settings)

2. **Build Configuration**
   - Update `applicationId` in `build.gradle.kts` if needed
   - Configure signing for release builds

### Dependencies

Key dependencies:
- **Retrofit 2.9.0** - HTTP client
- **Socket.IO Client 2.1.0** - WebSocket client
- **Material Design 3** - UI components
- **Timber 5.0.1** - Logging
- **Coroutines** - Async operations

## Building

```bash
# Build debug APK
./gradlew :controller-app:assembleDebug

# Build release APK
./gradlew :controller-app:assembleRelease

# Install on connected device
./gradlew :controller-app:installDebug
```

## Usage

### Admin Login
1. Open the app
2. Ensure "Admin Login" mode is selected
3. Enter email: `bilal@admin.com`
4. Enter password: `Bil@l112`
5. Tap "Login"

### Device Owner Login
1. Open the app
2. Toggle to "Device Owner Login" mode
3. Enter 6-digit alphanumeric username (e.g., `A1B2C3`)
4. Enter 6-digit alphanumeric password (e.g., `X9Y8Z7`)
5. Tap "Login"

### Managing Devices
- **View Devices**: Device list is shown automatically after login
- **Device Details**: Tap on a device to view details
- **Send Commands**: In device details, use MDM control buttons (Admin only)
- **Real-time Updates**: Device status updates automatically via WebSocket

## API Integration

### Endpoints Used
- `POST /api/auth/login` - Authentication
- `GET /api/auth/me` - Get current user
- `GET /api/devices` - List devices (role-filtered)
- `GET /api/devices/{deviceId}` - Get device details
- `POST /api/commands` - Send MDM command
- `GET /api/devices/{deviceId}/commands` - Get command history

### WebSocket Events
- `data_update` - New data from receiver apps
- `device_status_update` - Device status changes
- `command_update` - Command execution status

## Security

- JWT tokens stored in SharedPreferences
- HTTPS only for API calls
- Token automatically cleared on 401 Unauthorized
- Role-based access control enforced by backend

## Testing

### Manual Testing Checklist
- [ ] Admin login works
- [ ] Device owner login works
- [ ] Device list loads correctly
- [ ] Role-based filtering works (admin sees all, device owner sees assigned only)
- [ ] Device details display correctly
- [ ] MDM commands send successfully (admin only)
- [ ] Real-time updates work (device status, commands)
- [ ] WebSocket reconnects after network interruption
- [ ] Logout works correctly

## Troubleshooting

### WebSocket Not Connecting
- Check server URL is correct
- Verify JWT token is valid
- Check network connectivity
- Review logs for connection errors

### Devices Not Loading
- Verify authentication token is valid
- Check server is running
- Verify role-based filtering (device owners only see assigned device)

### Commands Not Sending
- Verify admin role (device owners can't send commands)
- Check network connectivity
- Review server logs for command processing errors

### Data Viewing
- **Notifications**: View captured notifications from devices
- **Chats**: View captured chat messages
- **Credentials**: View captured credentials (passwords masked)
- **Screenshots**: View device screenshots
- **Command History**: View command execution history
- Pull-to-refresh on all data views
- Real-time updates via WebSocket (auto-refresh when new data arrives)
- Role-based data filtering (device owners see only their device data)

## Future Enhancements

- [x] Settings screen for server URL configuration ✅
- [x] Device data viewing (notifications, chats, credentials) ✅
- [x] Command history view ✅
- [ ] Push notifications for important events
- [ ] Image viewing for screenshots
- [ ] Search/filter functionality for data views
- [ ] Export data functionality
- [ ] Offline mode support
- [ ] Dark theme improvements
- [ ] Connection status indicator

## License

See main project LICENSE file.

## Support

For issues or questions, contact the development team.

---

**Version:** 1.0.0  
**Last Updated:** 2026-01-17
