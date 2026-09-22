package com.visionguia.domain.vision

import com.visionguia.core.model.DistanceEstimate
import com.visionguia.core.model.DistanceSource
import com.visionguia.core.model.SensorReading
import kotlin.math.max

class DistanceEstimator {
    fun fromPhysical(readings: List<SensorReading>, targetAngle: Float): DistanceEstimate? = readings
        .filter { it.distanceCm != null && it.confidence >= 0.5f }
        .minByOrNull { kotlin.math.abs(it.angleDegrees - targetAngle) }
        ?.takeIf { kotlin.math.abs(it.angleDegrees - targetAngle) <= 25f }
        ?.let { DistanceEstimate(it.distanceCm!! / 100f, it.confidence, DistanceSource.ESP32) }

    fun visualHeuristic(boxHeightRatio: Float, referenceHeightMeters: Float = 1.7f): DistanceEstimate? {
        if (boxHeightRatio !in 0.03f..1f || referenceHeightMeters <= 0f) return null
        val meters = max(0.3f, 0.72f * referenceHeightMeters / boxHeightRatio)
        return DistanceEstimate(meters, 0.3f, DistanceSource.VISUAL_HEURISTIC)
    }
}
