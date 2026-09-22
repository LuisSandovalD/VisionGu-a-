package com.visionguia.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt

class EnergyVadEngine(
    private val threshold: Double = 700.0,
    private val silenceFrames: Int = 18,
) : VadEngine {
    private val mutable = MutableStateFlow(false)
    override val voiceActive: StateFlow<Boolean> = mutable
    private var silent = 0

    override fun processPcm16(samples: ShortArray) {
        if (samples.isEmpty()) return
        val rms = sqrt(samples.sumOf { it.toDouble() * it } / samples.size)
        if (rms >= threshold) {
            silent = 0
            mutable.value = true
        } else if (++silent >= silenceFrames) mutable.value = false
    }

    override fun reset() { silent = 0; mutable.value = false }
}
