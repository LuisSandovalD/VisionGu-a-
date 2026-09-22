package com.visionguia.voice

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoskSpeechRecognitionEngine @Inject constructor() : SpeechRecognitionEngine, RecognitionListener {
    private val mutable = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    override val state: StateFlow<SpeechRecognitionState> = mutable
    private var modelPath: String? = null
    private var model: Model? = null
    private var service: SpeechService? = null

    fun configure(path: File) { modelPath = path.absolutePath }

    override suspend fun startListening() = withContext(Dispatchers.IO) {
        val path = modelPath
        if (path == null || !File(path).isDirectory) {
            mutable.value = SpeechRecognitionState.Error("Falta el modelo Vosk en el dispositivo")
            return@withContext
        }
        runCatching {
            val localModel = model ?: Model(path).also { model = it }
            val recognizer = Recognizer(localModel, 16_000f)
            service?.stop()
            service = SpeechService(recognizer, 16_000f).also { it.startListening(this@VoskSpeechRecognitionEngine) }
            mutable.value = SpeechRecognitionState.Listening
        }.onFailure { mutable.value = SpeechRecognitionState.Error(it.message ?: "No se pudo iniciar Vosk") }
        Unit
    }

    override suspend fun stopListening() = withContext(Dispatchers.IO) {
        service?.stop(); service = null
        if (mutable.value is SpeechRecognitionState.Listening) mutable.value = SpeechRecognitionState.Idle
    }

    override suspend fun cancel() = withContext(Dispatchers.IO) {
        service?.cancel(); service = null; mutable.value = SpeechRecognitionState.Idle
    }

    override fun onPartialResult(hypothesis: String?) { jsonText(hypothesis)?.let { mutable.value = SpeechRecognitionState.Partial(it) } }
    override fun onResult(hypothesis: String?) { jsonText(hypothesis)?.takeIf(String::isNotBlank)?.let { mutable.value = SpeechRecognitionState.Final(it) } }
    override fun onFinalResult(hypothesis: String?) { onResult(hypothesis); service = null }
    override fun onError(exception: Exception?) { mutable.value = SpeechRecognitionState.Error(exception?.message ?: "Error de reconocimiento") }
    override fun onTimeout() { service = null; mutable.value = SpeechRecognitionState.Idle }
    private fun jsonText(value: String?): String? = value?.let { runCatching { JSONObject(it).optString("text").ifBlank { JSONObject(it).optString("partial") } }.getOrNull() }
}
