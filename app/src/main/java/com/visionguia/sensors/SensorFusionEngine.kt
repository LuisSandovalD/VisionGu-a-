package com.visionguia.sensors

import com.visionguia.core.model.SensorReading

data class FusedObstacle(val angleDegrees: Float, val distanceCm: Float, val confidence: Float, val sources: Set<String>)

class SensorFusionEngine {
    fun fuse(readings: List<SensorReading>, maxAgeMs: Long = 1_500, now: Long = System.currentTimeMillis()): List<FusedObstacle> {
        val valid = readings.filter { it.distanceCm != null && it.confidence > .25f && now - it.timestamp in 0..maxAgeMs }
        return valid.groupBy { ((it.angleDegrees + 15) / 30).toInt() }.values.map { group ->
            val total = group.sumOf { it.confidence.toDouble() }.toFloat()
            FusedObstacle(
                angleDegrees = group.sumOf { (it.angleDegrees * it.confidence).toDouble() }.toFloat() / total,
                distanceCm = group.minOf { it.distanceCm!! },
                confidence = (1f - group.fold(1f) { acc, r -> acc * (1f - r.confidence) }).coerceIn(0f, 1f),
                sources = group.mapTo(mutableSetOf()) { it.sourceId },
            )
        }.sortedBy { it.distanceCm }
    }
}
