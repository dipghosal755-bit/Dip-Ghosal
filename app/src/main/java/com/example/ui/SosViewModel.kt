package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import com.example.data.repository.SosRepository
import com.example.location.Coordinates
import com.example.location.LocationTracker
import com.example.risk.RiskAssessmentResult
import com.example.risk.RiskEngine
import com.example.sensor.FallDetector
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SosViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SosRepository(application)

    private val _currentCoordinates = MutableStateFlow(Coordinates(28.6139, 77.2090, 4.5f, true))
    val currentCoordinates: StateFlow<Coordinates> = _currentCoordinates.asStateFlow()

    val activeEmergency: StateFlow<EmergencyEntity?> = repository.activeEmergency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEmergencies: StateFlow<List<EmergencyEntity>> = repository.allEmergencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _locationHistory = MutableStateFlow<List<LocationHistoryEntity>>(emptyList())
    val locationHistory: StateFlow<List<LocationHistoryEntity>> = _locationHistory.asStateFlow()

    private val _riskAssessment = MutableStateFlow(
        RiskEngine.calculateRisk(
            isManualSos = false,
            isFallDetected = false,
            isNoMovement = false,
            isRouteDeviation = false
        )
    )
    val riskAssessment: StateFlow<RiskAssessmentResult> = _riskAssessment.asStateFlow()

    private val _isTrackingActive = MutableStateFlow(false)
    val isTrackingActive: StateFlow<Boolean> = _isTrackingActive.asStateFlow()

    private val _broadcastCount = MutableStateFlow(0)
    val broadcastCount: StateFlow<Int> = _broadcastCount.asStateFlow()

    private val _updateIntervalSeconds = MutableStateFlow(10)
    val updateIntervalSeconds: StateFlow<Int> = _updateIntervalSeconds.asStateFlow()

    private val _isRemoteBackendConnected = MutableStateFlow(false)
    val isRemoteBackendConnected: StateFlow<Boolean> = _isRemoteBackendConnected.asStateFlow()

    private val _isFallWarningDialogVisible = MutableStateFlow(false)
    val isFallWarningDialogVisible: StateFlow<Boolean> = _isFallWarningDialogVisible.asStateFlow()

    private val _fallCountdownSeconds = MutableStateFlow(10)
    val fallCountdownSeconds: StateFlow<Int> = _fallCountdownSeconds.asStateFlow()

    private val _isAddContactDialogVisible = MutableStateFlow(false)
    val isAddContactDialogVisible: StateFlow<Boolean> = _isAddContactDialogVisible.asStateFlow()

    private val _isSettingsDialogVisible = MutableStateFlow(false)
    val isSettingsDialogVisible: StateFlow<Boolean> = _isSettingsDialogVisible.asStateFlow()

    private val _isAlertPreviewVisible = MutableStateFlow(false)
    val isAlertPreviewVisible: StateFlow<Boolean> = _isAlertPreviewVisible.asStateFlow()

    private val _nearestSafetyZoneName = MutableStateFlow("College Campus (Main Block)")
    val nearestSafetyZoneName: StateFlow<String> = _nearestSafetyZoneName.asStateFlow()

    private val _isInsideSafetyZone = MutableStateFlow(true)
    val isInsideSafetyZone: StateFlow<Boolean> = _isInsideSafetyZone.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Mobile App, 1: Responder Command Center, 2: Risk Engine
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private var trackingJob: Job? = null
    private var fallCountdownJob: Job? = null
    private var locationHistoryObserverJob: Job? = null

    val locationTracker = LocationTracker(application) { coords ->
        _currentCoordinates.value = coords
        evaluateLocationZone(coords.latitude, coords.longitude)
    }

    val fallDetector = FallDetector(application) {
        onFallSuspected()
    }

    init {
        fallDetector.start()

        // Fetch initial GPS position
        locationTracker.fetchCurrentLocation { coords ->
            _currentCoordinates.value = coords
            evaluateLocationZone(coords.latitude, coords.longitude)
        }

        // Monitor active emergency changes to sync live tracking
        viewModelScope.launch {
            activeEmergency.collect { emergency ->
                if (emergency != null && emergency.status == "ACTIVE") {
                    startTracking(emergency.emergencyId)
                    observeHistory(emergency.emergencyId)
                    _riskAssessment.value = RiskEngine.calculateRisk(
                        isManualSos = true,
                        isFallDetected = emergency.emergencyType.contains("FALL"),
                        isNoMovement = emergency.emergencyType.contains("INACTIVITY"),
                        isRouteDeviation = !_isInsideSafetyZone.value
                    )
                } else {
                    stopTracking()
                    _locationHistory.value = emptyList()
                    _riskAssessment.value = RiskEngine.calculateRisk(
                        isManualSos = false,
                        isFallDetected = false,
                        isNoMovement = false,
                        isRouteDeviation = !_isInsideSafetyZone.value
                    )
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    private fun evaluateLocationZone(lat: Double, lng: Double) {
        val (zone, inside) = repository.evaluateSafetyZone(lat, lng)
        _nearestSafetyZoneName.value = zone?.name ?: "Unknown Area"
        _isInsideSafetyZone.value = inside

        val isSos = activeEmergency.value?.status == "ACTIVE"
        val isFall = activeEmergency.value?.emergencyType?.contains("FALL") == true
        _riskAssessment.value = RiskEngine.calculateRisk(
            isManualSos = isSos,
            isFallDetected = isFall,
            isNoMovement = false,
            isRouteDeviation = !inside
        )
    }

    fun triggerManualSos() {
        viewModelScope.launch {
            val coords = _currentCoordinates.value
            val deviation = !_isInsideSafetyZone.value
            repository.triggerSos(
                latitude = coords.latitude,
                longitude = coords.longitude,
                emergencyType = "MANUAL_SOS",
                isFallDetected = false,
                isNoMovement = false,
                isRouteDeviation = deviation
            )
        }
    }

    fun cancelSos(emergencyId: String) {
        viewModelScope.launch {
            repository.cancelEmergency(emergencyId)
            stopTracking()
        }
    }

    fun resolveEmergency(emergencyId: String) {
        viewModelScope.launch {
            repository.resolveEmergency(emergencyId)
        }
    }

    private fun startTracking(emergencyId: String) {
        if (_isTrackingActive.value) return
        _isTrackingActive.value = true
        _broadcastCount.value = 0

        trackingJob?.cancel()
        trackingJob = viewModelScope.launch {
            while (isActive) {
                val coords = locationTracker.stepSimulatedLocation()
                _currentCoordinates.value = coords
                evaluateLocationZone(coords.latitude, coords.longitude)

                repository.logLocationUpdate(
                    emergencyId = emergencyId,
                    latitude = coords.latitude,
                    longitude = coords.longitude,
                    accuracy = coords.accuracy
                )
                _broadcastCount.value += 1

                delay(_updateIntervalSeconds.value * 1000L)
            }
        }
    }

    private fun stopTracking() {
        _isTrackingActive.value = false
        trackingJob?.cancel()
        trackingJob = null
        locationHistoryObserverJob?.cancel()
        locationHistoryObserverJob = null
    }

    private fun observeHistory(emergencyId: String) {
        locationHistoryObserverJob?.cancel()
        locationHistoryObserverJob = viewModelScope.launch {
            repository.getLocationHistory(emergencyId).collect { list ->
                _locationHistory.value = list
            }
        }
    }

    fun onFallSuspected() {
        if (activeEmergency.value?.status == "ACTIVE") return
        _isFallWarningDialogVisible.value = true
        _fallCountdownSeconds.value = 10

        fallCountdownJob?.cancel()
        fallCountdownJob = viewModelScope.launch {
            for (i in 10 downTo 1) {
                _fallCountdownSeconds.value = i
                delay(1000)
            }
            if (_isFallWarningDialogVisible.value) {
                _isFallWarningDialogVisible.value = false
                val coords = _currentCoordinates.value
                val deviation = !_isInsideSafetyZone.value
                repository.triggerSos(
                    latitude = coords.latitude,
                    longitude = coords.longitude,
                    emergencyType = "FALL_DETECTED",
                    isFallDetected = true,
                    isNoMovement = true,
                    isRouteDeviation = deviation
                )
            }
        }
    }

    fun dismissFallWarning() {
        fallCountdownJob?.cancel()
        fallCountdownJob = null
        _isFallWarningDialogVisible.value = false
    }

    fun simulateFall() {
        onFallSuspected()
    }

    fun simulateRouteDeviation() {
        val deviated = locationTracker.jumpOutsideSafetyZone()
        _currentCoordinates.value = deviated
        evaluateLocationZone(deviated.latitude, deviated.longitude)
    }

    fun resetToCampus() {
        val campus = locationTracker.resetToCampus()
        _currentCoordinates.value = campus
        evaluateLocationZone(campus.latitude, campus.longitude)
    }

    fun showAddContactDialog(show: Boolean) {
        _isAddContactDialogVisible.value = show
    }

    fun showSettingsDialog(show: Boolean) {
        _isSettingsDialogVisible.value = show
    }

    fun showAlertPreview(show: Boolean) {
        _isAlertPreviewVisible.value = show
    }

    fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
        viewModelScope.launch {
            repository.addContact(name, phone, relationship, isPrimary)
            _isAddContactDialogVisible.value = false
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            repository.deleteContact(id)
        }
    }

    fun setUpdateInterval(seconds: Int) {
        _updateIntervalSeconds.value = seconds
    }

    fun updateBackendConfig(url: String, enableRemote: Boolean) {
        repository.setBackendUrl(url, enableRemote)
        viewModelScope.launch {
            _isRemoteBackendConnected.value = repository.checkBackendHealth()
        }
    }

    override fun onCleared() {
        super.onCleared()
        fallDetector.stop()
        locationTracker.stopLiveTracking()
        stopTracking()
    }
}
