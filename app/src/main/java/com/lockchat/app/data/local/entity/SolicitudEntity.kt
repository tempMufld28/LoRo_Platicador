package com.lockchat.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room: solicitudes de mensajes de nodos desconocidos.
 *
 * Cuando llega un mensaje por BLE de un nodeId que NO está guardado como contacto,
 * en lugar de descartarlo silenciosamente se captura aquí para que el usuario
 * decida aceptarlo (crea el contacto) o rechazarlo.
 *
 * Solo se conserva el último contenido recibido + contador de mensajes.
 * Al aceptar, el lastContent se promueve a mensaje IN de la nueva conversación.
 */
@Entity(tableName = "solicitudes")
data class SolicitudEntity(
    @PrimaryKey val nodeId: String,
    val handle: String,
    val lastContent: String,
    val count: Int = 1,
    val firstTimestamp: Long = System.currentTimeMillis(),
    val lastTimestamp: Long = System.currentTimeMillis()
)
