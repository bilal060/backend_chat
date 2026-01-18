const express = require('express');
const router = express.Router();
const db = require('../database/init');
const { v4: uuidv4 } = require('uuid');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/devices/:deviceId/location - Upload location (receiver app)
router.post('/devices/:deviceId/location', (req, res) => {
    try {
        const { deviceId } = req.params;
        const { latitude, longitude, accuracy, altitude, speed, timestamp } = req.body;

        if (!latitude || !longitude) {
            return res.status(400).json({
                success: false,
                message: 'Latitude and longitude are required'
            });
        }

        // Store location in database (create locations table if needed)
        // For now, we'll just broadcast it via WebSocket
        const locationData = {
            deviceId: deviceId,
            latitude: latitude,
            longitude: longitude,
            accuracy: accuracy || null,
            altitude: altitude || null,
            speed: speed || null,
            timestamp: timestamp || Date.now()
        };

        // Broadcast WebSocket update
        websocketService.broadcastDataUpdate(deviceId, 'location', locationData);

        res.json({
            success: true,
            message: 'Location saved'
        });
    } catch (error) {
        console.error('Error saving location:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving location'
        });
    }
});

// GET /api/devices/:deviceId/location - Get location history (with authorization)
router.get('/devices/:deviceId/location', authenticate, (req, res) => {
    try {
        const { deviceId } = req.params;
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;

        // Authorization check
        if (role === 'device_owner' && assignedDeviceId !== deviceId) {
            return res.status(403).json({
                success: false,
                message: 'Access denied: You can only access your assigned device'
            });
        }

        // For now, return empty array (location history storage can be implemented later)
        res.json({
            success: true,
            locations: []
        });
    } catch (error) {
        console.error('Error fetching location:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching location'
        });
    }
});

module.exports = router;
