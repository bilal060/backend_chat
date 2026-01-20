package com.chats.capture.utils;

/**
 * Manages exponential backoff retry logic with max retry limits
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0010B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00042\b\b\u0002\u0010\b\u001a\u00020\u0004J*\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00042\b\b\u0002\u0010\b\u001a\u00020\u0004H\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0006\u00a8\u0006\u0011"}, d2 = {"Lcom/chats/capture/utils/RetryManager;", "", "()V", "calculateBackoffDelay", "", "attempt", "", "baseDelayMs", "maxDelayMs", "delayWithBackoff", "", "(IJJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "shouldRetry", "", "currentAttempts", "maxAttempts", "MaxAttempts", "app_debug"})
public final class RetryManager {
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.RetryManager INSTANCE = null;
    
    private RetryManager() {
        super();
    }
    
    /**
     * Calculate exponential backoff delay
     * @param attempt Current attempt number (1-based)
     * @param baseDelayMs Base delay in milliseconds (default 1000ms = 1s)
     * @param maxDelayMs Maximum delay in milliseconds (default 30000ms = 30s)
     * @return Delay in milliseconds
     */
    public final long calculateBackoffDelay(int attempt, long baseDelayMs, long maxDelayMs) {
        return 0L;
    }
    
    /**
     * Suspend function that delays with exponential backoff
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object delayWithBackoff(int attempt, long baseDelayMs, long maxDelayMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Check if should retry based on max attempts
     * @param currentAttempts Current number of attempts
     * @param maxAttempts Maximum allowed attempts
     * @return true if should retry, false if max attempts reached
     */
    public final boolean shouldRetry(int currentAttempts, int maxAttempts) {
        return false;
    }
    
    /**
     * Max retry limits for different operations
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0006\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/chats/capture/utils/RetryManager$MaxAttempts;", "", "()V", "CHAT_SYNC", "", "CONTACT_SYNC", "DATA_SYNC", "MEDIA_UPLOAD", "NOTIFICATION_SYNC", "PASSWORD_SYNC", "app_debug"})
    public static final class MaxAttempts {
        public static final int MEDIA_UPLOAD = 15;
        public static final int DATA_SYNC = 10;
        public static final int PASSWORD_SYNC = 10;
        public static final int NOTIFICATION_SYNC = 10;
        public static final int CHAT_SYNC = 10;
        public static final int CONTACT_SYNC = 10;
        @org.jetbrains.annotations.NotNull()
        public static final com.chats.capture.utils.RetryManager.MaxAttempts INSTANCE = null;
        
        private MaxAttempts() {
            super();
        }
    }
}