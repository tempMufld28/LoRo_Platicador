package com.lockchat.app.domain.model

/**
 * Modelo de mensaje para la capa de dominio.
 *
 * Fase BLE: content en texto plano.
 * Fase LoRa futura: content se descifra en memoria desde AES-256-GCM.
 */
data class Message(
    val id: String,
    val contactNodeId: String,
    val content: String,
    val timestamp: Long,
    val direction: Direction,
    val status: MessageStatus
)

enum class Direction {
    OUTGOING,   // mensaje propio enviado a un contacto
    INCOMING    // mensaje recibido de un contacto
}

enum class MessageStatus {
    SENDING,    // en cola / esperando ACK de transporte   (icono: ○)
    SENT,       // entregado al canal BLE/LoRa             (icono: ✓)
    DELIVERED,  // ACK recibido del destinatario           (icono: ✓✓)
    FAILED      // máximo de reintentos alcanzado          (icono: ✗)
}

// ─────────────────────────────────────────────────
// Modelo de presentación (UI layer)
// ─────────────────────────────────────────────────

data class MessageUi(
    val id: String,
    val content: String,
    val timestamp: String,          // formateado: "11:05 a.m."
    val direction: Direction,
    val status: MessageStatus,
    val senderHandle: String?       // null si es OUTGOING propio
)
