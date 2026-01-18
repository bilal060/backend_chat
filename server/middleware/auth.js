const jwt = require('jsonwebtoken');
const config = require('../config/jwt');
const db = require('../database/init');

/**
 * JWT Authentication Middleware
 * Verifies JWT token and attaches user info to req.user
 */
const authenticate = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            return res.status(401).json({
                success: false,
                message: 'No token provided'
            });
        }

        const token = authHeader.substring(7); // Remove 'Bearer ' prefix

        // Verify token
        const decoded = jwt.verify(token, config.secret);

        // Get user from database
        db.get('SELECT * FROM users WHERE id = ?', [decoded.userId], (err, user) => {
            if (err) {
                console.error('Error fetching user:', err);
                return res.status(500).json({
                    success: false,
                    message: 'Error authenticating user'
                });
            }

            if (!user) {
                return res.status(401).json({
                    success: false,
                    message: 'User not found'
                });
            }

            // Get assigned deviceId for device owners
            if (user.role === 'device_owner') {
                db.get('SELECT deviceId FROM device_ownership WHERE userId = ?', [user.id], (err, ownership) => {
                    if (err) {
                        console.error('Error fetching device ownership:', err);
                        return res.status(500).json({
                            success: false,
                            message: 'Error authenticating user'
                        });
                    }

                    req.user = {
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        role: user.role,
                        deviceId: ownership ? ownership.deviceId : null
                    };

                    next();
                });
            } else {
                req.user = {
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    role: user.role
                };

                next();
            }
        });
    } catch (error) {
        if (error.name === 'JsonWebTokenError') {
            return res.status(401).json({
                success: false,
                message: 'Invalid token'
            });
        }

        if (error.name === 'TokenExpiredError') {
            return res.status(401).json({
                success: false,
                message: 'Token expired'
            });
        }

        console.error('Auth error:', error);
        return res.status(500).json({
            success: false,
            message: 'Error authenticating'
        });
    }
};

/**
 * Optional authentication - doesn't fail if no token
 */
const optionalAuth = (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            req.user = null;
            return next();
        }

        const token = authHeader.substring(7);
        const decoded = jwt.verify(token, config.secret);

        db.get('SELECT * FROM users WHERE id = ?', [decoded.userId], (err, user) => {
            if (err || !user) {
                req.user = null;
                return next();
            }

            if (user.role === 'device_owner') {
                db.get('SELECT deviceId FROM device_ownership WHERE userId = ?', [user.id], (err, ownership) => {
                    req.user = {
                        id: user.id,
                        username: user.username,
                        email: user.email,
                        role: user.role,
                        deviceId: ownership ? ownership.deviceId : null
                    };
                    next();
                });
            } else {
                req.user = {
                    id: user.id,
                    username: user.username,
                    email: user.email,
                    role: user.role
                };
                next();
            }
        });
    } catch (error) {
        req.user = null;
        next();
    }
};

module.exports = {
    authenticate,
    optionalAuth
};
