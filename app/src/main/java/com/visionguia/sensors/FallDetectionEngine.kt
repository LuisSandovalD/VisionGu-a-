package com.visionguia.sensors

import kotlin.math.abs

enum class FallState { NORMAL, IMPACT, AWAITING_MOTION, POSSIBLE_FALL }

class FallDetectionEngine(
    private val impactG: Float = 2.6f,
    private val stillnessGDelta: Float = .18f,
    private val confirmationMs: Long = 2_500,
) {
    var state = FallState.NORMAL
        private set
    private var impactAt = 0L

    fun update(accelerationG: Float, angularSpeedRadians: Float, timestamp: Long): FallState {
        state = when (state) {
            FallState.NORMAL -> if (accelerationG >= impactG && angularSpeedRadians > 1f) { impactAt = timestamp; FallState.IMPACT } else FallState.NORMAL
            FallState.IMPACT -> FallState.AWAITING_MOTION
            FallState.AWAITING_MOTION -> when {
                timestamp - impactAt > 8_000 -> FallState.NORMAL
                timestamp - impactAt >= confirmationMs && abs(accelerationG - 1f) <= stillnessGDelta && angularSpeedRadians < .2f -> FallState.POSSIBLE_FALL
                else -> FallState.AWAITING_MOTION
            }
            FallState.POSSIBLE_FALL -> FallState.POSSIBLE_FALL
        }
        return state
    }
    fun reset() { state = FallState.NORMAL; impactAt = 0 }
}
