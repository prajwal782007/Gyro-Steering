package com.prajwal.phonesteering.utils

import kotlin.math.abs

object AngleUtils {

    /**
     * Calculates the shortest difference between two angles in degrees.
     * Returns a value between -180 and 180.
     */
    fun calculateRelativeAngle(currentAngle: Float, centerAngle: Float): Float {
        var difference = currentAngle - centerAngle
        
        if (difference > 180f) {
            difference -= 360f
        }
        if (difference < -180f) {
            difference += 360f
        }
        
        return difference
    }

    /**
     * Clamps the angle to [-90, 90] and applies a deadzone around 0.
     */
    fun getSteeringAngle(relativeAngle: Float, deadzone: Float = 1.0f): Float {
        return if (abs(relativeAngle) < deadzone) {
            0.0f
        } else {
            relativeAngle.coerceIn(-90f, 90f)
        }
    }
}