package com.lockchat.app.ui.screens.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.data.local.dao.MensajeDao
import com.lockchat.app.data.transport.TransportManager
import com.lockchat.app.domain.model.ChatFilter
import com.lockchat.app.domain.model.ConversationUi
import com.lockchat.app.domain.model.TransportUiState
import com.lockchat.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChatsUiState(
    val conversations: List<ConversationUi> = emptyList(),
    val filter: ChatFilter               = ChatFilter.TODOS,
    val transportState: TransportUiState = TransportUiState.SIN_TRANSPORTE,
    val appVersion: String               = "Lock-Chat v0.9.8",
    val isLoading: Boolean               = false
) {
    val filteredConversations: List<ConversationUi>
        get() = when (filter) {
            ChatFilter.TODOS     -> conversations
            ChatFilter.NO_LEIDOS -> conversations.filter { it.unreadCount > 0 }
        }
    val totalUnread: Int get() = conversations.sumOf { it.unreadCount }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val mensajeDao: MensajeDao,
    private val transportManager: TransportManager
) : ViewModel() {

    private val _filter = MutableStateFlow(ChatFilter.TODOS)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale("es", "MX"))

    val uiState: StateFlow<ChatsUiState> = combine(
        contactRepository.observeAll(),
        _filter,
        transportManager.activeTransport,
        transportManager.connectedPeers,
        transportManager.loRaUsbTransport.isLoRaAvailable
    ) { contacts, filter, transport, connectedPeers, loraAvailable ->
        // Construir ConversationUi para cada contacto
        val conversations = contacts.map { contact ->
            val lastMsg = mensajeDao.lastMessage(contact.nodeId)
            val unread  = mensajeDao.observeUnreadCount(contact.nodeId).first()
            val isOnline = connectedPeers.containsKey(contact.nodeId)

            ConversationUi(
                contactId          = contact.nodeId,
                handle             = contact.handle,
                nodeIdFormatted    = contact.nodeIdFormatted,
                avatarInitials     = contact.initials,
                lastMessagePreview = lastMsg?.content ?: "Sin mensajes aún",
                lastMessageTime    = lastMsg?.let { timeFormat.format(Date(it.timestamp)) } ?: "",
                unreadCount        = unread,
                isOnline           = isOnline,
                isBleAvailable     = isOnline,
                isLoraAvailable    = loraAvailable
            )
        }.sortedByDescending { conv ->
            contacts.find { it.nodeId == conv.contactId }?.lastSeen ?: 0L
        }

        ChatsUiState(
            conversations  = conversations,
            filter         = filter,
            transportState = transport
        )
    }.stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChatsUiState(isLoading = true)
    )

    fun onFilterChange(filter: ChatFilter) {
        _filter.value = filter
    }
}
