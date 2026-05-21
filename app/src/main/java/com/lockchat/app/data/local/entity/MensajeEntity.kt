package com.lockchat.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room: mensajes de una conversación.
 *
 * Fase BLE (actual): content en texto plano — sin cifrado real.
 * Fase LoRa (futura): añadir ciphertext/nonce/firma y migración de DB.
 *
 * direction: "OUT" | "IN"
 * status:    "SENDING" | "SENT" | "DELIVERED" | "FAILED"
 */
@Entity(
    tableName = "mensajes",
    foreignKeys = [
        ForeignKey(
            entity        = ContactoEntity::class,
            parentColumns = ["nodeId"],
            childColumns  = ["contactNodeId"],
            onDelete      = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactNodeId"), Index("timestamp")]
)
data class MensajeEntity(
    @PrimaryKey val msgId: String,
    val contactNodeId: String,
    val direction: String,          // "OUT" | "IN"
    val content: String,            // texto plano (fase BLE)
    val timestamp: Long,            // Unix ms
    val leido: Boolean = false,
    val status: String = "SENDING"  // SENDING | SENT | DELIVERED | FAILED
) {
    override fun equals(other: Any?) = other is MensajeEntity && msgId == other.msgId
    override fun hashCode() = msgId.hashCode()
}
