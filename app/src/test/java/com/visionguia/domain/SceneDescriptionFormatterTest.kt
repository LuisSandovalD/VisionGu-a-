package com.visionguia.domain

import com.visionguia.domain.vision.SceneDescriptionFormatter
import com.visionguia.domain.vision.SceneObservation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SceneDescriptionFormatterTest {
    private val formatter = SceneDescriptionFormatter()

    @Test fun describesObjectsWithDirectionAndProximity() {
        val result = formatter.describeObservations(listOf(
            SceneObservation(label = "persona", confidence = .92f, left = 105f, top = 20f, right = 220f, bottom = 300f),
            SceneObservation(label = "silla", confidence = .81f, left = 230f, top = 150f, right = 310f, bottom = 310f),
        ))
        assertTrue(result.contains("persona al frente muy cerca"))
        assertTrue(result.contains("silla a la derecha cerca"))
    }

    @Test fun reportsWhenNothingIsRecognized() {
        assertEquals("No identifico objetos adelante en este momento", formatter.describeObservations(emptyList()))
    }
}
