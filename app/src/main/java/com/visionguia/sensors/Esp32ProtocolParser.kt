package com.visionguia.sensors

import com.visionguia.core.model.SensorReading
import com.visionguia.core.model.SensorType
import org.json.JSONObject

class Esp32ProtocolParser {
    fun parse(message: String, sourceId: String, now: Long = System.currentTimeMillis()): Result<SensorReading> = runCatching {
        require(message.length <= 512) { "Mensaje demasiado grande" }
        val json = JSONObject(message)
        require(json.optInt("v") == 1) { "Versión de protocolo no compatible" }
        val type = SensorType.valueOf(json.getString("type").uppercase())
        val angle = json.getDouble("angle").toFloat()
        val distance = if (json.isNull("distanceCm")) null else json.getDouble("distanceCm").toFloat()
        val confidence = json.optDouble("confidence", 1.0).toFloat()
        require(angle in -180f..180f && (distance == null || distance in 1f..1_000f) && confidence in 0f..1f)
        SensorReading(type, angle, distance, confidence, json.optLong("timestamp", now), sourceId)
    }
}
