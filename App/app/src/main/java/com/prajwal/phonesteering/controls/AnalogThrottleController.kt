package com.prajwal.phonesteering.controls

import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class AnalogThrottleController(
    private val maxTravelPx: Float,
    private val onThrottleChanged: (Float) -> Unit
) : View.OnTouchListener {

    private var activePointerId: Int = MotionEvent.INVALID_POINTER_ID
    private var initialTouchY: Float = 0f
    private var currentThrottle: Float = 0f

    // Package-private for testing
    internal fun getActivePointerId() = activePointerId
    internal fun getCurrentThrottle() = currentThrottle

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val action = event.actionMasked

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    activePointerId = event.getPointerId(0)
                    initialTouchY = event.getY(0)
                    updateThrottle(0f)
                    return true
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    val pointerIndex = event.actionIndex
                    activePointerId = event.getPointerId(pointerIndex)
                    initialTouchY = event.getY(pointerIndex)
                    updateThrottle(0f)
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    if (pointerIndex != -1) {
                        val currentY = event.getY(pointerIndex)
                        val travel = initialTouchY - currentY
                        val normalized = travel / maxTravelPx
                        updateThrottle(normalized)
                    } else {
                        resetThrottle()
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (activePointerId != MotionEvent.INVALID_POINTER_ID && activePointerId == event.getPointerId(0)) {
                    resetThrottle()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (activePointerId != MotionEvent.INVALID_POINTER_ID) {
                    val pointerIndex = event.actionIndex
                    val pointerId = event.getPointerId(pointerIndex)
                    if (pointerId == activePointerId) {
                        resetThrottle()
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                resetThrottle()
            }
        }
        return true // Consume events for the touch zone
    }

    fun resetThrottle() {
        activePointerId = MotionEvent.INVALID_POINTER_ID
        updateThrottle(0f)
    }

    private fun updateThrottle(rawNormalized: Float) {
        val clamped = max(0f, min(1f, rawNormalized))
        if (currentThrottle != clamped) {
            currentThrottle = clamped
            onThrottleChanged(currentThrottle)
        }
    }
}
