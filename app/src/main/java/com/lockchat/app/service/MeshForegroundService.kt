package com.lockchat.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.lockchat.app.MainActivity
import com.lockchat.app.data.transport.TransportManager
import com.lockchat.app.domain.model.TransportUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * MeshForegroundService — servicio en primer plano que mantiene el transport activo.
 *
 * Inicia BLE advertising + scanning en background.
 * La notificación muestra el transporte activo ("BLE" o "LoRa USB").
 */
@AndroidEntryPoint
class MeshForegroundService : Service() {

    @Inject
    lateinit var transportManager: TransportManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "lockchat_transport"
        const val NOTIF_ID   = 1001
        const val ACTION_STOP = "com.lockchat.app.STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Iniciando..."))
        try {
            transportManager.start()
        } catch (e: Exception) {
            // Defensivo: una excepción síncrona aquí no debe tirar el servicio.
            Log.e("MeshForegroundService", "Error al iniciar TransportManager", e)
        }

        // Actualizar notificación cuando cambia el transporte
        scope.launch {
            transportManager.activeTransport.collectLatest { transport ->
                val label = when (transport) {
                    TransportUiState.LORA_USB       -> "LoRa USB activo"
                    TransportUiState.BLE_DIRECT     -> "BLE activo"
                    TransportUiState.SIN_TRANSPORTE -> "Sin conexión"
                }
                val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                mgr.notify(NOTIF_ID, buildNotification(label))
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            transportManager.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        transportManager.stop()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(status: String): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingTap = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MeshForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Lock-Chat")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setOngoing(true)
            .setContentIntent(pendingTap)
            .addAction(android.R.drawable.ic_delete, "Detener", pendingStop)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lock-Chat — Transporte",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Estado del transporte BLE/LoRa de Lock-Chat"
            setShowBadge(false)
        }
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannel(channel)
    }
}
