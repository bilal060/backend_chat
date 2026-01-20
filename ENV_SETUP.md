# Environment Setup for Backend Server

**Date:** 2026-01-18  
**Status:** ✅ **COMPLETED**

---

## 🔧 **SETUP COMPLETED**

### **Changes Made:**
1. ✅ Installed `dotenv` package
2. ✅ Updated `server.js` to load environment variables from `.env` file
3. ✅ Created `.env` file with configuration
4. ✅ Copied `google-services.json` to server directory
5. ✅ Server restarted with new configuration

---

## 📋 **ENVIRONMENT VARIABLES**

The `.env` file has been created in `/Users/mac/Desktop/chats/server/.env` with the following variables:

### **Server Configuration**
- `PORT=3000` - Server port
- `NODE_ENV=development` - Environment mode

### **Firebase Configuration**
- `FIREBASE_CREDENTIALS` - (Optional) Firebase Admin SDK service account JSON as string
- `FIREBASE_CREDENTIALS_PATH` - (Optional) Path to Firebase Admin SDK service account JSON file

**⚠️ IMPORTANT:** The `google-services.json` file is for **client-side** Firebase SDK (Android app).  
For **server-side** Firebase Admin SDK, you need a **service account key file**.

### **How to Get Firebase Service Account Key:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `receiver-app-47f7e`
3. Go to **Project Settings** → **Service Accounts**
4. Click **Generate New Private Key**
5. Save the JSON file as `firebase-service-account.json` in the server directory
6. Update `.env` file:
   ```
   FIREBASE_CREDENTIALS_PATH=./firebase-service-account.json
   ```

### **JWT Configuration**
- `JWT_SECRET=your-super-secret-jwt-key-change-this-in-production` - Secret key for JWT tokens

### **Database Configuration**
- `DB_PATH=./database/capture.db` - SQLite database path

### **CORS Configuration**
- `CORS_ORIGIN=*` - CORS allowed origins

### **Rate Limiting**
- `RATE_LIMIT_WINDOW_MS=900000` - Rate limit window (15 minutes)
- `RATE_LIMIT_MAX=100` - Max requests per window

---

## 🚀 **STARTING THE SERVER**

The server is now configured to use environment variables. To start it:

```bash
cd /Users/mac/Desktop/chats/server
node server.js
```

Or with nodemon (auto-restart on changes):
```bash
npm run dev
```

---

## 📝 **NOTES**

1. **Firebase Admin SDK:** The server needs a Firebase Admin SDK service account key (different from `google-services.json`). Without it, FCM (Firebase Cloud Messaging) will not work, but the server will still run.

2. **JWT Secret:** Change the `JWT_SECRET` in production to a secure random string.

3. **Environment File:** The `.env` file is in `.gitignore` and should not be committed to version control. Use `.env.example` as a template.

4. **Current Status:** Server is running with basic configuration. Firebase Admin SDK will need service account credentials to enable FCM push notifications.

---

## ✅ **VERIFICATION**

After setup:
- ✅ Server loads environment variables from `.env`
- ✅ Server starts on port 3000 (or PORT from .env)
- ✅ Firebase config checks for credentials (warns if not found)
- ✅ JWT uses secret from environment

---

**Last Updated:** 2026-01-18  
**Status:** ✅ **SETUP COMPLETE - Server running with environment configuration**
