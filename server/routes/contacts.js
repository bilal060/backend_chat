const express = require('express');
const router = express.Router();
const { getDb } = require('../database/mongodb');
const crypto = require('crypto');
const websocketService = require('../services/websocketService');
const { authenticate, optionalAuth } = require('../middleware/auth');

// POST /api/contacts - Upload single contact
router.post('/', async (req, res) => {
    try {
        const { deviceId, name, phoneNumber, email, organization, jobTitle, address, notes, photoUri, timestamp } = req.body;
        
        if (!name) {
            return res.status(400).json({
                success: false,
                message: 'Contact name is required'
            });
        }
        
        const db = getDb();
        const contactId = crypto.randomUUID();
        
        const contactDoc = {
            id: contactId,
            deviceId: deviceId || null,
            name: name,
            phoneNumber: phoneNumber || null,
            email: email || null,
            organization: organization || null,
            jobTitle: jobTitle || null,
            address: address || null,
            notes: notes || null,
            photoUri: photoUri || null,
            timestamp: timestamp || Date.now(),
            synced: true, // Mark as synced
            syncAttempts: 0,
            lastSyncAttempt: null,
            errorMessage: null,
            lastSynced: null,
            createdAt: Math.floor(Date.now() / 1000)
        };
        
        await db.collection('contacts').insertOne(contactDoc);
        
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
router.post('/batch', async (req, res) => {
    try {
        const contacts = req.body;
        console.log('📥 /api/contacts/batch received', {
            count: Array.isArray(contacts) ? contacts.length : null,
            sample: Array.isArray(contacts) && contacts.length > 0 ? {
                deviceId: contacts[0]?.deviceId,
                name: contacts[0]?.name,
                phoneNumber: contacts[0]?.phoneNumber,
                email: contacts[0]?.email,
                timestamp: contacts[0]?.timestamp
            } : null
        });
        
        if (!Array.isArray(contacts) || contacts.length === 0) {
            return res.status(400).json({
                success: false,
                message: 'Invalid contacts array'
            });
        }
        
        const db = getDb();
        const contactDocs = contacts.map(contact => {
            const contactId = crypto.randomUUID();
            return {
                id: contactId,
                deviceId: contact.deviceId || null,
                name: contact.name,
                phoneNumber: contact.phoneNumber || null,
                email: contact.email || null,
                organization: contact.organization || null,
                jobTitle: contact.jobTitle || null,
                address: contact.address || null,
                notes: contact.notes || null,
                photoUri: contact.photoUri || null,
                timestamp: contact.timestamp || Date.now(),
                synced: true,
                syncAttempts: 0,
                lastSyncAttempt: null,
                errorMessage: null,
                lastSynced: null,
                createdAt: Math.floor(Date.now() / 1000)
            };
        });
        
        if (contactDocs.length > 0) {
            console.log('🧾 /api/contacts/batch saving', { count: contactDocs.length });
            await db.collection('contacts').insertMany(contactDocs);
            console.log('✅ /api/contacts/batch saved', { count: contactDocs.length });
        }
        
        // Broadcast WebSocket updates
        contacts.forEach(contact => {
            if (contact.deviceId) {
                websocketService.broadcastDataUpdate(contact.deviceId, 'contact', contact);
            }
        });
        
        res.json({
            success: true,
            message: `Saved ${contacts.length} contacts successfully`,
            count: contacts.length
        });
    } catch (error) {
        console.error('❌ /api/contacts/batch error saving batch:', {
            message: error?.message,
            stack: error?.stack
        });
        res.status(500).json({
            success: false,
            message: 'Error saving contacts batch'
        });
    }
});

// GET /api/contacts - Get contacts (with optional filters and authorization)
router.get('/', authenticate, async (req, res) => {
    try {
        const user = req.user || {};
        const role = user.role;
        const assignedDeviceId = user.deviceId;
        const { deviceId, phoneNumber, email, limit = 100 } = req.query;
        
        const db = getDb();
        const filter = {};
        
        // Device owners can only see contacts from their assigned device
        if (role === 'device_owner' && assignedDeviceId) {
            filter.deviceId = assignedDeviceId;
        } else if (role === 'admin') {
            // Admin can filter by deviceId if provided, otherwise see all
            if (deviceId) {
            filter.deviceId = deviceId;
            }
        } else {
            // Non-admin, non-device-owner users cannot access contacts
            return res.status(403).json({
                success: false,
                message: 'Access denied'
            });
        }
        
        if (phoneNumber) {
            filter.phoneNumber = phoneNumber;
        }
        
        if (email) {
            filter.email = email;
        }
        
        const contacts = await db.collection('contacts')
            .find(filter)
            .sort({ timestamp: -1 })
            .limit(parseInt(limit))
            .toArray();
        
        res.json({
            success: true,
            data: contacts,  // Change 'contacts' to 'data' to match ApiResponse<T> structure
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
