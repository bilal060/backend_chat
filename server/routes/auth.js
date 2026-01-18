const express = require('express');
const router = express.Router();
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const db = require('../database/init');
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
router.post('/login', (req, res) => {
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

        if (isAdminLogin) {
            // Admin login with email
            if (!email) {
                return res.status(400).json({
                    success: false,
                    message: 'Email is required for admin login'
                });
            }

            db.get('SELECT * FROM users WHERE email = ? AND role = ?', [email, 'admin'], (err, user) => {
                if (err) {
                    console.error('Error fetching admin:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error during login'
                    });
                }

                if (!user) {
                    return res.status(401).json({
                        success: false,
                        message: 'Invalid credentials'
                    });
                }

                // Verify password
                bcrypt.compare(password, user.password, (err, match) => {
                    if (err) {
                        console.error('Error comparing password:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Error during login'
                        });
                    }

                    if (!match) {
                        return res.status(401).json({
                            success: false,
                            message: 'Invalid credentials'
                        });
                    }

                    // Update last login
                    db.run('UPDATE users SET lastLogin = ? WHERE id = ?', [Date.now(), user.id]);

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
                });
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

            db.get('SELECT * FROM users WHERE username = ? AND role = ?', [username.toUpperCase(), 'device_owner'], (err, user) => {
                if (err) {
                    console.error('Error fetching device owner:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error during login'
                    });
                }

                if (!user) {
                    return res.status(401).json({
                        success: false,
                        message: 'Invalid credentials'
                    });
                }

                // Verify password
                bcrypt.compare(password, user.password, (err, match) => {
                    if (err) {
                        console.error('Error comparing password:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Error during login'
                        });
                    }

                    if (!match) {
                        return res.status(401).json({
                            success: false,
                            message: 'Invalid credentials'
                        });
                    }

                    // Get assigned deviceId
                    db.get('SELECT deviceId FROM device_ownership WHERE userId = ?', [user.id], (err, ownership) => {
                        if (err) {
                            console.error('Error fetching device ownership:', err);
                            return res.status(500).json({
                                success: false,
                                message: 'Error during login'
                            });
                        }

                        // Update last login
                        db.run('UPDATE users SET lastLogin = ? WHERE id = ?', [Date.now(), user.id]);

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
                    });
                });
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
router.post('/device-owner/register', authenticate, requireAdmin, (req, res) => {
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

        // Check if username already exists
        db.get('SELECT * FROM users WHERE username = ?', [username.toUpperCase()], (err, existing) => {
            if (err) {
                console.error('Error checking username:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error checking username'
                });
            }

            if (existing) {
                return res.status(400).json({
                    success: false,
                    message: 'Username already exists'
                });
            }

            // Hash password
            bcrypt.hash(password, 10, (err, hash) => {
                if (err) {
                    console.error('Error hashing password:', err);
                    return res.status(500).json({
                        success: false,
                        message: 'Error creating user'
                    });
                }

                // Create user
                const id = uuidv4();
                const stmt = db.prepare(`INSERT INTO users 
                    (id, username, password, role)
                    VALUES (?, ?, ?, ?)`);

                stmt.run(
                    id,
                    username.toUpperCase(),
                    hash,
                    'device_owner'
                );
                stmt.finalize();

                res.json({
                    success: true,
                    message: 'Device owner registered',
                    user: {
                        id: id,
                        username: username.toUpperCase(),
                        role: 'device_owner'
                    }
                });
            });
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
router.post('/device-owner/assign-device', authenticate, requireAdmin, (req, res) => {
    try {
        const { userId, deviceId } = req.body;

        if (!userId || !deviceId) {
            return res.status(400).json({
                success: false,
                message: 'userId and deviceId are required'
            });
        }

        // Check if user exists and is device owner
        db.get('SELECT * FROM users WHERE id = ? AND role = ?', [userId, 'device_owner'], (err, user) => {
            if (err) {
                console.error('Error checking user:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error checking user'
                });
            }

            if (!user) {
                return res.status(404).json({
                    success: false,
                    message: 'Device owner not found'
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
                        message: 'Device assigned to device owner'
                    });
                });
            });
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
