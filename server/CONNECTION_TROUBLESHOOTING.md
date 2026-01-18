# PostgreSQL Connection Troubleshooting

## Test Results

### Direct Connection (Port 5432)
- ❌ **Failed**: `ECONNREFUSED`
- **Possible causes**:
  - Supabase project is paused/inactive
  - Direct database connections disabled
  - Network/firewall blocking

### Connection Pooler (Port 6543)
- ❌ **Failed**: `Tenant or user not found`
- **Possible causes**:
  - Wrong username format
  - Wrong pooler hostname/region
  - Pooler not enabled for project

## Credentials Used
- **Host**: `db.ongmnrvnvsjryneiogav.supabase.co`
- **Port**: `5432` (direct) / `6543` (pooler)
- **Database**: `postgres`
- **User**: `postgres`
- **Password**: `J05w5K00qcl39cr8` ✅ (provided by user)

## What to Check in Supabase Dashboard

1. **Project Status**
   - Go to: https://supabase.com/dashboard/project/ongmnrvnvsjryneiogav
   - Verify project is **Active** (not paused)

2. **Database Settings**
   - Go to: Settings → Database
   - Check "Connection string" section
   - Copy the **exact** connection string shown

3. **Connection Pooling**
   - Check if pooling is enabled
   - Note the correct pooler hostname/region
   - Pooler username format may be different

## Next Steps

### Option 1: Get Exact Connection String from Dashboard
1. Open: https://supabase.com/dashboard/project/ongmnrvnvsjryneiogav/settings/database
2. Find "Connection string" → "URI"
3. Copy the **complete** connection string
4. Use it in `.env` as `DATABASE_URL=...`

### Option 2: Verify Project Status
- Ensure project is not paused
- Check if billing/subscription is active
- Verify database is accessible

### Option 3: Use SQLite (Current Working Solution)
- ✅ SQLite is working and all routes use it
- Can continue development with SQLite
- Switch to PostgreSQL later when connection is resolved

## Current Configuration
- **Working**: SQLite (all routes use `./database/init`)
- **Not Working**: PostgreSQL (connection refused/user not found)
