package com.example

import com.example.risk.RiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiskEngineUnitTest {

    @Test
    fun testNominalRiskIsLow() {
        val result = RiskEngine.calculateRisk(
            isManualSos = false,
            isFallDetected = false,
            isNoMovement = false,
            isRouteDeviation = false
        )
        assertEquals(0, result.score)
        assertEquals("LOW", result.level)
    }

    @Test
    fun testManualSosTriggersHighRisk() {
        val result = RiskEngine.calculateRisk(
            isManualSos = true,
            isFallDetected = false,
            isNoMovement = false,
            isRouteDeviation = false
        )
        assertEquals(50, result.score)
        assertEquals("HIGH", result.level)
    }

    @Test
    fun testManualSosPlusFallTriggersCriticalRisk() {
        val result = RiskEngine.calculateRisk(
            isManualSos = true,
            isFallDetected = true,
            isNoMovement = false,
            isRouteDeviation = false
        )
        // 50 + 25 = 75 -> CRITICAL
        assertEquals(75, result.score)
        assertEquals("CRITICAL", result.level)
    }

    @Test
    fun testCompoundScoreClampsTo100() {
        val result = RiskEngine.calculateRisk(
            isManualSos = true,
            isFallDetected = true,
            isNoMovement = true,
            isRouteDeviation = true,
            isLateNight = true,
            isLowBattery = true
        )
        // 50 + 25 + 15 + 10 + 5 + 5 = 110 -> clamped to 100
        assertEquals(100, result.score)
        assertEquals("CRITICAL", result.level)
        assertTrue(result.factors.size >= 4)
    }

    @Test
    fun testRouteDeviationAlone() {
        val result = RiskEngine.calculateRisk(
            isManualSos = false,
            isFallDetected = false,
            isNoMovement = false,
            isRouteDeviation = true
        )
        assertEquals(10, result.score)
        assertEquals("LOW", result.level)
    }
}
