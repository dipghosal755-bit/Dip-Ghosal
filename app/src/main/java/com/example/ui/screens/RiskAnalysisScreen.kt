package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.risk.RiskEngine
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.SafetyGreen
import com.example.ui.theme.WarningOrange
import com.example.ui.theme.WarningYellow

@Composable
fun RiskAnalysisScreen(modifier: Modifier = Modifier) {
    var testManualSos by remember { mutableStateOf(true) }
    var testFallDetected by remember { mutableStateOf(false) }
    var testNoMovement by remember { mutableStateOf(false) }
    var testRouteDeviation by remember { mutableStateOf(false) }
    var testLateNight by remember { mutableStateOf(false) }
    var testLowBattery by remember { mutableStateOf(false) }

    val evalResult = RiskEngine.calculateRisk(
        isManualSos = testManualSos,
        isFallDetected = testFallDetected,
        isNoMovement = testNoMovement,
        isRouteDeviation = testRouteDeviation,
        isLateNight = testLateNight,
        isLowBattery = testLowBattery
    )

    val levelColor = when (evalResult.level) {
        "CRITICAL" -> EmergencyRed
        "HIGH" -> WarningOrange
        "MEDIUM" -> WarningYellow
        else -> SafetyGreen
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "AI Emergency Risk Engine Architecture",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
        )
        Text(
            text = "Scikit-Learn Synthetic Classifier & Multi-Factor Heuristic Ensemble",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Evaluation Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATED EMERGENCY RISK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${evalResult.score}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = levelColor
                            )
                            Text(
                                text = " / 100",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(levelColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = evalResult.level,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { evalResult.score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = levelColor,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "AI Assessment Reasoning:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = evalResult.explanation,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Operational Recommendation:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = evalResult.recommendation,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Signal Testing Matrix
        Text(
            text = "SIMULATE RISK FACTORS & WEIGHT MATRIX:",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        FactorToggleRow(
            title = "Manual SOS Distress Button",
            points = "+50 pts",
            subtitle = "Direct human intentional alarm trigger",
            checked = testManualSos,
            onCheckedChange = { testManualSos = it },
            tag = "toggle_manual_sos"
        )

        FactorToggleRow(
            title = "Sudden Fall / High-G Impact",
            points = "+25 pts",
            subtitle = "Accelerometer detects >24 m/s² spike followed by stillness",
            checked = testFallDetected,
            onCheckedChange = { testFallDetected = it },
            tag = "toggle_fall"
        )

        FactorToggleRow(
            title = "Prolonged Inactivity / No Motion",
            points = "+15 pts",
            subtitle = "Zero device movement over 90+ seconds",
            checked = testNoMovement,
            onCheckedChange = { testNoMovement = it },
            tag = "toggle_no_movement"
        )

        FactorToggleRow(
            title = "Route Deviation / Boundary Exit",
            points = "+10 pts",
            subtitle = "Outside campus/hostel geofence radius",
            checked = testRouteDeviation,
            onCheckedChange = { testRouteDeviation = it },
            tag = "toggle_route_deviation"
        )

        FactorToggleRow(
            title = "Late Night Isolation (11 PM - 5 AM)",
            points = "+5 pts",
            subtitle = "Higher situational vulnerability window",
            checked = testLateNight,
            onCheckedChange = { testLateNight = it },
            tag = "toggle_late_night"
        )

        FactorToggleRow(
            title = "Critical Low Battery (<15%)",
            points = "+5 pts",
            subtitle = "Device nearing shutdown in active situation",
            checked = testLowBattery,
            onCheckedChange = { testLowBattery = it },
            tag = "toggle_low_battery"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reference Risk Score Intervals
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SPECIFIED RISK SCORE THRESHOLDS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0 – 19: 🟢 LOW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SafetyGreen)
                    Text("20 – 39: 🟡 MEDIUM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningYellow)
                    Text("40 – 69: 🟠 HIGH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningOrange)
                    Text("70+: 🔴 CRITICAL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmergencyRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Required AI Disclaimer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Note: This is an AI-assisted prototype risk model developed for Smart India Hackathon / BCA Project demonstration and not a guaranteed determination of a real emergency.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun FactorToggleRow(
    title: String,
    points: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = points,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
