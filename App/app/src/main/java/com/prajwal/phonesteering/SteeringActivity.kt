package com.prajwal.phonesteering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.prajwal.phonesteering.network.UdpStreamer
import com.prajwal.phonesteering.ui.SteeringScreen
import com.prajwal.phonesteering.ui.theme.CyberpunkTheme
import com.prajwal.phonesteering.utils.AngleUtils

class SteeringActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private var centerAngle = 0f
    private var setCenterRequested = false
    private val deadzone = 1.0f // ±1 degree deadzone

    // Compose States
    private var steeringAngleState = mutableStateOf(0f)
    private var throttleState = mutableStateOf(0f)
    private var brakeState = mutableStateOf(0f)
    private var networkStatusState = mutableStateOf("CONNECTING...")
    private var networkIpPortState = mutableStateOf("")
    private var packetsPerSecState = mutableStateOf(100) // Mock for now
    private var latencyState = mutableStateOf(5) // Mock for now
    private var sequenceNumberState = mutableStateOf(0) // Mock for now

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on during steering
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Retrieve intent extras
        val ip = intent.getStringExtra("IP_ADDRESS") ?: ""
        val port = intent.getIntExtra("PORT", 5005)
        centerAngle = intent.getFloatExtra("CENTER_ANGLE", 0f)
        val layoutType = intent.getIntExtra("LAYOUT_TYPE", 1)

        networkIpPortState.value = "$ip:$port"

        // Init sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        // Start streaming
        if (ip.isNotEmpty()) {
            UdpStreamer.startStreaming(ip, port) { status ->
                runOnUiThread {
                    networkStatusState.value = status
                }
            }
        } else {
            networkStatusState.value = "ERROR: INVALID IP"
        }

        setContent {
            CyberpunkTheme {
                val steering by steeringAngleState
                val throttle by throttleState
                val brake by brakeState
                val netStatus by networkStatusState
                val netIpPort by networkIpPortState
                val packets by packetsPerSecState
                val latency by latencyState
                val seqNum by sequenceNumberState

                SteeringScreen(
                    layoutType = layoutType,
                    steeringAngle = steering,
                    throttle = throttle,
                    brake = brake,
                    networkStatus = netStatus,
                    networkIpPort = netIpPort,
                    packetsPerSec = packets,
                    latency = latency,
                    sequenceNumber = seqNum,
                    onSetCenterClick = { setCenterRequested = true },
                    onExitClick = { finish() },
                    onDecelerateClick = {
                        throttleState.value = 0f
                        UdpStreamer.setThrottle(0f)
                        brakeState.value = 0f
                        UdpStreamer.setBrake(0f)
                    },
                    onThrottleChange = {
                        throttleState.value = it
                        UdpStreamer.setThrottle(it)
                    },
                    onBrakeChange = {
                        brakeState.value = it
                        UdpStreamer.setBrake(it)
                    }
                )
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
        throttleState.value = 0f
        brakeState.value = 0f
        UdpStreamer.setThrottle(0f)
        UdpStreamer.setBrake(0f)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[0] is azimuth (yaw) in radians
            val currentYawDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()

            if (setCenterRequested) {
                centerAngle = currentYawDegrees
                setCenterRequested = false
            }

            // Calculate relative angle with ±180° wraparound handling via AngleUtils
            val relativeAngle = AngleUtils.calculateRelativeAngle(currentYawDegrees, centerAngle)

            // Apply deadzone and clamp to ±90° via AngleUtils
            val steeringAngle = AngleUtils.getSteeringAngle(relativeAngle, deadzone)

            // Update state
            steeringAngleState.value = steeringAngle

            // Feed the final clamped steering angle to the UdpStreamer
            UdpStreamer.setSteeringAngle(steeringAngle)
            sequenceNumberState.value = (sequenceNumberState.value + 1) % 100000
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        throttleState.value = 0f
        brakeState.value = 0f
        UdpStreamer.setThrottle(0f)
        UdpStreamer.setBrake(0f)
        UdpStreamer.stopStreaming()
    }
}
