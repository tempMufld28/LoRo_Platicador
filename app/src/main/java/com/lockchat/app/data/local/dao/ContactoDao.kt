package com.lockchat.app.data.local.dao

import androidx.room.*
import com.lockchat.app.data.local.entity.ContactoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT * FROM contactos ORDER BY handle ASC")
    fun observeAll(): Flow<List<ContactoEntity>>

    @Query("SELECT * FROM contactos WHERE nodeId = :nodeId LIMIT 1")
    suspend fun findById(nodeId: String): ContactoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contacto: ContactoEntity)

    // ── Updates parciales (evitan DELETE+REPLACE que dispara CASCADE
    //    y borraría todos los mensajes del contacto) ──

    @Query("UPDATE contactos SET isOnline = :isOnline WHERE nodeId = :nodeId")
    suspend fun updateOnlineStatus(nodeId: String, isOnline: Boolean)

    @Query("UPDATE contactos SET lastSeen = :timestamp WHERE nodeId = :nodeId")
    suspend fun updateLastSeen(nodeId: String, timestamp: Long)

    @Delete
    suspend fun delete(contacto: ContactoEntity)

    @Query("DELETE FROM contactos WHERE nodeId = :nodeId")
    suspend fun deleteById(nodeId: String)

    @Query("SELECT COUNT(*) FROM contactos")
    suspend fun count(): Int
}
