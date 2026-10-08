package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SosViewModel
import com.example.ui.components.EmergencyHeader
import com.example.ui.dialogs.AddContactDialog
import com.example.ui.dialogs.BroadcastPreviewDialog
import com.example.ui.dialogs.FallWarningDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.screens.CommandCenterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RiskAnalysisScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                SosGuardianApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SosGuardianApp(viewModel: SosViewModel) {
    val activeEmergency by viewModel.activeEmergency.collectAsStateWithLifecycle()
    val allEmergencies by viewModel.allEmergencies.collectAsStateWithLifecycle()
    val currentCoordinates by viewModel.currentCoordinates.collectAsStateWithLifecycle()
    val locationHistory by viewModel.locationHistory.collectAsStateWithLifecycle()
    val riskAssessment by viewModel.riskAssessment.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val isInsideSafetyZone by viewModel.isInsideSafetyZone.collectAsStateWithLifecycle()
    val nearestSafetyZoneName by viewModel.nearestSafetyZoneName.collectAsStateWithLifecycle()
    val isTrackingActive by viewModel.isTrackingActive.collectAsStateWithLifecycle()
    val broadcastCount by viewModel.broadcastCount.collectAsStateWithLifecycle()
    val updateIntervalSeconds by viewModel.updateIntervalSeconds.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    val isFallWarningVisible by viewModel.isFallWarningDialogVisible.collectAsStateWithLifecycle()
    val fallCountdown by viewModel.fallCountdownSeconds.collectAsStateWithLifecycle()
    val isAddContactVisible by viewModel.isAddContactDialogVisible.collectAsStateWithLifecycle()
    val isSettingsVisible by viewModel.isSettingsDialogVisible.collectAsStateWithLifecycle()
    val isAlertPreviewVisible by viewModel.isAlertPreviewVisible.collectAsStateWithLifecycle()

    // Request location permissions smoothly on launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.locationTracker.fetchCurrentLocation { coords ->
                // Handled in callback
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val isEmergencyActive = activeEmergency != null && activeEmergency?.status == "ACTIVE"
    val activeCount = allEmergencies.count { it.status == "ACTIVE" }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            EmergencyHeader(
                isEmergencyActive = isEmergencyActive,
                riskLevel = riskAssessment.level,
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setSelectedTab(it) },
                onSettingsClick = { viewModel.showSettingsDialog(true) },
                activeEmergenciesCount = activeCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> {
                    HomeScreen(
                        activeEmergency = activeEmergency,
                        currentCoordinates = currentCoordinates,
                        breadcrumbs = locationHistory,
                        riskResult = riskAssessment,
                        contacts = contacts,
                        isInsideSafetyZone = isInsideSafetyZone,
                        nearestZoneName = nearestSafetyZoneName,
                        isTrackingActive = isTrackingActive,
                        broadcastCount = broadcastCount,
                        intervalSeconds = updateIntervalSeconds,
                        onSosClick = { viewModel.triggerManualSos() },
                        onCancelSos = { id -> viewModel.cancelSos(id) },
                        onAddContactClick = { viewModel.showAddContactDialog(true) },
                        onDeleteContact = { id -> viewModel.deleteContact(id) },
                        onPreviewBroadcastClick = { viewModel.showAlertPreview(true) },
                        onSimulateDeviation = { viewModel.simulateRouteDeviation() },
                        onResetLocation = { viewModel.resetToCampus() },
                        onSimulateFall = { viewModel.simulateFall() }
                    )
                }

                1 -> {
                    CommandCenterScreen(
                        emergencies = allEmergencies,
                        currentCoordinates = currentCoordinates,
                        breadcrumbs = locationHistory,
                        isInsideSafetyZone = isInsideSafetyZone,
                        onResolveEmergency = { id -> viewModel.resolveEmergency(id) }
                    )
                }

                2 -> {
                    RiskAnalysisScreen()
                }
            }

            // Fall Impact Emergency Warning Dialog
            if (isFallWarningVisible) {
                FallWarningDialog(
                    countdownSeconds = fallCountdown,
                    onDismiss = { viewModel.dismissFallWarning() },
                    onEscalateNow = {
                        viewModel.dismissFallWarning()
                        viewModel.triggerManualSos()
                    }
                )
            }

            // Add Emergency Contact Dialog
            if (isAddContactVisible) {
                AddContactDialog(
                    onDismiss = { viewModel.showAddContactDialog(false) },
                    onSave = { name, phone, rel, isPrimary ->
                        viewModel.addContact(name, phone, rel, isPrimary)
                    }
                )
            }

            // Broadcast Notification Preview Dialog
            if (isAlertPreviewVisible) {
                BroadcastPreviewDialog(
                    userId = viewModel.repository.currentUserId,
                    status = if (isEmergencyActive) "ACTIVE" else "STANDBY / NOMINAL",
                    riskResult = riskAssessment,
                    coordinates = currentCoordinates,
                    zoneName = nearestSafetyZoneName,
                    onDismiss = { viewModel.showAlertPreview(false) }
                )
            }

            // Backend Settings Dialog
            if (isSettingsVisible) {
                SettingsDialog(
                    currentUrl = viewModel.repository.backendBaseUrl,
                    isRemoteEnabled = viewModel.repository.isRemoteBackendEnabled,
                    currentInterval = updateIntervalSeconds,
                    onDismiss = { viewModel.showSettingsDialog(false) },
                    onSave = { url, remoteEnabled, interval ->
                        viewModel.updateBackendConfig(url, remoteEnabled)
                        viewModel.setUpdateInterval(interval)
                        viewModel.showSettingsDialog(false)
                    }
                )
            }
        }
    }
}
