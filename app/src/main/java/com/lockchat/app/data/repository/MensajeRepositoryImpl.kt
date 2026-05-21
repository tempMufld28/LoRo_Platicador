package com.lockchat.app.data.repository

import com.lockchat.app.data.local.dao.MensajeDao
import com.lockchat.app.data.local.entity.MensajeEntity
import com.lockchat.app.domain.model.Direction
import com.lockchat.app.domain.model.MessageStatus
import com.lockchat.app.domain.model.MessageUi
import com.lockchat.app.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MensajeRepositoryImpl @Inject constructor(
    private val dao: MensajeDao
) : MessageRepository {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale("es", "MX"))

    override fun getMessages(contactId: String): Flow<List<MessageUi>> =
        dao.observeByContact(contactId).map { list ->
            list.map { it.toUi() }
        }

    override suspend fun sendMessage(contactId: String, content: String): Result<Unit> = runCatching {
        val msg = MensajeEntity(
            msgId         = UUID.randomUUID().toString(),
            contactNodeId = contactId,
            direction     = "OUT",
            content       = content,
            timestamp     = System.currentTimeMillis(),
            status        = "SENDING"
        )
        dao.insert(msg)
    }

    /** Actualizar status de un mensaje (llamado por el transport layer) */
    suspend fun updateStatus(msgId: String, status: MessageStatus) {
        dao.updateStatus(msgId, status.name)
    }

    /** Guardar mensaje INCOMING recibido vía BLE */
    suspend fun saveIncoming(contactNodeId: String, content: String, senderHandle: String) {
        val msg = MensajeEntity(
            msgId         = UUID.randomUUID().toString(),
            contactNodeId = contactNodeId,
            direction     = "IN",
            content       = content,
            timestamp     = System.currentTimeMillis(),
            status        = "DELIVERED",
            leido         = false
        )
        dao.insert(msg)
    }

    override suspend fun retryMessage(messageId: String): Result<Unit> = runCatching {
        dao.updateStatus(messageId, "SENDING")
    }

    override suspend fun deleteMessage(messageId: String) {
        dao.delete(messageId)
    }

    fun observeUnreadCount(contactId: String): Flow<Int> =
        dao.observeUnreadCount(contactId)

    suspend fun markAllRead(contactId: String) {
        dao.markAllRead(contactId)
    }

    // ── Mapper ──────────────────────────────────────────────────────

    private fun MensajeEntity.toUi() = MessageUi(
        id            = msgId,
        content       = content,
        timestamp     = timeFormat.format(Date(timestamp)),
        direction     = if (direction == "OUT") Direction.OUTGOING else Direction.INCOMING,
        status        = MessageStatus.valueOf(status.ifBlank { "SENT" }),
        senderHandle  = null  // se populará desde ContactRepository cuando sea necesario
    )
}
