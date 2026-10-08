package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import com.example.data.model.SafetyZone
import com.example.network.EmergencyResponse
import com.example.network.LocationUpdateRequest
import com.example.network.SosApiService
import com.example.network.SosRequest
import com.example.risk.RiskEngine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SosRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val emergencyDao = database.emergencyDao()
    private val locationDao = database.locationDao()
    private val contactDao = database.contactDao()

    // Configurable backend URL (Default to local Android emulator loopback or custom)
    var backendBaseUrl: String = "http://10.0.2.2:8000/"
        private set

    private var apiService: SosApiService? = null
    var isRemoteBackendEnabled: Boolean = false
        private set

    // Default student user ID
    val currentUserId: String = "USER-SIH-" + (1000..9999).random()

    // Predefined Safety Zones for Hackathon / Student Demonstration
    val safetyZones = listOf(
        SafetyZone(
            name = "College Campus (Main Block)",
            latitude = 28.6139,
            longitude = 77.2090,
            radiusMeters = 600.0
        ),
        SafetyZone(
            name = "Student Hostel Block",
            latitude = 28.6185,
            longitude = 77.2140,
            radiusMeters = 400.0
        ),
        SafetyZone(
            name = "Home Safe Zone",
            latitude = 28.6250,
            longitude = 77.2000,
            radiusMeters = 500.0
        )
    )

    init {
        rebuildRetrofit(backendBaseUrl)
    }

    fun setBackendUrl(url: String, enableRemote: Boolean) {
        var formatted = url.trim()
        if (!formatted.endsWith("/")) formatted += "/"
        backendBaseUrl = formatted
        isRemoteBackendEnabled = enableRemote
        rebuildRetrofit(formatted)
    }

    private fun rebuildRetrofit(baseUrl: String) {
        try {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            apiService = retrofit.create(SosApiService::class.java)
        } catch (_: Exception) {
            apiService = null
        }
    }

    val activeEmergency: Flow<EmergencyEntity?> = emergencyDao.getActiveEmergency()
    val allEmergencies: Flow<List<EmergencyEntity>> = emergencyDao.getAllEmergencies()
    val contacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    fun getLocationHistory(emergencyId: String): Flow<List<LocationHistoryEntity>> {
        return locationDao.getLocationHistory(emergencyId)
    }

    suspend fun triggerSos(
        latitude: Double,
        longitude: Double,
        emergencyType: String = "MANUAL_SOS",
        isFallDetected: Boolean = false,
        isNoMovement: Boolean = false,
        isRouteDeviation: Boolean = false
    ): EmergencyEntity = withContext(Dispatchers.IO) {
        val riskResult = RiskEngine.calculateRisk(
            isManualSos = (emergencyType == "MANUAL_SOS" || emergencyType.contains("MANUAL")),
            isFallDetected = isFallDetected,
            isNoMovement = isNoMovement,
            isRouteDeviation = isRouteDeviation
        )

        val emergencyId = "EMG-" + UUID.randomUUID().toString().take(8).uppercase()
        val contributingText = riskResult.factors
            .filter { it.isPresent }
            .joinToString(", ") { "${it.title} (+${it.points})" }

        val entity = EmergencyEntity(
            emergencyId = emergencyId,
            userId = currentUserId,
            latitude = latitude,
            longitude = longitude,
            emergencyType = emergencyType,
            riskScore = riskResult.score,
            riskLevel = riskResult.level,
            status = "ACTIVE",
            contributingFactors = contributingText.ifEmpty { "Manual user distress" },
            timestamp = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis()
        )

        emergencyDao.insertEmergency(entity)

        // Store initial location breadcrumb
        locationDao.insertLocation(
            LocationHistoryEntity(
                emergencyId = emergencyId,
                userId = currentUserId,
                latitude = latitude,
                longitude = longitude,
                accuracy = 4.5f,
                timestamp = System.currentTimeMillis()
            )
        )

        // Remote sync attempt if remote backend enabled
        if (isRemoteBackendEnabled && apiService != null) {
            try {
                val req = SosRequest(
                    user_id = currentUserId,
                    latitude = latitude,
                    longitude = longitude,
                    emergency_type = emergencyType,
                    is_fall_detected = isFallDetected,
                    is_no_movement = isNoMovement,
                    is_route_deviation = isRouteDeviation
                )
                apiService?.createSos(req)
            } catch (_: Exception) {
                // Keep local entity active
            }
        }

        entity
    }

    suspend fun logLocationUpdate(
        emergencyId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float = 5.0f
    ) = withContext(Dispatchers.IO) {
        val loc = LocationHistoryEntity(
            emergencyId = emergencyId,
            userId = currentUserId,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            timestamp = System.currentTimeMillis()
        )
        locationDao.insertLocation(loc)

        // Update emergency record coordinates and timestamp
        val emg = emergencyDao.getEmergencyById(emergencyId)
        if (emg != null && emg.status == "ACTIVE") {
            emergencyDao.updateEmergency(
                emg.copy(
                    latitude = latitude,
                    longitude = longitude,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        if (isRemoteBackendEnabled && apiService != null) {
            try {
                apiService?.updateLocation(
                    LocationUpdateRequest(
                        emergency_id = emergencyId,
                        user_id = currentUserId,
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (_: Exception) {
                // Continue offline
            }
        }
    }

    suspend fun cancelEmergency(emergencyId: String) = withContext(Dispatchers.IO) {
        emergencyDao.updateStatus(emergencyId, "CANCELLED")

        if (isRemoteBackendEnabled && apiService != null) {
            try {
                apiService?.cancelEmergency(emergencyId)
            } catch (_: Exception) {
                // Local updated
            }
        }
    }

    suspend fun resolveEmergency(emergencyId: String) = withContext(Dispatchers.IO) {
        emergencyDao.updateStatus(emergencyId, "RESOLVED")
    }

    suspend fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) =
        withContext(Dispatchers.IO) {
            contactDao.insertContact(
                ContactEntity(
                    name = name,
                    phone = phone,
                    relationship = relationship,
                    isPrimary = isPrimary
                )
            )
        }

    suspend fun deleteContact(id: Long) = withContext(Dispatchers.IO) {
        contactDao.deleteContact(id)
    }

    suspend fun checkBackendHealth(): Boolean = withContext(Dispatchers.IO) {
        if (!isRemoteBackendEnabled || apiService == null) return@withContext false
        try {
            val response = apiService?.checkHealth()
            response?.isSuccessful == true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Check if a coordinate is inside any designated safety zone
     * Returns the nearest safety zone and whether distance is within radius.
     */
    fun evaluateSafetyZone(lat: Double, lng: Double): Pair<SafetyZone?, Boolean> {
        var nearestZone: SafetyZone? = null
        var minDistance = Double.MAX_VALUE

        for (zone in safetyZones) {
            val dist = calculateDistanceMeters(lat, lng, zone.latitude, zone.longitude)
            if (dist < minDistance) {
                minDistance = dist
                nearestZone = zone
            }
        }

        val isInside = nearestZone != null && minDistance <= nearestZone.radiusMeters
        return Pair(nearestZone, isInside)
    }

    // Haversine formula
    fun calculateDistanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
