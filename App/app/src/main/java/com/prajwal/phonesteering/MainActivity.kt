package com.prajwal.phonesteering

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prajwal.phonesteering.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null

    private var centerAngle = 0f
    private var isCalibrated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        binding.btnSetCenter.setOnClickListener {
            // The logic for setting the center will happen in the sensor update
            // We just flag that we want to calibrate the next reading
            isCalibrated = false 
        }
    }

    override fun onResume() {
        super.onResume()
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
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
                binding.tvCalibratedStatus.text = "Center calibrated: YES"
            }

            var relativeAngle = currentYawDegrees - centerAngle

            // Normalize to -180 to 180
            if (relativeAngle > 180) relativeAngle -= 360
            if (relativeAngle < -180) relativeAngle += 360

            // We want left to be negative and right to be positive.
            // Azimuth usually increases clockwise. 
            // In many cases, rotating left (counter-clockwise) decreases the angle.
            // Let's verify the display.
            
            binding.tvAngleValue.text = String.format("%.1f°", relativeAngle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this prototype
    }
}