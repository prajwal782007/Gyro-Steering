package com.prajwal.phonesteering.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwal.phonesteering.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun ThrottleBrakePanels(
    modifier: Modifier = Modifier,
    isBrake: Boolean,
    value: Float, // 0.0f to 1.0f
    onValueChanged: (Float) -> Unit
) {
    val color = if (isBrake) NeonRed else NeonGreen
    val label = if (isBrake) "BRAKE" else "ACCELERATOR"
    
    // Smooth animation for the fill level
    val animatedValue by animateFloatAsState(targetValue = value, label = "fillAnimation")

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label above
        Text(
            text = "//$label//",
            color = color,
            style = CyberpunkTypography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // The Capsule
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(300.dp)
                .shadow(
                    elevation = (animatedValue * 20).dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = color,
                    ambientColor = color
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF111111))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newValue = 1f - (offset.y / size.height.toFloat())
                            onValueChanged(max(0f, min(1f, newValue)))
                        },
                        onDrag = { change, _ ->
                            val newValue = 1f - (change.position.y / size.height.toFloat())
                            onValueChanged(max(0f, min(1f, newValue)))
                        },
                        onDragEnd = {
                            onValueChanged(0f)
                        },
                        onDragCancel = {
                            onValueChanged(0f)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw carbon fiber pattern (simplified here as dark grid)
                val gridSpacing = 10.dp.toPx()
                for (i in 0 until (canvasWidth / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF1A1A1A),
                        start = Offset(i * gridSpacing, 0f),
                        end = Offset(i * gridSpacing, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                for (i in 0 until (canvasHeight / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF1A1A1A),
                        start = Offset(0f, i * gridSpacing),
                        end = Offset(canvasWidth, i * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // Draw the fill
                val fillHeight = canvasHeight * animatedValue
                val fillTop = canvasHeight - fillHeight
                
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color.copy(alpha = 0.2f)),
                        startY = fillTop,
                        endY = canvasHeight
                    ),
                    topLeft = Offset(0f, fillTop),
                    size = Size(canvasWidth, fillHeight),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )

                // Draw chevron arrows inside
                val arrowCount = 5
                val arrowHeight = canvasHeight / (arrowCount + 1)
                val arrowWidth = canvasWidth * 0.6f
                val arrowXOffset = (canvasWidth - arrowWidth) / 2f

                for (i in 1..arrowCount) {
                    val arrowY = i * arrowHeight
                    
                    // Determine if this arrow should be glowing based on fill value
                    val isGlowing = arrowY >= fillTop

                    val arrowColor = if (isGlowing) color else color.copy(alpha = 0.2f)

                    val path = Path().apply {
                        if (isBrake) {
                            // Downward chevron
                            moveTo(arrowXOffset, arrowY)
                            lineTo(canvasWidth / 2f, arrowY + 20f)
                            lineTo(canvasWidth - arrowXOffset, arrowY)
                        } else {
                            // Upward chevron
                            moveTo(arrowXOffset, arrowY)
                            lineTo(canvasWidth / 2f, arrowY - 20f)
                            lineTo(canvasWidth - arrowXOffset, arrowY)
                        }
                    }

                    drawPath(
                        path = path,
                        color = arrowColor,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Draw Neon Border
                drawRoundRect(
                    color = color,
                    size = size,
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx()),
                    blendMode = BlendMode.Screen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "${(animatedValue * 100).toInt()}%",
            color = color,
            style = CyberpunkTypography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
