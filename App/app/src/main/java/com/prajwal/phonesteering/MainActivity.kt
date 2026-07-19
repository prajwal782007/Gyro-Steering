package com.prajwal.phonesteering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.prajwal.phonesteering.databinding.ActivityMainBinding
import com.prajwal.phonesteering.utils.AngleUtils

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private var centerAngle = 0f
    private var isCalibrated = false
    private val deadzone = 1.0f // ±1 degree deadzone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on during steering
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationSensor == null) {
            binding.tvCalibratedStatus.text = "Error: Sensor not found!"
        }

        binding.btnSetCenter.setOnClickListener {
            // Flag to recalibrate on next sensor event
            isCalibrated = false 
        }

        binding.btnToggleStream.setOnClickListener {
            val ip = binding.etIpAddress.text.toString().trim()
            val portStr = binding.etPort.text.toString().trim()
            
            if (!isCalibrated) {
                binding.tvNetworkStatus.text = "Set steering center before starting."
                return@setOnClickListener
            }
            if (ip.isEmpty()) {
                binding.tvNetworkStatus.text = "Invalid IP address."
                return@setOnClickListener
            }
            val port = portStr.toIntOrNull()
            if (port == null || port !in 1..65535) {
                binding.tvNetworkStatus.text = "Invalid port."
                return@setOnClickListener
            }

            // Start DashboardActivity
            val intent = android.content.Intent(this, DashboardActivity::class.java).apply {
                putExtra("IP_ADDRESS", ip)
                putExtra("PORT", port)
                putExtra("CENTER_ANGLE", centerAngle)
            }
            startActivity(intent)
        }


        binding.btnSendTestPacket.setOnClickListener {
            val ip = binding.etIpAddress.text.toString().trim()
            val portStr = binding.etPort.text.toString().trim()

            if (ip.isEmpty()) {
                binding.tvNetworkStatus.text = "Invalid IP address"
                return@setOnClickListener
            }

            val port = portStr.toIntOrNull()
            if (port == null || port !in 1..65535) {
                binding.tvNetworkStatus.text = "Invalid port"
                return@setOnClickListener
            }

            binding.tvNetworkStatus.text = "Sending..."

            com.prajwal.phonesteering.network.UdpSender.sendPacketAsync(ip, port, "TEST|HELLO_FROM_PHONE|1") { result ->
                runOnUiThread {
                    if (result.isSuccess) {
                        binding.tvNetworkStatus.text = "Packet sent successfully"
                    } else {
                        binding.tvNetworkStatus.text = "Send failed: ${result.exceptionOrNull()?.message}"
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[0] is azimuth (yaw) in radians
            val currentYawDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()

            if (!isCalibrated) {
                centerAngle = currentYawDegrees
                isCalibrated = true
                binding.tvCalibratedStatus.text = "Center: Calibrated ✓"
            }

            // 1. Calculate relative angle with ±180° wraparound handling via AngleUtils
            val relativeAngle = AngleUtils.calculateRelativeAngle(currentYawDegrees, centerAngle)

            // Apply deadzone and clamp to ±90° via AngleUtils
            val steeringAngle = AngleUtils.getSteeringAngle(relativeAngle, deadzone)

            // Determine direction for UI feedback
            val direction = when {
                steeringAngle < -deadzone -> "LEFT"
                steeringAngle > deadzone -> "RIGHT"
                else -> "CENTER"
            }

            // Update UI (Visual Indicator + Debug info)
            updateUI(currentYawDegrees, relativeAngle, steeringAngle, direction)
        }
    }

    private fun updateUI(rawYaw: Float, relative: Float, steering: Float, direction: String) {
        binding.apply {
            tvSteeringValue.text = String.format("%.1f°", steering)
            tvDirection.text = direction
            
            // 5. Update horizontal steering indicator
            // Progress 0 = -90°, 90 = 0°, 180 = +90°
            pbSteering.progress = (steering + 90).toInt()
            
            // 6. Display debugging information
            tvActualRotation.text = String.format("Actual Rotation: %.1f°", relative)
            tvRawYaw.text = String.format("Raw Yaw: %.1f°", rawYaw)
            
            // Color feedback for direction
            val color = when (direction) {
                "LEFT" -> android.R.color.holo_red_light
                "RIGHT" -> android.R.color.holo_green_light
                else -> android.R.color.holo_blue_dark
            }
            tvDirection.setTextColor(getColor(color))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}