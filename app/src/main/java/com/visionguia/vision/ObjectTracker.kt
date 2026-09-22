package com.visionguia.vision

import android.graphics.RectF
import com.visionguia.core.model.DetectedObject
import kotlin.math.max
import kotlin.math.min

class ObjectTracker(private val maxAgeMs: Long = 1_500) {
    private data class Track(val id: Long, val label: String, val box: RectF, val lastSeen: Long)
    private val tracks = mutableListOf<Track>()
    private var nextId = 1L

    fun update(detections: List<DetectedObject>, now: Long): List<DetectedObject> {
        tracks.removeAll { now - it.lastSeen > maxAgeMs }
        return detections.map { detection ->
            val match = tracks.filter { it.label == detection.label }.maxByOrNull { iou(it.box, detection.boundingBox) }
                ?.takeIf { iou(it.box, detection.boundingBox) > .3f }
            val id = match?.id ?: nextId++
            if (match != null) tracks.remove(match)
            tracks += Track(id, detection.label, RectF(detection.boundingBox), now)
            detection.copy(id = id)
        }
    }
    private fun iou(a: RectF, b: RectF): Float {
        val area = max(0f, min(a.right, b.right) - max(a.left, b.left)) * max(0f, min(a.bottom, b.bottom) - max(a.top, b.top))
        return area / (a.width() * a.height() + b.width() * b.height() - area).coerceAtLeast(.001f)
    }
}
