package com.chats.capture.managers;

/**
 * Manages screenshot capture and upload
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000b\u001a\u00020\fJ\u0016\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000fH\u0082@\u00a2\u0006\u0002\u0010\u0010R\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/chats/capture/managers/ScreenshotManager;", "", "context", "Landroid/content/Context;", "accessibilityService", "Landroid/accessibilityservice/AccessibilityService;", "(Landroid/content/Context;Landroid/accessibilityservice/AccessibilityService;)V", "screenshotCapture", "Lcom/chats/capture/utils/ScreenshotCapture;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "captureAndUploadScreenshot", "", "uploadScreenshot", "filePath", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class ScreenshotManager {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private final android.accessibilityservice.AccessibilityService accessibilityService = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    @org.jetbrains.annotations.Nullable()
    private final com.chats.capture.utils.ScreenshotCapture screenshotCapture = null;
    
    public ScreenshotManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.accessibilityservice.AccessibilityService accessibilityService) {
        super();
    }
    
    /**
     * Capture and upload screenshot
     */
    public final void captureAndUploadScreenshot() {
    }
    
    /**
     * Upload screenshot to server
     */
    private final java.lang.Object uploadScreenshot(java.lang.String filePath, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}