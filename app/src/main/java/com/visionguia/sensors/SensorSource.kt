package com.visionguia.sensors

import com.visionguia.core.model.SensorReading
import kotlinx.coroutines.flow.Flow

interface SensorSource {
    val readings: Flow<SensorReading>
    suspend fun start()
    suspend fun stop()
}
