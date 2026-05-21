package com.lockchat.app.ui.screens.addcontact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.domain.repository.ContactRepository
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class AddContactUiState(
    val activeTab: Int          = 0,        // 0 = Escanear, 1 = Mi QR
    val scanResult: String?     = null,     // JSON crudo del QR escaneado
    val previewContact: Contact? = null,    // contacto parseado listo para confirmar
    val myQrData: String        = "",       // JSON del QR propio
    val isLoading: Boolean      = false,
    val isSuccess: Boolean      = false,
    val errorMessage: String?   = null
)

@HiltViewModel
class AddContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddContactUiState())
    val uiState: StateFlow<AddContactUiState> = _uiState.asStateFlow()

    init {
        // Generar el JSON del QR propio al iniciar
        viewModelScope.launch {
            val identity = identityRepository.loadIdentity().getOrNull()
            if (identity != null) {
                val json = buildMyQrJson(identity.nodeId, identity.handle)
                _uiState.update { it.copy(myQrData = json) }
            }
        }
    }

    fun onTabSelected(tab: Int) {
        _uiState.update { it.copy(activeTab = tab, errorMessage = null) }
    }

    /**
     * Llamado cuando el escáner QR detecta un código.
     * Parsea el JSON y prepara el contacto para confirmación.
     */
    fun onQrScanned(rawValue: String) {
        _uiState.update { it.copy(scanResult = rawValue, errorMessage = null) }
        try {
            val json = JSONObject(rawValue)
            val version = json.optInt("v", 0)
            if (version != 1) {
                _uiState.update { it.copy(errorMessage = "QR no compatible (versión $version)") }
                return
            }
            val nodeId = json.getString("nodeId")
            val handle = json.getString("handle")

            if (nodeId.isBlank() || handle.isBlank()) {
                _uiState.update { it.copy(errorMessage = "QR inválido — datos incompletos") }
                return
            }

            val contact = Contact(nodeId = nodeId, handle = handle)
            _uiState.update { it.copy(previewContact = contact) }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "No se pudo leer el QR: ${e.message}") }
        }
    }

    /** Confirmar y guardar el contacto en Room */
    fun onConfirmContact() {
        val contact = _uiState.value.previewContact ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            contactRepository.addContact(contact)
                .fold(
                    onSuccess = { _uiState.update { it.copy(isLoading = false, isSuccess = true) } },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al guardar") }
                    }
                )
        }
    }

    fun onCancelPreview() {
        _uiState.update { it.copy(previewContact = null, scanResult = null) }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private fun buildMyQrJson(nodeId: String, handle: String): String {
        return JSONObject()
            .put("v", 1)
            .put("nodeId", nodeId)
            .put("handle", handle)
            .put("transport", "BLE")
            .toString()
    }
}
