package com.chats.capture.utils;

/**
 * Extracts media files from chat apps via accessibility service
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\b\u0010\b\u001a\u0004\u0018\u00010\tJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u0007H\u0002J$\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\t2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u000e0\u0011H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/chats/capture/utils/ChatMediaExtractor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "extractMediaFromNode", "", "", "rootNode", "Landroid/view/accessibility/AccessibilityNodeInfo;", "isMediaPath", "", "path", "traverseNode", "", "node", "action", "Lkotlin/Function1;", "app_debug"})
public final class ChatMediaExtractor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    public ChatMediaExtractor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Extract media files from accessibility node (chat message view)
     */
    @org.jetbrains.annotations.Nullable()
    public final java.util.List<java.lang.String> extractMediaFromNode(@org.jetbrains.annotations.Nullable()
    android.view.accessibility.AccessibilityNodeInfo rootNode) {
        return null;
    }
    
    /**
     * Traverse accessibility node tree recursively
     */
    private final void traverseNode(android.view.accessibility.AccessibilityNodeInfo node, kotlin.jvm.functions.Function1<? super android.view.accessibility.AccessibilityNodeInfo, kotlin.Unit> action) {
    }
    
    /**
     * Check if a string is a media file path
     */
    private final boolean isMediaPath(java.lang.String path) {
        return false;
    }
}