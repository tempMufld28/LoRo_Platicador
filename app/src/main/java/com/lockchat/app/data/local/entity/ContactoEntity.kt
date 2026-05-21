package com.lockchat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room: contactos de la red mesh.
 *
 * Fase BLE (actual): solo nodeId + handle. Sin claves criptográficas.
 * Fase LoRa (futura): añadir pubEd25519 y pubX25519 para cifrado E2E.
 *
 * nodeId: 8 chars hex (ej. "f1a2b3c4"), único por usuario.
 * handle: nombre público en la red.
 */
@Entity(tableName = "contactos")
data class ContactoEntity(
    @PrimaryKey val nodeId: String,
    val handle: String,
    val addedAt: Long = System.currentTimeMillis(),
    val lastSeen: Long? = null,
    val isOnline: Boolean = false
) {
    override fun equals(other: Any?) = other is ContactoEntity && nodeId == other.nodeId
    override fun hashCode() = nodeId.hashCode()
}
