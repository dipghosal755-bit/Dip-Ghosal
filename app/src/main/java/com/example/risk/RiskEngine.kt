package com.example.risk

data class RiskFactor(
    val title: String,
    val points: Int,
    val description: String,
    val isPresent: Boolean
)

data class RiskAssessmentResult(
    val score: Int,
    val level: String, // LOW, MEDIUM, HIGH, CRITICAL
    val factors: List<RiskFactor>,
    val explanation: String,
    val recommendation: String,
    val isAiAssisted: Boolean = true
) {
    companion object {
        const val DISCLAIMER = "AI-assisted prototype risk model: Evaluates multiple telemetry factors. Not a certified replacement for official emergency services."
    }
}

object RiskEngine {
    fun calculateRisk(
        isManualSos: Boolean,
        isFallDetected: Boolean,
        isNoMovement: Boolean,
        isRouteDeviation: Boolean,
        isLateNight: Boolean = false,
        isLowBattery: Boolean = false
    ): RiskAssessmentResult {
        val factorList = mutableListOf<RiskFactor>()

        var score = 0

        if (isManualSos) {
            score += 50
            factorList.add(RiskFactor("Manual SOS Trigger", 50, "Direct user activation", true))
        }

        if (isFallDetected) {
            score += 25
            factorList.add(RiskFactor("Sudden Fall / Impact", 25, "G-force anomaly detected by accelerometer", true))
        }

        if (isNoMovement) {
            score += 15
            factorList.add(RiskFactor("Prolonged Inactivity", 15, "No device movement following trigger", true))
        }

        if (isRouteDeviation) {
            score += 10
            factorList.add(RiskFactor("Route Deviation", 10, "Outside designated campus/safe zone boundary", true))
        }

        if (isLateNight) {
            score += 5
            factorList.add(RiskFactor("Late Night Factor", 5, "Activity during isolated night hours", true))
        }

        if (isLowBattery) {
            score += 5
            factorList.add(RiskFactor("Critical Battery Level", 5, "Battery under 15%", true))
        }

        // Clamp between 0 and 100
        val finalScore = score.coerceIn(0, 100)

        val level = when {
            finalScore >= 70 -> "CRITICAL"
            finalScore >= 40 -> "HIGH"
            finalScore >= 20 -> "MEDIUM"
            else -> "LOW"
        }

        val explanation = when {
            finalScore >= 70 -> "Multi-factor high-confidence alert triggered. User activated manual distress with compounded telemetry signals."
            finalScore >= 40 -> "High urgency detected. Active distress signal requires rapid responder triage and contact notification."
            finalScore >= 20 -> "Moderate risk profile. Movement anomaly or perimeter boundary exceeded without confirmed manual distress."
            else -> "Telemetry indicates nominal safety parameters. No active emergency threats flagged."
        }

        val recommendation = when {
            finalScore >= 70 -> "Immediate dispatch of nearest security team and automated alert to all primary emergency contacts."
            finalScore >= 40 -> "Alert responder dashboard, notify emergency contacts, and maintain continuous high-frequency GPS ping."
            finalScore >= 20 -> "Send verification prompt to user: 'Are you safe?'. Monitor for location changes."
            else -> "Normal standby mode. Periodic background check active."
        }

        return RiskAssessmentResult(
            score = finalScore,
            level = level,
            factors = factorList,
            explanation = explanation,
            recommendation = recommendation
        )
    }
}
