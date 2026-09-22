package com.visionguia.voice

import com.visionguia.core.model.SpeechPriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechOrchestrator @Inject constructor(private val synthesis: SpeechSynthesisEngine) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val lastAnnouncements = ConcurrentHashMap<String, Long>()
    private var active: Job? = null
    private var activePriority = SpeechPriority.ENVIRONMENT
    @Volatile var lastMessage: String = ""
        private set

    fun announce(text: String, priority: SpeechPriority, cooldownMs: Long = 4_000) {
        if (text.isBlank()) return
        val now = System.currentTimeMillis()
        if (now - (lastAnnouncements[text] ?: 0L) < cooldownMs) return
        lastAnnouncements[text] = now
        if (active?.isActive == true && priority.rank < activePriority.rank) return
        active?.cancel()
        activePriority = priority
        lastMessage = text
        active = scope.launch { synthesis.speak(text, interrupt = true) }
    }

    fun stop() { active?.cancel(); synthesis.stop() }
    fun repeat() { if (lastMessage.isNotBlank()) announce(lastMessage, SpeechPriority.CONVERSATION, 0) }
    fun changeRate(faster: Boolean) { synthesis.setRate(if (faster) 1.25f else 0.8f) }
}
