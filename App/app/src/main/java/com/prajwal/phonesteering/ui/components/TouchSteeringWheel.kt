package com.prajwal.phonesteering.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prajwal.phonesteering.ui.theme.CyberpunkTypography
import com.prajwal.phonesteering.ui.theme.NeonBlue
import kotlin.math.atan2

@Composable
fun TouchSteeringWheel(
    modifier: Modifier = Modifier,
    steeringAngle: Float,
    onSteeringChange: (Float) -> Unit
) {
    // We keep a local state for the dragged angle so that dragging feels instantaneous.
    // The visual angle is animated so snapping back to 0 is smooth.
    val animatedAngle by animateFloatAsState(targetValue = steeringAngle, label = "steeringWheelAnimation")

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "STEERING",
            color = NeonBlue,
            style = CyberpunkTypography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(Color(0xFF111111))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = offset.x - center.x
                            val dy = offset.y - center.y
                            // atan2 returns radians. Multiply by 180/PI for degrees.
                            // 0 degrees is right. We want 0 degrees to be up (top center).
                            val angleRad = atan2(dy.toDouble(), dx.toDouble())
                            var angleDeg = Math.toDegrees(angleRad).toFloat()
                            angleDeg += 90f // Shift so up is 0

                            // Normalize angle between -180 and 180
                            if (angleDeg > 180f) angleDeg -= 360f
                            if (angleDeg < -180f) angleDeg += 360f
                            
                            // Clamp between -90 and 90
                            val clampedAngle = angleDeg.coerceIn(-90f, 90f)
                            onSteeringChange(clampedAngle)
                        },
                        onDrag = { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = change.position.x - center.x
                            val dy = change.position.y - center.y
                            
                            val angleRad = atan2(dy.toDouble(), dx.toDouble())
                            var angleDeg = Math.toDegrees(angleRad).toFloat()
                            angleDeg += 90f

                            // Normalize angle between -180 and 180
                            if (angleDeg > 180f) angleDeg -= 360f
                            if (angleDeg < -180f) angleDeg += 360f
                            
                            val clampedAngle = angleDeg.coerceIn(-90f, 90f)
                            onSteeringChange(clampedAngle)
                        },
                        onDragEnd = {
                            // Snap to center
                            onSteeringChange(0f)
                        },
                        onDragCancel = {
                            // Snap to center
                            onSteeringChange(0f)
                        }
                    )
                }
        ) {
            // Draw the steering wheel
            Canvas(modifier = Modifier.fillMaxSize().rotate(animatedAngle)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 10.dp.toPx()

                // Outer ring
                drawCircle(
                    color = NeonBlue,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 8.dp.toPx())
                )

                // Inner ring
                drawCircle(
                    color = Color.DarkGray,
                    radius = radius * 0.7f,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Spokes
                drawLine(
                    color = NeonBlue,
                    start = center,
                    end = Offset(center.x - radius, center.y),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = NeonBlue,
                    start = center,
                    end = Offset(center.x + radius, center.y),
                    strokeWidth = 6.dp.toPx()
                )
                drawLine(
                    color = NeonBlue,
                    start = center,
                    end = Offset(center.x, center.y + radius),
                    strokeWidth = 6.dp.toPx()
                )

                // Top indicator
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(center.x, center.y - radius)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format("%.1f°", animatedAngle),
            color = NeonBlue,
            style = CyberpunkTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
