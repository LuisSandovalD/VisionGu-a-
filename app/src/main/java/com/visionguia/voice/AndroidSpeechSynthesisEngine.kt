package com.visionguia.voice

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface SpeechSynthesisEngine {
    suspend fun speak(text: String, interrupt: Boolean = false): Result<Unit>
    fun stop()
    fun setRate(rate: Float)
    fun setPitch(pitch: Float)
}

@Singleton
class AndroidSpeechSynthesisEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechSynthesisEngine {
    private var tts: TextToSpeech? = null
    private var init: CompletableDeferred<Boolean>? = null
    private var rate = 1f
    private var pitch = 1f

    private suspend fun ensureReady(): Boolean {
        tts?.let { return true }
        val waiter = init ?: CompletableDeferred<Boolean>().also { deferred ->
            init = deferred
            tts = TextToSpeech(context) { status ->
                val engine = tts
                if (status != TextToSpeech.SUCCESS || engine == null) deferred.complete(false)
                else {
                    val locales = listOf(Locale("es", "PE"), Locale.forLanguageTag("es-419"), Locale("es", "ES"))
                    val selected = locales.firstOrNull { engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
                    if (selected != null) engine.language = selected
                    engine.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).build())
                    deferred.complete(selected != null)
                }
            }
        }
        return waiter.await()
    }

    override suspend fun speak(text: String, interrupt: Boolean): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            check(ensureReady()) { "No hay un motor TTS en español disponible" }
            val id = UUID.randomUUID().toString()
            val done = CompletableDeferred<Unit>()
            tts!!.setSpeechRate(rate); tts!!.setPitch(pitch)
            tts!!.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) { if (!done.isCompleted) done.complete(Unit) }
                @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { if (!done.isCompleted) done.completeExceptionally(IllegalStateException("TTS error")) }
            })
            val queue = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            check(tts!!.speak(text, queue, Bundle(), id) == TextToSpeech.SUCCESS)
            done.await()
        }
    }

    override fun stop() { tts?.stop() }
    override fun setRate(rate: Float) { this.rate = rate.coerceIn(0.5f, 2f) }
    override fun setPitch(pitch: Float) { this.pitch = pitch.coerceIn(0.5f, 2f) }
}
