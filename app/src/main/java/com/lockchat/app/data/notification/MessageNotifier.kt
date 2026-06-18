package com.lockchat.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lockchat.app.MainActivity
import com.lockchat.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MessageNotifier — emite notificaciones del sistema cuando llega un mensaje BLE.
 *
 * - Canal de alta prioridad (heads-up, sonido) para mensajes entrantes.
 * - Suprime la notificación si el chat del contacto está abierto en primer plano.
 * - PendingIntent que abre directamente el ChatDetailScreen del contacto.
 * - ID de notificación por contacto (para acumular mensajes de la misma conversación).
 */
@Singleton
class MessageNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "lockchat_messages"
        const val EXTRA_CONTACT_ID = "extra_contact_id"

        // Base para IDs de notificación por contacto (nodeId.hashCode para que sea estable)
        private fun notifIdFor(nodeId: String): Int = 2000 + (nodeId.hashCode() and 0x3FFF)
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /** nodeId del chat abierto actualmente (null si ninguno) */
    private val _activeChatNodeId = MutableStateFlow<String?>(null)
    val activeChatNodeId: StateFlow<String?> = _activeChatNodeId

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Lock-Chat — Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mensajes entrantes de la red Lock-Chat"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** Llamado por ChatDetailViewModel al entrar/salir del detalle de un chat. */
    fun setActiveChat(nodeId: String?) {
        _activeChatNodeId.value = nodeId
        if (nodeId != null) {
            // Cancelar notificaciones del chat que se acaba de abrir
            notificationManager.cancel(notifIdFor(nodeId))
        }
    }

    /**
     * Muestra (o actualiza) la notificación de un mensaje entrante.
     * No hace nada si el chat del contacto está abierto.
     */
    fun showMessageNotification(fromNodeId: String, fromHandle: String, content: String) {
        // No notificar si el usuario está viendo justo este chat
        if (_activeChatNodeId.value == fromNodeId) return

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONTACT_ID, fromNodeId)
        }
        val pendingTap = PendingIntent.getActivity(
            context,
            fromNodeId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(fromHandle)
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(content)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingTap)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notifIdFor(fromNodeId), notification)
    }

    /** Cancela todas las notificaciones de mensajes (al cerrar la app, etc.). */
    fun cancelAll() {
        notificationManager.cancelAll()
    }
}
