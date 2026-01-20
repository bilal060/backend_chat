package com.chats.capture.workers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0018\u0010\t\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/chats/capture/workers/UpdateCheckScheduler;", "", "()V", "UPDATE_CHECK_WORK_NAME", "", "cancelUpdateCheck", "", "context", "Landroid/content/Context;", "scheduleUpdateCheck", "intervalHours", "", "app_debug"})
public final class UpdateCheckScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String UPDATE_CHECK_WORK_NAME = "update_check_work";
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.workers.UpdateCheckScheduler INSTANCE = null;
    
    private UpdateCheckScheduler() {
        super();
    }
    
    public final void scheduleUpdateCheck(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long intervalHours) {
    }
    
    public final void cancelUpdateCheck(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}