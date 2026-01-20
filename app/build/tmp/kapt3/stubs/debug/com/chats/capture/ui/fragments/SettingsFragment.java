package com.chats.capture.ui.fragments;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J&\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u001bH\u0016J\u001a\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u001d\u001a\u00020\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001e\u001a\u00020\u001bH\u0002J\b\u0010\u001f\u001a\u00020\u001bH\u0002J\b\u0010 \u001a\u00020\u001bH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006!"}, d2 = {"Lcom/chats/capture/ui/fragments/SettingsFragment;", "Landroidx/fragment/app/Fragment;", "()V", "buttonOpenServiceSettings", "Lcom/google/android/material/button/MaterialButton;", "buttonRefreshFcmToken", "buttonSaveServerUrl", "editTextServerUrl", "Lcom/google/android/material/textfield/TextInputEditText;", "serviceMonitor", "Lcom/chats/capture/managers/ServiceMonitor;", "textViewBatteryOptimizationStatus", "Landroid/widget/TextView;", "textViewFcmStatus", "textViewFcmToken", "textViewKeyboardServiceStatus", "textViewLastSyncTime", "textViewNotificationServiceStatus", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "", "onViewCreated", "view", "refreshFcmToken", "updateFcmStatus", "updateServiceStatus", "app_debug"})
public final class SettingsFragment extends androidx.fragment.app.Fragment {
    private com.google.android.material.textfield.TextInputEditText editTextServerUrl;
    private com.google.android.material.button.MaterialButton buttonSaveServerUrl;
    private android.widget.TextView textViewNotificationServiceStatus;
    private android.widget.TextView textViewKeyboardServiceStatus;
    private android.widget.TextView textViewBatteryOptimizationStatus;
    private android.widget.TextView textViewLastSyncTime;
    private android.widget.TextView textViewFcmStatus;
    private android.widget.TextView textViewFcmToken;
    private com.google.android.material.button.MaterialButton buttonOpenServiceSettings;
    private com.google.android.material.button.MaterialButton buttonRefreshFcmToken;
    private com.chats.capture.managers.ServiceMonitor serviceMonitor;
    
    public SettingsFragment() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void refreshFcmToken() {
    }
    
    @java.lang.Override()
    public void onResume() {
    }
    
    private final void updateServiceStatus() {
    }
    
    private final void updateFcmStatus() {
    }
}