package com.visionguia.presentation

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visionguia.core.model.AssistantState
import com.visionguia.core.model.DetectedObject
import com.visionguia.core.model.SpeechPriority
import com.visionguia.capabilities.CapabilityDetector
import com.visionguia.data.local.VisionGuiaDao
import com.visionguia.data.local.EmergencyContactEntity
import com.visionguia.data.model.BundledModelInstaller
import com.visionguia.data.model.ModelCatalog
import com.visionguia.data.model.ModelManager
import com.visionguia.data.settings.SettingsRepository
import com.visionguia.domain.assistant.IntentRouter
import com.visionguia.domain.assistant.UserIntent
import com.visionguia.domain.vision.SceneDescriptionFormatter
import com.visionguia.emergency.EmergencyManager
import com.visionguia.emergency.EmergencyState
import com.visionguia.llm.LlmRequest
import com.visionguia.llm.LiteRtLocalLlmEngine
import com.visionguia.phonecontrol.PhoneControlManager
import com.visionguia.voice.SpeechOrchestrator
import com.visionguia.voice.SpeechRecognitionEngine
import com.visionguia.voice.SpeechRecognitionState
import com.visionguia.voice.VoskSpeechRecognitionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MainUiState(
    val state: AssistantState = AssistantState.IDLE,
    val message: String = "Lista para ayudarte",
    val partialText: String = "",
    val modelStatus: String = "Comprobando modelos",
    val detectionCount: Int = 0,
    val pendingConfirmation: String? = null,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recognition: SpeechRecognitionEngine,
    private val vosk: VoskSpeechRecognitionEngine,
    private val speech: SpeechOrchestrator,
    private val phone: PhoneControlManager,
    private val emergency: EmergencyManager,
    private val settings: SettingsRepository,
    private val dao: VisionGuiaDao,
    private val llm: LiteRtLocalLlmEngine,
    private val bundledModels: BundledModelInstaller,
    private val modelManager: ModelManager,
    private val capabilities: CapabilityDetector,
) : ViewModel() {
    private val router = IntentRouter()
    private val mutable = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = mutable.asStateFlow()
    private var processing: Job? = null
    private var pending: UserIntent? = null
    private var flashOn = false
    private val sceneFormatter = SceneDescriptionFormatter()
    private var describeAheadRequested = false
    private var requestedObject: String? = null
    private var lastSceneSignature = ""
    private var lastSceneAnnouncementAt = 0L

    init {
        val models = File(context.filesDir, "models").apply { mkdirs() }
        val llmFile = File(models, "qwen3-0.6b-int4.litertlm")
        llm.configure(llmFile)
        viewModelScope.launch {
            mutable.value = mutable.value.copy(modelStatus = "Preparando modelos locales")
            bundledModels.install().fold(
                onSuccess = { installed ->
                    vosk.configure(installed.voskDirectory)
                    updateModelStatus(models)
                },
                onFailure = { mutable.value = mutable.value.copy(modelStatus = "Error de modelos incluidos: ${it.message}") },
            )
        }
        viewModelScope.launch {
            recognition.state.collectLatest { state ->
                when (state) {
                    SpeechRecognitionState.Idle -> mutable.value = mutable.value.copy(state = AssistantState.IDLE, partialText = "")
                    SpeechRecognitionState.Listening -> mutable.value = mutable.value.copy(state = AssistantState.LISTENING, message = "Escuchando")
                    is SpeechRecognitionState.Partial -> mutable.value = mutable.value.copy(partialText = state.text)
                    is SpeechRecognitionState.Final -> if (state.text.isNotBlank()) process(state.text)
                    is SpeechRecognitionState.Error -> respond(state.message, AssistantState.ERROR)
                }
            }
        }
        viewModelScope.launch {
            settings.settings.collectLatest { if (!it.onboardingComplete) {
                respond("Bienvenida a VisionGuía. Toca dos veces la pantalla para hablar. Mantén pulsado para repetir. Android necesita que confirmes los permisos. La cámara y el audio se procesan en tu teléfono. VisionGuía complementa, no reemplaza, tu bastón blanco, perro guía o entrenamiento de movilidad.")
                settings.completeOnboarding()
            } }
        }
    }

    private suspend fun updateModelStatus(models: File) {
        mutable.value = mutable.value.copy(modelStatus = buildString {
            append(if (File(models, "vosk-es").isDirectory) "Vosk listo" else "Vosk instalando")
            append(" · ")
            append(if (File(models, "yolov8n.onnx").isFile) "YOLO listo" else "YOLO instalando")
            append(" · ")
            append(if (modelManager.isVerified(ModelCatalog.localLlm)) "LLM listo" else "LLM descargando por Wi-Fi")
        })
    }

    fun startListening() {
        processing?.cancel(); speech.stop()
        viewModelScope.launch { recognition.startListening() }
    }
    fun stopAll() { processing?.cancel(); speech.stop(); viewModelScope.launch { recognition.cancel() }; mutable.value = mutable.value.copy(state = AssistantState.IDLE) }
    fun repeat() = speech.repeat()
    fun onDetections(items: List<DetectedObject>) {
        mutable.value = mutable.value.copy(detectionCount = items.size)
        if (describeAheadRequested) {
            describeAheadRequested = false
            requestedObject = null
            respond(sceneFormatter.describe(items))
            return
        }
        requestedObject?.let { requested ->
            val found = items.firstOrNull { it.label.contains(requested, ignoreCase = true) || requested.contains(it.label, ignoreCase = true) }
            if (found != null) {
                requestedObject = null
                respond(sceneFormatter.describe(listOf(found)), AssistantState.SEARCHING_OBJECT)
                return
            }
        }
        if (mutable.value.state in setOf(
                AssistantState.LISTENING,
                AssistantState.PROCESSING,
                AssistantState.THINKING,
                AssistantState.READING,
                AssistantState.SOS,
            )
        ) return
        if (items.isEmpty()) return
        val signature = sceneFormatter.signature(items)
        if (signature.isBlank()) return
        val now = System.currentTimeMillis()
        if (signature != lastSceneSignature || now - lastSceneAnnouncementAt >= 8_000L) {
            lastSceneSignature = signature
            lastSceneAnnouncementAt = now
            speech.announce(sceneFormatter.describe(items), SpeechPriority.ENVIRONMENT, 2_500L)
        }
    }
    fun onOcrText(text: String) {
        if (text.isNotBlank() && mutable.value.state == AssistantState.READING) respond(text.take(1_500), AssistantState.READING)
        if (text.isNotBlank() && mutable.value.state == AssistantState.ANALYZING_ENVIRONMENT) respond(text, AssistantState.SPEAKING)
    }
    fun saveEmergencyContact(name: String, phoneNumber: String) {
        val phone = phoneNumber.filter { it.isDigit() || it == '+' }
        if (name.isBlank() || !phone.matches(Regex("\\+?[0-9]{6,15}"))) {
            respond("El nombre o teléfono de emergencia no es válido.", AssistantState.ERROR); return
        }
        viewModelScope.launch {
            dao.savePrimaryContact(EmergencyContactEntity(name = name.trim(), phone = phone, isPrimary = true))
            respond("Contacto de emergencia guardado.")
        }
    }

    private fun process(text: String) {
        processing?.cancel()
        processing = viewModelScope.launch {
            mutable.value = mutable.value.copy(state = AssistantState.PROCESSING, message = text, partialText = "")
            val normalized = text.lowercase()
            if (pending != null && normalized in setOf("si", "sí", "confirmo", "confirmar")) {
                val action = pending; pending = null; mutable.value = mutable.value.copy(pendingConfirmation = null)
                if (action is UserIntent.Sos) activateSos()
                return@launch
            }
            if (normalized in setOf("no", "cancelar", "cancela")) { pending = null; emergency.cancel(); respond("Cancelado."); return@launch }
            when (val intent = router.route(text)) {
                UserIntent.Help -> respond("Puedo ayudarte con movilidad, lectura, teléfono, emergencia y configuración. Dime qué categoría quieres conocer.")
                UserIntent.Repeat -> repeat()
                UserIntent.Stop -> stopAll()
                UserIntent.Time -> respond("Son las ${phone.currentTime()}.")
                UserIntent.Date -> respond("Hoy es ${phone.currentDate()}.")
                UserIntent.Battery -> respond(phone.batteryPercent().let { if (it >= 0) "La batería está al $it por ciento." else "No pude consultar la batería." })
                UserIntent.Flashlight -> phone.setFlashlight(!flashOn).fold({ flashOn = !flashOn; respond(if (flashOn) "Linterna encendida." else "Linterna apagada.") }, { respond(it.message ?: "No pude cambiar la linterna.", AssistantState.ERROR) })
                UserIntent.WalkMode -> respond("Modo caminar activo. Mantén también tu ayuda principal de movilidad.", AssistantState.WALKING)
                UserIntent.DescribeAhead -> {
                    describeAheadRequested = true
                    requestedObject = null
                    respond("Analizando lo que hay adelante", AssistantState.SEARCHING_OBJECT)
                }
                UserIntent.AnalyzeEnvironment -> respond("Analizando ambiente y color...", AssistantState.ANALYZING_ENVIRONMENT)
                UserIntent.ReadText -> respond("Apunta la cámara al texto. El lector local se activará cuando la cámara esté autorizada.", AssistantState.READING)
                UserIntent.Diagnostics -> respond("Android ${Build.VERSION.RELEASE}. ${mutable.value.modelStatus}. Batería ${phone.batteryPercent()} por ciento. ${capabilities.spokenSummary()}.")
                is UserIntent.FindObject -> {
                    requestedObject = intent.name
                    describeAheadRequested = false
                    respond("Buscaré ${intent.name} y te avisaré cuando lo detecte", AssistantState.SEARCHING_OBJECT)
                }
                is UserIntent.ChangeSpeechRate -> { speech.changeRate(intent.faster); respond(if (intent.faster) "Hablaré más rápido." else "Hablaré más lento.") }
                UserIntent.Sos -> { pending = intent; mutable.value = mutable.value.copy(pendingConfirmation = "SOS"); respond("Voy a activar el SOS. Di confirmo para continuar o cancelar para detenerlo.", AssistantState.SOS) }
                is UserIntent.Conversation -> converse(intent.text)
            }
        }
    }

    private suspend fun converse(text: String) {
        if (llm.initialize().isFailure) { respond("El modelo conversacional local no está instalado. Los comandos básicos siguen disponibles."); return }
        mutable.value = mutable.value.copy(state = AssistantState.THINKING)
        llm.generate(LlmRequest(text)).collect { if (it.done) respond(it.text) }
    }

    private suspend fun activateSos() {
        emergency.activate { state ->
            when (state) {
                is EmergencyState.Countdown -> respond("SOS en ${state.seconds}. Di cancelar para detenerlo.", AssistantState.SOS)
                is EmergencyState.Failed -> respond(state.reason, AssistantState.ERROR)
                EmergencyState.Sent -> respond("Mensaje de emergencia enviado.", AssistantState.SOS)
                EmergencyState.Cancelled -> respond("SOS cancelado.")
                EmergencyState.Idle -> Unit
            }
        }
    }

    private fun respond(text: String, state: AssistantState = AssistantState.SPEAKING) {
        mutable.value = mutable.value.copy(state = state, message = text)
        speech.announce(text, if (state == AssistantState.SOS || state == AssistantState.ERROR) SpeechPriority.DANGER else SpeechPriority.CONVERSATION, 0)
    }
}
