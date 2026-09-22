package com.visionguia.model

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.visionguia.llm.LiteRtLocalLlmEngine
import com.visionguia.llm.LlmRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class LlmInitializationInstrumentedTest {
    @Test fun verifiedQwenLiteRtModelInitializes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val model = File(context.filesDir, "models/qwen3-0.6b-int4.litertlm")
        assumeTrue("El modelo se inyecta solo para la prueba física de integración", model.isFile)
        val engine = LiteRtLocalLlmEngine().apply { configure(model) }
        withTimeout(120_000) { engine.initialize().getOrThrow() }
        val response = withTimeout(120_000) {
            engine.generate(LlmRequest("Responde en una palabra: listo")).first { it.done }
        }
        assertTrue(response.text.isNotBlank())
        engine.release()
    }
}
