package com.chats.capture.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J \u0010\u0005\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\nH\u0002J\u0018\u0010\u000b\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\nH\u0002J\u0018\u0010\f\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\nH\u0002J\u0018\u0010\r\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\nH\u0002J\u0018\u0010\u000e\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\t\u001a\u00020\nH\u0002J\u001e\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0007\u0018\u00010\u00062\u0006\u0010\u0010\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u0007H\u0002J\u001a\u0010\u0016\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u0007H\u0002J\u000e\u0010\u001a\u001a\u0004\u0018\u00010\u0018*\u00020\u001bH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001c"}, d2 = {"Lcom/chats/capture/utils/MediaExtractor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "extractAppSpecific", "", "", "packageName", "notification", "Landroid/app/Notification;", "extractFromContentUri", "extractFromExtras", "extractFromMediaSession", "extractFromRemoteViews", "extractMediaFromNotification", "sbn", "Landroid/service/notification/StatusBarNotification;", "(Landroid/service/notification/StatusBarNotification;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isMediaUrl", "", "url", "saveBitmapToFile", "bitmap", "Landroid/graphics/Bitmap;", "prefix", "toBitmap", "Landroid/graphics/drawable/Drawable;", "app_debug"})
public final class MediaExtractor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    
    public MediaExtractor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object extractMediaFromNotification(@org.jetbrains.annotations.NotNull()
    android.service.notification.StatusBarNotification sbn, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractFromExtras(android.app.Notification notification) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractFromContentUri(android.app.Notification notification) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractFromMediaSession(android.app.Notification notification) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractFromRemoteViews(android.app.Notification notification) {
        return null;
    }
    
    private final java.util.List<java.lang.String> extractAppSpecific(java.lang.String packageName, android.app.Notification notification) {
        return null;
    }
    
    private final java.lang.String saveBitmapToFile(android.graphics.Bitmap bitmap, java.lang.String prefix) {
        return null;
    }
    
    private final android.graphics.Bitmap toBitmap(android.graphics.drawable.Drawable $this$toBitmap) {
        return null;
    }
    
    private final boolean isMediaUrl(java.lang.String url) {
        return false;
    }
}