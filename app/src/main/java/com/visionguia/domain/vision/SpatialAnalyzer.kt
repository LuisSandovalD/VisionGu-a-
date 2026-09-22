package com.visionguia.domain.vision

import android.graphics.RectF
import com.visionguia.core.model.Direction

data class SpatialPosition(val direction: Direction, val clockHour: Int)

class SpatialAnalyzer {
    fun analyze(box: RectF, frameWidth: Float): SpatialPosition {
        require(frameWidth > 0f)
        return analyzeCenter(box.centerX(), frameWidth)
    }

    fun analyzeCenter(centerX: Float, frameWidth: Float): SpatialPosition {
        require(frameWidth > 0f)
        val x = centerX.coerceIn(0f, frameWidth) / frameWidth
        val direction = when {
            x < 0.36f -> Direction.LEFT
            x > 0.64f -> Direction.RIGHT
            else -> Direction.FRONT
        }
        val hour = (9 + (x * 6).toInt()).coerceIn(9, 14).let { if (it > 12) it - 12 else it }
        return SpatialPosition(direction, hour)
    }
}
