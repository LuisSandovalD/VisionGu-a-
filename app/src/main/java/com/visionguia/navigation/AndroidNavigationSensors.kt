package com.visionguia.navigation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

data class NavigationFix(val point: GeoPoint, val accuracyMeters: Float, val timestamp: Long)

@Singleton
class AndroidLocationProvider @Inject constructor(@ApplicationContext private val context: Context) {
    @SuppressLint("MissingPermission")
    val fixes: Flow<NavigationFix> = callbackFlow {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            close(SecurityException("Falta ACCESS_FINE_LOCATION")); return@callbackFlow
        }
        val manager = context.getSystemService(LocationManager::class.java)
        val listener = LocationListener { location: Location ->
            trySend(NavigationFix(GeoPoint(location.latitude, location.longitude), location.accuracy, location.time))
        }
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        providers.forEach { manager.requestLocationUpdates(it, 1_000L, 1f, listener) }
        awaitClose { manager.removeUpdates(listener) }
    }
}

@Singleton
class AndroidOrientationProvider @Inject constructor(@ApplicationContext context: Context) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    val azimuthDegrees: Flow<Float> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) { close(IllegalStateException("Sensor de orientación no disponible")); return@callbackFlow }
        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            override fun onSensorChanged(event: SensorEvent) {
                val rotation = FloatArray(9); val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                trySend(((Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f))
            }
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
