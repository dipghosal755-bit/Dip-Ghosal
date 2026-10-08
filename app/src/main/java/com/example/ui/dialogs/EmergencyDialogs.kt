package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.location.Coordinates
import com.example.risk.RiskAssessmentResult
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SafetyGreen

@Composable
fun FallWarningDialog(
    countdownSeconds: Int,
    onDismiss: () -> Unit,
    onEscalateNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Force explicit user choice */ },
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(EmergencyRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Fall Warning",
                    tint = EmergencyRed,
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        title = {
            Text(
                text = "Possible Emergency Detected",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = EmergencyRed
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "A sudden impact or fall pattern was detected by phone motion sensors.",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Are you safe?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Escalating SOS in $countdownSeconds seconds...",
                    fontSize = 13.sp,
                    color = EmergencyRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                modifier = Modifier.testTag("btn_fall_i_am_safe")
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("I Am Safe (Cancel)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onEscalateNow,
                colors = ButtonDefaults.textButtonColors(contentColor = EmergencyRed),
                modifier = Modifier.testTag("btn_fall_escalate_now")
            ) {
                Text("Escalate SOS Now")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Mother") }
    val relations = listOf("Mother", "Father", "Friend", "Guardian", "Campus Security", "Doctor")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contact_phone")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Relationship:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    relations.forEach { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = { relationship = rel },
                            label = { Text(rel, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(name.trim(), phone.trim(), relationship, false)
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                modifier = Modifier.testTag("btn_save_contact")
            ) {
                Text("Save Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BroadcastPreviewDialog(
    userId: String,
    status: String,
    riskResult: RiskAssessmentResult,
    coordinates: Coordinates,
    zoneName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.CrisisAlert, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(32.dp))
        },
        title = {
            Text("Emergency Alert Notification Payload", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "This notification packet is automatically transmitted to emergency responders and contacts during SOS activation:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF10141D))
                        .padding(12.dp)
                ) {
                    Text(
                        text = """
🚨 [AI SOS GUARDIAN ALERT]
User ID: $userId
Status: $status
Risk Level: ${riskResult.level} (${riskResult.score}/100)
GPS: ${String.format("%.5f, %.5f", coordinates.latitude, coordinates.longitude)}
Zone: $zoneName
Telemetry: ${riskResult.factors.filter { it.isPresent }.joinToString { it.title }.ifEmpty { "Manual user distress" }}
Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())}
Live Tracking: Active (10s sync)
                        """.trimIndent(),
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SettingsDialog(
    currentUrl: String,
    isRemoteEnabled: Boolean,
    currentInterval: Int,
    onDismiss: () -> Unit,
    onSave: (url: String, remoteEnabled: Boolean, interval: Int) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    var remoteEnabled by remember { mutableStateOf(isRemoteEnabled) }
    var interval by remember { mutableIntStateOf(currentInterval) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Dns, contentDescription = null) },
        title = { Text("System & Backend Settings", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "FastAPI Backend Connection",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Backend Server URL") },
                    placeholder = { Text("http://10.0.2.2:8000/") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_backend_url")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Connect to Remote FastAPI", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = if (remoteEnabled) "Sends live HTTP requests to FastAPI" else "Using Local Prototype Engine",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = remoteEnabled,
                        onCheckedChange = { remoteEnabled = it },
                        modifier = Modifier.testTag("switch_remote_backend")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Live Tracking GPS Interval:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 30).forEach { sec ->
                        FilterChip(
                            selected = interval == sec,
                            onClick = { interval = sec },
                            label = { Text("${sec}s") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(url, remoteEnabled, interval)
                },
                modifier = Modifier.testTag("btn_save_settings")
            ) {
                Text("Save Configuration")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
