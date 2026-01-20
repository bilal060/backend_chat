const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/devices/register - Register new device (receiver app)
// No auth required - receiver apps register themselves
router.post('/register', async (req, res) => {
    try {
        const { deviceId, deviceName, model, osVersion, imei, fcmToken } = req.body;

        if (!deviceId) {
            return res.status(400).json({
                success: false,
                message: 'deviceId is required'
            });
        }

        const db = getDb();
        
        // Check if device already exists
        const existingDevice = await db.collection('devices').findOne({ deviceId });

        if (existingDevice) {
            // Update existing device
            const updateDoc = {
                $set: {
                    deviceName: deviceName || existingDevice.deviceName,
                    model: model || existingDevice.model,
                    osVersion: osVersion || existingDevice.osVersion,
                    imei: imei || existingDevice.imei,
                    fcmToken: fcmToken || existingDevice.fcmToken,
                    lastSeen: Date.now(),
                    status: 'active'
                }
            };

            await db.collection('devices').updateOne({ deviceId }, updateDoc);

            res.json({
                success: true,
                message: 'Device updated',
                device: {
                    id: existingDevice.id,
                    deviceId: deviceId,
                    deviceName: deviceName || existingDevice.deviceName,
                    model: model || existingDevice.model,
                    osVersion: osVersion || existingDevice.osVersion,
                    status: 'active'
                }
            });
        } else {
            // Create new device
            const id = uuidv4();
            const deviceDoc = {
                id: id,
                deviceId: deviceId,
                deviceName: deviceName || null,
                model: model || null,
                osVersion: osVersion || null,
                imei: imei || null,
                fcmToken: fcmToken || null,
                lastSeen: Date.now(),
                status: 'active',
                ownerId: null,
                createdAt: Math.floor(Date.now() / 1000)
            };

            await db.collection('devices').insertOne(deviceDoc);

            res.json({
                success: true,
                message: 'Device registered',
                device: {
                    id: id,
                    deviceId: deviceId,
                    deviceName: deviceName,
                    model: model,
                    osVersion: osVersion,
                    status: 'active'
                }
            });
        }
    } catch (error) {
        console.error('Error registering device:', error);
        res.status(500).json({
            success: false,
            message: 'Error registering device'
        });
    }
});

// GET /api/devices - List devices (with role-based filtering)
router.get('/', authenticate, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;

        const db = getDb();
        const filter = {};

        // Device owners can only see their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        }

        const devices = await db.collection('devices')
            .find(filter)
            .sort({ lastSeen: -1 })
            .toArray();

        res.json({
            success: true,
            devices: devices,
            count: devices.length
        });
    } catch (error) {
        console.error('Error fetching devices:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching devices'
        });
    }
});

// GET /api/devices/:deviceId/commands/pending - Get pending commands for device (for polling)
// MUST be before /:deviceId route to avoid route matching conflicts
router.get('/:deviceId/commands/pending', async (req, res) => {
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

        // Parse parameters JSON if it's a string
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

// GET /api/devices/:deviceId - Get device details (with authorization check)
router.get('/:deviceId', authenticate, async (req, res) => {
    try {
        const { deviceId } = req.params;
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;

        // Authorization check: Device owners can only access their assigned device
        if (role === 'device_owner' && assignedDeviceId !== deviceId) {
            return res.status(403).json({
                success: false,
                message: 'Access denied: You can only access your assigned device'
            });
        }

        const db = getDb();
        const device = await db.collection('devices').findOne({ deviceId });

        if (!device) {
            return res.status(404).json({
                success: false,
                message: 'Device not found'
            });
        }

        res.json({
            success: true,
            device: device
        });
    } catch (error) {
        console.error('Error fetching device:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching device'
        });
    }
});

// POST /api/devices/:deviceId/heartbeat - Update device heartbeat (receiver app)
router.post('/:deviceId/heartbeat', async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { fcmToken } = req.body;

        const db = getDb();
        const updateDoc = {
            $set: {
                lastSeen: Date.now(),
                status: 'active'
            }
        };

        if (fcmToken) {
            updateDoc.$set.fcmToken = fcmToken;
        }

        await db.collection('devices').updateOne({ deviceId }, updateDoc);

        res.json({
            success: true,
            message: 'Heartbeat updated'
        });
    } catch (error) {
        console.error('Error updating heartbeat:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating heartbeat'
        });
    }
});

// PUT /api/devices/:deviceId - Update device info (admin only)
router.put('/:deviceId', authenticate, async (req, res) => {
    try {
        const { deviceId } = req.params;
        const user = req.user || {};

        // Only admin can update device info
        if (user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Access denied: Admin only'
            });
        }

        const { deviceName, model, osVersion, status } = req.body;

        const updateDoc = { $set: {} };

        if (deviceName !== undefined) updateDoc.$set.deviceName = deviceName;
        if (model !== undefined) updateDoc.$set.model = model;
        if (osVersion !== undefined) updateDoc.$set.osVersion = osVersion;
        if (status !== undefined) updateDoc.$set.status = status;

        if (Object.keys(updateDoc.$set).length === 0) {
            return res.status(400).json({
                success: false,
                message: 'No fields to update'
            });
        }

        const db = getDb();
        await db.collection('devices').updateOne({ deviceId }, updateDoc);

        res.json({
            success: true,
            message: 'Device updated'
        });
    } catch (error) {
        console.error('Error updating device:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating device'
        });
    }
});

// POST /api/devices/:deviceId/assign-owner - Assign device to device owner (admin only)
router.post('/:deviceId/assign-owner', authenticate, async (req, res) => {
    try {
        const { deviceId } = req.params;
        const { userId } = req.body;
        const user = req.user || {};

        // Only admin can assign devices
        if (user.role !== 'admin') {
            return res.status(403).json({
                success: false,
                message: 'Access denied: Admin only'
            });
        }

        if (!userId) {
            return res.status(400).json({
                success: false,
                message: 'userId is required'
            });
        }

        const db = getDb();

        // Check if device exists
        const device = await db.collection('devices').findOne({ deviceId });
        if (!device) {
            return res.status(404).json({
                success: false,
                message: 'Device not found'
            });
        }

        // Check if user exists and is a device owner
        const owner = await db.collection('users').findOne({ id: userId, role: 'device_owner' });
        if (!owner) {
            return res.status(404).json({
                success: false,
                message: 'Device owner not found'
            });
        }

        // Check if device is already assigned
        const existing = await db.collection('device_ownership').findOne({ deviceId });

        if (existing) {
            // Update existing assignment
            await db.collection('device_ownership').updateOne(
                { deviceId },
                { $set: { userId, assignedAt: Math.floor(Date.now() / 1000) } }
            );
        } else {
            // Create new assignment
            const id = uuidv4();
            await db.collection('device_ownership').insertOne({
                id,
                userId,
                deviceId,
                assignedAt: Math.floor(Date.now() / 1000)
            });
        }

        // Update device ownerId
        await db.collection('devices').updateOne(
            { deviceId },
            { $set: { ownerId: userId } }
        );

        res.json({
            success: true,
            message: 'Device assigned to owner'
        });
    } catch (error) {
        console.error('Error assigning device:', error);
        res.status(500).json({
            success: false,
            message: 'Error assigning device'
        });
    }
});

// GET /api/devices/:deviceId/ownership - Get device ownership information
router.get('/:deviceId/ownership', authenticate, async (req, res) => {
    try {
        const { deviceId } = req.params;
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        
        // Authorization check: Device owners can only see their assigned device
        if (role === 'device_owner' && assignedDeviceId !== deviceId) {
            return res.status(403).json({
                success: false,
                message: 'Access denied: You can only access your assigned device'
            });
        }
        
        const db = getDb();
        const ownership = await db.collection('device_ownership')
            .findOne({ deviceId });
        
        if (ownership) {
            const owner = await db.collection('users')
                .findOne({ id: ownership.userId });
            
            res.json({
                success: true,
                data: {
                    deviceId: ownership.deviceId,
                    userId: ownership.userId,
                    owner: owner ? {
                        id: owner.id,
                        username: owner.username,
                        email: owner.email
                    } : null
                }
            });
        } else {
            res.json({
                success: true,
                data: null
            });
        }
    } catch (error) {
        console.error('Error fetching device ownership:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching device ownership'
        });
    }
});

module.exports = router;
