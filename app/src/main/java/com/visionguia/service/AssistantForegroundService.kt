package com.visionguia.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.visionguia.R
import com.visionguia.presentation.MainActivity

class AssistantForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL, "VisionGuía activa", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Permite identificar claramente cuándo están activos cámara o micrófono"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        startForeground(1001, NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_visionguia).setContentTitle("VisionGuía está activa")
            .setContentText("Toca para abrir. Cámara y micrófono se detienen desde la aplicación.")
            .setContentIntent(open).setOngoing(true).build())
        return START_NOT_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? = null
    companion object { private const val CHANNEL = "assistant_active" }
}
