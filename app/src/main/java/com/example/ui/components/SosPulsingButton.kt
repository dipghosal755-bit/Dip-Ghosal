package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedDark
import com.example.ui.theme.EmergencyRedLight

@Composable
fun SosPulsingButton(
    isEmergencyActive: Boolean,
    onSosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEmergencyActive) 1.35f else 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isEmergencyActive) 900 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale_1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isEmergencyActive) 1.55f else 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isEmergencyActive) 900 else 1800, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale_2"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isEmergencyActive) 900 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(240.dp)
    ) {
        // Outer Radar Pulse 2
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(pulseScale2)
                .clip(CircleShape)
                .background(
                    if (isEmergencyActive) EmergencyRedLight.copy(alpha = pulseAlpha * 0.7f)
                    else EmergencyRed.copy(alpha = pulseAlpha * 0.4f)
                )
        )

        // Outer Radar Pulse 1
        Box(
            modifier = Modifier
                .size(195.dp)
                .scale(pulseScale1)
                .clip(CircleShape)
                .background(
                    if (isEmergencyActive) EmergencyRed.copy(alpha = pulseAlpha)
                    else EmergencyRedDark.copy(alpha = pulseAlpha * 0.6f)
                )
        )

        // Main Solid Red SOS Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .shadow(elevation = if (isEmergencyActive) 24.dp else 14.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isEmergencyActive) listOf(
                            Color(0xFFFF3D00),
                            EmergencyRed,
                            Color(0xFF8B0000)
                        ) else listOf(
                            EmergencyRedLight,
                            EmergencyRed,
                            EmergencyRedDark
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    color = if (isEmergencyActive) Color.White else Color(0xFFFFCDD2),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 90.dp),
                    onClick = onSosClick
                )
                .testTag("sos_button")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isEmergencyActive) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Active Emergency",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Text(
                    text = if (isEmergencyActive) "ACTIVE" else "PRESS FOR HELP",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
