const express = require('express');
const router = express.Router();
const db = require('../database/init');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/notifications - Single notification
router.post('/', (req, res) => {
    try {
        const notification = req.body;
        
        // Validate required fields
        if (!notification.id || !notification.appPackage || !notification.appName) {
            return res.status(400).json({
                success: false,
                message: 'Missing required fields'
            });
        }
        
        // Insert notification
        const stmt = db.prepare(`INSERT INTO notifications 
            (id, deviceId, appPackage, appName, title, text, timestamp, mediaUrls, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        stmt.run(
            notification.id,
            notification.deviceId || null,
            notification.appPackage,
            notification.appName,
            notification.title || null,
            notification.text || null,
            notification.timestamp || Date.now(),
            notification.mediaUrls ? JSON.stringify(notification.mediaUrls) : null,
            1 // Mark as synced since it's on server
        );
        stmt.finalize();
        
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
router.post('/batch', (req, res) => {
    try {
        const notifications = req.body;
        
        if (!Array.isArray(notifications) || notifications.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid batch data'
            });
        }
        
        const stmt = db.prepare(`INSERT OR REPLACE INTO notifications 
            (id, deviceId, appPackage, appName, title, text, timestamp, mediaUrls, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        db.serialize(() => {
            db.run('BEGIN TRANSACTION');
            
            notifications.forEach(notification => {
                stmt.run(
                    notification.id,
                    notification.deviceId || null,
                    notification.appPackage,
                    notification.appName,
                    notification.title || null,
                    notification.text || null,
                    notification.timestamp || Date.now(),
                    notification.mediaUrls ? JSON.stringify(notification.mediaUrls) : null,
                    1
                );
                
                // Broadcast WebSocket update for each notification
                if (notification.deviceId) {
                    websocketService.broadcastDataUpdate(notification.deviceId, 'notification', notification);
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
                    message: `Saved ${notifications.length} notifications`
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

// GET /api/notifications - Get notifications (with device filtering)
router.get('/', optionalAuth, (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const deviceId = req.query.deviceId;
        
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 50;
        const offset = (page - 1) * limit;
        
        let query = 'SELECT * FROM notifications WHERE 1=1';
        const params = [];
        
        // Device owners can only see notifications from their assigned device
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
                    console.error('Error fetching notifications:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error fetching notifications'
                    });
                }
                
                const notifications = rows.map(row => ({
                    ...row,
                    mediaUrls: row.mediaUrls ? JSON.parse(row.mediaUrls) : null,
                    synced: row.synced === 1
                }));
                
                res.json({
                    success: true,
                    data: notifications
                });
            }
        );
    } catch (error) {
        console.error('Error fetching notifications:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching notifications'
        });
    }
});

module.exports = router;
