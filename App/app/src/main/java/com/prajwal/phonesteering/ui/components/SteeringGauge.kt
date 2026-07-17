package com.prajwal.phonesteering.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prajwal.phonesteering.ui.theme.*
import kotlin.math.abs

@Composable
fun SteeringGauge(
    modifier: Modifier = Modifier,
    steeringAngle: Float // -90 to +90
) {
    // Smooth the needle movement
    val animatedAngle by animateFloatAsState(
        targetValue = steeringAngle,
        animationSpec = tween(durationMillis = 100),
        label = "needleAnimation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = size.height * 0.45f
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            // Draw the Gauge Arcs
            val startAngleLeft = 180f
            val sweepAngleLeft = 90f // 180 to 270 (which is top)
            
            val startAngleRight = 270f
            val sweepAngleRight = 90f // 270 to 360

            val arcRect = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)

            // Left Arc (Blue)
            drawArc(
                color = NeonBlue,
                startAngle = startAngleLeft,
                sweepAngle = sweepAngleLeft,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )

            // Right Arc (Orange)
            drawArc(
                color = NeonOrange,
                startAngle = startAngleRight,
                sweepAngle = sweepAngleRight,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcRect,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                blendMode = BlendMode.Screen
            )

            // Draw Tick Marks
            for (i in -90..90 step 5) {
                val isMajor = i % 45 == 0
                val tickLength = if (isMajor) 20.dp.toPx() else 10.dp.toPx()
                val tickColor = if (i < 0) NeonBlue else if (i > 0) NeonOrange else White

                rotate(degrees = i.toFloat(), pivot = center) {
                    drawLine(
                        color = tickColor,
                        start = Offset(center.x, center.y - radius),
                        end = Offset(center.x, center.y - radius + tickLength),
                        strokeWidth = if (isMajor) 4.dp.toPx() else 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw Needle
            rotate(degrees = animatedAngle, pivot = center) {
                val needleColor = if (steeringAngle < 0) NeonBlue else NeonOrange
                val needleBloom = NeonBlue.copy(alpha = 0.5f)
                
                // Bloom
                drawLine(
                    color = needleBloom,
                    start = center,
                    end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                    strokeWidth = 12.dp.toPx(),
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Screen
                )
                // Core
                drawLine(
                    color = needleColor,
                    start = center,
                    end = Offset(center.x, center.y - radius + 10.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw wireframe supercar at the bottom center
            val carWidth = radius * 0.8f
            val carHeight = carWidth * 0.35f
            val carTop = center.y + radius * 0.2f
            
            // Draw Car Path (Abstract Cyberpunk Car)
            val carPath = Path().apply {
                val cx = center.x
                // Roof
                moveTo(cx - carWidth * 0.2f, carTop)
                lineTo(cx + carWidth * 0.2f, carTop)
                // Windshield
                lineTo(cx + carWidth * 0.4f, carTop + carHeight * 0.4f)
                lineTo(cx + carWidth * 0.5f, carTop + carHeight)
                // Bottom
                lineTo(cx - carWidth * 0.5f, carTop + carHeight)
                // Windshield left
                lineTo(cx - carWidth * 0.4f, carTop + carHeight * 0.4f)
                close()
            }

            drawPath(
                path = carPath,
                color = GlowColor,
                style = Stroke(width = 2.dp.toPx())
            )

            // Headlights (Blue left, Orange right)
            val leftHlX = center.x - carWidth * 0.4f
            val rightHlX = center.x + carWidth * 0.4f
            val hlY = carTop + carHeight * 0.7f

            drawLine(
                color = NeonBlue,
                start = Offset(leftHlX, hlY),
                end = Offset(leftHlX + carWidth * 0.1f, hlY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
                blendMode = BlendMode.Screen
            )

            drawLine(
                color = NeonOrange,
                start = Offset(rightHlX, hlY),
                end = Offset(rightHlX - carWidth * 0.1f, hlY),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
                blendMode = BlendMode.Screen
            )
        }

        // Steering Value Text in the center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${abs(steeringAngle).toInt()}°",
                style = CyberpunkTypography.displayLarge,
                color = White
            )
            
            val directionText = when {
                steeringAngle < -1f -> "LEFT"
                steeringAngle > 1f -> "RIGHT"
                else -> "CENTER"
            }
            val directionColor = when {
                steeringAngle < -1f -> NeonBlue
                steeringAngle > 1f -> NeonOrange
                else -> White
            }
            
            Text(
                text = directionText,
                style = CyberpunkTypography.titleLarge,
                color = directionColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}
