package com.prajwal.phonesteering.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prajwal.phonesteering.ui.components.SteeringGauge
import com.prajwal.phonesteering.ui.components.ThrottleBrakePanels
import com.prajwal.phonesteering.ui.theme.*

@Composable
fun SteeringScreen(
    layoutType: Int = 1,
    steeringAngle: Float,
    throttle: Float,
    brake: Float,
    networkStatus: String,
    networkIpPort: String,
    packetsPerSec: Int,
    latency: Int,
    sequenceNumber: Int,
    onSetCenterClick: () -> Unit,
    onExitClick: () -> Unit,
    onDecelerateClick: () -> Unit,
    onThrottleChange: (Float) -> Unit,
    onBrakeChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Top Navigation Bar
        TopNavBar(
            networkStatus = networkStatus,
            networkIpPort = networkIpPort,
            packetsPerSec = packetsPerSec
        )

        if (layoutType == 1) {
            // Layout 1: Split controls
            // Left Brake Panel
            ThrottleBrakePanels(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 32.dp),
                isBrake = true,
                value = brake,
                onValueChanged = onBrakeChange
            )

            // Right Accelerator Panel
            ThrottleBrakePanels(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 32.dp),
                isBrake = false,
                value = throttle,
                onValueChanged = onThrottleChange
            )

            // Center Area (Gauge + Buttons)
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.5f)
                ) {
                    SteeringGauge(steeringAngle = steeringAngle)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Set As Centre Button
                SetCenterButton(onClick = onSetCenterClick)

                Spacer(modifier = Modifier.height(8.dp))

                // Decelerate Button
                DecelerateButton(onClick = onDecelerateClick)

                Spacer(modifier = Modifier.height(8.dp))

                // Exit Button
                ExitButton(onClick = onExitClick)
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            // Layout 2: Controls on Left, Steering on Right
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 60.dp, bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Brake and Throttle together
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ThrottleBrakePanels(
                        isBrake = true,
                        value = brake,
                        onValueChanged = onBrakeChange
                    )
                    ThrottleBrakePanels(
                        isBrake = false,
                        value = throttle,
                        onValueChanged = onThrottleChange
                    )
                }
                
                // Center Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SetCenterButton(onClick = onSetCenterClick)
                    Spacer(modifier = Modifier.height(8.dp))
                    DecelerateButton(onClick = onDecelerateClick)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExitButton(onClick = onExitClick)
                }

                // Right side: Steering Gauge
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(start = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SteeringGauge(steeringAngle = steeringAngle)
                }
            }
        }

        // Bottom Information Bar
        BottomInfoBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            steeringAngle = steeringAngle,
            throttle = throttle,
            latency = latency,
            sequenceNumber = sequenceNumber
        )
    }
}

@Composable
fun TopNavBar(
    networkStatus: String,
    networkIpPort: String,
    packetsPerSec: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(1.dp, NeonBlue.copy(alpha = 0.5f), CutCornerShape(12.dp))
            .background(Color(0xFF050B14).copy(alpha = 0.8f))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Network Status
        Column {
            Text(text = networkStatus, color = NeonGreen, style = CyberpunkTypography.bodyLarge)
            Text(text = networkIpPort, color = White, style = CyberpunkTypography.labelSmall)
        }

        // Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "GYRO STEERING",
                color = White,
                style = CyberpunkTypography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "TILT. CONTROL. DOMINATE.",
                color = White.copy(alpha = 0.7f),
                style = CyberpunkTypography.labelSmall
            )
        }

        // Packets & Battery
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "PACKETS / SEC", color = White.copy(alpha = 0.7f), style = CyberpunkTypography.labelSmall)
                Text(text = packetsPerSec.toString(), color = NeonBlue, style = CyberpunkTypography.bodyLarge)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "BATTERY", color = White.copy(alpha = 0.7f), style = CyberpunkTypography.labelSmall)
                Text(text = "100%", color = NeonGreen, style = CyberpunkTypography.bodyLarge) // Placeholder
            }
        }
    }
}

@Composable
fun BottomInfoBar(
    modifier: Modifier = Modifier,
    steeringAngle: Float,
    throttle: Float,
    latency: Int,
    sequenceNumber: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(50.dp)
            .border(1.dp, NeonBlue.copy(alpha = 0.5f), CutCornerShape(8.dp))
            .background(Color(0xFF0D0514).copy(alpha = 0.8f))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        InfoPanel("STEERING", String.format("%.1f°", steeringAngle), if (steeringAngle < 0) NeonBlue else NeonOrange)
        InfoPanel("THROTTLE", "${(throttle * 100).toInt()}%", NeonGreen)
        InfoPanel("STATUS", "LIVE CONTROL", NeonGreen)
        InfoPanel("SEQ", sequenceNumber.toString(), NeonBlue)
        InfoPanel("LATENCY", "${latency}ms", NeonBlue)
    }
}

@Composable
fun InfoPanel(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = White.copy(alpha = 0.7f), style = CyberpunkTypography.labelSmall)
        Text(text = value, color = valueColor, style = CyberpunkTypography.bodyLarge)
    }
}

@Composable
fun SetCenterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CutCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, NeonBlue, CutCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 48.dp, vertical = 16.dp)
    ) {
        Text(
            text = "SET AS CENTRE",
            color = White,
            style = CyberpunkTypography.titleLarge,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun DecelerateButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CutCornerShape(8.dp))
            .background(Color(0xFF1A0F00))
            .border(2.dp, NeonOrange, CutCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 8.dp)
    ) {
        Text(
            text = "DECELERATE",
            color = NeonOrange,
            style = CyberpunkTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExitButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CutCornerShape(8.dp))
            .background(Color(0xFF2A0F17))
            .border(2.dp, NeonRed, CutCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp, vertical = 8.dp)
    ) {
        Text(
            text = "EXIT",
            color = NeonRed,
            style = CyberpunkTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
