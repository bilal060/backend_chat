const { MongoClient } = require('mongodb');
const bcrypt = require('bcrypt');

// MongoDB connection string
const MONGODB_URI = process.env.MONGODB_URI || 'mongodb://localhost:27017';
const DB_NAME = process.env.MONGODB_DB_NAME || 'chat_capture';

async function updateAdminPassword() {
    let client = null;
    
    try {
        // Connect to MongoDB
        client = new MongoClient(MONGODB_URI);
        await client.connect();
        const db = client.db(DB_NAME);
        
        console.log('Connected to MongoDB');
        
        // Find admin user by username or email
        const admin = await db.collection('users').findOne({
            $or: [
                { username: 'admin', role: 'admin' },
                { role: 'admin' }
            ]
        });
        
        if (!admin) {
            console.log('❌ Admin user not found. Creating new admin user...');
            
            // Create new admin user
            const { v4: uuidv4 } = require('uuid');
            const adminId = uuidv4();
            const passwordHash = await bcrypt.hash('Adm!n', 10);
            const createdAt = Math.floor(Date.now() / 1000);
            
            await db.collection('users').insertOne({
                id: adminId,
                username: 'admin',
                email: 'admin@admin.com',
                password: passwordHash,
                role: 'admin',
                createdAt: createdAt,
                lastLogin: null
            });
            
            console.log('✅ Admin user created successfully');
            console.log('   Username: admin');
            console.log('   Password: Adm!n');
        } else {
            console.log(`Found admin user: ${admin.username || admin.email}`);
            
            // Update password
            const passwordHash = await bcrypt.hash('Adm!n', 10);
            
            await db.collection('users').updateOne(
                { id: admin.id },
                {
                    $set: {
                        username: 'admin',
                        password: passwordHash
                    }
                }
            );
            
            console.log('✅ Admin password updated successfully');
            console.log('   Username: admin');
            console.log('   Password: Adm!n');
        }
        
    } catch (error) {
        console.error('❌ Error updating admin password:', error);
        process.exit(1);
    } finally {
        if (client) {
            await client.close();
            console.log('MongoDB connection closed');
        }
    }
}

// Run the update
updateAdminPassword()
    .then(() => {
        console.log('\n✅ Done!');
        process.exit(0);
    })
    .catch((error) => {
        console.error('❌ Fatal error:', error);
        process.exit(1);
    });
