# Commands Endpoint Fix

**Date:** 2026-01-18  
**Issue:** `404 Not Found` for `/api/devices/{deviceId}/commands/pending`  
**Status:** ✅ **FIXED**

---

## 🔧 **FIX APPLIED**

### **Problem:**
The app was calling `GET /api/devices/{deviceId}/commands/pending` but the server returned `404 Not Found`. The route was incorrectly placed in the commands router instead of the devices router.

### **Solution:**
1. ✅ Added route to `devices.js` router: `GET /:deviceId/commands/pending`
2. ✅ Placed route **BEFORE** `/:deviceId` route to avoid Express route matching conflicts
3. ✅ Route returns pending commands for the specified device

---

## 📋 **CHANGES MADE**

### **1. Server Route** (`server/routes/devices.js`)
- Added `GET /:deviceId/commands/pending` route
- Route is placed **before** `GET /:deviceId` to ensure proper matching
- Returns JSON: `{ success: true, commands: [...] }`
- No authentication required (for device polling)

### **2. Route Order**
- ✅ `GET /:deviceId/commands/pending` (specific route - matches first)
- ✅ `GET /:deviceId` (generic route - matches second)

---

## ✅ **EXPECTED BEHAVIOR**

- ✅ App can poll for pending commands: `GET /api/devices/{deviceId}/commands/pending`
- ✅ Returns empty array if no pending commands: `{ success: true, commands: [] }`
- ✅ Returns pending commands if any exist
- ✅ Command polling manager works correctly

---

## 🔍 **VERIFICATION**

After this fix:
- ✅ Command polling endpoint accessible
- ✅ No more 404 errors
- ✅ Commands can be fetched by device ID

---

## 📝 **NOTE**

The route must be placed **before** the generic `/:deviceId` route in Express, otherwise Express will match `/:deviceId` first and never reach the commands endpoint.

---

**Last Updated:** 2026-01-18  
**Status:** ✅ **FIXED - Commands endpoint accessible**
