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
import com.prajwal.phonesteering.ui.theme.*

@Composable
fun DashboardScreen(
    onControl1Click: () -> Unit,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SELECT CONTROLLER",
                color = White,
                style = CyberpunkTypography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Control 1 Button
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .clip(CutCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .border(2.dp, NeonBlue, CutCornerShape(8.dp))
                    .clickable(onClick = onControl1Click)
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CONTROL 1",
                    color = NeonBlue,
                    style = CyberpunkTypography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Back Button
            Box(
                modifier = Modifier
                    .clip(CutCornerShape(8.dp))
                    .background(Color(0xFF2A0F17))
                    .border(2.dp, NeonRed, CutCornerShape(8.dp))
                    .clickable(onClick = onBackClick)
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "BACK",
                    color = NeonRed,
                    style = CyberpunkTypography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
