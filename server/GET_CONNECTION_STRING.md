# How to Get Supabase PostgreSQL Connection String

## Current Issue
Connection is failing - likely due to incorrect password in `.env` file.

## Steps to Fix:

### 1. Open Supabase Dashboard
Go to: **https://supabase.com/dashboard/project/ongmnrvnvsjryneiogav/settings/database**

### 2. Find Connection String Section
Look for "Connection string" → "URI" tab

### 3. Copy the Connection String
You'll see something like this:

**For Connection Pooling (Recommended):**
```
postgresql://postgres.ongmnrvnvsjryneiogav:[YOUR-PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres
```

**OR for Direct Connection:**
```
postgresql://postgres:[YOUR-PASSWORD]@db.ongmnrvnvsjryneiogav.supabase.co:5432/postgres
```

### 4. Extract the Password
The `[YOUR-PASSWORD]` part is what you need. It's between `:` and `@` in the connection string.

### 5. Update .env File

**Option A: Use full DATABASE_URL (Recommended)**
```bash
DATABASE_URL=postgresql://postgres.ongmnrvnvsjryneiogav:[PASSWORD]@aws-0-us-east-1.pooler.supabase.com:6543/postgres
```

**Option B: Update DB_PASSWORD only**
```bash
DB_PASSWORD=[PASSWORD_FROM_CONNECTION_STRING]
```

### 6. Test Connection
```bash
node test-supabase-connection.js
```

## Quick Test Command
After updating `.env`, run:
```bash
node -e "require('dotenv').config(); const {Pool} = require('pg'); const p = new Pool({connectionString: process.env.DATABASE_URL || \`postgresql://postgres:\${process.env.DB_PASSWORD}@\${process.env.DB_HOST}:\${process.env.DB_PORT}/\${process.env.DB_NAME}\`, ssl: {rejectUnauthorized: false}}); p.query('SELECT 1').then(() => console.log('✅ Connected!')).catch(e => console.log('❌', e.message)); setTimeout(() => process.exit(0), 3000);"
```
