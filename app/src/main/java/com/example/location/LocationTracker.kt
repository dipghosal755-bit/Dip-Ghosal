package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 4.0f,
    val isSimulated: Boolean = false
)

class LocationTracker(
    private val context: Context,
    private val onLocationUpdated: (Coordinates) -> Unit
) {
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    // Initial default fallback coordinate (e.g. Campus coordinates 28.6139, 77.2090)
    var currentCoordinates = Coordinates(28.6139, 77.2090, 5.0f, true)
        private set

    // Simulated path step index for testing route changes
    private var simStep = 0

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun fetchCurrentLocation(onResult: (Coordinates) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(currentCoordinates)
            return
        }

        try {
            fusedClient.lastLocation
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        currentCoordinates = Coordinates(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracy = loc.accuracy,
                            isSimulated = false
                        )
                        onResult(currentCoordinates)
                    } else {
                        // Return current coordinates or fallback
                        onResult(currentCoordinates)
                    }
                }
                .addOnFailureListener {
                    onResult(currentCoordinates)
                }
        } catch (_: SecurityException) {
            onResult(currentCoordinates)
        }
    }

    fun startLiveTracking(intervalSeconds: Long = 10) {
        if (isTracking) return
        isTracking = true

        if (!hasLocationPermission()) {
            // Run simulated updates if no physical permission
            startSimulationTicker(intervalSeconds)
            return
        }

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalSeconds * 1000
        ).setMinUpdateIntervalMillis(intervalSeconds * 1000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    val coords = Coordinates(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracy = loc.accuracy,
                        isSimulated = false
                    )
                    currentCoordinates = coords
                    onLocationUpdated(coords)
                }
            }
        }

        try {
            locationCallback?.let { cb ->
                fusedClient.requestLocationUpdates(request, cb, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            startSimulationTicker(intervalSeconds)
        }
    }

    fun stopLiveTracking() {
        isTracking = false
        locationCallback?.let { cb ->
            fusedClient.removeLocationUpdates(cb)
        }
        locationCallback = null
    }

    private fun startSimulationTicker(intervalSeconds: Long) {
        // Step slightly to simulate movement
        stepSimulatedLocation()
    }

    fun stepSimulatedLocation(): Coordinates {
        simStep++
        // Jitter / walk step ~35 meters
        val latDelta = 0.00032 * (if (simStep % 2 == 0) 1 else -1)
        val lonDelta = 0.00028 * (simStep % 3 - 1)

        val updated = currentCoordinates.copy(
            latitude = currentCoordinates.latitude + latDelta,
            longitude = currentCoordinates.longitude + lonDelta,
            accuracy = 3.8f,
            isSimulated = true
        )
        currentCoordinates = updated
        onLocationUpdated(updated)
        return updated
    }

    fun jumpOutsideSafetyZone(): Coordinates {
        // Move coordinates ~1.2 km away to simulate route deviation
        val deviated = currentCoordinates.copy(
            latitude = currentCoordinates.latitude + 0.0125,
            longitude = currentCoordinates.longitude + 0.0095,
            accuracy = 6.0f,
            isSimulated = true
        )
        currentCoordinates = deviated
        onLocationUpdated(deviated)
        return deviated
    }

    fun resetToCampus(): Coordinates {
        val campus = Coordinates(28.6139, 77.2090, 4.0f, true)
        currentCoordinates = campus
        onLocationUpdated(campus)
        return campus
    }
}
