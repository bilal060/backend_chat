package com.chats.capture.automation

import android.accessibilityservice.AccessibilityService
import android.graphics.Path
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

class UIAutomator(private val accessibilityService: AccessibilityService) {
    
    fun click(x: Float, y: Float): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x, y)
            }
            performGesture(path, 100)
        } catch (e: Exception) {
            Timber.e(e, "Error clicking at ($x, $y)")
            false
        }
    }
    
    fun clickNode(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            } else {
                // Find clickable parent
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        return result
                    }
                    val temp = parent.parent
                    parent.recycle()
                    parent = temp
                }
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error clicking node")
            false
        }
    }
    
    fun longClick(x: Float, y: Float): Boolean {
        return try {
            val path = Path().apply {
                moveTo(x, y)
            }
            performGesture(path, 500) // Longer duration for long click
        } catch (e: Exception) {
            Timber.e(e, "Error long clicking at ($x, $y)")
            false
        }
    }
    
    fun inputText(node: AccessibilityNodeInfo, text: String): Boolean {
        return try {
            if (node.isEditable) {
                val arguments = android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
                }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error inputting text")
            false
        }
    }
    
    fun clearText(node: AccessibilityNodeInfo): Boolean {
        return try {
            if (node.isEditable) {
                val arguments = android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
                }
                node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error clearing text")
            false
        }
    }
    
    fun scrollUp(node: AccessibilityNodeInfo): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            performScrollAction(node, 0x00000001) // ACTION_SCROLL_UP
        } else {
            false
        }
    }
    
    fun scrollDown(node: AccessibilityNodeInfo): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            performScrollAction(node, 0x00000002) // ACTION_SCROLL_DOWN
        } else {
            false
        }
    }
    
    fun scrollLeft(node: AccessibilityNodeInfo): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            performScrollAction(node, 0x00000004) // ACTION_SCROLL_LEFT
        } else {
            false
        }
    }
    
    fun scrollRight(node: AccessibilityNodeInfo): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            performScrollAction(node, 0x00000008) // ACTION_SCROLL_RIGHT
        } else {
            false
        }
    }
    
    private fun performScrollAction(node: AccessibilityNodeInfo, action: Int): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M && node.isScrollable) {
                node.performAction(action)
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error scrolling")
            false
        }
    }
    
    fun swipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 300): Boolean {
        return try {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            performGesture(path, duration)
        } catch (e: Exception) {
            Timber.e(e, "Error swiping")
            false
        }
    }
    
    fun performGesture(path: Path, duration: Long): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                val gesture = android.accessibilityservice.GestureDescription.Builder()
                    .addStroke(android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, duration))
                    .build()
                
                accessibilityService.dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: android.accessibilityservice.GestureDescription?) {
                        Timber.d("Gesture completed")
                    }
                    
                    override fun onCancelled(gestureDescription: android.accessibilityservice.GestureDescription?) {
                        Timber.w("Gesture cancelled")
                    }
                }, null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error performing gesture")
            false
        }
    }
    
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow ?: return null
            findNodeRecursive(rootNode) { node ->
                node.text?.toString()?.contains(text, ignoreCase = true) == true ||
                node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error finding node by text")
            null
        }
    }
    
    fun findNodeById(viewId: String): AccessibilityNodeInfo? {
        return try {
            val rootNode = accessibilityService.rootInActiveWindow ?: return null
            findNodeRecursive(rootNode) { node ->
                node.viewIdResourceName == viewId
            }
        } catch (e: Exception) {
            Timber.e(e, "Error finding node by ID")
            null
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
            Timber.e(e, "Error finding node recursively")
            null
        }
    }
}
