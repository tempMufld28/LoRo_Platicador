package com.lockchat.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lockchat.app.domain.model.Identity
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "lockchat_identity")

/**
 * Implementación real de IdentityRepository usando DataStore.
 *
 * El nodeId es generado una sola vez (4 bytes aleatorios) y persiste permanentemente.
 * No hay PIN, no hay cifrado local en esta fase — la seguridad viene del transporte E2E.
 */
@Singleton
class IdentityDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : IdentityRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _identity = MutableStateFlow<Identity?>(null)
    private val _hasIdentity = MutableStateFlow(false)

    companion object {
        private val KEY_HAS_IDENTITY = booleanPreferencesKey("has_identity")
        private val KEY_NODE_ID      = stringPreferencesKey("node_id")
        private val KEY_HANDLE       = stringPreferencesKey("handle")
    }

    init {
        // Cargar identidad al iniciar (bloqueante solo en init, en hilo IO)
        scope.launch {
            val prefs = context.dataStore.data.first()
            val has = prefs[KEY_HAS_IDENTITY] ?: false
            _hasIdentity.value = has
            if (has) {
                val nodeId = prefs[KEY_NODE_ID] ?: return@launch
                val handle = prefs[KEY_HANDLE]  ?: return@launch
                _identity.value = Identity(nodeId = nodeId, handle = handle)
            }
        }
    }

    override fun hasIdentity(): Boolean = runBlocking {
        context.dataStore.data.first()[KEY_HAS_IDENTITY] ?: false
    }

    override fun observeIdentity(): StateFlow<Identity?> = _identity.asStateFlow()

    override suspend fun createIdentity(handle: String): Result<Identity> {
        return runCatching {
            // Genera nodeId único: 4 bytes aleatorios como hex
            val nodeId = Random.nextBytes(4).joinToString("") { "%02x".format(it) }
            val identity = Identity(nodeId = nodeId, handle = handle.trim())

            context.dataStore.edit { prefs ->
                prefs[KEY_HAS_IDENTITY] = true
                prefs[KEY_NODE_ID]      = identity.nodeId
                prefs[KEY_HANDLE]       = identity.handle
            }
            _identity.value    = identity
            _hasIdentity.value = true
            identity
        }
    }

    override suspend fun loadIdentity(): Result<Identity> {
        return runCatching {
            val prefs  = context.dataStore.data.first()
            val nodeId = prefs[KEY_NODE_ID] ?: error("Sin identidad guardada")
            val handle = prefs[KEY_HANDLE]  ?: error("Handle no encontrado")
            Identity(nodeId = nodeId, handle = handle).also { _identity.value = it }
        }
    }

    override suspend fun updateHandle(newHandle: String): Result<Unit> {
        return runCatching {
            context.dataStore.edit { prefs ->
                prefs[KEY_HANDLE] = newHandle.trim()
            }
            _identity.value = _identity.value?.copy(handle = newHandle.trim())
        }
    }
}
