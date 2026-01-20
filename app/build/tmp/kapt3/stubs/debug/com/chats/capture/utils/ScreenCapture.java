package com.chats.capture.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010!\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\t\u001a\u00020\nJ \u0010\u000e\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\n2\f\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\r0\u0012H\u0002J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\n2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\rJ\u0018\u0010\u0015\u001a\u0004\u0018\u00010\n2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\rJ(\u0010\u0017\u001a\u0004\u0018\u00010\n2\b\u0010\u0010\u001a\u0004\u0018\u00010\n2\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u001a0\u0019H\u0002J\u000e\u0010\u001b\u001a\u00020\r2\u0006\u0010\u001c\u001a\u00020\bJ(\u0010\u001d\u001a\u00020\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\n2\f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00122\u0006\u0010 \u001a\u00020!H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\""}, d2 = {"Lcom/chats/capture/utils/ScreenCapture;", "", "accessibilityService", "Landroid/accessibilityservice/AccessibilityService;", "(Landroid/accessibilityservice/AccessibilityService;)V", "gson", "Lcom/google/gson/Gson;", "captureScreenHierarchy", "Lcom/chats/capture/utils/ScreenCaptureData;", "rootNode", "Landroid/view/accessibility/AccessibilityNodeInfo;", "extractAllText", "", "", "extractTextFromNode", "", "node", "texts", "", "findNodeById", "viewId", "findNodeByText", "text", "findNodeRecursive", "predicate", "Lkotlin/Function1;", "", "toJson", "screenData", "traverseNode", "elements", "Lcom/chats/capture/utils/UIElement;", "depth", "", "app_debug"})
public final class ScreenCapture {
    @org.jetbrains.annotations.NotNull()
    private final android.accessibilityservice.AccessibilityService accessibilityService = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    
    public ScreenCapture(@org.jetbrains.annotations.NotNull()
    android.accessibilityservice.AccessibilityService accessibilityService) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.chats.capture.utils.ScreenCaptureData captureScreenHierarchy(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo rootNode) {
        return null;
    }
    
    private final void traverseNode(android.view.accessibility.AccessibilityNodeInfo node, java.util.List<com.chats.capture.utils.UIElement> elements, int depth) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> extractAllText(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo rootNode) {
        return null;
    }
    
    private final void extractTextFromNode(android.view.accessibility.AccessibilityNodeInfo node, java.util.List<java.lang.String> texts) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.view.accessibility.AccessibilityNodeInfo findNodeByText(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo rootNode, @org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.view.accessibility.AccessibilityNodeInfo findNodeById(@org.jetbrains.annotations.NotNull()
    android.view.accessibility.AccessibilityNodeInfo rootNode, @org.jetbrains.annotations.NotNull()
    java.lang.String viewId) {
        return null;
    }
    
    private final android.view.accessibility.AccessibilityNodeInfo findNodeRecursive(android.view.accessibility.AccessibilityNodeInfo node, kotlin.jvm.functions.Function1<? super android.view.accessibility.AccessibilityNodeInfo, java.lang.Boolean> predicate) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String toJson(@org.jetbrains.annotations.NotNull()
    com.chats.capture.utils.ScreenCaptureData screenData) {
        return null;
    }
}