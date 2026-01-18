# Chat Capture Server

Backend server for the Chat Capture Android application.

## Installation

```bash
npm install
```

## Configuration

1. Set your server URL in the Android app settings
2. Update `updates/manifest.json` with your domain and version info
3. Place APK files in `updates/apks/` directory

## Running

```bash
# Development
npm run dev

# Production
npm start
```

## API Endpoints

- `POST /api/notifications` - Upload single notification
- `POST /api/notifications/batch` - Upload batch of notifications
- `GET /api/notifications` - Get notifications (paginated)
- `POST /api/chats` - Upload single chat
- `POST /api/chats/batch` - Upload batch of chats
- `GET /api/chats` - Get chats (paginated)
- `POST /api/media/upload` - Upload media file
- `GET /updates/manifest.json` - Get update manifest
- `GET /updates/apks/:filename` - Download APK file

## Database

SQLite database is automatically created at `database/capture.db`

## File Storage

Uploaded media files are stored in `uploads/notifications/` directory organized by app package and date.
