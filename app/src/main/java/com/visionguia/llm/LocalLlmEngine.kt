package com.visionguia.llm

import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class LlmRequest(val prompt: String, val recentContext: List<String> = emptyList())
data class LlmChunk(val text: String, val done: Boolean)

interface LocalLlmEngine {
    suspend fun initialize(): Result<Unit>
    fun generate(request: LlmRequest): Flow<LlmChunk>
    suspend fun cancel()
    suspend fun release()
}

@Singleton
class LiteRtLocalLlmEngine @Inject constructor() : LocalLlmEngine {
    private var modelFile: File? = null
    @Volatile private var engine: Engine? = null
    @Volatile private var conversation: Conversation? = null
    @Volatile private var cancelled = false

    fun configure(file: File) { modelFile = file }

    override suspend fun initialize(): Result<Unit> = runCatching {
        if (engine != null) return@runCatching
        val model = requireNotNull(modelFile) { "No se configuró el modelo LLM local" }
        require(model.isFile) { "No existe el modelo LLM" }
        val localEngine = Engine(EngineConfig(modelPath = model.absolutePath))
        localEngine.initialize()
        engine = localEngine
    }

    override fun generate(request: LlmRequest): Flow<LlmChunk> = flow {
        val localEngine = checkNotNull(engine) { "El LLM local no está cargado" }
        val localConversation = localEngine.createConversation(ConversationConfig(maxOutputToken = 256))
        conversation = localConversation
        cancelled = false
        val prompt = buildString {
            append(SYSTEM_PROMPT)
            if (request.recentContext.isNotEmpty()) append("\nContexto reciente:\n").append(request.recentContext.takeLast(6).joinToString("\n"))
            append("\nUsuario: ").append(request.prompt.take(2_000)).append("\nVisionGuía:")
        }
        val complete = StringBuilder()
        try {
            localConversation.sendMessageAsync(prompt).collect { message ->
                val text = message.contents.contents
                    .filterIsInstance<Content.Text>()
                    .joinToString(separator = "") { it.text }
                if (!cancelled && text.isNotEmpty()) {
                    complete.append(text)
                    emit(LlmChunk(text, false))
                }
            }
            if (!cancelled) emit(LlmChunk(complete.toString().trim(), true))
        } finally {
            localConversation.close()
            if (conversation == localConversation) conversation = null
        }
    }

    override suspend fun cancel() { cancelled = true; conversation?.cancelProcess() }
    override suspend fun release() {
        cancelled = true
        conversation?.close(); conversation = null
        engine?.close(); engine = null
    }

    companion object {
        private const val SYSTEM_PROMPT = """Eres VisionGuía, asistente local para una persona ciega. Responde en español peruano con frases cortas. Prioriza seguridad. No inventes objetos, textos, distancias ni acciones ejecutadas. Si falta información, dilo. Las mediciones visuales son aproximadas. Complementas, no sustituyes, un bastón blanco, perro guía o entrenamiento profesional."""
    }
}
