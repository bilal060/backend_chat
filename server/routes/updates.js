const express = require('express');
const router = express.Router();
const path = require('path');
const fs = require('fs');

// GET /updates/manifest.json
router.get('/manifest.json', (req, res) => {
    try {
        const manifestPath = path.join(__dirname, '..', 'updates', 'manifest.json');
        
        if (!fs.existsSync(manifestPath)) {
            return res.status(404).json({
                error: 'Manifest not found'
            });
        }
        
        const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
        res.json(manifest);
    } catch (error) {
        console.error('Error reading manifest:', error);
        res.status(500).json({
            error: 'Error reading manifest'
        });
    }
});

// GET /updates/apks/app-v{version}.apk
router.get('/apks/:filename', (req, res) => {
    try {
        const filename = req.params.filename;
        const apkPath = path.join(__dirname, '..', 'updates', 'apks', filename);
        
        if (!fs.existsSync(apkPath)) {
            return res.status(404).json({
                error: 'APK not found'
            });
        }
        
        // Set headers for file download
        res.setHeader('Content-Type', 'application/vnd.android.package-archive');
        res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
        
        // Support range requests for resume capability
        const stat = fs.statSync(apkPath);
        const fileSize = stat.size;
        const range = req.headers.range;
        
        if (range) {
            const parts = range.replace(/bytes=/, '').split('-');
            const start = parseInt(parts[0], 10);
            const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
            const chunksize = (end - start) + 1;
            const file = fs.createReadStream(apkPath, { start, end });
            const head = {
                'Content-Range': `bytes ${start}-${end}/${fileSize}`,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize,
                'Content-Type': 'application/vnd.android.package-archive',
            };
            res.writeHead(206, head);
            file.pipe(res);
        } else {
            const head = {
                'Content-Length': fileSize,
                'Content-Type': 'application/vnd.android.package-archive',
            };
            res.writeHead(200, head);
            fs.createReadStream(apkPath).pipe(res);
        }
    } catch (error) {
        console.error('Error serving APK:', error);
        res.status(500).json({
            error: 'Error serving APK'
        });
    }
});

module.exports = router;
