package com.lockchat.app.data.repository

import com.lockchat.app.data.local.dao.SolicitudDao
import com.lockchat.app.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Modelo de dominio para una solicitud de mensaje de un nodo desconocido.
 */
data class Solicitud(
    val nodeId: String,
    val handle: String,
    val lastContent: String,
    val count: Int,
    val firstTimestamp: Long,
    val lastTimestamp: Long
) {
    val nodeIdFormatted: String get() = nodeId.chunked(2).joinToString(":")
    val initials: String get() = handle.take(2).uppercase()
}

/**
 * SolicitudRepository — buzón de mensajes de remitentes desconocidos.
 *
 * Los mensajes BLE cuyo remitente no es un contacto guardado se capturan aquí
 * (en lugar de descartarse) para que el usuario los revise y acepte/rechace.
 */
@Singleton
class SolicitudRepository @Inject constructor(
    private val dao: SolicitudDao
) {
    fun observeAll(): Flow<List<Solicitud>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeCount(): Flow<Int> = dao.observeCount()

    suspend fun saveUnknown(fromNodeId: String, fromHandle: String, content: String) {
        dao.upsert(fromNodeId, fromHandle, content, System.currentTimeMillis())
    }

    suspend fun delete(nodeId: String) = dao.delete(nodeId)

    private fun SolicitudEntity.toDomain() = Solicitud(
        nodeId = nodeId,
        handle = handle,
        lastContent = lastContent,
        count = count,
        firstTimestamp = firstTimestamp,
        lastTimestamp = lastTimestamp
    )
}
