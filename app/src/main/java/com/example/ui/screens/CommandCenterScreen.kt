package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmergencyEntity
import com.example.data.model.LocationHistoryEntity
import com.example.location.Coordinates
import com.example.ui.components.LiveMapView
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommandCenterScreen(
    emergencies: List<EmergencyEntity>,
    currentCoordinates: Coordinates,
    breadcrumbs: List<LocationHistoryEntity>,
    isInsideSafetyZone: Boolean,
    onResolveEmergency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sort emergencies by priority: CRITICAL -> HIGH -> MEDIUM -> LOW
    val priorityWeight = mapOf(
        "CRITICAL" to 4,
        "HIGH" to 3,
        "MEDIUM" to 2,
        "LOW" to 1
    )

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedEmergencyId by remember { mutableStateOf<String?>(null) }
    var dispatchedEmergencies by remember { mutableStateOf(setOf<String>()) }

    val sortedEmergencies = emergencies
        .sortedWith(
            compareByDescending<EmergencyEntity> { it.status == "ACTIVE" }
                .thenByDescending { priorityWeight[it.riskLevel] ?: 0 }
                .thenByDescending { it.riskScore }
                .thenByDescending { it.lastUpdated }
        )
        .filter {
            when (selectedFilter) {
                "ACTIVE" -> it.status == "ACTIVE"
                "CRITICAL" -> it.riskLevel == "CRITICAL"
                "RESOLVED" -> it.status == "RESOLVED" || it.status == "CANCELLED"
                else -> true
            }
        }

    val activeCount = emergencies.count { it.status == "ACTIVE" }
    val criticalCount = emergencies.count { it.riskLevel == "CRITICAL" && it.status == "ACTIVE" }
    val highCount = emergencies.count { it.riskLevel == "HIGH" && it.status == "ACTIVE" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Dashboard Title & Subtitle
        Text(
            text = "AI SOS Command Center",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        )
        Text(
            text = "Real-time Multi-Incident Triage & Responder Dispatch Hub",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Metric Stat Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "ACTIVE",
                count = activeCount,
                color = if (activeCount > 0) EmergencyRed else SafetyGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "CRITICAL",
                count = criticalCount,
                color = EmergencyRed,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "HIGH",
                count = highCount,
                color = WarningOrange,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "TOTAL LOGGED",
                count = emergencies.size,
                color = RadarCyan,
                modifier = Modifier.weight(1.2f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Responder Live Tactical Map
        LiveMapView(
            currentCoordinates = currentCoordinates,
            breadcrumbs = breadcrumbs,
            isEmergencyActive = activeCount > 0,
            isInsideSafetyZone = isInsideSafetyZone,
            modifier = Modifier.testTag("command_center_map")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "ACTIVE", "CRITICAL", "RESOLVED").forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Incident List
        Text(
            text = "PRIORITIZED EMERGENCY QUEUE (${sortedEmergencies.size}):",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (sortedEmergencies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No incidents matching filter. System nominal.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedEmergencies, key = { it.emergencyId }) { emg ->
                    val isDispatched = dispatchedEmergencies.contains(emg.emergencyId)
                    EmergencyIncidentCard(
                        emergency = emg,
                        isDispatched = isDispatched,
                        onDispatch = {
                            dispatchedEmergencies = dispatchedEmergencies + emg.emergencyId
                        },
                        onResolve = { onResolveEmergency(emg.emergencyId) }
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmergencyIncidentCard(
    emergency: EmergencyEntity,
    isDispatched: Boolean,
    onDispatch: () -> Unit,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val levelColor = when (emergency.riskLevel) {
        "CRITICAL" -> EmergencyRed
        "HIGH" -> WarningOrange
        "MEDIUM" -> WarningYellow
        else -> SafetyGreen
    }

    val isActive = emergency.status == "ACTIVE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("incident_card_${emergency.emergencyId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isActive && emergency.riskLevel == "CRITICAL")
            androidx.compose.foundation.BorderStroke(1.5.dp, EmergencyRed)
        else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: User ID + Priority Pill + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isActive) levelColor else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = emergency.userId,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${emergency.emergencyId}]",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(levelColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${emergency.riskLevel} (${emergency.riskScore})",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "INCIDENT TYPE",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = emergency.emergencyType.replace("_", " "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LAST UPDATE",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(emergency.lastUpdated))
                    Text(
                        text = timeStr,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Coordinates
            Text(
                text = "📍 GPS: ${String.format("%.5f° N, %.5f° E", emergency.latitude, emergency.longitude)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (emergency.contributingFactors.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Signals: ${emergency.contributingFactors}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    if (isDispatched) {
                        Text(
                            text = "🚔 Patrol Dispatched",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    } else {
                        OutlinedButton(
                            onClick = onDispatch,
                            modifier = Modifier.testTag("btn_dispatch_${emergency.emergencyId}")
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch Responder", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = onResolve,
                        colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                        modifier = Modifier.testTag("btn_resolve_${emergency.emergencyId}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Resolve", fontSize = 11.sp)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STATUS: ${emergency.status}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
