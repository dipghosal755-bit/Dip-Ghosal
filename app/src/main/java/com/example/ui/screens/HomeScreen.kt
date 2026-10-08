package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactEntity
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import com.example.location.Coordinates
import com.example.risk.RiskAssessmentResult
import com.example.ui.components.ContactsSection
import com.example.ui.components.LiveMapView
import com.example.ui.components.LocationCard
import com.example.ui.components.RiskCard
import com.example.ui.components.SosPulsingButton
import com.example.ui.theme.EmergencyRed

@Composable
fun HomeScreen(
    activeEmergency: EmergencyEntity?,
    currentCoordinates: Coordinates,
    breadcrumbs: List<LocationHistoryEntity>,
    riskResult: RiskAssessmentResult,
    contacts: List<ContactEntity>,
    isInsideSafetyZone: Boolean,
    nearestZoneName: String,
    isTrackingActive: Boolean,
    broadcastCount: Int,
    intervalSeconds: Int,
    onSosClick: () -> Unit,
    onCancelSos: (String) -> Unit,
    onAddContactClick: () -> Unit,
    onDeleteContact: (Long) -> Unit,
    onPreviewBroadcastClick: () -> Unit,
    onSimulateDeviation: () -> Unit,
    onResetLocation: () -> Unit,
    onSimulateFall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEmergencyActive = activeEmergency != null && activeEmergency.status == "ACTIVE"
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active SOS Banner
        AnimatedVisibility(
            visible = isEmergencyActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EmergencyRed)
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🚨 SOS ACTIVE - DISPATCH ALERTED",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Live location streaming every ${intervalSeconds}s to responders",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Button(
                        onClick = { activeEmergency?.emergencyId?.let { onCancelSos(it) } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = EmergencyRed
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_cancel_sos")
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Center Piece: Large Circular Pulsing SOS Button
        Spacer(modifier = Modifier.height(10.dp))

        SosPulsingButton(
            isEmergencyActive = isEmergencyActive,
            onSosClick = {
                if (isEmergencyActive) {
                    activeEmergency?.emergencyId?.let { onCancelSos(it) }
                } else {
                    onSosClick()
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isEmergencyActive) "Tap again or press Cancel to deactivate emergency"
                   else "Tap button to immediately trigger AI emergency SOS",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tactical Live Map View
        LiveMapView(
            currentCoordinates = currentCoordinates,
            breadcrumbs = breadcrumbs,
            isEmergencyActive = isEmergencyActive,
            isInsideSafetyZone = isInsideSafetyZone
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Location Status & Geofence Card
        LocationCard(
            coordinates = currentCoordinates,
            nearestZoneName = nearestZoneName,
            isInsideZone = isInsideSafetyZone,
            isTrackingActive = isTrackingActive,
            broadcastCount = broadcastCount,
            intervalSeconds = intervalSeconds,
            onSimulateDeviation = onSimulateDeviation,
            onResetLocation = onResetLocation,
            onSimulateFall = onSimulateFall
        )

        Spacer(modifier = Modifier.height(16.dp))

        // AI Risk Engine Assessment Card
        RiskCard(riskResult = riskResult)

        Spacer(modifier = Modifier.height(16.dp))

        // Emergency Contacts Section
        ContactsSection(
            contacts = contacts,
            onAddContactClick = onAddContactClick,
            onDeleteContact = onDeleteContact,
            onPreviewBroadcastClick = onPreviewBroadcastClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
