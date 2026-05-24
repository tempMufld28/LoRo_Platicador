package com.lockchat.app.data.transport

import android.util.Log
import com.lockchat.app.data.repository.ContactoRepositoryImpl
import com.lockchat.app.data.repository.MensajeRepositoryImpl
import com.lockchat.app.data.transport.ble.BleTransport
import com.lockchat.app.data.transport.ble.IncomingMessage
import com.lockchat.app.data.transport.lora.LoRaUsbTransport
import com.lockchat.app.domain.model.TransportState
import com.lockchat.app.domain.model.TransportUiState
import com.lockchat.app.domain.repository.IdentityRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TransportManager — orquestador de transportes.
 *
 * Lógica de selección:
 *  1. Si LoRa USB está disponible → usa LoRaUsbTransport (prioridad)
 *  2. Si no → usa BleTransport
 *
 * Gestiona:
 *  - Ciclo de vida de ambos transportes
 *  - Enrutamiento de mensajes entrantes → MensajeRepository
 *  - ACKs de mensajes enviados → actualizar status en Room
 *  - Estado de conexión de peers → actualizar ContactRepository
 */
@Singleton
class TransportManager @Inject constructor(
    val bleTransport: BleTransport,
    val loRaUsbTransport: LoRaUsbTransport,
    private val identityRepository: IdentityRepository,
    private val mensajeRepository: MensajeRepositoryImpl,
    private val contactoRepository: ContactoRepositoryImpl
) {
    companion object {
        private const val TAG = "TransportManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _activeTransport = MutableStateFlow(TransportUiState.SIN_TRANSPORTE)
    val activeTransport: StateFlow<TransportUiState> = _activeTransport.asStateFlow()

    /** Nodos actualmente conectados (nodeId → handle) — actualizado en tiempo real */
    private val _connectedPeers = MutableStateFlow<Map<String, String>>(emptyMap())
    val connectedPeers: StateFlow<Map<String, String>> = _connectedPeers.asStateFlow()

    /** Flujo interno para interceptar las respuestas PONG */
    private val _pongReceivedFlow = MutableSharedFlow<Pair<String, Int>>(extraBufferCapacity = 64)

    fun start() {
        val identity = identityRepository.observeIdentity().value ?: run {
            Log.w(TAG, "Sin identidad — TransportManager no iniciado")
            return
        }

        // Configurar identidad en BLE
        bleTransport.localNodeId = identity.nodeId
        bleTransport.localHandle = identity.handle

        // Iniciar BLE
        bleTransport.start()
        _activeTransport.value = TransportUiState.BLE_DIRECT

        // Observar peers BLE conectados y actualizar estado online/offline
        scope.launch {
            var previousConnectedIds = emptySet<String>()
            combine(
                bleTransport.connectedNodeIds,
                bleTransport.discoveredPeers
            ) { connectedIds, discovered ->
                connectedIds to discovered
            }.collect { (connectedIds, discovered) ->
                // Mapear solo los nodos REALMENTE conectados
                val mapped = connectedIds.associateWith { nodeId ->
                    discovered[nodeId]?.handle ?: "desconocido"
                }
                _connectedPeers.value = mapped

                // Nuevos conectados → marcar online
                (connectedIds - previousConnectedIds).forEach { nodeId ->
                    contactoRepository.updateOnlineStatus(nodeId, true)
                }

                // Desconectados → marcar offline
                (previousConnectedIds - connectedIds).forEach { nodeId ->
                    contactoRepository.updateOnlineStatus(nodeId, false)
                }

                previousConnectedIds = connectedIds
            }
        }

        // Observar mensajes BLE entrantes
        scope.launch {
            bleTransport.incomingMessages.collect { incoming ->
                handleIncomingMessage(incoming)
            }
        }

        // Observar disponibilidad de LoRa USB
        scope.launch {
            loRaUsbTransport.isLoRaAvailable.collect { available ->
                if (available) {
                    _activeTransport.value = TransportUiState.LORA_USB
                    Log.i(TAG, "Cambiando a LoRa USB")
                } else if (_activeTransport.value == TransportUiState.LORA_USB) {
                    _activeTransport.value = TransportUiState.BLE_DIRECT
                    Log.i(TAG, "LoRa USB desconectado — volviendo a BLE")
                }
            }
        }

        Log.i(TAG, "TransportManager iniciado — identidad: ${identity.handle} (${identity.nodeId})")
    }

    fun stop() {
        bleTransport.stop()
        scope.cancel()
        _activeTransport.value = TransportUiState.SIN_TRANSPORTE
        _connectedPeers.value = emptyMap()
    }

    /**
     * Envía un mensaje a un contacto.
     * Usa LoRa si está disponible, BLE si no.
     */
    suspend fun sendMessage(toNodeId: String, content: String): Result<Unit> {
        return when (_activeTransport.value) {
            TransportUiState.LORA_USB -> loRaUsbTransport.sendMessage(toNodeId, content)
            TransportUiState.BLE_DIRECT -> bleTransport.sendMessage(toNodeId, content)
            TransportUiState.SIN_TRANSPORTE -> Result.failure(Exception("Sin transporte activo"))
        }
    }

    /**
     * Envía un ping a un contacto y retorna el RTT en ms.
     * Soporta tanto BLE como LoRa. Envía PING:seq y espera el PONG:seq.
     */
    suspend fun ping(toNodeId: String, seq: Int): Result<Long> {
        val sentAt = System.currentTimeMillis()
        
        // Enviar el PING por el transporte activo
        val sendResult = sendMessage(toNodeId, "PING:$seq")
        if (sendResult.isFailure) {
            return Result.failure(sendResult.exceptionOrNull() ?: Exception("Error al enviar PING"))
        }

        // Esperar el PONG correspondiente con un timeout de 4 segundos
        return try {
            withTimeout(4000L) {
                _pongReceivedFlow
                    .first { it.first == toNodeId && it.second == seq }
                val rtt = System.currentTimeMillis() - sentAt
                Result.success(rtt)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("Timeout esperando respuesta PONG"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun onUsbDeviceDetached() {
        loRaUsbTransport.onDeviceDetached()
    }

    fun onUsbDeviceAttached() {
        loRaUsbTransport.detectDevice()
    }

    // ── Manejo de mensajes entrantes ──────────────────────────────────

    private suspend fun handleIncomingMessage(msg: IncomingMessage) {
        Log.d(TAG, "MSG ENTRANTE de ${msg.fromHandle}: ${msg.content}")

        // Verificar si el contacto existe en nuestra lista
        val contact = contactoRepository.findById(msg.fromNodeId)

        if (contact == null) {
            // Contacto desconocido — ignorar (solo se aceptan mensajes de contactos guardados)
            Log.w(TAG, "Mensaje de nodo desconocido: ${msg.fromNodeId} — ignorado")
            return
        }

        // Detectar si es un PING y responder con PONG
        if (msg.content.startsWith("PING:")) {
            val seq = msg.content.removePrefix("PING:")
            sendMessage(msg.fromNodeId, "PONG:$seq")
            return
        }

        // Interceptar PONG para medir RTT en tiempo real
        if (msg.content.startsWith("PONG:")) {
            val seq = msg.content.removePrefix("PONG:").toIntOrNull() ?: 0
            _pongReceivedFlow.tryEmit(Pair(msg.fromNodeId, seq))
            return
        }

        // Guardar mensaje en Room
        mensajeRepository.saveIncoming(
            contactNodeId = msg.fromNodeId,
            content       = msg.content,
            senderHandle  = msg.fromHandle
        )

        // Actualizar lastSeen del contacto
        contactoRepository.updateLastSeen(msg.fromNodeId, System.currentTimeMillis())
    }
}
