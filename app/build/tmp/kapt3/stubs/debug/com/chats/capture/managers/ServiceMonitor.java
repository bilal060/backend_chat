package com.chats.capture.managers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0007\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\tJ\u0006\u0010\n\u001a\u00020\u000bJ\u0006\u0010\f\u001a\u00020\u000bJ\u0006\u0010\r\u001a\u00020\bJ\u0006\u0010\u000e\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/chats/capture/managers/ServiceMonitor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "monitorScope", "Lkotlinx/coroutines/CoroutineScope;", "checkServices", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "isAccessibilityServiceEnabled", "", "isNotificationServiceEnabled", "restartServices", "startMonitoring", "app_debug"})
public final class ServiceMonitor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope monitorScope = null;
    
    public ServiceMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void startMonitoring() {
    }
    
    private final java.lang.Object checkServices(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final boolean isNotificationServiceEnabled() {
        return false;
    }
    
    public final boolean isAccessibilityServiceEnabled() {
        return false;
    }
    
    public final void restartServices() {
    }
}