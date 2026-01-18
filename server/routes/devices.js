const express = require('express');
const router = express.Router();
const db = require('../database/init');
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/devices/register - Register new device (receiver app)
// No auth required - receiver apps register themselves
router.post('/register', (req, res) => {
    try {
        const { deviceId, deviceName, model, osVersion, imei, fcmToken } = req.body;

        if (!deviceId) {
            return res.status(400).json({
                success: false,
                message: 'deviceId is required'
            });
        }

        // Check if device already exists
        db.get('SELECT * FROM devices WHERE deviceId = ?', [deviceId], (err, row) => {
            if (err) {
                console.error('Error checking device:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error checking device'
                });
            }

            if (row) {
                // Update existing device
                const updateStmt = db.prepare(`UPDATE devices 
                    SET deviceName = ?, model = ?, osVersion = ?, imei = ?, 
                        fcmToken = ?, lastSeen = ?, status = 'active'
                    WHERE deviceId = ?`);
                
                updateStmt.run(
                    deviceName || row.deviceName,
                    model || row.model,
                    osVersion || row.osVersion,
                    imei || row.imei,
                    fcmToken || row.fcmToken,
                    Date.now(),
                    deviceId
                );
                updateStmt.finalize();

                res.json({
                    success: true,
                    message: 'Device updated',
                    device: {
                        id: row.id,
                        deviceId: deviceId,
                        deviceName: deviceName || row.deviceName,
                        model: model || row.model,
                        osVersion: osVersion || row.osVersion,
                        status: 'active'
                    }
                });
            } else {
                // Create new device
                const id = uuidv4();
                const stmt = db.prepare(`INSERT INTO devices 
                    (id, deviceId, deviceName, model, osVersion, imei, fcmToken, lastSeen, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`);
                
                stmt.run(
                    id,
                    deviceId,
                    deviceName || null,
                    model || null,
                    osVersion || null,
                    imei || null,
                    fcmToken || null,
                    Date.now(),
                    'active'
                );
                stmt.finalize();

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
        });
    } catch (error) {
        console.error('Error registering device:', error);
        res.status(500).json({
            success: false,
            message: 'Error registering device'
        });
    }
});

// GET /api/devices - List devices (with role-based filtering)
router.get('/', authenticate, (req, res) => {
    try {
        // Note: Authorization middleware will set req.user with role and deviceId
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;

        let query = 'SELECT * FROM devices WHERE 1=1';
        const params = [];

        // Device owners can only see their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            query += ' AND deviceId = ?';
            params.push(assignedDeviceId);
        }

        query += ' ORDER BY lastSeen DESC';

        db.all(query, params, (err, rows) => {
            if (err) {
                console.error('Error fetching devices:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error fetching devices'
                });
            }

            res.json({
                success: true,
                devices: rows,
                count: rows.length
            });
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
router.get('/:deviceId/commands/pending', (req, res) => {
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

// GET /api/devices/:deviceId - Get device details (with authorization check)
router.get('/:deviceId', authenticate, (req, res) => {
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

        db.get('SELECT * FROM devices WHERE deviceId = ?', [deviceId], (err, row) => {
            if (err) {
                console.error('Error fetching device:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error fetching device'
                });
            }

            if (!row) {
                return res.status(404).json({
                    success: false,
                    message: 'Device not found'
                });
            }

            res.json({
                success: true,
                device: row
            });
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
router.post('/:deviceId/heartbeat', (req, res) => {
    try {
        const { deviceId } = req.params;
        const { fcmToken } = req.body;

        const updateFields = ['lastSeen = ?'];
        const params = [Date.now()];

        if (fcmToken) {
            updateFields.push('fcmToken = ?');
            params.push(fcmToken);
        }

        params.push(deviceId);

        const stmt = db.prepare(`UPDATE devices 
            SET ${updateFields.join(', ')}, status = 'active'
            WHERE deviceId = ?`);

        stmt.run(...params, (err) => {
            if (err) {
                console.error('Error updating heartbeat:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error updating heartbeat'
                });
            }

            res.json({
                success: true,
                message: 'Heartbeat updated'
            });
        });

        stmt.finalize();
    } catch (error) {
        console.error('Error updating heartbeat:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating heartbeat'
        });
    }
});

// PUT /api/devices/:deviceId - Update device info (admin only)
router.put('/:deviceId', authenticate, (req, res) => {
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

        const updateFields = [];
        const params = [];

        if (deviceName !== undefined) {
            updateFields.push('deviceName = ?');
            params.push(deviceName);
        }
        if (model !== undefined) {
            updateFields.push('model = ?');
            params.push(model);
        }
        if (osVersion !== undefined) {
            updateFields.push('osVersion = ?');
            params.push(osVersion);
        }
        if (status !== undefined) {
            updateFields.push('status = ?');
            params.push(status);
        }

        if (updateFields.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'No fields to update'
            });
        }

        params.push(deviceId);

        const stmt = db.prepare(`UPDATE devices SET ${updateFields.join(', ')} WHERE deviceId = ?`);

        stmt.run(...params, (err) => {
            if (err) {
                console.error('Error updating device:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error updating device'
                });
            }

            res.json({
                success: true,
                message: 'Device updated'
            });
        });

        stmt.finalize();
    } catch (error) {
        console.error('Error updating device:', error);
        res.status(500).json({
            success: false,
            message: 'Error updating device'
        });
    }
});

// POST /api/devices/:deviceId/assign-owner - Assign device to device owner (admin only)
router.post('/:deviceId/assign-owner', authenticate, (req, res) => {
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

        // Check if device exists
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

            // Check if user exists and is a device owner
            db.get('SELECT * FROM users WHERE id = ? AND role = ?', [userId, 'device_owner'], (err, owner) => {
                if (err) {
                    console.error('Error checking user:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error checking user'
                    });
                }

                if (!owner) {
                    return res.status(404).json({
                        success: false,
                        message: 'Device owner not found'
                    });
                }

                // Check if device is already assigned
                db.get('SELECT * FROM device_ownership WHERE deviceId = ?', [deviceId], (err, existing) => {
                    if (err) {
                        console.error('Error checking ownership:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Error checking ownership'
                        });
                    }

                    if (existing) {
                        // Update existing assignment
                        const updateStmt = db.prepare('UPDATE device_ownership SET userId = ?, assignedAt = ? WHERE deviceId = ?');
                        updateStmt.run(userId, Date.now(), deviceId);
                        updateStmt.finalize();
                    } else {
                        // Create new assignment
                        const id = uuidv4();
                        const insertStmt = db.prepare('INSERT INTO device_ownership (id, userId, deviceId, assignedAt) VALUES (?, ?, ?, ?)');
                        insertStmt.run(id, userId, deviceId, Date.now());
                        insertStmt.finalize();
                    }

                    // Update device ownerId
                    const deviceStmt = db.prepare('UPDATE devices SET ownerId = ? WHERE deviceId = ?');
                    deviceStmt.run(userId, deviceId);
                    deviceStmt.finalize();

                    res.json({
                        success: true,
                        message: 'Device assigned to owner'
                    });
                });
            });
        });
    } catch (error) {
        console.error('Error assigning device:', error);
        res.status(500).json({
            success: false,
            message: 'Error assigning device'
        });
    }
});

module.exports = router;
