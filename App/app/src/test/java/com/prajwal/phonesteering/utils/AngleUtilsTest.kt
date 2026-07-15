package com.prajwal.phonesteering.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class AngleUtilsTest {

    private val EPSILON = 0.01f

    // ── calculateRelativeAngle ─────────────────────────────────────

    @Test
    fun `wraparound - center at 170, current at -170 gives +20`() {
        val result = AngleUtils.calculateRelativeAngle(-170f, 170f)
        assertEquals(20f, result, EPSILON)
    }

    @Test
    fun `wraparound - center at -170, current at 170 gives -20`() {
        val result = AngleUtils.calculateRelativeAngle(170f, -170f)
        assertEquals(-20f, result, EPSILON)
    }

    @Test
    fun `wraparound - center at 170, current at -165 gives +25`() {
        val result = AngleUtils.calculateRelativeAngle(-165f, 170f)
        assertEquals(25f, result, EPSILON)
    }

    @Test
    fun `wraparound - center at 0, current at 179 gives -181 becomes +179`() {
        val result = AngleUtils.calculateRelativeAngle(179f, 0f)
        assertEquals(179f, result, EPSILON)
    }

    @Test
    fun `wraparound - center at 0, current at -179 gives +179`() {
        val result = AngleUtils.calculateRelativeAngle(-179f, 0f)
        assertEquals(179f, result, EPSILON)
    }

    @Test
    fun `same angle gives zero`() {
        val result = AngleUtils.calculateRelativeAngle(45f, 45f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `simple positive difference`() {
        val result = AngleUtils.calculateRelativeAngle(80f, 35f)
        assertEquals(45f, result, EPSILON)
    }

    @Test
    fun `simple negative difference`() {
        val result = AngleUtils.calculateRelativeAngle(35f, 80f)
        assertEquals(-45f, result, EPSILON)
    }

    @Test
    fun `center at -90, current at 90 gives 0 because they are opposite`() {
        val result = AngleUtils.calculateRelativeAngle(90f, -90f)
        assertEquals(180f, result, EPSILON)
    }

    @Test
    fun `center at 180, current at -180 gives 0`() {
        val result = AngleUtils.calculateRelativeAngle(-180f, 180f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `rotating right from center produces positive`() {
        val result = AngleUtils.calculateRelativeAngle(100f, 80f)
        assertEquals(20f, result, EPSILON)
    }

    @Test
    fun `rotating left from center produces negative`() {
        val result = AngleUtils.calculateRelativeAngle(60f, 80f)
        assertEquals(-20f, result, EPSILON)
    }

    // ── getSteeringAngle ───────────────────────────────────────────

    @Test
    fun `deadzone -0_8 gives zero`() {
        val result = AngleUtils.getSteeringAngle(-0.8f, 1.0f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `deadzone +0_6 gives zero`() {
        val result = AngleUtils.getSteeringAngle(0.6f, 1.0f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `just outside deadzone negative passes through`() {
        val result = AngleUtils.getSteeringAngle(-1.5f, 1.0f)
        assertEquals(-1.5f, result, EPSILON)
    }

    @Test
    fun `just outside deadzone positive passes through`() {
        val result = AngleUtils.getSteeringAngle(2.0f, 1.0f)
        assertEquals(2.0f, result, EPSILON)
    }

    @Test
    fun `clamped -120 becomes -90`() {
        val result = AngleUtils.getSteeringAngle(-120f, 1.0f)
        assertEquals(-90f, result, EPSILON)
    }

    @Test
    fun `clamped +130 becomes +90`() {
        val result = AngleUtils.getSteeringAngle(130f, 1.0f)
        assertEquals(90f, result, EPSILON)
    }

    @Test
    fun `zero relative angle gives zero steering`() {
        val result = AngleUtils.getSteeringAngle(0f, 1.0f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `exactly +90 stays +90`() {
        val result = AngleUtils.getSteeringAngle(90f, 1.0f)
        assertEquals(90f, result, EPSILON)
    }

    @Test
    fun `exactly -90 stays -90`() {
        val result = AngleUtils.getSteeringAngle(-90f, 1.0f)
        assertEquals(-90f, result, EPSILON)
    }

    @Test
    fun `within deadzone at +0_99 gives zero`() {
        val result = AngleUtils.getSteeringAngle(0.99f, 1.0f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `within deadzone at -0_99 gives zero`() {
        val result = AngleUtils.getSteeringAngle(-0.99f, 1.0f)
        assertEquals(0f, result, EPSILON)
    }

    @Test
    fun `default deadzone is 1_0`() {
        assertEquals(0f, AngleUtils.getSteeringAngle(0.5f), EPSILON)
        assertEquals(0.5f, AngleUtils.getSteeringAngle(0.5f, 0.1f), EPSILON)
    }
}
