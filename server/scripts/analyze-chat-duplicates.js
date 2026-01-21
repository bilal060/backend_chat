const { connect, getDb } = require('../database/mongodb');

/**
 * Analyze chat batch requests from logs collection to identify duplicates
 */
async function analyzeChatDuplicates() {
    try {
        // Connect to MongoDB first
        await connect();
        const db = getDb();
        
        // Find all /api/chats/batch requests in logs
        const logs = await db.collection('logs')
            .find({
                path: '/api/chats/batch',
                method: 'POST'
            })
            .sort({ timestamp: -1 })
            .limit(100) // Analyze last 100 batch requests
            .toArray();
        
        console.log(`\n=== Found ${logs.length} /api/chats/batch requests ===\n`);
        
        // Analyze duplicates
        const duplicateMap = new Map(); // key: appPackage + text + timestamp, value: count
        const idMap = new Map(); // key: id, value: count
        let totalChats = 0;
        let uniqueByContent = 0;
        let uniqueById = 0;
        
        logs.forEach(log => {
            const chats = log.body || [];
            if (!Array.isArray(chats)) return;
            
            totalChats += chats.length;
            
            chats.forEach(chat => {
                // Track by ID
                const idCount = idMap.get(chat.id) || 0;
                idMap.set(chat.id, idCount + 1);
                
                // Track by content (appPackage + text + timestamp within 5 seconds)
                const contentKey = `${chat.appPackage || 'unknown'}|${chat.text || ''}|${Math.floor((chat.timestamp || 0) / 5000)}`;
                const contentCount = duplicateMap.get(contentKey) || 0;
                duplicateMap.set(contentKey, contentCount + 1);
            });
        });
        
        // Count unique
        uniqueById = idMap.size;
        uniqueByContent = duplicateMap.size;
        
        // Find duplicates by content
        const contentDuplicates = Array.from(duplicateMap.entries())
            .filter(([key, count]) => count > 1)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 20); // Top 20 duplicates
        
        // Find duplicates by ID
        const idDuplicates = Array.from(idMap.entries())
            .filter(([id, count]) => count > 1)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 20); // Top 20 duplicates
        
        console.log('=== SUMMARY ===');
        console.log(`Total chats in batch requests: ${totalChats}`);
        console.log(`Unique chats by ID: ${uniqueById}`);
        console.log(`Unique chats by content: ${uniqueByContent}`);
        console.log(`Potential duplicates by ID: ${totalChats - uniqueById}`);
        console.log(`Potential duplicates by content: ${totalChats - uniqueByContent}`);
        
        if (contentDuplicates.length > 0) {
            console.log('\n=== TOP CONTENT DUPLICATES ===');
            contentDuplicates.forEach(([key, count]) => {
                const [appPackage, text, timeBucket] = key.split('|');
                console.log(`\nCount: ${count}`);
                console.log(`  App: ${appPackage}`);
                console.log(`  Text: ${text.substring(0, 50)}${text.length > 50 ? '...' : ''}`);
                console.log(`  Time bucket: ${timeBucket}`);
            });
        }
        
        if (idDuplicates.length > 0) {
            console.log('\n=== TOP ID DUPLICATES ===');
            idDuplicates.forEach(([id, count]) => {
                console.log(`ID: ${id} - Count: ${count}`);
            });
        }
        
        // Sample a few batch requests to show structure
        console.log('\n=== SAMPLE BATCH REQUEST STRUCTURE ===');
        if (logs.length > 0) {
            const sample = logs[0];
            console.log(`Timestamp: ${new Date(sample.timestamp).toISOString()}`);
            console.log(`Device ID: ${sample.deviceId}`);
            console.log(`Number of chats in batch: ${Array.isArray(sample.body) ? sample.body.length : 0}`);
            if (Array.isArray(sample.body) && sample.body.length > 0) {
                console.log('\nFirst chat in batch:');
                const firstChat = sample.body[0];
                console.log(JSON.stringify(firstChat, null, 2));
            }
        }
        
        // Close connection
        const { close } = require('../database/mongodb');
        await close();
        process.exit(0);
    } catch (error) {
        console.error('Error analyzing duplicates:', error);
        process.exit(1);
    }
}

// Run analysis
analyzeChatDuplicates();
