package com.visionguia.emergency

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.visionguia.data.local.VisionGuiaDao
import com.visionguia.phonecontrol.PhoneControlManager
import com.visionguia.navigation.AndroidLocationProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

sealed interface EmergencyState {
    data object Idle : EmergencyState
    data class Countdown(val seconds: Int) : EmergencyState
    data class Failed(val reason: String) : EmergencyState
    data object Sent : EmergencyState
    data object Cancelled : EmergencyState
}

@Singleton
class EmergencyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: VisionGuiaDao,
    private val phone: PhoneControlManager,
    private val location: AndroidLocationProvider,
) {
    @Volatile private var cancelled = false

    suspend fun activate(onState: (EmergencyState) -> Unit) {
        cancelled = false
        for (second in 5 downTo 1) { onState(EmergencyState.Countdown(second)); delay(1_000); if (cancelled) { onState(EmergencyState.Cancelled); return } }
        val contact = dao.primaryContact() ?: return onState(EmergencyState.Failed("No hay contacto de emergencia configurado"))
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            onState(EmergencyState.Failed("Falta permiso para enviar SMS")); return
        }
        val fix = withTimeoutOrNull(5_000) { location.fixes.first() }
        val message = fix?.let { "Necesito ayuda. Mi ubicación aproximada: https://maps.google.com/?q=${it.point.latitude},${it.point.longitude}" }
            ?: "Necesito ayuda. No fue posible obtener mi ubicación."
        runCatching { smsManager().sendTextMessage(contact.phone, null, message, null, null) }
            .onSuccess { onState(EmergencyState.Sent); phone.call(contact.phone) }
            .onFailure { onState(EmergencyState.Failed(it.message ?: "No se pudo enviar el SOS")) }
    }
    fun cancel() { cancelled = true }

    @Suppress("DEPRECATION")
    private fun smsManager(): SmsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
        context.getSystemService(SmsManager::class.java)
    } else SmsManager.getDefault()
}
