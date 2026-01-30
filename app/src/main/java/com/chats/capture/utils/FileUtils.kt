package com.chats.capture.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object FileUtils {
    
    private const val MAX_FILE_SIZE_FOR_IMMEDIATE_UPLOAD = 10 * 1024 * 1024L // 10MB
    
    /**
     * Get file path from URI (handles various URI schemes)
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        return try {
            when {
                // File URI
                uri.scheme == "file" -> uri.path
                
                // Content URI
                uri.scheme == "content" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        getPathFromContentUri(context, uri)
                    } else {
                        getPathFromContentUriLegacy(context, uri)
                    }
                }
                
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting path from URI: $uri")
            null
        }
    }
    
    /**
     * Get path from content URI (Android 10+)
     */
    private fun getPathFromContentUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        var cursor: Cursor? = null
        
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                
                when {
                    // External storage
                    "com.android.externalstorage.documents" == uri.authority -> {
                        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            filePath = "${Environment.getExternalStorageDirectory()}/${split[1]}"
                        }
                    }
                    // Downloads
                    "com.android.providers.downloads.documents" == uri.authority -> {
                        if (docId.startsWith("raw:")) {
                            filePath = docId.substring(4)
                        } else {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                docId.toLong()
                            )
                            filePath = getDataColumn(context, contentUri, null, null)
                        }
                    }
                    // Media
                    "com.android.providers.media.documents" == uri.authority -> {
                        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        val type = split[0]
                        val contentUri = when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }
                        if (contentUri != null) {
                            val selection = "_id=?"
                            val selectionArgs = arrayOf(split[1])
                            filePath = getDataColumn(context, contentUri, selection, selectionArgs)
                        }
                    }
                }
            } else {
                // Standard content URI
                filePath = getDataColumn(context, uri, null, null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting path from content URI")
        } finally {
            cursor?.close()
        }
        
        return filePath
    }
    
    /**
     * Get path from content URI (Android 9 and below)
     */
    private fun getPathFromContentUriLegacy(context: Context, uri: Uri): String? {
        var filePath: String? = null
        var cursor: Cursor? = null
        
        try {
            cursor = context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting path from content URI (legacy)")
        } finally {
            cursor?.close()
        }
        
        return filePath
    }
    
    /**
     * Get data column from content URI
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        
        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(column)
                    return it.getString(columnIndex)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting data column")
        } finally {
            cursor?.close()
        }
        
        return null
    }
    
    /**
     * Calculate SHA-256 checksum of file
     */
    fun calculateChecksum(file: File): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            FileInputStream(file).use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating checksum")
            ""
        }
    }
    
    /**
     * Get MIME type from file extension
     */
    fun getMimeTypeFromFile(file: File): String {
        val extension = file.extension.lowercase()
        return when (extension) {
            // Images
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            // Videos
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "3gp" -> "video/3gpp"
            // Audio
            "mp3" -> "audio/mpeg"
            "ogg" -> "audio/ogg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            "flac" -> "audio/flac"
            // Documents
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            // Archives
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            // Text
            "txt" -> "text/plain"
            "json" -> "application/json"
            "xml" -> "application/xml"
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "text/javascript"
            // Default
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Check if file should be uploaded immediately (< 10MB)
     */
    fun shouldUploadImmediately(file: File): Boolean {
        return file.exists() && file.isFile && file.length() > 0 && file.length() <= MAX_FILE_SIZE_FOR_IMMEDIATE_UPLOAD
    }
}
