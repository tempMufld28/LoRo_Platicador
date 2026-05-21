package com.lockchat.app.ui.screens.ping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lockchat.app.data.transport.TransportManager
import com.lockchat.app.domain.model.LatencySample
import com.lockchat.app.domain.model.NodeUi
import com.lockchat.app.domain.model.PacketLogEntry
import com.lockchat.app.domain.model.PingStats
import com.lockchat.app.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PingUiState(
    val availableNodes: List<NodeUi>        = emptyList(),
    val selectedNode: NodeUi?               = null,
    val isRunning: Boolean                  = false,
    val currentLatencyMs: Int?              = null,
    val stats: PingStats                    = PingStats(),
    val latencySamples: List<LatencySample> = emptyList(),
    val packetLog: List<PacketLogEntry>     = emptyList(),
    val warning: String?                    = null
)

@HiltViewModel
class PingViewModel @Inject constructor(
    private val transportManager: TransportManager,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PingUiState())
    val uiState: StateFlow<PingUiState> = _uiState.asStateFlow()

    private var pingJob: Job? = null
    private var seq = 0

    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale("es", "MX"))

    init {
        // Observar contactos guardados y cruzar con peers conectados en tiempo real
        viewModelScope.launch {
            combine(
                contactRepository.observeAll(),
                transportManager.connectedPeers
            ) { contacts, peers ->
                contacts.map { contact ->
                    NodeUi(
                        nodeId = contact.nodeId,
                        handle = contact.handle,
                        isOnline = peers.containsKey(contact.nodeId)
                    )
                }
            }.collect { nodes ->
                _uiState.update { state ->
                    state.copy(
                        availableNodes = nodes,
                        selectedNode   = state.selectedNode?.let { sel ->
                            nodes.find { it.nodeId == sel.nodeId }
                        } ?: nodes.firstOrNull()
                    )
                }
            }
        }
    }

    fun onNodeSelected(node: NodeUi) {
        _uiState.update { it.copy(selectedNode = node) }
    }

    fun onStartPing() {
        val node = _uiState.value.selectedNode ?: return
        if (_uiState.value.isRunning) return

        seq = 0
        _uiState.update {
            it.copy(
                isRunning      = true,
                latencySamples = emptyList(),
                packetLog      = emptyList(),
                stats          = PingStats(),
                warning        = null
            )
        }

        pingJob = viewModelScope.launch {
            // Enviar un ping cada segundo
            while (isActive) {
                val currentSeq = ++seq

                val result = transportManager.ping(node.nodeId, currentSeq)
                val rtt = result.getOrNull()?.toInt()
                val isTimeout = rtt == null

                val logEntry = PacketLogEntry(
                    timestamp = timeFormat.format(Date()),
                    seq       = currentSeq,
                    nodeId    = node.nodeId,
                    latencyMs = rtt,
                    isTimeout = isTimeout
                )

                _uiState.update { state ->
                    val newSamples = state.latencySamples + LatencySample(currentSeq, rtt)
                    val newLog     = (listOf(logEntry) + state.packetLog).take(100)

                    val received = newSamples.count { it.latencyMs != null }
                    val minMs    = newSamples.mapNotNull { it.latencyMs }.minOrNull()
                    val maxMs    = newSamples.mapNotNull { it.latencyMs }.maxOrNull()
                    val avgMs    = if (received > 0)
                        newSamples.mapNotNull { it.latencyMs }.average().toInt()
                    else null
                    val loss     = if (newSamples.isNotEmpty())
                        (newSamples.count { it.latencyMs == null }.toFloat() / newSamples.size) * 100f
                    else 0f

                    val stats = PingStats(
                        sent        = currentSeq,
                        received    = received,
                        minMs       = minMs,
                        maxMs       = maxMs,
                        avgMs       = avgMs,
                        lossPercent = loss
                    )

                    val warning = if (loss > 20f)
                        "Alta pérdida de paquetes (${loss.toInt()}%). Señal débil."
                    else null

                    state.copy(
                        currentLatencyMs = rtt,
                        stats            = stats,
                        latencySamples   = newSamples,
                        packetLog        = newLog,
                        warning          = warning
                    )
                }

                delay(1_000L)
            }
        }
    }

    fun onStopPing() {
        pingJob?.cancel()
        pingJob = null
        _uiState.update { it.copy(isRunning = false, currentLatencyMs = null) }
    }

    override fun onCleared() {
        super.onCleared()
        pingJob?.cancel()
    }
}
