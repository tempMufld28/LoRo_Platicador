package com.lockchat.app.ui.screens.chatdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.data.repository.MensajeRepositoryImpl
import com.lockchat.app.data.transport.TransportManager
import com.lockchat.app.domain.model.MessageStatus
import com.lockchat.app.domain.model.MessageUi
import com.lockchat.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatDetailUiState(
    val contactHandle: String   = "",
    val nodeIdFormatted: String = "",
    val isOnline: Boolean       = false,
    val channelInfo: String     = "BLE directo",
    val messages: List<MessageUi> = emptyList(),
    val inputText: String       = "",
    val isSending: Boolean      = false,
    val sendError: String?      = null
)

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactRepository: ContactRepository,
    private val mensajeRepository: MensajeRepositoryImpl,
    private val transportManager: TransportManager
) : ViewModel() {

    private val contactId: String = checkNotNull(savedStateHandle["contactId"])

    private val _inputText = MutableStateFlow("")
    private val _isSending = MutableStateFlow(false)
    private val _sendError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ChatDetailUiState> = combine(
        contactRepository.observeAll(),
        mensajeRepository.getMessages(contactId),
        transportManager.connectedPeers,
        _inputText,
        _isSending
    ) { contacts, messages, peers, input, sending ->
        val contact = contacts.find { it.nodeId == contactId }
        val transport = transportManager.activeTransport.value

        ChatDetailUiState(
            contactHandle   = contact?.handle ?: "desconocido",
            nodeIdFormatted = contact?.nodeIdFormatted ?: contactId,
            isOnline        = peers.containsKey(contactId),
            channelInfo     = when (transport) {
                com.lockchat.app.domain.model.TransportUiState.LORA_USB   -> "LoRa 915 MHz"
                com.lockchat.app.domain.model.TransportUiState.BLE_DIRECT -> "BLE directo"
                else -> "Sin conexión"
            },
            messages  = messages,
            inputText = input,
            isSending = sending
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatDetailUiState()
    )

    fun onInputChange(text: String) {
        _inputText.value = text
        _sendError.value = null
    }

    fun onSend() {
        val content = _inputText.value.trim()
        if (content.isBlank() || _isSending.value) return

        viewModelScope.launch {
            _isSending.value = true
            _inputText.value = ""

            // 1. Persistir en Room como SENDING
            mensajeRepository.sendMessage(contactId, content)

            // 2. Enviar via transport
            val result = transportManager.sendMessage(contactId, content)

            // 3. Actualizar status
            result.fold(
                onSuccess = {
                    // El status se actualiza a SENT — se quedaría en SENDING
                    // hasta recibir ACK del otro lado en una fase futura
                    _isSending.value = false
                },
                onFailure = { e ->
                    _sendError.value = e.message
                    _isSending.value = false
                }
            )
        }
    }

    /** Marca todos los mensajes de este contacto como leídos */
    fun onScreenEntered() {
        viewModelScope.launch {
            mensajeRepository.markAllRead(contactId)
        }
    }
}
