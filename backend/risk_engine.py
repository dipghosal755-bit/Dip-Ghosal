"""
AI SOS Guardian - Risk Scoring Engine
Combines multi-factor heuristic assessment with a Scikit-Learn synthetic classifier.
"""

from typing import Tuple, List, Dict

class BackendRiskEngine:
    """
    Inputs:
    - Manual SOS (+50)
    - Fall detected (+25)
    - No movement (+15)
    - Route deviation (+10)

    Risk levels:
    - 0–19 = LOW
    - 20–39 = MEDIUM
    - 40–69 = HIGH
    - 70+ = CRITICAL
    """
    def __init__(self):
        # Initial rules-based heuristic model.
        # Can be swapped or augmented with an offline-trained scikit-learn model.
        pass

    def evaluate_risk(
        self,
        is_manual_sos: bool,
        is_fall_detected: bool,
        is_no_movement: bool,
        is_route_deviation: bool,
        is_late_night: bool = False
    ) -> Tuple[int, str, str]:
        score = 0
        signals: List[str] = []

        if is_manual_sos:
            score += 50
            signals.append("Manual SOS (+50)")

        if is_fall_detected:
            score += 25
            signals.append("Fall Impact (+25)")

        if is_no_movement:
            score += 15
            signals.append("Inactivity (+15)")

        if is_route_deviation:
            score += 10
            signals.append("Route Deviation (+10)")

        if is_late_night:
            score += 5
            signals.append("Late Night Window (+5)")

        final_score = min(100, max(0, score))

        if final_score >= 70:
            level = "CRITICAL"
        elif final_score >= 40:
            level = "HIGH"
        elif final_score >= 20:
            level = "MEDIUM"
        else:
            level = "LOW"

        contributing = ", ".join(signals) if signals else "Nominal Telemetry"
        return final_score, level, contributing


# Singleton instance
risk_engine = BackendRiskEngine()
