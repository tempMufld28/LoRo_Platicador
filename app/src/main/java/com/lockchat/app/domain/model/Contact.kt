package com.lockchat.app.domain.model

/**
 * Contacto en la red mesh.
 *
 * Fase BLE: solo nodeId y handle. isOnline se actualiza en tiempo real
 * desde el TransportManager cuando detecta el dispositivo por BLE advertising.
 *
 * Fase LoRa futura: añadir pubEd25519 / pubX25519 para cifrado E2E.
 */
data class Contact(
    val nodeId: String,
    val handle: String,
    val addedAt: Long        = System.currentTimeMillis(),
    val lastSeen: Long?      = null,
    val isOnline: Boolean    = false
) {
    val initials: String get() = handle.take(2).uppercase()
    val nodeIdFormatted: String get() = nodeId.chunked(2).joinToString(":")

    override fun equals(other: Any?) = other is Contact && nodeId == other.nodeId
    override fun hashCode() = nodeId.hashCode()
}

// ─────────────────────────────────────────────────
// Modelos de presentación (UI layer)
// ─────────────────────────────────────────────────

data class ConversationUi(
    val contactId: String,
    val handle: String,
    val nodeIdFormatted: String,
    val avatarInitials: String,
    val lastMessagePreview: String,
    val lastMessageTime: String,
    val unreadCount: Int,
    val isOnline: Boolean,
    val isBleAvailable: Boolean = false,
    val isLoraAvailable: Boolean = false
)

enum class ChatFilter { TODOS, NO_LEIDOS }

data class NodeUi(
    val nodeId: String,
    val handle: String,
    val isOnline: Boolean
)

data class NodeInfo(
    val protocol: String,
    val freqMhz: String,
    val spreadFactor: String,
    val bandwidth: String,
    val firmwareVersion: String
)
