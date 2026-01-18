# MongoDB Migration Effort Assessment

## Current State

### Database Usage
- **Current DB**: SQLite (working, callback-based)
- **Route Files**: 10 files using database
- **Database Calls**: ~264 calls across codebase
- **Tables/Collections**: 9 tables
  - notifications
  - chats
  - media_files
  - credentials
  - contacts
  - devices
  - commands
  - users
  - device_ownership

### Database Operations Used
- **Query Types**: SELECT (WHERE, ORDER BY, LIMIT, OFFSET)
- **Write Types**: INSERT, UPDATE, DELETE
- **Relationships**: Foreign keys (device_ownership, commands → devices)
- **Patterns**: Callback-based SQLite API

## Migration Effort: **MODERATE to HIGH**

### ⏱️ Estimated Time: **2-4 days** (depending on testing)

---

## Required Changes

### 1. **Database Layer** (4-6 hours)
   - [ ] Install MongoDB driver: `npm install mongodb` or `mongoose`
   - [ ] Create new `database/mongodb.js` connection file
   - [ ] Convert schema from SQL tables to MongoDB collections
   - [ ] Setup connection pooling and error handling
   - [ ] Create indexes (similar to current SQL indexes)

### 2. **Route Files** (12-16 hours) - **10 files**
   - [ ] `routes/auth.js` - ~13 database calls
   - [ ] `routes/chats.js` - ~5 database calls  
   - [ ] `routes/commands.js` - ~5 database calls
   - [ ] `routes/contacts.js` - ~3 database calls
   - [ ] `routes/credentials.js` - ~3 database calls
   - [ ] `routes/devices.js` - ~14 database calls
   - [ ] `routes/media.js` - ~1 database call
   - [ ] `routes/notifications.js` - ~5 database calls
   - [ ] `routes/screenshots.js` - Check if uses DB
   - [ ] `routes/location.js` - Check if uses DB

   **Changes per route:**
   - Convert SQL queries to MongoDB queries
   - Change callbacks → async/await (or keep callbacks with MongoDB)
   - Update WHERE clauses → MongoDB filter objects
   - Update ORDER BY → `.sort()`
   - Update LIMIT/OFFSET → `.limit()` / `.skip()`
   - Handle JSON fields (already TEXT in SQLite)

### 3. **Middleware** (1-2 hours)
   - [ ] `middleware/auth.js` - Uses `db.get()` for user lookup
   - [ ] Convert to MongoDB `.findOne()`

### 4. **Services** (1-2 hours)
   - [ ] `services/websocketService.js` - Check database usage
   - [ ] Update any database calls

### 5. **Schema Conversion** (2-3 hours)

#### SQLite → MongoDB Mapping:
```
SQL Tables          →  MongoDB Collections
─────────────────────────────────────────────
devices             →  devices
users               →  users  
notifications       →  notifications
chats               →  chats
credentials         →  credentials
contacts            →  contacts
media_files         →  media_files
commands            →  commands
device_ownership    →  device_ownership (or embedded in devices)
```

#### Data Type Changes:
- `INTEGER` → `Number`
- `TEXT` → `String`
- `TEXT` (JSON) → `Object` or keep as String
- `DEFAULT 0` → Default in schema or application logic
- `PRIMARY KEY` → `_id` field
- Foreign keys → References or embedded documents

### 6. **Query Conversion Examples**

#### SQL → MongoDB:
```javascript
// SQL (current)
db.all('SELECT * FROM chats WHERE deviceId = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?', 
  [deviceId, limit, offset], callback)

// MongoDB (new)
db.collection('chats')
  .find({ deviceId: deviceId })
  .sort({ timestamp: -1 })
  .limit(limit)
  .skip(offset)
  .toArray(callback)

// OR with async/await:
const chats = await db.collection('chats')
  .find({ deviceId: deviceId })
  .sort({ timestamp: -1 })
  .limit(limit)
  .skip(offset)
  .toArray()
```

### 7. **Testing** (4-6 hours)
   - [ ] Test all API endpoints
   - [ ] Verify data integrity
   - [ ] Test relationships/queries
   - [ ] Performance testing
   - [ ] Migration script for existing SQLite data (if needed)

---

## Pros of MongoDB Migration

✅ **Scalability**: Better for horizontal scaling
✅ **Flexible Schema**: Easier schema changes
✅ **JSON Native**: Better fit for JavaScript/Node.js
✅ **Document Model**: May fit your data better (nested structures)
✅ **Cloud Options**: MongoDB Atlas available

## Cons of Migration

❌ **Migration Effort**: 2-4 days of work
❌ **Breaking Changes**: All routes need updating
❌ **Testing Required**: Extensive testing needed
❌ **Learning Curve**: Team needs MongoDB knowledge
❌ **SQLite Works**: Current setup is functional

---

## Migration Strategy

### Option 1: Full Migration (Recommended if migrating)
1. Setup MongoDB connection
2. Create MongoDB schema/collections
3. Convert routes one by one
4. Test thoroughly
5. Switch production

### Option 2: Hybrid Approach
- Keep SQLite for now
- Setup MongoDB in parallel
- Migrate routes gradually
- Switch when ready

### Option 3: Use MongoDB Driver with Same Patterns
- Keep callback patterns (MongoDB supports callbacks)
- Just change query syntax
- Minimal refactoring

---

## Quick Start if Proceeding

```bash
# Install MongoDB driver
npm install mongodb

# Or with Mongoose (ORM)
npm install mongoose
```

**MongoDB Connection Example:**
```javascript
const { MongoClient } = require('mongodb');
const client = new MongoClient('mongodb://localhost:27017');
await client.connect();
const db = client.db('chat_capture');
```

---

## Recommendation

**Current Status**: SQLite is working perfectly ✅

**Migration Decision:**
- **If you need scalability/cloud hosting**: MongoDB worth it
- **If SQLite meets needs**: Not worth 2-4 days effort right now
- **If future-proofing**: Consider migration during next major refactor

**Effort Rating**: 🟡 **MODERATE-HIGH** (2-4 days, ~20-30 hours)
