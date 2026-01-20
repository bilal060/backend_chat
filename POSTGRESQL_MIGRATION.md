# PostgreSQL Migration Guide

**Date:** 2026-01-18  
**Status:** 🔄 **IN PROGRESS**

---

## 📋 **MIGRATION OVERVIEW**

Migrating from SQLite to PostgreSQL for better scalability, performance, and production readiness.

---

## ✅ **COMPLETED STEPS**

1. ✅ Installed PostgreSQL client library (`pg`)
2. ✅ Created PostgreSQL database initialization file (`database/postgres.js`)
3. ✅ Updated `server.js` to use PostgreSQL
4. ✅ Added PostgreSQL environment variables to `.env`

---

## 🔧 **REMAINING STEPS**

### **1. Install PostgreSQL Server**

**macOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt-get update
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**Windows:**
Download and install from: https://www.postgresql.org/download/windows/

### **2. Create Database**

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE chat_capture;

# Create user (optional)
CREATE USER chat_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE chat_capture TO chat_user;

# Exit
\q
```

### **3. Update Environment Variables**

Update `/Users/mac/Desktop/chats/server/.env`:

```env
# PostgreSQL Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=chat_capture
DB_USER=postgres
DB_PASSWORD=postgres
```

### **4. Update All Route Files**

All route files need to be updated to use PostgreSQL syntax:

**SQLite Syntax (old):**
```javascript
db.get('SELECT * FROM devices WHERE deviceId = ?', [deviceId], (err, row) => {
    // callback
});
```

**PostgreSQL Syntax (new):**
```javascript
const row = await db.get('SELECT * FROM devices WHERE deviceId = $1', [deviceId]);
```

**Key Changes:**
- `?` placeholders → `$1, $2, $3...`
- Callbacks → `async/await`
- `db.get()` → `await db.get()`
- `db.all()` → `await db.all()`
- `db.run()` → `await db.run()`
- `db.prepare()` → `db.prepare()` (returns async object)

### **5. Files to Update**

- [ ] `routes/devices.js`
- [ ] `routes/notifications.js`
- [ ] `routes/chats.js`
- [ ] `routes/credentials.js`
- [ ] `routes/contacts.js`
- [ ] `routes/commands.js`
- [ ] `routes/media.js`
- [ ] `routes/auth.js`
- [ ] `routes/location.js`
- [ ] `routes/screenshots.js`
- [ ] `routes/updates.js`
- [ ] `middleware/auth.js`
- [ ] `services/websocketService.js`

---

## 🔄 **MIGRATION SCRIPT**

To migrate existing SQLite data to PostgreSQL:

```bash
# Export SQLite data
sqlite3 database/capture.db .dump > sqlite_dump.sql

# Convert SQLite syntax to PostgreSQL
# (Manual conversion needed for some syntax differences)
```

---

## 📝 **SYNTAX DIFFERENCES**

| SQLite | PostgreSQL |
|--------|------------|
| `INTEGER` | `BIGINT` |
| `TEXT` | `VARCHAR(255)` or `TEXT` |
| `?` placeholder | `$1, $2, $3...` |
| `strftime('%s', 'now')` | `EXTRACT(EPOCH FROM NOW())::BIGINT` |
| `db.get()` (callback) | `await db.get()` (promise) |
| `db.all()` (callback) | `await db.all()` (promise) |
| `db.run()` (callback) | `await db.run()` (promise) |

---

## 🧪 **TESTING**

After migration:

1. **Test Database Connection:**
```bash
cd /Users/mac/Desktop/chats/server
node -e "const db = require('./database/postgres'); db.query('SELECT NOW()').then(r => console.log(r.rows[0])).catch(console.error);"
```

2. **Test Server:**
```bash
npm start
# Check health endpoint
curl http://localhost:3000/health
```

3. **Test API Endpoints:**
- Device registration
- Notification upload
- Commands polling
- Data retrieval

---

## ⚠️ **IMPORTANT NOTES**

1. **Backup SQLite Database:**
   ```bash
   cp database/capture.db database/capture.db.backup
   ```

2. **Data Migration:**
   - Existing SQLite data needs to be migrated manually
   - Use a migration script or export/import process

3. **Rollback Plan:**
   - Keep SQLite code in `database/init.js` as backup
   - Can switch back by changing `server.js` import

---

## 📚 **RESOURCES**

- PostgreSQL Documentation: https://www.postgresql.org/docs/
- Node.js pg library: https://node-postgres.com/
- Migration guide: This document

---

**Last Updated:** 2026-01-18  
**Status:** 🔄 **MIGRATION IN PROGRESS**
