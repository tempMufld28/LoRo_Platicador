package com.lockchat.app.domain.repository

import com.lockchat.app.domain.model.MessageUi
import kotlinx.coroutines.flow.Flow

/** Mensajes de una conversación individual */
interface MessageRepository {
    fun getMessages(contactId: String): Flow<List<MessageUi>>
    suspend fun sendMessage(contactId: String, content: String): Result<String>
    suspend fun retryMessage(messageId: String): Result<Unit>
    suspend fun deleteMessage(messageId: String)
}
