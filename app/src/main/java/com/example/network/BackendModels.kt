package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SosRequest(
    val user_id: String,
    val latitude: Double,
    val longitude: Double,
    val emergency_type: String = "MANUAL_SOS",
    val is_fall_detected: Boolean = false,
    val is_no_movement: Boolean = false,
    val is_route_deviation: Boolean = false
)

@JsonClass(generateAdapter = true)
data class LocationUpdateRequest(
    val emergency_id: String,
    val user_id: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 5.0f,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class EmergencyResponse(
    val emergency_id: String,
    val user_id: String,
    val latitude: Double,
    val longitude: Double,
    val emergency_type: String,
    val risk_score: Int,
    val risk_level: String,
    val status: String,
    val timestamp: Long,
    val contributing_factors: String? = null
)

@JsonClass(generateAdapter = true)
data class CancelResponse(
    val emergency_id: String,
    val status: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class ContactDto(
    val name: String,
    val phone: String,
    val relationship: String
)
