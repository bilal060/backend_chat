# Quick Start Guide - Dual App MDM System

**Get up and running in 5 minutes!**

---

## Prerequisites

- Node.js 16+ installed
- Android Studio installed
- Android device or emulator
- Firebase project (for FCM)

---

## Step 1: Backend Setup (2 minutes)

```bash
# Navigate to server directory
cd server

# Install dependencies
npm install

# Start the server
npm start
```

**Server runs on:** `http://localhost:3000`

**Default Admin Credentials:**
- Email: `bilal@admin.com`
- Password: `Bil@l112`

---

## Step 2: Firebase Setup (1 minute)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use existing
3. Add Android app with package: `com.chats.capture`
4. Download `google-services.json`
5. Place it in `app/src/main/google-services.json`

**For Controller App (optional):**
- Add another Android app with package: `com.chats.controller`
- Download `google-services.json` (if using FCM in controller app)

---

## Step 3: Receiver App Setup (1 minute)

1. Open Android Studio
2. Open project: `app/` directory
3. Sync Gradle
4. Configure server URL:
   - Go to Settings in app (after first install)
   - Enter server URL: `http://YOUR_IP:3000/` (replace YOUR_IP)
5. Build and install on device:
   ```bash
   ./gradlew :app:installDebug
   ```

**Note:** The app runs silently - you won't see it in the app drawer!

---

## Step 4: Controller App Setup (1 minute)

1. Open Android Studio
2. Open project: `controller-app/` directory
3. Sync Gradle
4. Configure server URL:
   - Edit `ControllerApplication.kt` or use SharedPreferences
   - Default: `https://your-server.com/`
   - Change to: `http://YOUR_IP:3000/` (replace YOUR_IP)
5. Build and install:
   ```bash
   ./gradlew :controller-app:installDebug
   ```

---

## Step 5: Test the System

### Test Receiver App Registration
1. Install receiver app on device
2. Check server logs - you should see device registration
3. Device should appear in controller app

### Test Controller App Login
1. Open controller app
2. Login as Admin:
   - Email: `bilal@admin.com`
   - Password: `Bil@l112`
3. You should see device list

### Test MDM Command
1. In controller app, tap on a device
2. Tap "Capture Screenshot" (or any command)
3. Check receiver app logs - command should execute
4. Screenshot should upload to server

### Test Real-time Updates
1. Send a command from controller app
2. Watch for real-time status update
3. Device status should update automatically

---

## Configuration

### Server URL Configuration

**Receiver App:**
- Settings screen in app
- SharedPreferences key: `server_url`

**Controller App:**
- SharedPreferences key: `server_url`
- Default: `https://your-server.com/`

### Network Configuration

**For Local Testing:**
- Use your computer's IP address: `http://192.168.x.x:3000`
- Ensure firewall allows port 3000
- Both devices must be on same network

**For Production:**
- Use HTTPS with valid SSL certificate
- Update server URL in both apps
- Configure domain name

---

## Troubleshooting

### Receiver App Not Registering
- ✅ Check server is running
- ✅ Verify server URL is correct
- ✅ Check network connectivity
- ✅ Review server logs for errors

### Controller App Can't Connect
- ✅ Verify server URL
- ✅ Check authentication credentials
- ✅ Review network connectivity
- ✅ Check server logs

### WebSocket Not Connecting
- ✅ Verify JWT token is valid
- ✅ Check server WebSocket is running
- ✅ Review network/firewall settings
- ✅ Check logs for connection errors

### Commands Not Executing
- ✅ Verify receiver app is running
- ✅ Check FCM token is registered
- ✅ Review command polling logs
- ✅ Verify device is online

---

## Common Issues

### Issue: "Connection refused"
**Solution:** Server not running or wrong IP address

### Issue: "Invalid credentials"
**Solution:** Check admin email/password or device owner credentials

### Issue: "No devices found"
**Solution:** 
- Verify receiver app is installed and running
- Check device registration in server logs
- Verify role-based filtering (device owners only see assigned device)

### Issue: "WebSocket connection failed"
**Solution:**
- Check JWT token is valid
- Verify server WebSocket is initialized
- Check network connectivity

---

## Next Steps

1. **Create Device Owner Account:**
   - Login as admin in controller app
   - Use admin API to create device owner
   - Assign device to device owner

2. **Configure Production:**
   - Set up HTTPS server
   - Configure domain name
   - Update server URLs in apps
   - Set up SSL certificates

3. **Monitor System:**
   - Check server logs regularly
   - Monitor device heartbeats
   - Review command execution status
   - Check WebSocket connections

---

## Support Resources

- **Backend API Docs:** `server/README.md`
- **Controller App Docs:** `controller-app/README.md`
- **Status Document:** `DUAL_APP_MDM_STATUS.md`
- **Real-time Updates:** `REALTIME_UPDATES_INTEGRATION.md`

---

## Quick Commands Reference

```bash
# Start backend server
cd server && npm start

# Build receiver app
./gradlew :app:assembleDebug

# Build controller app
./gradlew :controller-app:assembleDebug

# Install receiver app
./gradlew :app:installDebug

# Install controller app
./gradlew :controller-app:installDebug

# View server logs
# (Check terminal where server is running)

# View Android logs
adb logcat | grep -i "chats\|mdm\|capture"
```

---

**Ready to go!** 🚀

If you encounter any issues, check the troubleshooting section or review the detailed documentation.

---

**Last Updated:** 2026-01-17
