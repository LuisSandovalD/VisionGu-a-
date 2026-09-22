package com.visionguia.sensors

import com.visionguia.core.model.SensorReading
import com.visionguia.core.model.SensorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorsTest {
    @Test fun parsesVersionedEsp32MessagesAndRejectsBadInput() {
        val parser = Esp32ProtocolParser()
        val result = parser.parse("""{"v":1,"type":"tof","angle":-10,"distanceCm":73.5,"confidence":0.91}""", "unit", 10L).getOrThrow()
        assertEquals(SensorType.TOF, result.type)
        assertEquals(73.5f, result.distanceCm!!, .001f)
        assertTrue(parser.parse("""{"v":2,"type":"tof","angle":0,"distanceCm":20}""", "unit").isFailure)
        assertTrue(parser.parse("""{"v":1,"type":"tof","angle":0,"distanceCm":-1}""", "unit").isFailure)
    }
    @Test fun fusesNearbyFreshReadings() {
        val now = 10_000L
        val readings = listOf(
            SensorReading(SensorType.TOF, 1f, 90f, .8f, now, "tof"),
            SensorReading(SensorType.ULTRASONIC, 4f, 100f, .7f, now, "ultrasonic"),
        )
        val fused = SensorFusionEngine().fuse(readings, now = now)
        assertEquals(1, fused.size)
        assertEquals(90f, fused.first().distanceCm, .001f)
        assertTrue(fused.first().confidence > .8f)
    }
    @Test fun fallRequiresImpactRotationAndPostImpactStillness() {
        val engine = FallDetectionEngine(confirmationMs = 2_000)
        assertEquals(FallState.IMPACT, engine.update(3f, 2f, 0L))
        assertEquals(FallState.AWAITING_MOTION, engine.update(1.4f, .5f, 100L))
        assertEquals(FallState.POSSIBLE_FALL, engine.update(1.02f, .05f, 2_100L))
        engine.reset()
        assertEquals(FallState.NORMAL, engine.update(3f, .1f, 3_000L))
    }
}
