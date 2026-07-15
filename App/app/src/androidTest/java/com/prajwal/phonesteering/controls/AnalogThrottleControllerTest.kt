package com.prajwal.phonesteering.controls

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalogThrottleControllerTest {

    private lateinit var controller: AnalogThrottleController
    private var lastThrottle: Float = 0f
    private val maxTravel = 200f

    @Before
    fun setup() {
        controller = AnalogThrottleController(maxTravel) { throttle ->
            lastThrottle = throttle
        }
    }

    private fun createMotionEvent(action: Int, y: Float): MotionEvent {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        return MotionEvent.obtain(downTime, eventTime, action, 100f, y, 0)
    }

    @Test
    fun testNoMovement() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        assertEquals(0.0f, lastThrottle, 0.001f)
    }

    @Test
    fun test25PercentTravel() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        
        // Move up by 50px (50 / 200 = 0.25)
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 450f))
        assertEquals(0.25f, lastThrottle, 0.001f)
    }

    @Test
    fun test50PercentTravel() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        
        // Move up by 100px (100 / 200 = 0.50)
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 400f))
        assertEquals(0.50f, lastThrottle, 0.001f)
    }

    @Test
    fun test100PercentTravel() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        
        // Move up by 200px
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 300f))
        assertEquals(1.0f, lastThrottle, 0.001f)
    }

    @Test
    fun testBeyondMaxTravel() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        
        // Move up by 300px
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 200f))
        assertEquals(1.0f, lastThrottle, 0.001f) // Should clamp to 1.0
    }

    @Test
    fun testMovementDownward() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        
        // Move down by 100px
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 600f))
        assertEquals(0.0f, lastThrottle, 0.001f) // Should clamp to 0.0
    }

    @Test
    fun testReleaseResetsThrottle() {
        val view: View? = null
        controller.onTouch(view!!, createMotionEvent(MotionEvent.ACTION_DOWN, 500f))
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_MOVE, 300f))
        assertEquals(1.0f, lastThrottle, 0.001f)
        
        controller.onTouch(view, createMotionEvent(MotionEvent.ACTION_UP, 300f))
        assertEquals(0.0f, lastThrottle, 0.001f)
    }
}
