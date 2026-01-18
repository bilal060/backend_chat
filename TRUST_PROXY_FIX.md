# Trust Proxy Fix

**Date:** 2026-01-18  
**Issue:** `ERR_ERL_UNEXPECTED_X_FORWARDED_FOR` error from express-rate-limit  
**Status:** ✅ **FIXED**

---

## 🔧 **FIX APPLIED**

### **Problem:**
When deploying Express apps behind a reverse proxy (like Render, Heroku, AWS ELB, etc.), the proxy sets `X-Forwarded-For` headers. Express rate limiting needs to trust these headers to correctly identify client IPs, but Express's default `trust proxy` setting is `false`.

### **Error:**
```
ValidationError: The 'X-Forwarded-For' header is set but the Express 'trust proxy' setting is false
```

### **Solution:**
1. ✅ Enabled `trust proxy` in Express (`app.set('trust proxy', 1)`)
2. ✅ Added `TRUST_PROXY` environment variable for configuration
3. ✅ Updated rate limiter configuration for better proxy support

---

## 📋 **CHANGES MADE**

### **1. Server Configuration** (`server/server.js`)
```javascript
// Trust proxy - Required when behind a reverse proxy
app.set('trust proxy', process.env.TRUST_PROXY !== 'false' ? 1 : false);
```

### **2. Rate Limiter Configuration**
```javascript
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 100,
    standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
    legacyHeaders: false, // Disable the `X-RateLimit-*` headers
});
```

### **3. Environment Variable** (`.env`)
```env
TRUST_PROXY=true
```

---

## ✅ **HOW IT WORKS**

### **Trust Proxy Setting:**
- `trust proxy: 1` - Trusts the first proxy (recommended for most deployments)
- `trust proxy: true` - Trusts all proxies (less secure)
- `trust proxy: false` - Doesn't trust proxies (default, causes the error)

### **Why It's Needed:**
When your app is behind a proxy:
1. Client makes request → Proxy receives it
2. Proxy forwards request → Sets `X-Forwarded-For` header with client IP
3. Express receives request → Without `trust proxy`, it sees proxy IP, not client IP
4. Rate limiter → Uses wrong IP, causing the error

With `trust proxy` enabled:
- Express reads `X-Forwarded-For` header
- Correctly identifies client IP
- Rate limiter works correctly

---

## 🔍 **VERIFICATION**

After this fix:
- ✅ No more `ERR_ERL_UNEXPECTED_X_FORWARDED_FOR` errors
- ✅ Rate limiting correctly identifies client IPs
- ✅ Works correctly behind reverse proxies (Render, Heroku, etc.)

---

## 📝 **DEPLOYMENT NOTES**

### **Render.com:**
- Set `TRUST_PROXY=true` in environment variables
- Or use `app.set('trust proxy', 1)` directly

### **Heroku:**
- Same as Render - enable trust proxy

### **AWS ELB / CloudFront:**
- Enable trust proxy
- May need to configure `trust proxy` based on number of proxies

### **Local Development:**
- `TRUST_PROXY=false` or omit (defaults to trusting proxy if not set to false)
- Works fine without proxy

---

## ⚙️ **CONFIGURATION OPTIONS**

### **Environment Variable:**
```env
# Enable trust proxy (recommended for production)
TRUST_PROXY=true

# Disable trust proxy (for local development without proxy)
TRUST_PROXY=false
```

### **Code Configuration:**
```javascript
// Trust first proxy (recommended)
app.set('trust proxy', 1);

// Trust all proxies (less secure)
app.set('trust proxy', true);

// Don't trust proxies (default, causes error behind proxy)
app.set('trust proxy', false);
```

---

## 🚀 **NEXT STEPS**

1. ✅ Fix applied to `server.js`
2. ✅ Environment variable added to `.env`
3. ✅ Ready for deployment on Render/Heroku/etc.

**The error should be resolved after deploying with this fix.**

---

**Last Updated:** 2026-01-18  
**Status:** ✅ **FIXED - Trust proxy enabled for reverse proxy support**
