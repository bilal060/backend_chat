const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/chats - Single chat
router.post('/', async (req, res) => {
    try {
        const chat = req.body;
        
        // Validate required fields
        if (!chat.id || !chat.appPackage || !chat.appName || !chat.text) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields'
            });
        }
        
        const db = getDb();
        const chatDoc = {
            id: chat.id,
            deviceId: chat.deviceId || null,
            appPackage: chat.appPackage,
            appName: chat.appName,
            chatIdentifier: chat.chatIdentifier || null,
            text: chat.text,
            timestamp: chat.timestamp || Date.now(),
            synced: true, // Mark as synced
            syncAttempts: 0,
            lastSyncAttempt: null,
            errorMessage: null,
            createdAt: Math.floor(Date.now() / 1000)
        };
        
        // Use replaceOne with upsert for INSERT OR REPLACE behavior
        await db.collection('chats').replaceOne(
            { id: chat.id },
            chatDoc,
            { upsert: true }
        );
        
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
router.post('/batch', async (req, res) => {
    try {
        const chats = req.body;
        
        if (!Array.isArray(chats) || chats.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid batch data'
            });
        }
        
        const db = getDb();
        const operations = chats.map(chat => ({
            replaceOne: {
                filter: { id: chat.id },
                replacement: {
                    id: chat.id,
                    deviceId: chat.deviceId || null,
                    appPackage: chat.appPackage,
                    appName: chat.appName,
                    chatIdentifier: chat.chatIdentifier || null,
                    text: chat.text,
                    timestamp: chat.timestamp || Date.now(),
                    synced: true,
                    syncAttempts: 0,
                    lastSyncAttempt: null,
                    errorMessage: null,
                    createdAt: Math.floor(Date.now() / 1000)
                },
                upsert: true
            }
        }));
        
        await db.collection('chats').bulkWrite(operations);
        
        // Broadcast WebSocket updates for each chat
        chats.forEach(chat => {
            if (chat.deviceId) {
                websocketService.broadcastDataUpdate(chat.deviceId, 'chat', chat);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${chats.length} chats`
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
router.get('/', optionalAuth, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const deviceId = req.query.deviceId;
        
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const skip = (page - 1) * limit;
        
        const db = getDb();
        const filter = {};
        
        // Device owners can only see chats from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        } else if (deviceId) {
            // Admin can filter by deviceId
            filter.deviceId = deviceId;
        }
        
        const chats = await db.collection('chats')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(limit)
            .skip(skip)
            .toArray();
        
        // Convert synced boolean back to number format if needed (for compatibility)
        const formattedChats = chats.map(chat => ({
            ...chat,
            synced: chat.synced ? 1 : 0
        }));
        
        res.json({
            success: true,
            data: formattedChats
        });
    } catch (error) {
        console.error('Error fetching chats:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching chats'
        });
    }
});

module.exports = router;
