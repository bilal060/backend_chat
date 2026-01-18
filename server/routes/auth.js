const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { getDb } = require('../database/mongodb');
const config = require('../config/jwt');
const { v4: uuidv4 } = require('uuid');
const { authenticate, optionalAuth } = require('../middleware/auth');
const { requireAdmin } = require('../middleware/authorization');

/**
 * Validate 6-digit alphanumeric string
 */
const isValidAlphanumeric = (str) => {
    return /^[A-Za-z0-9]{6}$/.test(str);
};

/**
 * POST /api/auth/login - Login (supports both admin email/password and device owner 6-digit credentials)
 */
router.post('/login', async (req, res) => {
    try {
        const { email, username, password, loginType } = req.body;

        if (!password) {
            return res.status(400).json({
                success: false,
                message: 'Password is required'
            });
        }

        // Determine login type
        const isAdminLogin = loginType === 'admin' || email;
        const isDeviceOwnerLogin = loginType === 'device_owner' || username;

        const db = getDb();

        if (isAdminLogin) {
            // Admin login with email
            if (!email) {
                return res.status(400).json({
                    success: false,
                    message: 'Email is required for admin login'
                });
            }

            const user = await db.collection('users').findOne({ email, role: 'admin' });

            if (!user) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }

            // Verify password
            const match = await bcrypt.compare(password, user.password);

            if (!match) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }

            // Update last login
            await db.collection('users').updateOne(
                { id: user.id },
                { $set: { lastLogin: Date.now() } }
            );

            // Generate JWT token
            const token = jwt.sign(
                { userId: user.id, role: user.role },
                config.secret,
                { expiresIn: config.expiresIn }
            );

            res.json({
                success: true,
                message: 'Login successful',
                token: token,
                user: {
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    role: user.role
                }
            });
        } else if (isDeviceOwnerLogin) {
            // Device owner login with 6-digit alphanumeric username
            if (!username) {
                return res.status(400).json({
                    success: false,
                    message: 'Username is required for device owner login'
                });
            }

            // Validate 6-digit alphanumeric format
            if (!isValidAlphanumeric(username)) {
                return res.status(400).json({
                    success: false,
                    message: 'Username must be 6 alphanumeric characters'
                });
            }

            if (!isValidAlphanumeric(password)) {
                return res.status(400).json({
                    success: false,
                    message: 'Password must be 6 alphanumeric characters'
                });
            }

            const user = await db.collection('users').findOne({ 
                username: username.toUpperCase(), 
                role: 'device_owner' 
            });

            if (!user) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }

            // Verify password
            const match = await bcrypt.compare(password, user.password);

            if (!match) {
                return res.status(401).json({
                    success: false,
                    message: 'Invalid credentials'
                });
            }

            // Get assigned deviceId
            const ownership = await db.collection('device_ownership').findOne({ userId: user.id });

            // Update last login
            await db.collection('users').updateOne(
                { id: user.id },
                { $set: { lastLogin: Date.now() } }
            );

            // Generate JWT token
            const token = jwt.sign(
                { userId: user.id, role: user.role, deviceId: ownership ? ownership.deviceId : null },
                config.secret,
                { expiresIn: config.expiresIn }
            );

            res.json({
                success: true,
                message: 'Login successful',
                token: token,
                user: {
                    id: user.id,
                    username: user.username,
                    role: user.role,
                    deviceId: ownership ? ownership.deviceId : null
                }
            });
        } else {
            return res.status(400).json({
                success: false,
                message: 'Invalid login type. Provide email for admin or username for device owner'
            });
        }
    } catch (error) {
        console.error('Login error:', error);
        res.status(500).json({
            success: false,
            message: 'Error during login'
        });
    }
});

/**
 * POST /api/auth/refresh - Refresh JWT token
 */
router.post('/refresh', authenticate, (req, res) => {
    try {
        const user = req.user;

        // Generate new token
        const token = jwt.sign(
            { userId: user.id, role: user.role, deviceId: user.deviceId },
            config.secret,
            { expiresIn: config.expiresIn }
        );

        res.json({
            success: true,
            token: token
        });
    } catch (error) {
        console.error('Refresh error:', error);
        res.status(500).json({
            success: false,
            message: 'Error refreshing token'
        });
    }
});

/**
 * GET /api/auth/me - Get current user info
 */
router.get('/me', authenticate, (req, res) => {
    try {
        const user = req.user;

        res.json({
            success: true,
            user: {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role,
                deviceId: user.deviceId || null
            }
        });
    } catch (error) {
        console.error('Get user error:', error);
        res.status(500).json({
            success: false,
            message: 'Error fetching user info'
        });
    }
});

/**
 * POST /api/auth/device-owner/register - Register new device owner (admin only)
 */
router.post('/device-owner/register', authenticate, requireAdmin, async (req, res) => {
    try {
        const { username, password } = req.body;

        if (!username || !password) {
            return res.status(400).json({
                success: false,
                message: 'Username and password are required'
            });
        }

        // Validate 6-digit alphanumeric format
        if (!isValidAlphanumeric(username)) {
            return res.status(400).json({
                success: false,
                message: 'Username must be exactly 6 alphanumeric characters'
            });
        }

        if (!isValidAlphanumeric(password)) {
            return res.status(400).json({
                success: false,
                message: 'Password must be exactly 6 alphanumeric characters'
            });
        }

        const db = getDb();

        // Check if username already exists
        const existing = await db.collection('users').findOne({ username: username.toUpperCase() });

        if (existing) {
            return res.status(400).json({
                success: false,
                message: 'Username already exists'
            });
        }

        // Hash password
        const hash = await bcrypt.hash(password, 10);

        // Create user
        const id = uuidv4();
        const userDoc = {
            id: id,
            username: username.toUpperCase(),
            password: hash,
            role: 'device_owner',
            email: null,
            createdAt: Math.floor(Date.now() / 1000),
            lastLogin: null
        };

        await db.collection('users').insertOne(userDoc);

        res.json({
            success: true,
            message: 'Device owner registered',
            user: {
                id: id,
                username: username.toUpperCase(),
                role: 'device_owner'
            }
        });
    } catch (error) {
        console.error('Register device owner error:', error);
        res.status(500).json({
            success: false,
            message: 'Error registering device owner'
        });
    }
});

/**
 * POST /api/auth/device-owner/assign-device - Assign device to device owner (admin only)
 */
router.post('/device-owner/assign-device', authenticate, requireAdmin, async (req, res) => {
    try {
        const { userId, deviceId } = req.body;

        if (!userId || !deviceId) {
            return res.status(400).json({
                success: false,
                message: 'userId and deviceId are required'
            });
        }

        const db = getDb();

        // Check if user exists and is device owner
        const user = await db.collection('users').findOne({ id: userId, role: 'device_owner' });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: 'Device owner not found'
            });
        }

        // Check if device exists
        const device = await db.collection('devices').findOne({ deviceId });

        if (!device) {
            return res.status(404).json({
                success: false,
                message: 'Device not found'
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
            message: 'Device assigned to device owner'
        });
    } catch (error) {
        console.error('Assign device error:', error);
        res.status(500).json({
            success: false,
            message: 'Error assigning device'
        });
    }
});

module.exports = router;
