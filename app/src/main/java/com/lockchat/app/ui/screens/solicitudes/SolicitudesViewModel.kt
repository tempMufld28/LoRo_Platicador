package com.lockchat.app.ui.screens.solicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.data.repository.MensajeRepositoryImpl
import com.lockchat.app.data.repository.Solicitud
import com.lockchat.app.data.repository.SolicitudRepository
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SolicitudesUiState(
    val solicitudes: List<Solicitud> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class SolicitudesViewModel @Inject constructor(
    private val solicitudRepository: SolicitudRepository,
    private val contactRepository: ContactRepository,
    private val mensajeRepository: MensajeRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolicitudesUiState(isLoading = true))
    val uiState: StateFlow<SolicitudesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            solicitudRepository.observeAll().collect { list ->
                _uiState.update { it.copy(solicitudes = list, isLoading = false) }
            }
        }
    }

    /** Acepta una solicitud: crea el contacto, promueve el último mensaje a la conversación y borra la solicitud. */
    fun onAccept(solicitud: Solicitud) {
        viewModelScope.launch {
            runCatching {
                contactRepository.addContact(
                    Contact(nodeId = solicitud.nodeId, handle = solicitud.handle)
                ).getOrThrow()
                mensajeRepository.saveIncoming(
                    contactNodeId = solicitud.nodeId,
                    content = solicitud.lastContent,
                    senderHandle = solicitud.handle
                )
                solicitudRepository.delete(solicitud.nodeId)
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Error al aceptar") }
            }
        }
    }

    /** Rechaza una solicitud: la elimina del buzón sin crear contacto. */
    fun onReject(solicitud: Solicitud) {
        viewModelScope.launch {
            runCatching { solicitudRepository.delete(solicitud.nodeId) }
                .onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Error al rechazar") }
                }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
