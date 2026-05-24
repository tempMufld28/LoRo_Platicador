package com.lockchat.app.data.repository

import com.lockchat.app.data.local.dao.ContactoDao
import com.lockchat.app.data.local.entity.ContactoEntity
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactoRepositoryImpl @Inject constructor(
    private val dao: ContactoDao
) : ContactRepository {

    override fun observeAll(): Flow<List<Contact>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun findById(nodeId: String): Contact? =
        dao.findById(nodeId)?.toDomain()

    override suspend fun addContact(contact: Contact): Result<Unit> = runCatching {
        dao.insert(contact.toEntity())
    }

    override suspend fun deleteContact(nodeId: String): Result<Unit> = runCatching {
        dao.deleteById(nodeId)
    }

    override suspend fun updateOnlineStatus(nodeId: String, isOnline: Boolean) {
        dao.findById(nodeId)?.let { entity ->
            dao.insert(entity.copy(isOnline = isOnline))
        }
    }

    override suspend fun updateLastSeen(nodeId: String, timestamp: Long) {
        dao.findById(nodeId)?.let { entity ->
            dao.insert(entity.copy(lastSeen = timestamp))
        }
    }

    // ── Mappers ──────────────────────────────────────────────────────

    private fun ContactoEntity.toDomain() = Contact(
        nodeId   = nodeId,
        handle   = handle,
        addedAt  = addedAt,
        lastSeen = lastSeen,
        isOnline = isOnline
    )

    private fun Contact.toEntity() = ContactoEntity(
        nodeId   = nodeId,
        handle   = handle,
        addedAt  = addedAt,
        lastSeen = lastSeen,
        isOnline = isOnline
    )
}
