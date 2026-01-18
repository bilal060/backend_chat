const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const db = require('../database/init');
const { v4: uuidv4 } = require('uuid');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// Configure multer for screenshot uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const deviceId = req.params.deviceId || 'unknown';
        const screenshotsDir = path.join(__dirname, '../uploads/screenshots', deviceId);
        if (!fs.existsSync(screenshotsDir)) {
            fs.mkdirSync(screenshotsDir, { recursive: true });
        }
        cb(null, screenshotsDir);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, `screenshot-${uniqueSuffix}.png`);
    }
});

const upload = multer({
    storage: storage,
    limits: {
        fileSize: 10 * 1024 * 1024 // 10MB max file size
    },
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'image/png' || file.mimetype === 'image/jpeg') {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type. Only PNG and JPEG are allowed.'));
        }
    }
});

// POST /api/devices/:deviceId/screenshots - Upload screenshot (receiver app)
router.post('/devices/:deviceId/screenshots', upload.single('screenshot'), (req, res) => {
    try {
        const { deviceId } = req.params;
        const file = req.file;

        if (!file) {
            return res.status(400).json({
                success: false,
                message: 'No file uploaded'
            });
        }

        const screenshotData = {
            id: uuidv4(),
            deviceId: deviceId,
            filePath: file.path,
            fileName: file.filename,
            fileSize: file.size,
            timestamp: Date.now()
        };

        // Broadcast WebSocket update
        websocketService.broadcastDataUpdate(deviceId, 'screenshot', screenshotData);

        res.json({
            success: true,
            message: 'Screenshot uploaded',
            screenshot: screenshotData
        });
    } catch (error) {
        console.error('Error uploading screenshot:', error);
        res.status(500).json({
            success: false,
            message: 'Error uploading screenshot'
        });
    }
});

// GET /api/devices/:deviceId/screenshots - Get screenshot history (with authorization)
router.get('/devices/:deviceId/screenshots', authenticate, (req, res) => {
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

        // For now, return empty array (screenshot history storage can be implemented later)
        res.json({
            success: true,
            screenshots: []
        });
    } catch (error) {
        console.error('Error fetching screenshots:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching screenshots'
        });
    }
});

module.exports = router;
