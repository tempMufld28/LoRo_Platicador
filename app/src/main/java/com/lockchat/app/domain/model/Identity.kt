package com.lockchat.app.domain.model

/**
 * Identidad local del usuario en la red mesh.
 *
 * nodeId: 4 bytes aleatorios generados una sola vez en el primer arranque.
 * handle: nombre visible en la red para todos los contactos.
 *
 * Las claves criptográficas (Ed25519, X25519) se incorporarán en la fase LoRa
 * cuando se active el cifrado E2E real.
 */
data class Identity(
    val nodeId: String,      // "f1a2b3c4" (hex sin separadores)
    val handle: String       // nombre público en la red
) {
    /** Iniciales para el avatar (primeras 2 letras del handle en mayúsculas) */
    val initials: String get() = handle.take(2).uppercase()

    /** Versión formateada para mostrar en UI: "f1:a2:b3:c4" */
    val nodeIdFormatted: String get() =
        nodeId.chunked(2).joinToString(":")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Identity) return false
        return nodeId == other.nodeId
    }

    override fun hashCode(): Int = nodeId.hashCode()
}
