# Supabase Database Connection Setup

## Your Supabase Project
- **Project URL**: https://ongmnrvnvsjryneiogav.supabase.co
- **Project Dashboard**: https://supabase.com/dashboard/project/ongmnrvnvsjryneiogav

## Step 1: Get Database Connection String

1. Go to: **Supabase Dashboard → Project Settings → Database**
   - Direct link: https://supabase.com/dashboard/project/ongmnrvnvsjryneiogav/settings/database

2. Find **"Connection string"** section

3. You'll see options:
   - **URI** (for direct connection)
   - **Connection Pooling** (recommended for server apps)

## Step 2: Choose Connection Method

### Option A: Connection Pooling (Recommended)
Use the **Transaction** or **Session** pooler (port 6543):
```
postgresql://postgres.ongmnrvnvsjryneiogav:[PASSWORD]@aws-0-[REGION].pooler.supabase.com:6543/postgres
```

### Option B: Direct Connection (Port 5432)
```
postgresql://postgres:[PASSWORD]@db.ongmnrvnvsjryneiogav.supabase.co:5432/postgres
```

⚠️ **Important**: You need to find the **PASSWORD** in the connection string shown in the dashboard!

## Step 3: Update .env File

Add to your `.env` file:

```bash
# Option 1: Use DATABASE_URL (recommended)
DATABASE_URL=postgresql://postgres.ongmnrvnvsjryneiogav:[PASSWORD]@aws-0-[REGION].pooler.supabase.com:6543/postgres

# OR Option 2: Use individual parameters
DB_HOST=db.ongmnrvnvsjryneiogav.supabase.co
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=[PASSWORD_FROM_DASHBOARD]
DB_NAME=postgres
DB_SSL=true
```

## Step 4: Test Connection

After updating `.env`, run:
```bash
node test-supabase-connection.js
```

## Current Status

- ✅ **SQLite**: Working (currently used by all routes)
- ⏳ **PostgreSQL**: Awaiting connection string from Supabase dashboard

## Notes

- The **publishable key** (`sb_publishable_...`) is for frontend API access only
- For database connection, you need the **database password** from the connection string
- Connection pooling (port 6543) is recommended for server applications
- Direct connection (port 5432) has connection limits
