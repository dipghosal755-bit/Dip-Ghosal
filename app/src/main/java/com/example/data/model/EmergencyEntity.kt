package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergencies")
data class EmergencyEntity(
    @PrimaryKey
    val emergencyId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val emergencyType: String, // MANUAL_SOS, FALL_DETECTED, INACTIVITY, ROUTE_DEVIATION
    val riskScore: Int,        // 0 to 100
    val riskLevel: String,      // LOW, MEDIUM, HIGH, CRITICAL
    val status: String,         // ACTIVE, CANCELLED, RESOLVED
    val contributingFactors: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "location_history")
data class LocationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val emergencyId: String,
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 5.0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val relationship: String, // Mother, Father, Friend, Guardian, etc.
    val isPrimary: Boolean = false
)

data class SafetyZone(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Double
)
