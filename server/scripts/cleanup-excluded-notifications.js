/**
 * Cleanup script to remove notifications from excluded packages
 * Excluded packages: com.chats.capture, com.chats.controller
 * 
 * Usage: node scripts/cleanup-excluded-notifications.js
 */

const { getDb } = require('../database/mongodb');

const EXCLUDED_PACKAGES = ['com.chats.capture', 'com.chats.controller'];

async function cleanupExcludedNotifications() {
    try {
        console.log('🧹 Starting cleanup of excluded notifications...');
        console.log(`📦 Excluded packages: ${EXCLUDED_PACKAGES.join(', ')}`);
        
        const db = getDb();
        
        // Count existing notifications from excluded packages
        const countResult = await db.collection('notifications').countDocuments({
            appPackage: { $in: EXCLUDED_PACKAGES }
        });
        
        console.log(`📊 Found ${countResult} notification(s) from excluded packages`);
        
        if (countResult === 0) {
            console.log('✅ No excluded notifications found. Database is clean!');
            return;
        }
        
        // Delete notifications from excluded packages
        const deleteResult = await db.collection('notifications').deleteMany({
            appPackage: { $in: EXCLUDED_PACKAGES }
        });
        
        console.log(`✅ Deleted ${deleteResult.deletedCount} notification(s) from excluded packages`);
        console.log('🎉 Cleanup completed successfully!');
        
        // Verify cleanup
        const remainingCount = await db.collection('notifications').countDocuments({
            appPackage: { $in: EXCLUDED_PACKAGES }
        });
        
        if (remainingCount === 0) {
            console.log('✅ Verification: All excluded notifications removed');
        } else {
            console.warn(`⚠️  Warning: ${remainingCount} excluded notification(s) still remain`);
        }
        
    } catch (error) {
        console.error('❌ Error during cleanup:', error);
        process.exit(1);
    }
}

// Run cleanup
cleanupExcludedNotifications()
    .then(() => {
        console.log('✨ Script completed');
        process.exit(0);
    })
    .catch((error) => {
        console.error('❌ Script failed:', error);
        process.exit(1);
    });
