package com.lockchat.app.data.local.dao

import androidx.room.*
import com.lockchat.app.data.local.entity.SolicitudEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SolicitudDao {

    @Query("SELECT * FROM solicitudes ORDER BY lastTimestamp DESC")
    fun observeAll(): Flow<List<SolicitudEntity>>

    @Query("SELECT COUNT(*) FROM solicitudes")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM solicitudes WHERE nodeId = :nodeId LIMIT 1")
    suspend fun findById(nodeId: String): SolicitudEntity?

    /**
     * Upsert: si la solicitud ya existe, incrementa el contador y actualiza el
     * último contenido + timestamp. Si no, la crea.
     */
    @Query(
        """
        INSERT INTO solicitudes(nodeId, handle, lastContent, count, firstTimestamp, lastTimestamp)
        VALUES(:nodeId, :handle, :content, 1, :ts, :ts)
        ON CONFLICT(nodeId) DO UPDATE SET
            lastContent = :content,
            count = count + 1,
            lastTimestamp = :ts
        """
    )
    suspend fun upsert(nodeId: String, handle: String, content: String, ts: Long)

    @Query("DELETE FROM solicitudes WHERE nodeId = :nodeId")
    suspend fun delete(nodeId: String)
}
