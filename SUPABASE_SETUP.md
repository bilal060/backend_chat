# Supabase PostgreSQL Setup

**Date:** 2026-01-18  
**Status:** 🔄 **CONFIGURATION IN PROGRESS**

---

## 🔧 **SUPABASE CONFIGURATION**

### **Provided Credentials:**
- **Supabase URL:** `https://ongmnrvnvsjryneiogav.supabase.co`
- **Publishable Key:** `sb_publishable_yInItB5GZkIJGuJ2aIR8Og_PzmgOkJa`

---

## 📋 **SETUP STEPS**

### **1. Get Database Password**

The publishable key is for frontend use. For backend PostgreSQL connection, you need:

1. Go to [Supabase Dashboard](https://supabase.com/dashboard)
2. Select your project: `ongmnrvnvsjryneiogav`
3. Go to **Settings** → **Database**
4. Find **Connection string** section
5. Copy the **Connection string** (URI format) or note the **Database password**

### **2. Update Environment Variables**

Update `/Users/mac/Desktop/chats/server/.env` with one of these options:

#### **Option A: Connection String (Recommended)**
```env
DATABASE_URL=postgresql://postgres:[YOUR-PASSWORD]@db.ongmnrvnvsjryneiogav.supabase.co:5432/postgres
DB_SSL=true
```

#### **Option B: Individual Parameters**
```env
DB_HOST=db.ongmnrvnvsjryneiogav.supabase.co
DB_PORT=5432
DB_NAME=postgres
DB_USER=postgres
DB_PASSWORD=[YOUR-SUPABASE-DB-PASSWORD]
DB_SSL=true
```

### **3. Database Connection Details**

From your Supabase URL, the database connection details are:
- **Host:** `db.ongmnrvnvsjryneiogav.supabase.co`
- **Port:** `5432`
- **Database:** `postgres` (default)
- **User:** `postgres` (default)
- **Password:** [Get from Supabase Dashboard]
- **SSL:** Required (set to `true`)

---

## 🔐 **GETTING THE DATABASE PASSWORD**

### **Method 1: From Supabase Dashboard**
1. Login to [Supabase Dashboard](https://supabase.com/dashboard)
2. Select project: `ongmnrvnvsjryneiogav`
3. Go to **Settings** → **Database**
4. Scroll to **Connection string** section
5. Click **URI** tab
6. Copy the connection string (includes password)

### **Method 2: Reset Password**
1. Go to **Settings** → **Database**
2. Click **Reset database password**
3. Copy the new password
4. Update `.env` file

---

## ✅ **VERIFICATION**

After updating `.env`, test the connection:

```bash
cd /Users/mac/Desktop/chats/server
node -e "
const db = require('./database/postgres');
db.query('SELECT NOW() as current_time')
  .then(r => {
    console.log('✅ Database connected:', r.rows[0]);
    process.exit(0);
  })
  .catch(err => {
    console.error('❌ Connection failed:', err.message);
    process.exit(1);
  });
"
```

---

## 📝 **ENVIRONMENT VARIABLES SUMMARY**

Add to `.env`:

```env
# Supabase Configuration
SUPABASE_URL=https://ongmnrvnvsjryneiogav.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_yInItB5GZkIJGuJ2aIR8Og_PzmgOkJa

# PostgreSQL Connection (choose one method)

# Method 1: Connection String (Recommended)
DATABASE_URL=postgresql://postgres:[PASSWORD]@db.ongmnrvnvsjryneiogav.supabase.co:5432/postgres
DB_SSL=true

# Method 2: Individual Parameters
# DB_HOST=db.ongmnrvnvsjryneiogav.supabase.co
# DB_PORT=5432
# DB_NAME=postgres
# DB_USER=postgres
# DB_PASSWORD=[YOUR-PASSWORD]
# DB_SSL=true
```

---

## ⚠️ **IMPORTANT NOTES**

1. **Password Required:** The publishable key is NOT the database password. You need the actual database password from Supabase Dashboard.

2. **SSL Required:** Supabase requires SSL connections. Set `DB_SSL=true` or use connection string with SSL.

3. **Connection String Format:**
   ```
   postgresql://[USER]:[PASSWORD]@[HOST]:[PORT]/[DATABASE]
   ```

4. **Database Name:** Usually `postgres` for Supabase, but check your project settings.

5. **Security:** Never commit the `.env` file with real passwords to version control.

---

## 🚀 **NEXT STEPS**

1. ✅ Get database password from Supabase Dashboard
2. ✅ Update `.env` file with connection string or parameters
3. ✅ Test database connection
4. ✅ Initialize database schema (will run automatically on server start)
5. ✅ Update all route files to use PostgreSQL syntax

---

**Last Updated:** 2026-01-18  
**Status:** ⏳ **WAITING FOR DATABASE PASSWORD**
