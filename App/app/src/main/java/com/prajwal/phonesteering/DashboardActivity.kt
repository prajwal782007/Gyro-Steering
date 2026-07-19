package com.prajwal.phonesteering

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.prajwal.phonesteering.ui.DashboardScreen
import com.prajwal.phonesteering.ui.theme.CyberpunkTheme

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve intent extras
        val ip = intent.getStringExtra("IP_ADDRESS") ?: ""
        val port = intent.getIntExtra("PORT", 5005)
        val centerAngle = intent.getFloatExtra("CENTER_ANGLE", 0f)

        setContent {
            CyberpunkTheme {
                DashboardScreen(
                    onControl1Click = {
                        val intent = Intent(this, SteeringActivity::class.java).apply {
                            putExtra("IP_ADDRESS", ip)
                            putExtra("PORT", port)
                            putExtra("CENTER_ANGLE", centerAngle)
                        }
                        startActivity(intent)
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }
}
