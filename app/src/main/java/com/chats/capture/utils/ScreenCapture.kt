package com.chats.capture.utils

import android.view.accessibility.AccessibilityNodeInfo
import com.google.gson.Gson
import timber.log.Timber

class ScreenCapture(private val accessibilityService: android.accessibilityservice.AccessibilityService) {
    
    private val gson = Gson()
    
    fun captureScreenHierarchy(rootNode: AccessibilityNodeInfo): ScreenCaptureData {
        val elements = mutableListOf<UIElement>()
        traverseNode(rootNode, elements, 0)
        
        return ScreenCaptureData(
            timestamp = System.currentTimeMillis(),
            elementCount = elements.size,
            elements = elements
        )
    }
    
    private fun traverseNode(node: AccessibilityNodeInfo?, elements: MutableList<UIElement>, depth: Int) {
        if (node == null || depth > 20) return // Prevent infinite recursion
        
        try {
            val rect = android.graphics.Rect()
            node.getBoundsInScreen(rect)
            val element = UIElement(
                className = node.className?.toString() ?: "",
                text = node.text?.toString(),
                contentDescription = node.contentDescription?.toString(),
                viewId = node.viewIdResourceName,
                bounds = Rect(
                    left = rect.left,
                    top = rect.top,
                    right = rect.right,
                    bottom = rect.bottom
                ),
                clickable = node.isClickable,
                focusable = node.isFocusable,
                scrollable = node.isScrollable,
                editable = node.isEditable,
                checkable = node.isCheckable,
                checked = node.isChecked,
                enabled = node.isEnabled,
                selected = node.isSelected,
                depth = depth
            )
            
            elements.add(element)
            
            // Traverse children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                traverseNode(child, elements, depth + 1)
                child?.recycle()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error traversing node")
        }
    }
    
    fun extractAllText(rootNode: AccessibilityNodeInfo): List<String> {
        val texts = mutableListOf<String>()
        extractTextFromNode(rootNode, texts)
        return texts.filter { it.isNotBlank() }
    }
    
    private fun extractTextFromNode(node: AccessibilityNodeInfo?, texts: MutableList<String>) {
        if (node == null) return
        
        try {
            node.text?.toString()?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
            node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                extractTextFromNode(child, texts)
                child?.recycle()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error extracting text")
        }
    }
    
    fun findNodeByText(rootNode: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        return findNodeRecursive(rootNode) { node ->
            node.text?.toString()?.contains(text, ignoreCase = true) == true ||
            node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true
        }
    }
    
    fun findNodeById(rootNode: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        return findNodeRecursive(rootNode) { node ->
            node.viewIdResourceName == viewId
        }
    }
    
    private fun findNodeRecursive(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        
        return try {
            if (predicate(node)) {
                return node
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                val result = findNodeRecursive(child, predicate)
                if (result != null) {
                    return result
                }
                child?.recycle()
            }
            null
        } catch (e: Exception) {
            Timber.e(e, "Error finding node")
            null
        }
    }
    
    fun toJson(screenData: ScreenCaptureData): String {
        return gson.toJson(screenData)
    }
}

data class ScreenCaptureData(
    val timestamp: Long,
    val elementCount: Int,
    val elements: List<UIElement>
)

data class UIElement(
    val className: String,
    val text: String?,
    val contentDescription: String?,
    val viewId: String?,
    val bounds: Rect,
    val clickable: Boolean,
    val focusable: Boolean,
    val scrollable: Boolean,
    val editable: Boolean,
    val checkable: Boolean,
    val checked: Boolean,
    val enabled: Boolean,
    val selected: Boolean,
    val depth: Int
)

data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)
