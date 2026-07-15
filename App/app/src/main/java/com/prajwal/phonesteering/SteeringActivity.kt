package com.prajwal.phonesteering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.prajwal.phonesteering.controls.AnalogThrottleController
import com.prajwal.phonesteering.databinding.ActivitySteeringBinding
import com.prajwal.phonesteering.network.UdpStreamer
import com.prajwal.phonesteering.utils.AngleUtils

class SteeringActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivitySteeringBinding
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private lateinit var throttleController: AnalogThrottleController

    private var centerAngle = 0f
    private val deadzone = 1.0f // ±1 degree deadzone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySteeringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on during steering
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Retrieve intent extras
        val ip = intent.getStringExtra("IP_ADDRESS") ?: ""
        val port = intent.getIntExtra("PORT", 5005)
        centerAngle = intent.getFloatExtra("CENTER_ANGLE", 0f)

        // Init throttle controller
        val maxTravelPx = 200f * resources.displayMetrics.density
        throttleController = AnalogThrottleController(maxTravelPx) { throttle ->
            UdpStreamer.setThrottle(throttle)
            runOnUiThread {
                binding.tvThrottleValue.text = "${(throttle * 100).toInt()}%"
                binding.pbThrottle.progress = (throttle * 100).toInt()
            }
        }
        binding.flAcceleratorZone.setOnTouchListener(throttleController)

        // Init sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        binding.btnStopStream.setOnClickListener {
            finish() // This will trigger onDestroy and stop streaming
        }

        // Start streaming
        if (ip.isNotEmpty()) {
            UdpStreamer.startStreaming(ip, port) { status ->
                runOnUiThread {
                    binding.tvNetworkStatus.text = status
                }
            }
        } else {
            binding.tvNetworkStatus.text = "Error: Invalid IP"
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
        throttleController.resetThrottle()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[0] is azimuth (yaw) in radians
            val currentYawDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()

            // Calculate relative angle with ±180° wraparound handling via AngleUtils
            val relativeAngle = AngleUtils.calculateRelativeAngle(currentYawDegrees, centerAngle)

            // Apply deadzone and clamp to ±90° via AngleUtils
            val steeringAngle = AngleUtils.getSteeringAngle(relativeAngle, deadzone)

            // Feed the final clamped steering angle to the UdpStreamer
            UdpStreamer.setSteeringAngle(steeringAngle)

            // Determine direction for UI feedback
            val direction = when {
                steeringAngle < -deadzone -> "LEFT"
                steeringAngle > deadzone -> "RIGHT"
                else -> "CENTER"
            }

            updateUI(steeringAngle, direction)
        }
    }

    private fun updateUI(steering: Float, direction: String) {
        binding.apply {
            tvSteeringValue.text = String.format("%.1f°", steering)
            tvDirection.text = direction
            
            // Update horizontal steering indicator
            // Progress 0 = -90°, 90 = 0°, 180 = +90°
            pbSteering.progress = (steering + 90).toInt()
            
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

    override fun onDestroy() {
        super.onDestroy()
        throttleController.resetThrottle()
        UdpStreamer.stopStreaming()
    }
}
