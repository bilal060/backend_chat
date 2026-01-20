const { getDb } = require('../database/mongodb');
const fileLogger = require('../utils/logger');

/**
 * Middleware to log all request payloads from devices to:
 * 1. MongoDB logs collection
 * 2. .log files (one per endpoint type per day)
 * Only logs POST/PUT requests that come from devices (not admin endpoints)
 */
function requestLogger(req, res, next) {
    // Only log POST and PUT requests
    if (req.method !== 'POST' && req.method !== 'PUT') {
        return next();
    }

    // Skip logging for certain endpoints (auth, admin operations, etc.)
    const skipPaths = [
        '/api/auth',
        '/api/devices/:deviceId/assign-owner',
        '/health'
    ];

    // Special handling for /api/devices/:deviceId (PUT only, exact match)
    // We want to skip PUT /api/devices/:deviceId but log POST /api/devices/register and POST /api/devices/:deviceId/heartbeat
    if (req.method === 'PUT' && /^\/api\/devices\/[^\/]+$/.test(req.path)) {
        return next();
    }

    const shouldSkip = skipPaths.some(path => {
        const pattern = path.replace(/:[^/]+/g, '[^/]+');
        const regex = new RegExp(`^${pattern}$`); // Use $ to match end of string
        return regex.test(req.path);
    });

    if (shouldSkip) {
        return next();
    }

    // Log asynchronously to not block the request
    setImmediate(async () => {
        try {
            const db = getDb();
            const deviceId = req.body?.deviceId || req.params?.deviceId || null;
            
            // For file uploads (multipart/form-data), req.body may be limited
            // Note: req.file is not available here as multer processes after middleware
            // File metadata would need to be logged separately in route handlers if needed
            const logEntry = {
                timestamp: Date.now(),
                method: req.method,
                path: req.path,
                url: req.originalUrl || req.url,
                deviceId: deviceId,
                headers: {
                    'content-type': req.headers['content-type'],
                    'user-agent': req.headers['user-agent'],
                    'content-length': req.headers['content-length']
                },
                body: req.body || {}, // May be limited for multipart/form-data
                query: req.query,
                params: req.params,
                ip: req.ip || req.connection.remoteAddress,
                createdAt: Math.floor(Date.now() / 1000)
            };

            // Log to MongoDB logs collection
            await db.collection('logs').insertOne(logEntry);

            // Log to .log file based on endpoint type
            // Extract endpoint name from path (e.g., /api/chats -> chats, /api/chats/batch -> chats)
            const endpointMatch = req.path.match(/\/api\/([^\/]+)/);
            if (endpointMatch) {
                const endpointName = endpointMatch[1];
                // For batch endpoints like /api/chats/batch, endpointName is already "chats"
                // For regular endpoints like /api/chats, endpointName is "chats"
                // So we can use endpointName directly
                const logFileName = endpointName;
                
                // Log the full request payload to .log file
                fileLogger.log(`device-requests-${logFileName}`, logEntry);
            } else {
                // Fallback: log to generic device-requests log
                fileLogger.log('device-requests', logEntry);
            }
        } catch (error) {
            // Don't throw - logging should not break the application
            console.error('Error logging request:', error);
        }
    });

    next();
}

module.exports = requestLogger;
