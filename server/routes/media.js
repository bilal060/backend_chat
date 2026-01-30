const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');
const { getDb } = require('../database/mongodb');
const websocketService = require('../services/websocketService');
const { authenticate } = require('../middleware/auth');

// Configure multer for media uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const appPackage = req.body.appPackage || 'unknown';
        const dateDir = new Date().toISOString().split('T')[0];
        const dest = path.join(__dirname, '..', 'uploads', 'notifications', appPackage, dateDir);
        if (!fs.existsSync(dest)) {
            fs.mkdirSync(dest, { recursive: true });
        }
        cb(null, dest);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({
    storage: storage,
    limits: {
        fileSize: 50 * 1024 * 1024 // 50MB
    }
});

// POST /api/media/upload
router.post('/upload', upload.single('file'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: 'No file uploaded'
            });
        }
        
        const { notificationId, checksum, mimeType } = req.body;
        
        if (!notificationId) {
            // Delete uploaded file
            fs.unlinkSync(req.file.path);
            return res.status(400).json({
                success: false,
                message: 'Missing notification ID'
            });
        }
        
        // Calculate checksum if provided
        let calculatedChecksum = null;
        if (checksum) {
            const fileBuffer = fs.readFileSync(req.file.path);
            calculatedChecksum = crypto.createHash('sha256').update(fileBuffer).digest('hex');
            
            // Verify checksum if provided
            if (checksum !== calculatedChecksum) {
                fs.unlinkSync(req.file.path);
                return res.status(400).json({
                    success: false,
                    message: 'Checksum mismatch'
                });
            }
        }
        
        // Generate file URL based on actual path structure
        const appPackage = req.body.appPackage || 'unknown';
        const dateDir = new Date().toISOString().split('T')[0];
        const relativePath = `notifications/${appPackage}/${dateDir}/${req.file.filename}`;
        const baseUrl = (process.env.PUBLIC_BASE_URL || `${req.protocol}://${req.get('host')}`).replace(/\/$/, '');
        const fileUrl = `${baseUrl}/uploads/${relativePath}`;
        
        // Determine media type folder (images, videos, audio)
        const mimeTypeStr = mimeType || req.file.mimetype || '';
        let mediaTypeFolder = 'other';
        if (mimeTypeStr.startsWith('image/')) {
            mediaTypeFolder = 'images';
        } else if (mimeTypeStr.startsWith('video/')) {
            mediaTypeFolder = 'videos';
        } else if (mimeTypeStr.startsWith('audio/')) {
            mediaTypeFolder = 'audio';
        }
        
        // Move file to organized folder structure: uploads/{mediaType}/{appPackage}/{date}/{filename}
        const organizedDest = path.join(__dirname, '..', 'uploads', mediaTypeFolder, appPackage, dateDir);
        if (!fs.existsSync(organizedDest)) {
            fs.mkdirSync(organizedDest, { recursive: true });
        }
        
        const organizedPath = path.join(organizedDest, req.file.filename);
        fs.renameSync(req.file.path, organizedPath);
        
        const organizedFileUrl = `${baseUrl}/uploads/${mediaTypeFolder}/${appPackage}/${dateDir}/${req.file.filename}`;
        
        // Save to database
        const deviceId = req.body.deviceId || null;
        const db = getDb();
        const mediaId = crypto.randomUUID();
        
        const mediaDoc = {
            id: mediaId,
            deviceId: deviceId,
            notificationId: notificationId,
            localPath: organizedPath,
            remoteUrl: organizedFileUrl,
            fileSize: req.file.size,
            mimeType: mimeTypeStr,
            checksum: calculatedChecksum || checksum || '',
            uploadStatus: 'SUCCESS',
            uploadAttempts: 0,
            lastUploadAttempt: null,
            errorMessage: null,
            createdAt: Math.floor(Date.now() / 1000)
        };
        
        await db.collection('media_files').insertOne(mediaDoc);
        
        // Broadcast WebSocket update
        if (deviceId) {
            websocketService.broadcastDataUpdate(deviceId, 'media', {
                id: mediaId,
                notificationId: notificationId,
            fileUrl: organizedFileUrl,
                mimeType: mimeTypeStr
            });
        }
        
        res.json({
            success: true,
            message: 'Media uploaded successfully',
            fileUrl: organizedFileUrl,
            checksum: calculatedChecksum || checksum,
            mediaType: mediaTypeFolder,
            fileSize: req.file.size
        });
    } catch (error) {
        console.error('Error uploading media:', error);
        if (req.file && fs.existsSync(req.file.path)) {
            fs.unlinkSync(req.file.path);
        }
        res.status(500).json({
            success: false,
            message: 'Error uploading media'
        });
    }
});

// GET /api/media - Get media files (with optional filters and authorization)
router.get('/', authenticate, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const { deviceId, limit = 100, uploadStatus } = req.query;
        
        const db = getDb();
        const filter = {};
        
        // Device owners can only see media from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        } else if (role === 'admin') {
            // Admin can filter by deviceId if provided, otherwise see all
            if (deviceId) {
                filter.deviceId = deviceId;
            }
        } else {
            // Non-admin, non-device-owner users cannot access media
            return res.status(403).json({
                success: false,
                message: 'Access denied'
            });
        }
        
        // Filter by upload status if provided
        if (uploadStatus) {
            filter.uploadStatus = uploadStatus;
        }
        
        const mediaFiles = await db.collection('media_files')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(parseInt(limit))
            .toArray();
        
        res.json({
            success: true,
            mediaFiles: mediaFiles,
            count: mediaFiles.length
        });
    } catch (error) {
        console.error('Error fetching media files:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching media files'
        });
    }
});

// Serve uploaded files
router.use('/files', express.static(path.join(__dirname, '..', 'uploads')));

module.exports = router;
