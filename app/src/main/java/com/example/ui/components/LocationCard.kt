package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.location.Coordinates
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.WarningOrange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationCard(
    coordinates: Coordinates,
    nearestZoneName: String,
    isInsideZone: Boolean,
    isTrackingActive: Boolean,
    broadcastCount: Int,
    intervalSeconds: Int,
    onSimulateDeviation: () -> Unit,
    onResetLocation: () -> Unit,
    onSimulateFall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "GPS Location",
                        tint = RadarCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Location Status",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Live Tracking Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isTrackingActive) EmergencyRed.copy(alpha = 0.15f)
                            else SafetyGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isTrackingActive) EmergencyRed else SafetyGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTrackingActive) "LIVE ($intervalSeconds s)" else "STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTrackingActive) EmergencyRed else SafetyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coordinates Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GPS LATITUDE / LONGITUDE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = String.format("%.5f° N, %.5f° E", coordinates.latitude, coordinates.longitude),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ACCURACY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "±${coordinates.accuracy.toInt()}m",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = SafetyGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Safety Zone & Route Deviation Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isInsideZone) Icons.Default.GpsFixed else Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = if (isInsideZone) SafetyGreen else WarningOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isInsideZone) "Safety Zone: $nearestZoneName (Inside Perimeter)"
                           else "Route Deviation: Outside $nearestZoneName (+10 Risk)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isInsideZone) SafetyGreen else WarningOrange
                )
            }

            if (isTrackingActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📡 Broadcasts sent to backend: $broadcastCount updates",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Controls for Hackathon / Demonstration
            Text(
                text = "DEMONSTRATION & SENSOR TESTING:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = onSimulateDeviation,
                    label = { Text("Simulate Deviation", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.AltRoute, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = WarningOrange.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("btn_simulate_deviation")
                )

                AssistChip(
                    onClick = onResetLocation,
                    label = { Text("Reset to Campus", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    modifier = Modifier.testTag("btn_reset_campus")
                )

                AssistChip(
                    onClick = onSimulateFall,
                    label = { Text("Simulate Fall Impact", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = EmergencyRed.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("btn_simulate_fall")
                )
            }
        }
    }
}
