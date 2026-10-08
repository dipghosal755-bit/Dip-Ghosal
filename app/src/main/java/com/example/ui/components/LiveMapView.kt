package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationHistoryEntity
import com.example.location.Coordinates
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SafetyGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveMapView(
    currentCoordinates: Coordinates,
    breadcrumbs: List<LocationHistoryEntity>,
    isEmergencyActive: Boolean,
    isInsideSafetyZone: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_radar")

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_pulse"
    )

    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_sweep"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0C1017))
            .border(
                width = 1.dp,
                color = if (isEmergencyActive) EmergencyRed.copy(alpha = 0.5f) else RadarCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Custom Tactical Canvas Map
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val centerY = height / 2f

            // Tactical Grid lines
            val gridStep = 40f
            var x = 0f
            while (x <= width) {
                drawLine(
                    color = Color(0xFF1B2433),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y <= height) {
                drawLine(
                    color = Color(0xFF1B2433),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // Safety Zone Geofence Circle (Campus perimeter)
            val campusZoneRadius = 85f
            drawCircle(
                color = SafetyGreen.copy(alpha = 0.12f),
                radius = campusZoneRadius,
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = SafetyGreen.copy(alpha = 0.5f),
                radius = campusZoneRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.5f)
            )

            // Radar Sweep line
            val rad = Math.toRadians(sweepAngle.toDouble())
            val sweepX = centerX + (120f * cos(rad)).toFloat()
            val sweepY = centerY + (120f * sin(rad)).toFloat()
            drawLine(
                color = RadarCyan.copy(alpha = 0.25f),
                start = Offset(centerX, centerY),
                end = Offset(sweepX, sweepY),
                strokeWidth = 2f
            )

            // Draw Breadcrumbs path
            if (breadcrumbs.size > 1) {
                val path = Path()
                val sorted = breadcrumbs.takeLast(15)
                val baseLat = sorted.first().latitude
                val baseLng = sorted.first().longitude

                sorted.forEachIndexed { index, loc ->
                    // Normalize relative to center
                    val px = centerX + ((loc.longitude - baseLng) * 120000).toFloat().coerceIn(-centerX + 30, centerX - 30)
                    val py = centerY - ((loc.latitude - baseLat) * 120000).toFloat().coerceIn(-centerY + 30, centerY - 30)

                    if (index == 0) {
                        path.moveTo(px, py)
                    } else {
                        path.lineTo(px, py)
                    }

                    // Breadcrumb dot
                    drawCircle(
                        color = RadarCyan.copy(alpha = 0.6f),
                        radius = 3.5f,
                        center = Offset(px, py)
                    )
                }

                drawPath(
                    path = path,
                    color = RadarCyan.copy(alpha = 0.8f),
                    style = Stroke(width = 2.5f)
                )
            }

            // User Moving Marker (Epicenter)
            val markerColor = if (isEmergencyActive) EmergencyRed else RadarCyan

            // Radiating radar pulse circle
            drawCircle(
                color = markerColor.copy(alpha = (1f - (pulseRadius / 45f)).coerceIn(0f, 0.6f)),
                radius = pulseRadius,
                center = Offset(centerX, centerY)
            )

            // Outer ring
            drawCircle(
                color = Color.White,
                radius = 9f,
                center = Offset(centerX, centerY)
            )

            // Inner core
            drawCircle(
                color = markerColor,
                radius = 6.5f,
                center = Offset(centerX, centerY)
            )
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isEmergencyActive) EmergencyRed else SafetyGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEmergencyActive) "LIVE EMERGENCY SATELLITE RADAR" else "TACTICAL GPS SCANNER",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isInsideSafetyZone) "CAMPUS ZONE" else "ZONE DEVIATION",
                    color = if (isInsideSafetyZone) SafetyGreen else EmergencyRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom HUD Coordinates
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column {
                Text(
                    text = "TARGET: ${String.format("%.5f, %.5f", currentCoordinates.latitude, currentCoordinates.longitude)}",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "BREADCRUMBS: ${breadcrumbs.size} WAYPOINTS RECORDED",
                    color = RadarCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )
            }
        }
    }
}
