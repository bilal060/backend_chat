const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');
const fcmService = require('../services/fcmService');

// POST /api/commands - Queue command for device (admin only)
router.post('/', authenticate, async (req, res) => {
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

        const db = getDb();

        // Validate device exists
        const device = await db.collection('devices').findOne({ deviceId });
        if (!device) {
            return res.status(404).json({
                success: false,
                message: 'Device not found'
            });
        }

        // Create command
        const id = uuidv4();
        const commandDoc = {
            id: id,
            deviceId: deviceId,
            action: action,
            parameters: parameters || null,
            status: 'pending',
            result: null,
            createdAt: Date.now(),
            executedAt: null
        };

        await db.collection('commands').insertOne(commandDoc);

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
    } catch (error) {
        console.error('Error queueing command:', error);
        res.status(500).json({
            success: false,
            message: 'Error queueing command'
        });
    }
});

// GET /api/devices/:deviceId/commands/pending - Get pending commands (for polling)
router.get('/devices/:deviceId/commands/pending', async (req, res) => {
    try {
        const { deviceId } = req.params;

        const db = getDb();
        const commands = await db.collection('commands')
            .find({ 
                deviceId: deviceId,
                status: 'pending'
            })
            .sort({ createdAt: 1 })
            .toArray();

        // Format parameters (handle both string and object)
        const formattedCommands = commands.map(cmd => ({
            ...cmd,
            parameters: typeof cmd.parameters === 'string' ? JSON.parse(cmd.parameters) : (cmd.parameters || {})
        }));

        res.json({
            success: true,
            commands: formattedCommands
        });
    } catch (error) {
        console.error('Error fetching pending commands:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching pending commands'
        });
    }
});

// PUT /api/commands/:commandId/result - Update command result
router.put('/:commandId/result', async (req, res) => {
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

        const db = getDb();
        await db.collection('commands').updateOne(
            { id: commandId },
            {
                $set: {
                    status: success ? 'completed' : 'failed',
                    result: result,
                    executedAt: Date.now()
                }
            }
        );

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
router.get('/devices/:deviceId/commands', authenticate, async (req, res) => {
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
        const skip = parseInt(req.query.offset) || 0;

        const db = getDb();
        const commands = await db.collection('commands')
            .find({ deviceId: deviceId })
            .sort({ createdAt: -1 })
            .limit(limit)
            .skip(skip)
            .toArray();

        // Format JSON fields (handle both string and object)
        const formattedCommands = commands.map(cmd => ({
            ...cmd,
            parameters: typeof cmd.parameters === 'string' ? JSON.parse(cmd.parameters) : (cmd.parameters || {}),
            result: typeof cmd.result === 'string' ? JSON.parse(cmd.result) : (cmd.result || null)
        }));

        res.json({
            success: true,
            commands: formattedCommands,
            count: formattedCommands.length
        });
    } catch (error) {
        console.error('Error fetching commands:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching commands'
        });
    }
});

module.exports = router;
