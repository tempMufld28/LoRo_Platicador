package com.lockchat.app.domain.repository

import com.lockchat.app.domain.model.Identity
import kotlinx.coroutines.flow.StateFlow

/** Gestión de la identidad local del usuario. Sin PIN — la seguridad E2E es de capa de transporte. */
interface IdentityRepository {
    fun hasIdentity(): Boolean
    fun observeIdentity(): StateFlow<Identity?>
    suspend fun createIdentity(handle: String): Result<Identity>
    suspend fun loadIdentity(): Result<Identity>
    suspend fun updateHandle(newHandle: String): Result<Unit>
}
