/**
 * Database abstraction layer
 * MongoDB interface for all database operations
 */
const { getDb, dbHelpers } = require('./mongodb');

/**
 * MongoDB database wrapper
 * Provides consistent database interface for routes
 */
class MongoDBAdapter {
    constructor() {
        this.db = null;
    }

    async getCollection(collectionName) {
        if (!this.db) {
            this.db = getDb();
        }
        return this.db.collection(collectionName);
    }

    // MongoDB query methods
    async get(collection, filter, callback) {
        try {
            const collectionObj = await this.getCollection(collection);
            const result = await collectionObj.findOne(filter);
            
            if (callback) {
                callback(null, result);
            } else {
                return result;
            }
        } catch (error) {
            if (callback) {
                callback(error, null);
            } else {
                throw error;
            }
        }
    }

    async all(collection, filter, options, callback) {
        try {
            const collectionObj = await this.getCollection(collection);
            let query = collectionObj.find(filter || {});
            
            if (options && options.sort) {
                query = query.sort(options.sort);
            }
            if (options && options.limit) {
                query = query.limit(options.limit);
            }
            if (options && options.skip) {
                query = query.skip(options.skip);
            }
            
            const results = await query.toArray();
            
            if (callback) {
                callback(null, results);
            } else {
                return results;
            }
        } catch (error) {
            if (callback) {
                callback(error, null);
            } else {
                throw error;
            }
        }
    }

    async run(collection, operation, filter, update, callback) {
        try {
            const collectionObj = await this.getCollection(collection);
            let result;
            
            if (operation === 'insert') {
                result = await collectionObj.insertOne(update);
                const dbResult = { lastID: result.insertedId, changes: 1 };
                if (callback) callback(null, dbResult);
                else return dbResult;
            } else if (operation === 'update') {
                result = await collectionObj.updateMany(filter, { $set: update });
                const dbResult = { changes: result.modifiedCount };
                if (callback) callback(null, dbResult);
                else return dbResult;
            } else if (operation === 'delete') {
                result = await collectionObj.deleteMany(filter);
                const dbResult = { changes: result.deletedCount };
                if (callback) callback(null, dbResult);
                else return dbResult;
            }
        } catch (error) {
            if (callback) {
                callback(error, null);
            } else {
                throw error;
            }
        }
    }

    prepare(query) {
        // For MongoDB, we'll parse the SQL-like query and return an adapter
        // This maintains compatibility with existing code
        return new PreparedStatementAdapter(this, query);
    }

    serialize(callback) {
        // MongoDB doesn't need serialization, just execute callback
        if (callback) callback();
    }
}

class PreparedStatementAdapter {
    constructor(db, query) {
        this.db = db;
        this.query = query;
        this.operation = null;
        this.collection = null;
        this.params = [];
    }

    run(...args) {
        // Determine operation and collection from query
        const queryLower = this.query.toLowerCase();
        
        if (queryLower.includes('insert into')) {
            this.operation = 'insert';
            const match = queryLower.match(/insert into\s+(\w+)/);
            this.collection = match ? match[1] : null;
            this.params = args.length === 1 && Array.isArray(args[0]) ? args[0] : args;
            return this.executeInsert();
        } else if (queryLower.includes('update')) {
            this.operation = 'update';
            const match = queryLower.match(/update\s+(\w+)/);
            this.collection = match ? match[1] : null;
            this.params = args.length === 1 && Array.isArray(args[0]) ? args[0] : args;
            return this.executeUpdate();
        }
        
        // Default: treat as insert
        return this.executeInsert();
    }

    async executeInsert() {
        try {
            // Parse INSERT query to extract fields and values
            // Format: INSERT INTO collection (field1, field2, ...) VALUES (?, ?, ...)
            const fieldMatch = this.query.match(/\(([^)]+)\)/g);
            if (!fieldMatch || fieldMatch.length < 2) {
                throw new Error('Invalid INSERT query format');
            }
            
            const fields = fieldMatch[0].replace(/[()]/g, '').split(',').map(f => f.trim());
            const values = this.params;
            
            const doc = {};
            fields.forEach((field, index) => {
                if (values[index] !== undefined) {
                    doc[field] = values[index];
                }
            });
            
            // Convert synced integer to boolean if present
            if (doc.synced !== undefined && typeof doc.synced === 'number') {
                doc.synced = doc.synced === 1;
            }
            
            const collectionObj = (await this.db.getCollection(this.collection));
            const result = await collectionObj.insertOne(doc);
            
            return { lastID: result.insertedId, changes: 1 };
        } catch (error) {
            console.error('Insert error:', error);
            throw error;
        }
    }

    async executeUpdate() {
        // TODO: Implement UPDATE parsing
        return { changes: 0 };
    }

    finalize() {
        // No-op for MongoDB
    }
}

// Create singleton instance
let dbAdapter = null;

function getDB() {
    if (!dbAdapter) {
        dbAdapter = new MongoDBAdapter();
    }
    return dbAdapter;
}

module.exports = getDB();
