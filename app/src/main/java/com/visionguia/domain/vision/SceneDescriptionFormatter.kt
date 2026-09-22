package com.visionguia.domain.vision

import com.visionguia.core.model.DetectedObject
import com.visionguia.core.model.Direction

data class SceneObservation(
    val label: String,
    val confidence: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
}

class SceneDescriptionFormatter(
    private val frameWidth: Float = 320f,
    private val frameHeight: Float = 320f,
) {
    private val spatial = SpatialAnalyzer()

    fun describe(items: List<DetectedObject>, maximum: Int = 4): String = describeObservations(
        items.map { SceneObservation(it.label, it.confidence, it.boundingBox.left, it.boundingBox.top, it.boundingBox.right, it.boundingBox.bottom) },
        maximum,
    )

    fun describeObservations(items: List<SceneObservation>, maximum: Int = 4): String {
        val relevant = items
            .filter { it.confidence >= 0.45f }
            .sortedWith(compareByDescending<SceneObservation> { it.height }.thenByDescending { it.confidence })
            .distinctBy { it.label to spatial.analyzeCenter(it.centerX, frameWidth).direction }
            .take(maximum)
        if (relevant.isEmpty()) return "No identifico objetos adelante en este momento"
        val parts = relevant.map { item ->
            val direction = when (spatial.analyzeCenter(item.centerX, frameWidth).direction) {
                Direction.LEFT -> "a la izquierda"
                Direction.FRONT -> "al frente"
                Direction.RIGHT -> "a la derecha"
            }
            val heightRatio = item.height / frameHeight
            val proximity = when {
                heightRatio >= 0.65f -> " muy cerca"
                heightRatio >= 0.38f -> " cerca"
                else -> ""
            }
            "${item.label} $direction$proximity"
        }
        val spoken = if (parts.size == 1) parts.first() else parts.dropLast(1).joinToString(", ") + " y " + parts.last()
        return "Detecto $spoken"
    }

    fun signature(items: List<DetectedObject>): String = signatureObservations(
        items.map { SceneObservation(it.label, it.confidence, it.boundingBox.left, it.boundingBox.top, it.boundingBox.right, it.boundingBox.bottom) },
    )

    fun signatureObservations(items: List<SceneObservation>): String = items
        .filter { it.confidence >= 0.45f }
        .sortedByDescending { it.height }
        .take(4)
        .joinToString("|") { "${it.label}:${spatial.analyzeCenter(it.centerX, frameWidth).direction}" }
}
