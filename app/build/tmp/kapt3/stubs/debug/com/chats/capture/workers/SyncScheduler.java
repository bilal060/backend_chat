package com.chats.capture.workers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u0010\u0010\u000e\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0002J\u001f\u0010\u000f\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\n\b\u0002\u0010\u0010\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/chats/capture/workers/SyncScheduler;", "", "()V", "LOW_BATTERY_SYNC_INTERVAL_MINUTES", "", "LOW_BATTERY_THRESHOLD", "", "NORMAL_SYNC_INTERVAL_MINUTES", "SYNC_WORK_NAME", "", "cancelSync", "", "context", "Landroid/content/Context;", "getBatteryLevel", "scheduleSync", "intervalMinutes", "(Landroid/content/Context;Ljava/lang/Long;)V", "app_debug"})
public final class SyncScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SYNC_WORK_NAME = "sync_work";
    private static final long NORMAL_SYNC_INTERVAL_MINUTES = 15L;
    private static final long LOW_BATTERY_SYNC_INTERVAL_MINUTES = 60L;
    private static final int LOW_BATTERY_THRESHOLD = 20;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.workers.SyncScheduler INSTANCE = null;
    
    private SyncScheduler() {
        super();
    }
    
    public final void scheduleSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    java.lang.Long intervalMinutes) {
    }
    
    /**
     * Get current battery level percentage
     */
    private final int getBatteryLevel(android.content.Context context) {
        return 0;
    }
    
    public final void cancelSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}