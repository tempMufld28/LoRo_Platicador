package com.lockchat.app.data.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.lockchat.app.domain.model.TransportState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BleTransport — transporte bidireccional BLE para Lock-Chat.
 *
 * Cada dispositivo actúa simultáneamente como:
 *  - GATT Server (periférico): recibe mensajes de otros via escritura en CHAR_RX
 *  - GATT Client (central): escanea, conecta y envía via escritura en CHAR_RX del remoto
 *
 * Protocolo de mensajes (texto plano fase BLE):
 *  Formato: "nodeId:handle:mensaje" en UTF-8
 *
 * UUIDs (Nordic UART style, personalizados para Lock-Chat):
 *  SERVICE  = 6E400001-B5A3-F393-E0A9-E50E24DCCA9E
 *  CHAR_TX  = 6E400003-...  (notificaciones: server → client)
 *  CHAR_RX  = 6E400002-...  (escritura: client → server)
 */
@Singleton
@SuppressLint("MissingPermission")
class BleTransport @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BleTransport"

        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_TX_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_RX_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val CCCD_UUID: UUID    = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        const val MAX_PACKET_SIZE = 512  // después de MTU negotiation
    }

    // ── Estado ────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(TransportState.DISCONNECTED)
    val state: StateFlow<TransportState> = _state.asStateFlow()

    /** Peers conectados actualmente: nodeId → BluetoothGatt */
    private val connectedClients = mutableMapOf<String, BluetoothGatt>()

    /** Nodos realmente conectados (fase GATT client con servicios descubiertos) */
    private val _connectedNodeIds = MutableStateFlow<Set<String>>(emptySet())
    val connectedNodeIds: StateFlow<Set<String>> = _connectedNodeIds.asStateFlow()

    /** Peers descubiertos via advertising (nodeId → handle, dirección MAC) */
    private val _discoveredPeers = MutableStateFlow<Map<String, PeerInfo>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, PeerInfo>> = _discoveredPeers.asStateFlow()

    /** Mensajes recibidos */
    private val _incomingMessages = MutableSharedFlow<IncomingMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<IncomingMessage> = _incomingMessages.asSharedFlow()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var gattServer: BluetoothGattServer? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** nodeId e identidad local (se setean desde TransportManager al iniciar) */
    var localNodeId: String = ""
    var localHandle: String = ""

    // ── Inicialización ────────────────────────────────────────────────

    fun start() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _state.value = TransportState.ERROR
            Log.e(TAG, "Bluetooth no disponible o desactivado")
            return
        }
        startGattServer()
        startAdvertising()
        startScanning()
        _state.value = TransportState.SCANNING
        Log.i(TAG, "BleTransport iniciado. nodeId=$localNodeId handle=$localHandle")
    }

    fun stop() {
        gattServer?.close()
        advertiser?.stopAdvertising(advertiseCallback)
        scanner?.stopScan(scanCallback)
        connectedClients.values.forEach { it.close() }
        connectedClients.clear()
        _connectedNodeIds.value = emptySet()
        _discoveredPeers.value = emptyMap()
        _state.value = TransportState.DISCONNECTED
        Log.i(TAG, "BleTransport detenido")
    }

    // ── GATT Server ───────────────────────────────────────────────────

    private fun startGattServer() {
        gattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Característica TX: notificaciones (server → client)
        val charTx = BluetoothGattCharacteristic(
            CHAR_TX_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val cccd = BluetoothGattDescriptor(
            CCCD_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        charTx.addDescriptor(cccd)

        // Característica RX: escritura (client → server)
        val charRx = BluetoothGattCharacteristic(
            CHAR_RX_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        service.addCharacteristic(charTx)
        service.addCharacteristic(charRx)
        gattServer?.addService(service)
        Log.i(TAG, "GATT Server iniciado")
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (characteristic.uuid != CHAR_RX_UUID || value == null) return
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }

            val raw = String(value, Charsets.UTF_8)
            Log.d(TAG, "MSG recibido de ${device.address}: $raw")

            // Formato esperado: "nodeId:handle:contenido"
            val parts = raw.split(":", limit = 3)
            if (parts.size == 3) {
                val (nodeId, handle, content) = parts
                scope.launch {
                    _incomingMessages.emit(IncomingMessage(nodeId, handle, content, device.address))
                }
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Cliente conectado: ${device.address}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Cliente desconectado: ${device.address}")
            }
        }
    }

    // ── Advertising ───────────────────────────────────────────────────

    private fun startAdvertising() {
        advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        // Incluir nodeId COMPLETO en los datos del advertising.
        // nodeId es 8 hex chars (ej: "f1a2b3c4") → se codifica como sus 4 bytes hex reales.
        val nodeBytes = localNodeId.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .addManufacturerData(0x4C43, nodeBytes) // "LC" = Lock-Chat manufacturer ID
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .build()

        // Nombre del dispositivo BLE = handle (máx 8 chars)
        bluetoothAdapter?.name = "LC_${localHandle.take(8)}"

        advertiser?.startAdvertising(settings, data, scanResponse, advertiseCallback)
        Log.i(TAG, "Advertising iniciado como LC_${localHandle.take(8)} nodeId=$localNodeId")
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i(TAG, "Advertising activo")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertising falló: código $errorCode")
            _state.value = TransportState.ERROR
        }
    }

    // ── Scanning ──────────────────────────────────────────────────────

    private fun startScanning() {
        scanner = bluetoothAdapter?.bluetoothLeScanner
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

        scanner?.startScan(listOf(filter), settings, scanCallback)
        Log.i(TAG, "Scanning activo")
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: return

            // Solo nos interesan dispositivos Lock-Chat (nombre empieza con "LC_")
            if (!name.startsWith("LC_")) return

            val handle = name.removePrefix("LC_")
            // Extraer nodeId del manufacturer data — decodificar bytes hex → string hex
            val mfData = result.scanRecord?.getManufacturerSpecificData(0x4C43)
            val nodeId = mfData?.joinToString("") { "%02x".format(it.toInt() and 0xFF) }
                ?: device.address.replace(":", "").lowercase()

            if (nodeId == localNodeId) return  // somos nosotros mismos

            Log.d(TAG, "Scan result: handle=$handle nodeId=$nodeId rssi=${result.rssi}")

            val current = _discoveredPeers.value.toMutableMap()
            if (!current.containsKey(nodeId)) {
                current[nodeId] = PeerInfo(nodeId, handle, device, result.rssi)
                _discoveredPeers.value = current
                Log.i(TAG, "Peer descubierto: handle=$handle nodeId=$nodeId rssi=${result.rssi}")
                // Conectar automáticamente
                scope.launch { connectToPeer(device, nodeId, handle) }
            } else {
                // Actualizar RSSI del peer ya descubierto
                current[nodeId] = current[nodeId]!!.copy(rssi = result.rssi)
                _discoveredPeers.value = current
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan falló: código $errorCode")
        }
    }

    // ── GATT Client ───────────────────────────────────────────────────

    private fun connectToPeer(device: BluetoothDevice, nodeId: String, handle: String) {
        Log.i(TAG, "Conectando a $handle ($nodeId)...")
        device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Conectado a $handle ($nodeId)")
                    connectedClients[nodeId] = gatt
                    // Primero negociar MTU, luego descubrir servicios en el callback onMtuChanged
                    gatt.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Desconectado de $handle ($nodeId)")
                    connectedClients.remove(nodeId)
                    _connectedNodeIds.value = connectedClients.keys.toSet()
                    gatt.close()
                    // Actualizar estado del peer
                    val peers = _discoveredPeers.value.toMutableMap()
                    peers.remove(nodeId)
                    _discoveredPeers.value = peers
                    if (connectedClients.isEmpty()) {
                        _state.value = TransportState.SCANNING
                    }
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                Log.i(TAG, "MTU negociado: $mtu para $handle")
                // Ahora sí descubrir servicios (después de que el MTU se estabilizó)
                gatt.discoverServices()
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Servicios descubiertos en $handle ($nodeId)")
                    // Suscribir a notificaciones TX
                    val charTx = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHAR_TX_UUID)
                    if (charTx != null) {
                        gatt.setCharacteristicNotification(charTx, true)
                        val desc = charTx.getDescriptor(CCCD_UUID)
                        desc?.let {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(it)
                        }
                    }
                    _connectedNodeIds.value = connectedClients.keys.toSet()
                    _state.value = TransportState.CONNECTED
                } else {
                    Log.e(TAG, "Error descubriendo servicios en $handle: status=$status")
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == CHAR_TX_UUID) {
                    val raw = String(characteristic.value, Charsets.UTF_8)
                    val parts = raw.split(":", limit = 3)
                    if (parts.size == 3) {
                        val (rNodeId, rHandle, content) = parts
                        scope.launch {
                            _incomingMessages.emit(IncomingMessage(rNodeId, rHandle, content, gatt.device.address))
                        }
                    }
                }
            }
        }, BluetoothDevice.TRANSPORT_LE)
    }

    // ── Envío ─────────────────────────────────────────────────────────

    /**
     * Envía un mensaje a un contacto específico.
     * @param toNodeId nodeId del destinatario
     * @param content texto del mensaje
     * @return Result.success si se envió al canal BLE, failure si no hay conexión
     */
    suspend fun sendMessage(toNodeId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        val gatt = connectedClients[toNodeId]
            ?: return@withContext Result.failure(Exception("Peer $toNodeId no conectado"))

        val charRx = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHAR_RX_UUID)
            ?: return@withContext Result.failure(Exception("Característica RX no encontrada"))

        val packet = "$localNodeId:$localHandle:$content".toByteArray(Charsets.UTF_8)
        charRx.value = packet
        charRx.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        val success = gatt.writeCharacteristic(charRx)
        if (success) Result.success(Unit)
        else Result.failure(Exception("writeCharacteristic falló"))
    }

    fun isConnectedTo(nodeId: String): Boolean = connectedClients.containsKey(nodeId)
    fun connectedNodeIds(): Set<String> = connectedClients.keys.toSet()
}

// ── Data classes ──────────────────────────────────────────────────────

data class PeerInfo(
    val nodeId: String,
    val handle: String,
    val device: BluetoothDevice,
    val rssi: Int,
    val connectedAt: Long = System.currentTimeMillis()
)

data class IncomingMessage(
    val fromNodeId: String,
    val fromHandle: String,
    val content: String,
    val deviceAddress: String
)
