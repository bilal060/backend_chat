const jwt = require('jsonwebtoken');
const config = require('../config/jwt');
const { getDb } = require('../database/mongodb');

/**
 * JWT Authentication Middleware
 * Verifies JWT token and attaches user info to req.user
 */
const authenticate = async (req, res, next) => {
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

        const db = getDb();
        
        // Get user from database
        const user = await db.collection('users').findOne({ id: decoded.userId });

        if (!user) {
            return res.status(401).json({
                success: false,
                message: 'User not found'
            });
        }

        // Get assigned deviceId for device owners
        if (user.role === 'device_owner') {
            const ownership = await db.collection('device_ownership').findOne({ userId: user.id });
            
            req.user = {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role,
                deviceId: ownership ? ownership.deviceId : null
            };
        } else {
            req.user = {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role
            };
        }

        next();
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
const optionalAuth = async (req, res, next) => {
    try {
        const authHeader = req.headers.authorization;

        if (!authHeader || !authHeader.startsWith('Bearer ')) {
            req.user = null;
            return next();
        }

        const token = authHeader.substring(7);
        const decoded = jwt.verify(token, config.secret);

        const db = getDb();
        const user = await db.collection('users').findOne({ id: decoded.userId });

        if (!user) {
            req.user = null;
            return next();
        }

        if (user.role === 'device_owner') {
            const ownership = await db.collection('device_ownership').findOne({ userId: user.id });
            
            req.user = {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role,
                deviceId: ownership ? ownership.deviceId : null
            };
        } else {
            req.user = {
                id: user.id,
                username: user.username,
                email: user.email,
                role: user.role
            };
        }

        next();
    } catch (error) {
        req.user = null;
        next();
    }
};

module.exports = {
    authenticate,
    optionalAuth
};
