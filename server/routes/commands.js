const express = require('express');
const router = express.Router();
const db = require('../database/init');
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');
const fcmService = require('../services/fcmService');

// POST /api/commands - Queue command for device (admin only)
router.post('/', authenticate, (req, res) => {
    try {
        const user = req.user || {};

        // Only admin can queue commands
        if (user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Access denied: Admin only'
            });
        }

        const { deviceId, action, parameters } = req.body;

        if (!deviceId || !action) {
            return res.status(400).json({
                success: false,
                message: 'deviceId and action are required'
            });
        }

        // Validate device exists
        db.get('SELECT * FROM devices WHERE deviceId = ?', [deviceId], (err, device) => {
            if (err) {
                console.error('Error checking device:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error checking device'
                });
            }

            if (!device) {
                return res.status(404).json({
                    success: false,
                    message: 'Device not found'
                });
            }

            // Create command
            const id = uuidv4();
            const stmt = db.prepare(`INSERT INTO commands 
                (id, deviceId, action, parameters, status, createdAt)
                VALUES (?, ?, ?, ?, ?, ?)`);

            stmt.run(
                id,
                deviceId,
                action,
                parameters ? JSON.stringify(parameters) : null,
                'pending',
                Date.now()
            );
            stmt.finalize();

            // Send FCM push notification to device
            const command = {
                id: id,
                action: action,
                parameters: parameters || {}
            };

            fcmService.sendCommandToDevice(deviceId, command)
                .then(() => {
                    console.log(`FCM command sent to device ${deviceId}`);
                })
                .catch((err) => {
                    console.error(`Failed to send FCM command to device ${deviceId}:`, err);
                    // Don't fail the request if FCM fails - device can poll for commands
                });

            res.json({
                success: true,
                message: 'Command queued',
                command: {
                    id: id,
                    deviceId: deviceId,
                    action: action,
                    status: 'pending'
                }
            });
        });
    } catch (error) {
        console.error('Error queueing command:', error);
        res.status(500).json({
            success: false,
            message: 'Error queueing command'
        });
    }
});

// GET /api/devices/:deviceId/commands/pending - Get pending commands (for polling)
// Note: This route is mounted at /api/commands, but we need it at /api/devices
// So we'll add it to devices route instead, or create a separate route here
// For now, adding a route that matches the app's expected path
router.get('/devices/:deviceId/commands/pending', (req, res) => {
    try {
        const { deviceId } = req.params;

        db.all(`SELECT * FROM commands 
            WHERE deviceId = ? AND status = 'pending' 
            ORDER BY createdAt ASC`, 
            [deviceId], 
            (err, rows) => {
                if (err) {
                    console.error('Error fetching pending commands:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error fetching pending commands'
                    });
                }

                // Parse parameters JSON
                const commands = rows.map(row => ({
                    ...row,
                    parameters: row.parameters ? JSON.parse(row.parameters) : {}
                }));

                res.json({
                    success: true,
                    commands: commands
                });
            }
        );
    } catch (error) {
        console.error('Error fetching pending commands:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching pending commands'
        });
    }
});

// PUT /api/commands/:commandId/result - Update command result
router.put('/:commandId/result', (req, res) => {
    try {
        const { commandId } = req.params;
        const { success, message, data } = req.body;

        if (success === undefined) {
            return res.status(400).json({
                success: false,
                message: 'success field is required'
            });
        }

        const result = {
            success: success,
            message: message || null,
            data: data || null
        };

        const stmt = db.prepare(`UPDATE commands 
            SET status = ?, result = ?, executedAt = ?
            WHERE id = ?`);

        stmt.run(
            success ? 'completed' : 'failed',
            JSON.stringify(result),
            Date.now(),
            commandId
        );
        stmt.finalize();

        res.json({
            success: true,
            message: 'Command result updated'
        });
    } catch (error) {
        console.error('Error updating command result:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating command result'
        });
    }
});

// GET /api/devices/:deviceId/commands - Get command history (with authorization)
router.get('/devices/:deviceId/commands', authenticate, (req, res) => {
    try {
        const { deviceId } = req.params;
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;

        // Authorization check: Device owners can only see commands for their assigned device
        if (role === 'device_owner' && assignedDeviceId !== deviceId) {
            return res.status(403).json({
                success: false,
                message: 'Access denied: You can only access your assigned device'
            });
        }

        const limit = parseInt(req.query.limit) || 50;
        const offset = parseInt(req.query.offset) || 0;

        db.all(`SELECT * FROM commands 
            WHERE deviceId = ? 
            ORDER BY createdAt DESC 
            LIMIT ? OFFSET ?`, 
            [deviceId, limit, offset], 
            (err, rows) => {
                if (err) {
                    console.error('Error fetching commands:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error fetching commands'
                    });
                }

                // Parse JSON fields
                const commands = rows.map(row => ({
                    ...row,
                    parameters: row.parameters ? JSON.parse(row.parameters) : {},
                    result: row.result ? JSON.parse(row.result) : null
                }));

                res.json({
                    success: true,
                    commands: commands,
                    count: commands.length
                });
            }
        );
    } catch (error) {
        console.error('Error fetching commands:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching commands'
        });
    }
});

module.exports = router;
