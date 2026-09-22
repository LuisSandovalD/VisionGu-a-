package com.visionguia.core.model

import android.graphics.RectF

enum class AssistantState { IDLE, LISTENING, PROCESSING, THINKING, SPEAKING, WALKING, NAVIGATING, READING, SEARCHING_OBJECT, ANALYZING_ENVIRONMENT, SOS, ERROR }
enum class SpeechPriority(val rank: Int) { ENVIRONMENT(0), CONVERSATION(1), NAVIGATION(2), DANGER(3) }
enum class Direction { LEFT, FRONT, RIGHT }
enum class DistanceSource { ESP32, TOF, ARCORE_DEPTH, MONOCULAR, VISUAL_HEURISTIC, UNKNOWN }
enum class SensorType { ULTRASONIC, TOF, IMU, CAMERA_DEPTH }
enum class CapabilityStatus { AVAILABLE, LIMITED, REQUIRES_PERMISSION, REQUIRES_USER_ACTION, UNSUPPORTED }
enum class ThermalState { NORMAL, WARM, HOT, CRITICAL }

data class DetectedObject(
    val id: Long? = null,
    val label: String,
    val confidence: Float,
    val boundingBox: RectF,
    val timestamp: Long,
)

data class DistanceEstimate(val meters: Float, val confidence: Float, val source: DistanceSource) {
    init { require(meters >= 0f); require(confidence in 0f..1f) }
    fun spokenSpanish(): String = when {
        confidence < 0.35f -> "distancia incierta"
        meters < 1f -> "aproximadamente ${(meters * 100).toInt()} centímetros"
        else -> "aproximadamente %.1f metros".format(java.util.Locale("es", "PE"), meters)
    }
}

data class SensorReading(
    val type: SensorType,
    val angleDegrees: Float,
    val distanceCm: Float?,
    val confidence: Float,
    val timestamp: Long,
    val sourceId: String,
)

sealed interface AppError {
    val technical: String
    val accessible: String
    val recovery: String
    data class CameraUnavailable(override val technical: String) : AppError {
        override val accessible = "La cámara no está disponible."
        override val recovery = "Comprueba el permiso o reinicia la cámara."
    }
    data class PermissionDenied(val permission: String) : AppError {
        override val technical = "Permission denied: $permission"
        override val accessible = "Falta un permiso de Android."
        override val recovery = "Abre permisos y autoriza solo la función que deseas usar."
    }
    data class ModelMissing(val model: String) : AppError {
        override val technical = "Model missing: $model"
        override val accessible = "Falta el modelo local de $model."
        override val recovery = "Descárgalo desde Modelos cuando tengas conexión."
    }
    data class ModelCorrupted(val model: String) : AppError {
        override val technical = "SHA-256 mismatch: $model"
        override val accessible = "El modelo descargado no superó la verificación."
        override val recovery = "Elimina la descarga y vuelve a intentarlo."
    }
    data class External(override val technical: String, override val accessible: String, override val recovery: String) : AppError
}
