package com.prajwal.phonesteering.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

// Cyberpunk Color Palette
val BackgroundDark = Color(0xFF050505)
val NeonBlue = Color(0xFF00C8FF)
val NeonGreen = Color(0xFF7DFF00)
val NeonRed = Color(0xFFFF295D)
val NeonOrange = Color(0xFFFFAA00)
val NeonPurple = Color(0xFF9B5CFF)
val White = Color(0xFFFFFFFF)
val GlowColor = Color(0x6600C8FF) // Subtle blue glow for general elements

val CyberpunkTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 54.sp,
        letterSpacing = 2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp
    )
)

private val CyberpunkColorScheme = darkColorScheme(
    background = BackgroundDark,
    surface = BackgroundDark,
    primary = NeonBlue,
    secondary = NeonPurple,
    tertiary = NeonGreen,
    error = NeonRed,
    onBackground = White,
    onSurface = White
)

@Composable
fun CyberpunkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberpunkColorScheme,
        typography = CyberpunkTypography,
        content = content
    )
}
