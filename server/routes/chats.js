const express = require('express');
const router = express.Router();
const db = require('../database/init');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/chats - Single chat
router.post('/', (req, res) => {
    try {
        const chat = req.body;
        
        // Validate required fields
        if (!chat.id || !chat.appPackage || !chat.appName || !chat.text) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields'
            });
        }
        
        // Insert chat
        const stmt = db.prepare(`INSERT INTO chats 
            (id, deviceId, appPackage, appName, chatIdentifier, text, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`);
        
        stmt.run(
            chat.id,
            chat.deviceId || null,
            chat.appPackage,
            chat.appName,
            chat.chatIdentifier || null,
            chat.text,
            chat.timestamp || Date.now(),
            1 // Mark as synced
        );
        stmt.finalize();
        
        // Broadcast WebSocket update
        if (chat.deviceId) {
            websocketService.broadcastDataUpdate(chat.deviceId, 'chat', chat);
        }
        
        res.json({
            success: true,
            message: 'Chat saved successfully'
        });
    } catch (error) {
        console.error('Error saving chat:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving chat'
        });
    }
});

// POST /api/chats/batch - Batch chats
router.post('/batch', (req, res) => {
    try {
        const chats = req.body;
        
        if (!Array.isArray(chats) || chats.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid batch data'
            });
        }
        
        const stmt = db.prepare(`INSERT OR REPLACE INTO chats 
            (id, deviceId, appPackage, appName, chatIdentifier, text, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)`);
        
        db.serialize(() => {
            db.run('BEGIN TRANSACTION');
            
            chats.forEach(chat => {
                stmt.run(
                    chat.id,
                    chat.deviceId || null,
                    chat.appPackage,
                    chat.appName,
                    chat.chatIdentifier || null,
                    chat.text,
                    chat.timestamp || Date.now(),
                    1
                );
                
                // Broadcast WebSocket update for each chat
                if (chat.deviceId) {
                    websocketService.broadcastDataUpdate(chat.deviceId, 'chat', chat);
                }
            });
            
            db.run('COMMIT', (err) => {
                if (err) {
                    console.error('Error committing batch:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error saving batch'
                    });
                }
                
                stmt.finalize();
                res.json({
                    success: true,
                    message: `Saved ${chats.length} chats`
                });
            });
        });
    } catch (error) {
        console.error('Error saving batch:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving batch'
        });
    }
});

// GET /api/chats - Get chats (with device filtering)
router.get('/', optionalAuth, (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const deviceId = req.query.deviceId;
        
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const offset = (page - 1) * limit;
        
        let query = 'SELECT * FROM chats WHERE 1=1';
        const params = [];
        
        // Device owners can only see chats from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            query += ' AND deviceId = ?';
            params.push(assignedDeviceId);
        } else if (deviceId) {
            // Admin can filter by deviceId
            query += ' AND deviceId = ?';
            params.push(deviceId);
        }
        
        query += ' ORDER BY timestamp DESC LIMIT ? OFFSET ?';
        params.push(limit, offset);
        
        db.all(query, params,
            (err, rows) => {
                if (err) {
                    console.error('Error fetching chats:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error fetching chats'
                    });
                }
                
                const chats = rows.map(row => ({
                    ...row,
                    synced: row.synced === 1
                }));
                
                res.json({
                    success: true,
                    data: chats
                });
            }
        );
    } catch (error) {
        console.error('Error fetching chats:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching chats'
        });
    }
});

module.exports = router;
