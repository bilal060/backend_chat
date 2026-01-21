const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/notifications - Single notification
router.post('/', async (req, res) => {
    try {
        const notification = req.body;
        
        // Validate required fields
        if (!notification.id || !notification.appPackage || !notification.appName) {
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
        
        // Broadcast WebSocket update
        if (notification.deviceId) {
            websocketService.broadcastDataUpdate(notification.deviceId, 'notification', notification);
        }
        
        res.json({
            success: true,
            message: 'Notification saved successfully'
        });
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
        const notifications = req.body;
        
        if (!Array.isArray(notifications) || notifications.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid batch data'
            });
        }
        
        const db = getDb();
        const ids = notifications.map(notification => notification.id).filter(Boolean);
        const existingNotifications = ids.length
            ? await db.collection('notifications').find({ id: { $in: ids } }).toArray()
            : [];
        const existingIconMap = new Map(existingNotifications.map(n => [n.id, n.iconUrl]));

        const operations = notifications.map(notification => {
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
        
        await db.collection('notifications').bulkWrite(operations);
        
        // Broadcast WebSocket updates for each notification
        notifications.forEach(notification => {
            if (notification.deviceId) {
                websocketService.broadcastDataUpdate(notification.deviceId, 'notification', notification);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${notifications.length} notifications`
        });
    } catch (error) {
        console.error('Error saving batch:', error);
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
        const limit = parseInt(req.query.limit) || 50;
        const skip = (page - 1) * limit;
        
        const db = getDb();
        const filter = {};
        
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
        
        const notifications = await db.collection('notifications')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(limit)
            .skip(skip)
            .toArray();
        
        // Format response - convert synced to boolean (handle both boolean and number formats)
        const formattedNotifications = notifications.map(notification => ({
            ...notification,
            mediaUrls: notification.mediaUrls || null,
            serverMediaUrls: notification.serverMediaUrls || null,
            synced: notification.synced === true || notification.synced === 1
        }));
        
        res.json({
            success: true,
            data: formattedNotifications
        });
    } catch (error) {
        console.error('Error fetching notifications:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching notifications'
        });
    }
});

module.exports = router;
