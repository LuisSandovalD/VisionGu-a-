package com.visionguia.domain

import com.visionguia.core.model.Direction
import com.visionguia.core.model.DistanceSource
import com.visionguia.core.model.SensorReading
import com.visionguia.core.model.SensorType
import com.visionguia.domain.vision.DistanceEstimator
import com.visionguia.domain.vision.SpatialAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionAlgorithmsTest {
    @Test fun mapsHorizontalPosition() {
        val analyzer = SpatialAnalyzer()
        assertEquals(Direction.LEFT, analyzer.analyzeCenter(10f, 100f).direction)
        assertEquals(Direction.FRONT, analyzer.analyzeCenter(50f, 100f).direction)
        assertEquals(Direction.RIGHT, analyzer.analyzeCenter(90f, 100f).direction)
    }
    @Test fun physicalDistanceHasPriorityAndAngleGate() {
        val reading = SensorReading(SensorType.TOF, 5f, 82f, .9f, 100L, "esp32")
        val estimator = DistanceEstimator()
        val estimate = estimator.fromPhysical(listOf(reading), 0f)!!
        assertEquals(DistanceSource.ESP32, estimate.source)
        assertEquals(.82f, estimate.meters, .001f)
        assertNull(estimator.fromPhysical(listOf(reading), 90f))
    }
    @Test fun visualDistanceIsExplicitlyLowConfidence() {
        val estimate = DistanceEstimator().visualHeuristic(.5f)!!
        assertTrue(estimate.confidence < .5f)
        assertTrue(estimate.spokenSpanish().startsWith("distancia") || estimate.spokenSpanish().startsWith("aproximadamente"))
    }
}
