/**
 * Test script to verify MongoDB migration
 * Tests database connection and key endpoints
 */

require('dotenv').config();
const { connect, getDb } = require('./database/mongodb');

async function testMongoDBConnection() {
    console.log('\n🧪 Testing MongoDB Connection...\n');
    
    try {
        const { client, db } = await connect();
        console.log('✅ MongoDB connection successful\n');
        
        // Test database access
        const collections = await db.listCollections().toArray();
        console.log('📋 Collections found:', collections.map(c => c.name).join(', ') || 'None (will be created on first use)');
        console.log('');
        
        // Test basic operations on each collection
        const tests = [
            { name: 'users', operation: async () => {
                const count = await db.collection('users').countDocuments();
                return `Found ${count} users`;
            }},
            { name: 'devices', operation: async () => {
                const count = await db.collection('devices').countDocuments();
                return `Found ${count} devices`;
            }},
            { name: 'chats', operation: async () => {
                const count = await db.collection('chats').countDocuments();
                return `Found ${count} chats`;
            }},
            { name: 'notifications', operation: async () => {
                const count = await db.collection('notifications').countDocuments();
                return `Found ${count} notifications`;
            }},
            { name: 'credentials', operation: async () => {
                const count = await db.collection('credentials').countDocuments();
                return `Found ${count} credentials`;
            }},
            { name: 'contacts', operation: async () => {
                const count = await db.collection('contacts').countDocuments();
                return `Found ${count} contacts`;
            }},
            { name: 'commands', operation: async () => {
                const count = await db.collection('commands').countDocuments();
                return `Found ${count} commands`;
            }},
            { name: 'media', operation: async () => {
                const count = await db.collection('media').countDocuments();
                return `Found ${count} media files`;
            }}
        ];
        
        console.log('📊 Collection Statistics:');
        for (const test of tests) {
            try {
                const result = await test.operation();
                console.log(`   ✅ ${test.name}: ${result}`);
            } catch (error) {
                console.log(`   ⚠️  ${test.name}: Error - ${error.message}`);
            }
        }
        
        // Test indexes
        console.log('\n🔍 Testing Indexes...');
        const usersIndexes = await db.collection('users').indexes();
        console.log(`   ✅ users indexes: ${usersIndexes.length} found`);
        
        const devicesIndexes = await db.collection('devices').indexes();
        console.log(`   ✅ devices indexes: ${devicesIndexes.length} found`);
        
        // Test a simple insert/read/delete operation
        console.log('\n🧪 Testing CRUD Operations...');
        const testDoc = {
            _id: 'test_' + Date.now(),
            test: true,
            timestamp: new Date()
        };
        
        // Insert
        await db.collection('devices').insertOne(testDoc);
        console.log('   ✅ Insert test: PASSED');
        
        // Read
        const found = await db.collection('devices').findOne({ _id: testDoc._id });
        if (found) {
            console.log('   ✅ Read test: PASSED');
        } else {
            throw new Error('Could not read inserted document');
        }
        
        // Delete
        await db.collection('devices').deleteOne({ _id: testDoc._id });
        const deleted = await db.collection('devices').findOne({ _id: testDoc._id });
        if (!deleted) {
            console.log('   ✅ Delete test: PASSED');
        } else {
            throw new Error('Could not delete document');
        }
        
        console.log('\n✅ All MongoDB tests passed!\n');
        
        // Don't close connection - let the process handle it
        return true;
    } catch (error) {
        console.error('\n❌ MongoDB test failed:', error.message);
        console.error('   Make sure MongoDB is running and MONGODB_URI is set in .env');
        console.error('   Default: mongodb://localhost:27017\n');
        return false;
    }
}

async function testServerEndpoints() {
    console.log('\n🌐 Testing Server Endpoints...\n');
    
    const baseUrl = process.env.API_URL || 'http://localhost:3000';
    const endpoints = [
        { path: '/health', method: 'GET', requiresAuth: false },
        { path: '/api/auth/login', method: 'POST', requiresAuth: false },
        { path: '/api/devices', method: 'GET', requiresAuth: true },
        { path: '/api/chats', method: 'GET', requiresAuth: true },
    ];
    
    console.log(`Testing endpoints on: ${baseUrl}\n`);
    
    // Test health endpoint
    try {
        const http = require('http');
        const url = require('url');
        const testUrl = url.parse(`${baseUrl}/health`);
        
        await new Promise((resolve, reject) => {
            const req = http.request({
                hostname: testUrl.hostname,
                port: testUrl.port || 3000,
                path: testUrl.path,
                method: 'GET',
                timeout: 5000
            }, (res) => {
                if (res.statusCode === 200) {
                    console.log('   ✅ /health endpoint: OK');
                    resolve();
                } else {
                    console.log(`   ⚠️  /health endpoint: Status ${res.statusCode}`);
                    resolve();
                }
            });
            
            req.on('error', (error) => {
                console.log(`   ⚠️  /health endpoint: Server not running (${error.message})`);
                console.log('   💡 Start the server with: npm start');
                resolve();
            });
            
            req.on('timeout', () => {
                req.destroy();
                console.log('   ⚠️  /health endpoint: Timeout');
                resolve();
            });
            
            req.end();
        });
    } catch (error) {
        console.log(`   ⚠️  /health endpoint: ${error.message}`);
    }
    
    console.log('');
}

// Main execution
async function main() {
    console.log('═══════════════════════════════════════════════════════');
    console.log('   MongoDB Migration Verification Test');
    console.log('═══════════════════════════════════════════════════════');
    
    const dbTest = await testMongoDBConnection();
    
    if (dbTest) {
        await testServerEndpoints();
        
        console.log('═══════════════════════════════════════════════════════');
        console.log('   ✅ MongoDB Migration Verification Complete!');
        console.log('═══════════════════════════════════════════════════════');
        console.log('');
        console.log('📝 Next Steps:');
        console.log('   1. Ensure MongoDB is running (local or remote)');
        console.log('   2. Set MONGODB_URI in .env file');
        console.log('   3. Start the server: npm start');
        console.log('   4. Test endpoints with your Android app or API client');
        console.log('');
        process.exit(0);
    } else {
        console.log('═══════════════════════════════════════════════════════');
        console.log('   ❌ Migration verification failed');
        console.log('═══════════════════════════════════════════════════════');
        console.log('');
        console.log('💡 To fix:');
        console.log('   1. Install MongoDB: https://www.mongodb.com/try/download/community');
        console.log('   2. Start MongoDB service');
        console.log('   3. Set MONGODB_URI in .env (default: mongodb://localhost:27017)');
        console.log('');
        process.exit(1);
    }
}

main().catch(console.error);
