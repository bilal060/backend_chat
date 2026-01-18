const express = require('express');
const router = express.Router();
const db = require('../database/init');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/contacts - Upload single contact
router.post('/', (req, res) => {
    try {
        const { deviceId, name, phoneNumber, email, organization, jobTitle, address, notes, photoUri, timestamp } = req.body;
        
        if (!name) {
            return res.status(400).json({
                success: false,
                message: 'Contact name is required'
            });
        }
        
        // Save to database
        const contactId = crypto.randomUUID();
        const stmt = db.prepare(`INSERT INTO contacts 
            (id, deviceId, name, phoneNumber, email, organization, jobTitle, address, notes, photoUri, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        stmt.run(
            contactId,
            deviceId || null,
            name,
            phoneNumber || null,
            email || null,
            organization || null,
            jobTitle || null,
            address || null,
            notes || null,
            photoUri || null,
            timestamp || Date.now(),
            1 // Mark as synced
        );
        stmt.finalize();
        
        // Broadcast WebSocket update
        if (deviceId) {
            websocketService.broadcastDataUpdate(deviceId, 'contact', req.body);
        }
        
        res.json({
            success: true,
            message: 'Contact saved successfully',
            id: contactId
        });
    } catch (error) {
        console.error('Error saving contact:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving contact'
        });
    }
});

// POST /api/contacts/batch - Upload multiple contacts
router.post('/batch', (req, res) => {
    try {
        const contacts = req.body;
        
        if (!Array.isArray(contacts) || contacts.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid contacts array'
            });
        }
        
        const stmt = db.prepare(`INSERT INTO contacts 
            (id, deviceId, name, phoneNumber, email, organization, jobTitle, address, notes, photoUri, timestamp, synced)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`);
        
        const transaction = db.transaction((contacts) => {
            for (const contact of contacts) {
                const contactId = crypto.randomUUID();
                stmt.run(
                    contactId,
                    contact.deviceId || null,
                    contact.name,
                    contact.phoneNumber || null,
                    contact.email || null,
                    contact.organization || null,
                    contact.jobTitle || null,
                    contact.address || null,
                    contact.notes || null,
                    contact.photoUri || null,
                    contact.timestamp || Date.now(),
                    1 // Mark as synced
                );
                
                // Broadcast WebSocket update
                if (contact.deviceId) {
                    websocketService.broadcastDataUpdate(contact.deviceId, 'contact', contact);
                }
            }
        });
        
        transaction(contacts);
        stmt.finalize();
        
        res.json({
            success: true,
            message: `Saved ${contacts.length} contacts successfully`,
            count: contacts.length
        });
    } catch (error) {
        console.error('Error saving contacts batch:', error);
        res.status(500).json({
            success: false,
            message: 'Error saving contacts batch'
        });
    }
});

// GET /api/contacts - Get contacts (with optional filters and authorization)
router.get('/', optionalAuth, (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const { deviceId, phoneNumber, email, limit = 100 } = req.query;
        
        let query = 'SELECT * FROM contacts WHERE 1=1';
        const params = [];
        
        // Device owners can only see contacts from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            query += ' AND deviceId = ?';
            params.push(assignedDeviceId);
        } else if (deviceId) {
            // Admin can filter by deviceId
            query += ' AND deviceId = ?';
            params.push(deviceId);
        }
        
        if (phoneNumber) {
            query += ' AND phoneNumber = ?';
            params.push(phoneNumber);
        }
        
        if (email) {
            query += ' AND email = ?';
            params.push(email);
        }
        
        query += ' ORDER BY timestamp DESC LIMIT ?';
        params.push(parseInt(limit));
        
        const stmt = db.prepare(query);
        const contacts = stmt.all(...params);
        stmt.finalize();
        
        res.json({
            success: true,
            contacts: contacts,
            count: contacts.length
        });
    } catch (error) {
        console.error('Error fetching contacts:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching contacts'
        });
    }
});

module.exports = router;
