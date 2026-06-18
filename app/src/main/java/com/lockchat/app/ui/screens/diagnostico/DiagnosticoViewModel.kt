package com.lockchat.app.ui.screens.diagnostico

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.data.transport.TransportManager
import com.lockchat.app.data.transport.ble.BleTransport
import com.lockchat.app.data.transport.ble.PeerInfo
import com.lockchat.app.domain.model.TransportState
import com.lockchat.app.domain.model.TransportUiState
import com.lockchat.app.domain.repository.IdentityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticoUiState(
    val localNodeId: String = "?",
    val localHandle: String = "?",
    val bleState: TransportState = TransportState.DISCONNECTED,
    val activeTransport: TransportUiState = TransportUiState.SIN_TRANSPORTE,
    val discoveredPeers: Map<String, PeerInfo> = emptyMap(),
    val connectedNodeIds: Set<String> = emptySet(),
    val connectedPeers: Map<String, String> = emptyMap(),
    val bluetoothEnabled: Boolean = false,
    val logLines: List<String> = emptyList()
)

@HiltViewModel
class DiagnosticoViewModel @Inject constructor(
    private val transportManager: TransportManager,
    private val bleTransport: BleTransport,
    private val identityRepository: IdentityRepository
) : ViewModel() {

    private val _logLines = MutableStateFlow<List<String>>(emptyList())

    init {
        viewModelScope.launch {
            identityRepository.observeIdentity().filterNotNull().collect { identity ->
                addLog("Identidad: ${identity.handle} (${identity.nodeId})")
            }
        }
    }

    fun addLog(msg: String) {
        val ts = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        _logLines.value = (listOf("[$ts] $msg") + _logLines.value).take(200)
    }

    val uiState: StateFlow<DiagnosticoUiState> = combine(
        transportManager.activeTransport,
        transportManager.connectedPeers,
        bleTransport.state,
        bleTransport.discoveredPeers,
        bleTransport.connectedNodeIds,
        _logLines
    ) { values ->
        val activeTransport = values[0] as TransportUiState
        val connectedPeers = values[1] as Map<String, String>
        val bleState = values[2] as TransportState
        val discoveredPeers = values[3] as Map<String, PeerInfo>
        val connectedNodeIds = values[4] as Set<String>
        val logLines = values[5] as List<String>

        val identity = identityRepository.observeIdentity().value
        DiagnosticoUiState(
            localNodeId = identity?.nodeId ?: "?",
            localHandle = identity?.handle ?: "?",
            bleState = bleState,
            activeTransport = activeTransport,
            discoveredPeers = discoveredPeers,
            connectedNodeIds = connectedNodeIds,
            connectedPeers = connectedPeers,
            bluetoothEnabled = bleState != TransportState.DISCONNECTED || discoveredPeers.isNotEmpty(),
            logLines = logLines
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiagnosticoUiState()
    )
}
