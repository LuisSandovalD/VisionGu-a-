package com.visionguia.capabilities

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.visionguia.core.model.CapabilityStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class DeviceCapability { CAMERA, MICROPHONE, LOCATION, BLUETOOTH_LE, TELEPHONY, ACCELEROMETER, COMPASS }

@Singleton
class CapabilityDetector @Inject constructor(@ApplicationContext private val context: Context) {
    fun detect(): Map<DeviceCapability, CapabilityStatus> {
        val packageManager = context.packageManager
        val sensors = context.getSystemService(SensorManager::class.java)
        fun permission(name: String) = ContextCompat.checkSelfPermission(context, name) == PackageManager.PERMISSION_GRANTED
        fun hardware(feature: String) = packageManager.hasSystemFeature(feature)
        fun protected(feature: String, granted: Boolean) = when {
            !hardware(feature) -> CapabilityStatus.UNSUPPORTED
            !granted -> CapabilityStatus.REQUIRES_PERMISSION
            else -> CapabilityStatus.AVAILABLE
        }
        val bluetoothPermission = Build.VERSION.SDK_INT < 31 || permission(Manifest.permission.BLUETOOTH_CONNECT)
        val bluetoothEnabled = runCatching { context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true }.getOrDefault(false)
        return linkedMapOf(
            DeviceCapability.CAMERA to protected(PackageManager.FEATURE_CAMERA_ANY, permission(Manifest.permission.CAMERA)),
            DeviceCapability.MICROPHONE to protected(PackageManager.FEATURE_MICROPHONE, permission(Manifest.permission.RECORD_AUDIO)),
            DeviceCapability.LOCATION to protected(PackageManager.FEATURE_LOCATION, permission(Manifest.permission.ACCESS_FINE_LOCATION)),
            DeviceCapability.BLUETOOTH_LE to when {
                !hardware(PackageManager.FEATURE_BLUETOOTH_LE) -> CapabilityStatus.UNSUPPORTED
                !bluetoothPermission -> CapabilityStatus.REQUIRES_PERMISSION
                !bluetoothEnabled -> CapabilityStatus.REQUIRES_USER_ACTION
                else -> CapabilityStatus.AVAILABLE
            },
            DeviceCapability.TELEPHONY to protected(PackageManager.FEATURE_TELEPHONY, permission(Manifest.permission.CALL_PHONE) && permission(Manifest.permission.SEND_SMS)),
            DeviceCapability.ACCELEROMETER to if (sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) CapabilityStatus.AVAILABLE else CapabilityStatus.UNSUPPORTED,
            DeviceCapability.COMPASS to if (sensors?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null || sensors?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) CapabilityStatus.AVAILABLE else CapabilityStatus.UNSUPPORTED,
        )
    }

    fun spokenSummary(): String = detect().entries.joinToString("; ") { (capability, status) ->
        "${capability.name.lowercase().replace('_', ' ')}: ${status.name.lowercase().replace('_', ' ')}"
    }
}
