package com.chats.capture.utils;

/**
 * Buffers key events for a message until it's complete.
 * Handles persistence for crash recovery and app switching.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\u0018\u0000 #2\u00020\u0001:\u0002\"#B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J4\u0010\r\u001a\u00020\b2\u0006\u0010\u000e\u001a\u00020\u00072\u0006\u0010\u000f\u001a\u00020\u00072\b\u0010\u0010\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0011\u001a\u00020\u00072\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0007J\u0006\u0010\u0013\u001a\u00020\u0014J\u000e\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u000e\u001a\u00020\u0007J\u0010\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u000e\u001a\u00020\u0007H\u0002J\u0010\u0010\u0017\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u0007J\u0010\u0010\u0018\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u0007J\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\b0\u001aJ\u000e\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u000e\u001a\u00020\u0007J\u000e\u0010\u001d\u001a\u00020\u00142\u0006\u0010\u000e\u001a\u00020\u0007J\u0010\u0010\u001e\u001a\u00020\u00142\u0006\u0010\u001f\u001a\u00020\bH\u0002J\u0006\u0010 \u001a\u00020\u0014J\u0012\u0010!\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u0007H\u0002R\u001a\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/chats/capture/utils/MessageBuffer;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "buffers", "Ljava/util/concurrent/ConcurrentHashMap;", "", "Lcom/chats/capture/utils/MessageBuffer$BufferData;", "gson", "Lcom/google/gson/Gson;", "prefs", "Landroid/content/SharedPreferences;", "addKeyEvent", "packageName", "appName", "chatIdentifier", "text", "keyEvent", "clearAllBuffers", "", "clearBuffer", "clearPersistedBuffer", "getAndClear", "getBuffer", "getTimedOutBuffers", "", "isMessageComplete", "", "markComplete", "persistBuffer", "buffer", "restoreAllBuffers", "restoreBuffer", "BufferData", "Companion", "app_debug"})
public final class MessageBuffer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.google.gson.Gson gson = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.SharedPreferences prefs = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, com.chats.capture.utils.MessageBuffer.BufferData> buffers = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_BUFFER_PREFIX = "buffer_";
    private static final long MESSAGE_TIMEOUT_MS = 5000L;
    private static final long DEBOUNCE_MS = 50L;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.utils.MessageBuffer.Companion Companion = null;
    
    public MessageBuffer(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Add a key event to the buffer for a package
     */
    @org.jetbrains.annotations.NotNull()
    public final com.chats.capture.utils.MessageBuffer.BufferData addKeyEvent(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName, @org.jetbrains.annotations.NotNull()
    java.lang.String appName, @org.jetbrains.annotations.Nullable()
    java.lang.String chatIdentifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.Nullable()
    java.lang.String keyEvent) {
        return null;
    }
    
    /**
     * Check if a message is complete based on timeout
     */
    public final boolean isMessageComplete(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return false;
    }
    
    /**
     * Mark message as complete (Enter key pressed or send button clicked)
     */
    public final void markComplete(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
    }
    
    /**
     * Get and clear completed buffer
     */
    @org.jetbrains.annotations.Nullable()
    public final com.chats.capture.utils.MessageBuffer.BufferData getAndClear(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return null;
    }
    
    /**
     * Get buffer without clearing (for checking)
     */
    @org.jetbrains.annotations.Nullable()
    public final com.chats.capture.utils.MessageBuffer.BufferData getBuffer(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
        return null;
    }
    
    /**
     * Clear buffer (user switched apps or cancelled)
     */
    public final void clearBuffer(@org.jetbrains.annotations.NotNull()
    java.lang.String packageName) {
    }
    
    /**
     * Get all buffers that have timed out
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.chats.capture.utils.MessageBuffer.BufferData> getTimedOutBuffers() {
        return null;
    }
    
    /**
     * Persist buffer to SharedPreferences for crash recovery
     */
    private final void persistBuffer(com.chats.capture.utils.MessageBuffer.BufferData buffer) {
    }
    
    /**
     * Restore buffer from SharedPreferences
     */
    private final com.chats.capture.utils.MessageBuffer.BufferData restoreBuffer(java.lang.String packageName) {
        return null;
    }
    
    /**
     * Clear persisted buffer
     */
    private final void clearPersistedBuffer(java.lang.String packageName) {
    }
    
    /**
     * Clear all buffers (on app start, restore from persistence)
     */
    public final void clearAllBuffers() {
    }
    
    /**
     * Restore all buffers from persistence (call on app start)
     */
    public final void restoreAllBuffers() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u001a\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BM\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\n\u00a2\u0006\u0002\u0010\fJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000f\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0003J\t\u0010 \u001a\u00020\u0003H\u00c6\u0003J\t\u0010!\u001a\u00020\nH\u00c6\u0003J\t\u0010\"\u001a\u00020\nH\u00c6\u0003JW\u0010#\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u00072\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\nH\u00c6\u0001J\u0013\u0010$\u001a\u00020%2\b\u0010&\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\'\u001a\u00020(H\u00d6\u0001J\t\u0010)\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u001c\u0010\u0005\u001a\u0004\u0018\u00010\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u000e\"\u0004\b\u0010\u0010\u0011R\u001a\u0010\b\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u000e\"\u0004\b\u0013\u0010\u0011R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u001a\u0010\u000b\u001a\u00020\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u000eR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0017\u00a8\u0006*"}, d2 = {"Lcom/chats/capture/utils/MessageBuffer$BufferData;", "", "packageName", "", "appName", "chatIdentifier", "keyHistory", "", "currentText", "startTime", "", "lastKeyTime", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;Ljava/lang/String;JJ)V", "getAppName", "()Ljava/lang/String;", "getChatIdentifier", "setChatIdentifier", "(Ljava/lang/String;)V", "getCurrentText", "setCurrentText", "getKeyHistory", "()Ljava/util/List;", "getLastKeyTime", "()J", "setLastKeyTime", "(J)V", "getPackageName", "getStartTime", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class BufferData {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String packageName = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String appName = null;
        @org.jetbrains.annotations.Nullable()
        private java.lang.String chatIdentifier;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<java.lang.String> keyHistory = null;
        @org.jetbrains.annotations.NotNull()
        private java.lang.String currentText;
        private final long startTime = 0L;
        private long lastKeyTime;
        
        public BufferData(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String appName, @org.jetbrains.annotations.Nullable()
        java.lang.String chatIdentifier, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> keyHistory, @org.jetbrains.annotations.NotNull()
        java.lang.String currentText, long startTime, long lastKeyTime) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPackageName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getAppName() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getChatIdentifier() {
            return null;
        }
        
        public final void setChatIdentifier(@org.jetbrains.annotations.Nullable()
        java.lang.String p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> getKeyHistory() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCurrentText() {
            return null;
        }
        
        public final void setCurrentText(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        public final long getStartTime() {
            return 0L;
        }
        
        public final long getLastKeyTime() {
            return 0L;
        }
        
        public final void setLastKeyTime(long p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<java.lang.String> component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        public final long component6() {
            return 0L;
        }
        
        public final long component7() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.chats.capture.utils.MessageBuffer.BufferData copy(@org.jetbrains.annotations.NotNull()
        java.lang.String packageName, @org.jetbrains.annotations.NotNull()
        java.lang.String appName, @org.jetbrains.annotations.Nullable()
        java.lang.String chatIdentifier, @org.jetbrains.annotations.NotNull()
        java.util.List<java.lang.String> keyHistory, @org.jetbrains.annotations.NotNull()
        java.lang.String currentText, long startTime, long lastKeyTime) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/chats/capture/utils/MessageBuffer$Companion;", "", "()V", "DEBOUNCE_MS", "", "KEY_BUFFER_PREFIX", "", "MESSAGE_TIMEOUT_MS", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}