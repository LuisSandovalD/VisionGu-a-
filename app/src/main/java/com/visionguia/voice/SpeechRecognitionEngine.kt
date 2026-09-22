package com.visionguia.voice

import kotlinx.coroutines.flow.StateFlow

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data object Listening : SpeechRecognitionState
    data class Partial(val text: String) : SpeechRecognitionState
    data class Final(val text: String) : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}

interface SpeechRecognitionEngine {
    val state: StateFlow<SpeechRecognitionState>
    suspend fun startListening()
    suspend fun stopListening()
    suspend fun cancel()
}

interface VadEngine {
    val voiceActive: StateFlow<Boolean>
    fun processPcm16(samples: ShortArray)
    fun reset()
}
