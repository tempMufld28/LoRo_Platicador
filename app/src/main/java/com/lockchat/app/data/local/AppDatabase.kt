package com.lockchat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lockchat.app.data.local.dao.ContactoDao
import com.lockchat.app.data.local.dao.MensajeDao
import com.lockchat.app.data.local.dao.SolicitudDao
import com.lockchat.app.data.local.entity.ContactoEntity
import com.lockchat.app.data.local.entity.MensajeEntity
import com.lockchat.app.data.local.entity.SolicitudEntity

/**
 * Base de datos Room de Lock-Chat — versión 3.
 *
 * v1 → v2: rediseño de esquema para fase BLE (content en plaintext,
 *           eliminación de ciphertext/nonce/firma, contactos simplificados).
 * v2 → v3: tabla `solicitudes` para mensajes de nodos desconocidos (buzón).
 * fallbackToDestructiveMigration: ok en dev — en producción se añadirá Migration.
 *
 * Fase LoRa futura: añadir SQLCipher como openHelperFactory.
 */
@Database(
    entities = [
        ContactoEntity::class,
        MensajeEntity::class,
        SolicitudEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactoDao(): ContactoDao
    abstract fun mensajeDao(): MensajeDao
    abstract fun solicitudDao(): SolicitudDao
}
