package com.lockchat.app.data.local.dao

import androidx.room.*
import com.lockchat.app.data.local.entity.MensajeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MensajeDao {

    @Query("SELECT * FROM mensajes WHERE contactNodeId = :contactId ORDER BY timestamp ASC")
    fun observeByContact(contactId: String): Flow<List<MensajeEntity>>

    @Query("SELECT * FROM mensajes WHERE contactNodeId = :contactId ORDER BY timestamp DESC LIMIT 1")
    suspend fun lastMessage(contactId: String): MensajeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(msg: MensajeEntity)

    @Query("UPDATE mensajes SET status = :status WHERE msgId = :msgId")
    suspend fun updateStatus(msgId: String, status: String)

    @Query("UPDATE mensajes SET leido = 1 WHERE contactNodeId = :contactId AND direction = 'IN'")
    suspend fun markAllRead(contactId: String)

    @Query("SELECT COUNT(*) FROM mensajes WHERE contactNodeId = :contactId AND leido = 0 AND direction = 'IN'")
    fun observeUnreadCount(contactId: String): Flow<Int>

    @Query("DELETE FROM mensajes WHERE msgId = :msgId")
    suspend fun delete(msgId: String)
}
