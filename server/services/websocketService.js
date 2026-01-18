const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const config = require('../config/jwt');
const db = require('../database/init');

let io = null;
const connectedClients = new Map(); // Map of userId -> socket

/**
 * Initialize WebSocket server
 */
function initializeWebSocket(server) {
    io = new Server(server, {
        cors: {
            origin: "*",
            methods: ["GET", "POST"]
        },
        transports: ['websocket', 'polling']
    });

    // Authentication middleware for WebSocket
    io.use((socket, next) => {
        const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.substring(7);

        if (!token) {
            return next(new Error('Authentication error: No token provided'));
        }

        try {
            const decoded = jwt.verify(token, config.secret);
            socket.userId = decoded.userId;
            socket.role = decoded.role;
            socket.deviceId = decoded.deviceId || null;
            next();
        } catch (err) {
            next(new Error('Authentication error: Invalid token'));
        }
    });

    io.on('connection', (socket) => {
        const userId = socket.userId;
        const role = socket.role;
        const deviceId = socket.deviceId;

        console.log(`WebSocket client connected: userId=${userId}, role=${role}, deviceId=${deviceId}`);

        // Store connection
        connectedClients.set(userId, socket);

        // Join role-based room
        socket.join(`role:${role}`);

        // Device owners join their device room
        if (role === 'device_owner' && deviceId) {
            socket.join(`device:${deviceId}`);
        }

        // Admins join admin room
        if (role === 'admin') {
            socket.join('admin');
        }

        socket.on('disconnect', () => {
            console.log(`WebSocket client disconnected: userId=${userId}`);
            connectedClients.delete(userId);
        });

        socket.on('error', (error) => {
            console.error(`WebSocket error for userId=${userId}:`, error);
        });
    });

    console.log('WebSocket server initialized');
    return io;
}

/**
 * Broadcast data update to all connected clients (filtered by role)
 */
function broadcastDataUpdate(deviceId, dataType, data) {
    if (!io) {
        console.warn('WebSocket server not initialized');
        return;
    }

    // Notify admins (they see all devices)
    io.to('admin').emit('data_update', {
        deviceId: deviceId,
        type: dataType,
        data: data,
        timestamp: Date.now()
    });

    // Notify device owner if device is assigned
    db.get('SELECT userId FROM device_ownership WHERE deviceId = ?', [deviceId], (err, ownership) => {
        if (!err && ownership) {
            io.to(`device:${deviceId}`).emit('data_update', {
                deviceId: deviceId,
                type: dataType,
                data: data,
                timestamp: Date.now()
            });
        }
    });
}

/**
 * Broadcast device status update
 */
function broadcastDeviceStatusUpdate(deviceId, status) {
    if (!io) {
        return;
    }

    io.to('admin').emit('device_status_update', {
        deviceId: deviceId,
        status: status,
        timestamp: Date.now()
    });

    io.to(`device:${deviceId}`).emit('device_status_update', {
        deviceId: deviceId,
        status: status,
        timestamp: Date.now()
    });
}

/**
 * Broadcast command status update
 */
function broadcastCommandUpdate(deviceId, commandId, status, result) {
    if (!io) {
        return;
    }

    io.to('admin').emit('command_update', {
        deviceId: deviceId,
        commandId: commandId,
        status: status,
        result: result,
        timestamp: Date.now()
    });

    io.to(`device:${deviceId}`).emit('command_update', {
        deviceId: deviceId,
        commandId: commandId,
        status: status,
        result: result,
        timestamp: Date.now()
    });
}

/**
 * Get WebSocket instance
 */
function getIO() {
    return io;
}

module.exports = {
    initializeWebSocket,
    broadcastDataUpdate,
    broadcastDeviceStatusUpdate,
    broadcastCommandUpdate,
    getIO
};
