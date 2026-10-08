package com.example.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SosApiService {
    @POST("sos")
    suspend fun createSos(
        @Body request: SosRequest
    ): Response<EmergencyResponse>

    @POST("location")
    suspend fun updateLocation(
        @Body request: LocationUpdateRequest
    ): Response<Map<String, Any>>

    @GET("emergencies")
    suspend fun getEmergencies(): Response<List<EmergencyResponse>>

    @GET("locations/{user_id}")
    suspend fun getUserLocations(
        @Path("user_id") userId: String
    ): Response<List<LocationUpdateRequest>>

    @POST("emergency/{emergency_id}/cancel")
    suspend fun cancelEmergency(
        @Path("emergency_id") emergencyId: String
    ): Response<CancelResponse>

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, String>>
}
