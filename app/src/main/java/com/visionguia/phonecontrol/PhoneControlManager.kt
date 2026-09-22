package com.visionguia.phonecontrol

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneControlManager @Inject constructor(@ApplicationContext private val context: Context) {
    fun batteryPercent(): Int = context.getSystemService(BatteryManager::class.java)
        ?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
    fun currentTime(): String = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("H 'con' mm", Locale("es", "PE")))
    fun currentDate(): String = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "PE")))

    fun setFlashlight(enabled: Boolean): Result<Unit> = runCatching {
        val manager = context.getSystemService(CameraManager::class.java)
        val cameraId = manager.cameraIdList.firstOrNull { manager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
            ?: error("El dispositivo no tiene linterna")
        manager.setTorchMode(cameraId, enabled)
    }

    fun call(phone: String): Result<Unit> = runCatching {
        require(phone.matches(Regex("[+0-9 ]{6,20}")))
        check(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) { "Falta permiso para llamar" }
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phone)}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun openApp(packageName: String): Result<Unit> = runCatching {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: error("Aplicación no disponible")
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
