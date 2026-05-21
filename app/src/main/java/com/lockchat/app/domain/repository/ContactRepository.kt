package com.lockchat.app.domain.repository

import com.lockchat.app.domain.model.Contact
import kotlinx.coroutines.flow.Flow

/** Gestión de contactos persistidos en Room */
interface ContactRepository {
    fun observeAll(): Flow<List<Contact>>
    suspend fun findById(nodeId: String): Contact?
    suspend fun addContact(contact: Contact): Result<Unit>
    suspend fun deleteContact(nodeId: String): Result<Unit>
    suspend fun updateOnlineStatus(nodeId: String, isOnline: Boolean)
    suspend fun updateLastSeen(nodeId: String, timestamp: Long)
}
