package com.lockchat.app.data.transport.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.lockchat.app.domain.model.TransportState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import java.util.concurrent.ConcurrentHashMap
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
 *  CHAR_INFO= 6E400004-...  (lectura: "nodeId:handle" del remote — binding autoritativo)
 *
 * Binding nodeId↔peer:
 *  El manufacturer data del advertising puede truncarse en algunos OEM, así que NO se
 *  confía en él como fuente única. Tras onServicesDiscovered se lee CHAR_INFO del peer
 *  para obtener su nodeId/handle autoritativos. Solo entonces la conexión se registra
 *  como "lista para enviar". El nodeId del advertising se usa solo como hint de dedup.
 */
@Singleton
@SuppressLint("MissingPermission")
class BleTransport @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BleTransport"
        private const val WRITE_ACK_TIMEOUT_MS = 3000L

        val SERVICE_UUID: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_TX_UUID: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_RX_UUID: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
        val CHAR_INFO_UUID: UUID = UUID.fromString("6E400004-B5A3-F393-E0A9-E50E24DCCA9E")
        val CCCD_UUID: UUID    = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

        const val MAX_PACKET_SIZE = 512  // después de MTU negotiation
    }

    // ── Estado ────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(TransportState.DISCONNECTED)
    val state: StateFlow<TransportState> = _state.asStateFlow()

    /** Peers listos para enviar: nodeId → GattConnection (servicios descubiertos + CHAR_INFO leído) */
    private val connectedClients = ConcurrentHashMap<String, GattConnection>()

    /** Conexiones abiertas cuyo nodeId aún no se confirma via CHAR_INFO: MAC → GattConnection */
    private val pendingConnections = ConcurrentHashMap<String, GattConnection>()

    /** MACs con un connectGatt en vuelo o ya conectado (dedup de scanning) */
    private val connectingMacs = ConcurrentHashMap<String, Unit>()

    /** Nodos realmente conectados y listos (fase GATT client con servicios + identidad) */
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
    private var reconnectJob: Job? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** nodeId e identidad local (se setean desde TransportManager al iniciar) */
    var localNodeId: String = ""
    var localHandle: String = ""

    // ── Inicialización ────────────────────────────────────────────────

    fun start() {
        try {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                _state.value = TransportState.ERROR
                Log.e(TAG, "Bluetooth no disponible o desactivado")
                return
            }
            startGattServer()
            startAdvertising()
            startScanning()
            startReconnectLoop()
            _state.value = TransportState.SCANNING
            Log.i(TAG, "BleTransport iniciado. nodeId=$localNodeId handle=$localHandle")
        } catch (e: SecurityException) {
            // No tenemos todos los permisos BLE runtime (SCAN/CONNECT/ADVERTISE).
            // Sin esto el servicio podría crashear; marcamos ERROR y mantenemos vivo el FGS.
            _state.value = TransportState.ERROR
            Log.e(TAG, "SecurityException al iniciar BLE — ¿faltan permisos runtime?", e)
        }
    }

    /** Reconexión periódica: si un peer está descubierto pero no conectado, reintentar. */
    private fun startReconnectLoop() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            while (isActive) {
                val discovered = _discoveredPeers.value
                val toReconnect = discovered.values.filter { peer ->
                    !connectedClients.containsKey(peer.nodeId) &&
                    !connectingMacs.containsKey(peer.device.address)
                }
                if (toReconnect.isNotEmpty()) {
                    Log.i(TAG, "Reconnect loop: ${toReconnect.size} peers descubiertos sin conexión")
                    toReconnect.forEach { peer ->
                        Log.i(TAG, "Reconnect loop: intentando ${peer.handle} (${peer.nodeId})")
                        connectToPeer(peer.device, peer.nodeId, peer.handle)
                        delay(500) // espaciar reconexiones
                    }
                }
                delay(2_000)
            }
        }
    }

    fun stop() {
        reconnectJob?.cancel()
        reconnectJob = null
        runCatching { gattServer?.close() }
        runCatching { advertiser?.stopAdvertising(advertiseCallback) }
        runCatching { scanner?.stopScan(scanCallback) }
        connectedClients.values.forEach { runCatching { it.gatt.close() } }
        pendingConnections.values.forEach { runCatching { it.gatt.close() } }
        connectedClients.clear()
        pendingConnections.clear()
        connectingMacs.clear()
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

        // Característica INFO: lectura (client → server) — devuelve "nodeId:handle" local
        val charInfo = BluetoothGattCharacteristic(
            CHAR_INFO_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        service.addCharacteristic(charTx)
        service.addCharacteristic(charRx)
        service.addCharacteristic(charInfo)
        gattServer?.addService(service)
        Log.i(TAG, "GATT Server iniciado")
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == CHAR_INFO_UUID) {
                val resp = "$localNodeId:$localHandle".toByteArray(Charsets.UTF_8)
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, resp)
            } else {
                gattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED, 0, null
                )
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            // SIEMPRE enviar respuesta si el cliente la espera, incluso en caso de error.
            // Si no respondemos, el cliente GATT puede colgarse esperando el ACK y
            // eventualmente el stack BLE desconecta la conexión.
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
            }

            if (characteristic.uuid != CHAR_RX_UUID || value == null) return

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
        // Nota: es solo un hint; el binding autoritativo se hace leyendo CHAR_INFO.
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
            val scanRecord = result.scanRecord
            val name = device.name ?: scanRecord?.deviceName
            val hasOurServiceUuid = scanRecord?.serviceUuids?.contains(ParcelUuid(SERVICE_UUID)) == true

            // Log de TODO lo que escaneamos para diagnóstico
            Log.d(
                TAG,
                "Scan: name='$name' mac=${device.address} rssi=${result.rssi} hasOurUuid=$hasOurServiceUuid serviceUuids=${scanRecord?.serviceUuids}"
            )

            // Conectar a peers Lock-Chat. Los detectamos por:
            // 1) Nombre LC_*, o 2) Nuestro SERVICE_UUID en el advertising.
            // No descartamos por falta de nombre: algunos OEM no lo propagan.
            val isLockChat = name?.startsWith("LC_") == true || hasOurServiceUuid
            if (!isLockChat) return

            val handle = name?.removePrefix("LC_") ?: "?"

            // Hint de nodeId desde manufacturer data (puede ser null si se truncó).
            val mfData = scanRecord?.getManufacturerSpecificData(0x4C43)
            val nodeIdHint = mfData?.joinToString("") { "%02x".format(it.toInt() and 0xFF) }

            if (nodeIdHint == localNodeId) {
                Log.d(TAG, "Es nuestro propio nodeId — ignorado")
                return
            }

            // Actualizar RSSI del peer ya conocido
            if (nodeIdHint != null) {
                val current = _discoveredPeers.value
                if (current.containsKey(nodeIdHint)) {
                    _discoveredPeers.value = current.toMutableMap().apply {
                        this[nodeIdHint] = this[nodeIdHint]!!.copy(rssi = result.rssi)
                    }
                    // Si dejó de estar conectado, intentar reconectar
                    maybeReconnect(device, nodeIdHint, handle)
                    return
                }
            }

            // Dedup: no reconectar si ya está conectado o en vuelo
            if (nodeIdHint != null && connectedClients.containsKey(nodeIdHint)) return
            if (connectingMacs.containsKey(device.address)) return

            Log.i(TAG, "Peer descubierto: handle=$handle nodeId=${nodeIdHint ?: "?"} rssi=${result.rssi}")
            // Conectar automáticamente
            scope.launch { connectToPeer(device, nodeIdHint, handle) }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan falló: código $errorCode")
        }
    }

    /** Si conocemos un peer pero no está conectado, intenta reconectarlo. */
    private fun maybeReconnect(device: BluetoothDevice, nodeIdHint: String, handle: String) {
        if (!connectingMacs.containsKey(device.address) && !connectedClients.containsKey(nodeIdHint)) {
            Log.i(TAG, "maybeReconnect: $handle ($nodeIdHint) está descubierto pero no conectado")
            scope.launch { connectToPeer(device, nodeIdHint, handle) }
        }
    }

    // ── GATT Client ───────────────────────────────────────────────────

    private fun connectToPeer(device: BluetoothDevice, nodeIdHint: String?, handleHint: String) {
        if (nodeIdHint != null && connectedClients.containsKey(nodeIdHint)) return
        if (connectingMacs.putIfAbsent(device.address, Unit) != null) return

        Log.i(TAG, "Conectando a $handleHint (nodeId=${nodeIdHint ?: "?"})...")

        // Timeout de seguridad: si connectGatt no genera callback alguno en 12s,
        // limpiamos la MAC para permitir un nuevo intento.
        scope.launch {
            delay(12_000)
            if (connectingMacs.remove(device.address) != null) {
                Log.w(TAG, "Timeout de conexión a $handleHint (${device.address}) — liberando dedup")
            }
        }

        device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Conectado a ${gatt.device.address}")
                    // Crear conexión pendiente hasta confirmar identidad via CHAR_INFO
                    val conn = GattConnection(gatt, device, nodeIdHint, handleHint)
                    pendingConnections[gatt.device.address] = conn
                    // Primero negociar MTU, luego descubrir servicios en onMtuChanged
                    gatt.requestMtu(512)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    val wasReady = findConnection(gatt)?.ready ?: false
                    val connNodeId = findConnection(gatt)?.nodeId
                    val connDevice = device
                    val connHandleHint = handleHint
                    Log.i(TAG, "Desconectado de ${gatt.device.address} (wasReady=$wasReady status=$status)")
                    // Liberar la cola de escritura si había un write pendiente
                    findConnection(gatt)?.onWriteResult(false)
                    cleanupConnection(gatt)
                    gatt.close()
                    if (connectedClients.isEmpty()) {
                        _state.value = TransportState.SCANNING
                    }
                    // Reconexión automática: si la conexión estaba lista, intentar reconectar
                    // tras un breve delay. El scanning también puede detectarlo, pero esto
                    // acelera la recuperación.
                    if (wasReady) {
                        scope.launch {
                            delay(1500)  // dar tiempo al stack BLE para estabilizar
                            // Solo reconectar si no hay ya una conexión nueva a este peer
                            if (connNodeId != null && !connectedClients.containsKey(connNodeId)
                                && !connectingMacs.containsKey(connDevice.address)) {
                                Log.i(TAG, "Reconectando a $connHandleHint ($connNodeId)...")
                                connectToPeer(connDevice, connNodeId, connHandleHint ?: "?")
                            }
                        }
                    }
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                Log.i(TAG, "MTU negociado: $mtu para ${gatt.device.address}")
                gatt.discoverServices()
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.e(TAG, "Error descubriendo servicios en ${gatt.device.address}: status=$status")
                    return
                }
                Log.i(TAG, "Servicios descubiertos en ${gatt.device.address}")

                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    Log.e(TAG, "Servicio Lock-Chat NO encontrado en ${gatt.device.address} tras discoverServices")
                    return
                }

                // ── Registrar la conexión como "ready" inmediatamente si tenemos nodeIdHint ──
                // No esperamos a CHAR_INFO para no bloquear el envío. El manufacturer data
                // del advertising suele estar disponible; si se truncó, CHAR_INFO lo corregirá.
                if (nodeIdHint != null) {
                    Log.i(TAG, "Registrando conexión con nodeIdHint=$nodeIdHint (CHAR_INFO corregirá si difiere)")
                    registerConnection(gatt, nodeIdHint, handleHint)
                } else {
                    Log.w(TAG, "Sin nodeIdHint del advertising — intentando CHAR_INFO o esperando primer mensaje")
                }

                // ── Encolar operaciones GATT de forma secuencial ──
                // BLE solo permite UNA operación GATT a la vez. Secuencia:
                //   1. writeDescriptor (suscripción a notificaciones TX)
                //   2. onDescriptorWrite → readCharacteristic (CHAR_INFO)
                val charTx = service.getCharacteristic(CHAR_TX_UUID)
                if (charTx != null) {
                    gatt.setCharacteristicNotification(charTx, true)
                    val desc = charTx.getDescriptor(CCCD_UUID)
                    if (desc != null) {
                        val writeOk: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val rc = gatt.writeDescriptor(desc, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                            Log.i(TAG, "writeDescriptor (API33+) → status=$rc")
                            rc == BluetoothGatt.GATT_SUCCESS
                        } else {
                            @Suppress("DEPRECATION")
                            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            @Suppress("DEPRECATION")
                            val ok = gatt.writeDescriptor(desc)
                            Log.i(TAG, "writeDescriptor (legacy) → $ok")
                            ok
                        }
                        if (!writeOk) {
                            Log.w(TAG, "writeDescriptor falló — las notificaciones TX pueden no llegar")
                        }
                    } else {
                        Log.w(TAG, "CCCD no encontrado en CHAR_TX — sin notificaciones TX")
                    }
                } else {
                    Log.w(TAG, "CHAR_TX no encontrada en el servicio remoto")
                }
            }

            // ── Tras completarse el writeDescriptor, leer CHAR_INFO ──
            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                Log.i(TAG, "onDescriptorWrite status=$status para ${gatt.device.address}")
                if (descriptor.uuid == CCCD_UUID && status == BluetoothGatt.GATT_SUCCESS) {
                    // Ahora sí es seguro hacer readCharacteristic
                    val charInfo = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHAR_INFO_UUID)
                    if (charInfo != null) {
                        Log.i(TAG, "Leyendo CHAR_INFO de ${gatt.device.address}")
                        @Suppress("DEPRECATION")
                        val ok = gatt.readCharacteristic(charInfo)
                        Log.i(TAG, "readCharacteristic → $ok")
                    } else {
                        Log.w(TAG, "CHAR_INFO no encontrada — usando nodeIdHint si existe")
                    }
                }
            }

            // ── CHAR_INFO read (API 33+) ──
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                Log.i(TAG, "onCharacteristicRead (API33+) uuid=${characteristic.uuid} status=$status len=${value.size}")
                if (characteristic.uuid == CHAR_INFO_UUID && status == BluetoothGatt.GATT_SUCCESS) {
                    val raw = String(value, Charsets.UTF_8)
                    Log.i(TAG, "CHAR_INFO leído: '$raw'")
                    val p = raw.split(":", limit = 2)
                    if (p.size == 2) {
                        registerConnection(gatt, p[0], p[1])
                    } else if (nodeIdHint != null) {
                        registerConnection(gatt, nodeIdHint, handleHint)
                    }
                }
            }

            // ── CHAR_INFO read (API < 33) ──
            @Suppress("DEPRECATION")
            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.i(TAG, "onCharacteristicRead (legacy) uuid=${characteristic.uuid} status=$status")
                if (characteristic.uuid == CHAR_INFO_UUID && status == BluetoothGatt.GATT_SUCCESS) {
                    @Suppress("DEPRECATION")
                    val raw = String(characteristic.value, Charsets.UTF_8)
                    Log.i(TAG, "CHAR_INFO leído: '$raw'")
                    val p = raw.split(":", limit = 2)
                    if (p.size == 2) {
                        registerConnection(gatt, p[0], p[1])
                    } else if (nodeIdHint != null) {
                        registerConnection(gatt, nodeIdHint, handleHint)
                    }
                }
            }

            // ── ACK de escritura ──
            override fun onCharacteristicWrite(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.i(TAG, "onCharacteristicWrite status=$status (0=SUCCESS) para ${gatt.device.address}")
                findConnection(gatt)?.onWriteResult(status == BluetoothGatt.GATT_SUCCESS)
            }

            // ── Notificación TX recibida (API 33+) ──
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray
            ) {
                handleNotification(gatt, value)
            }

            // ── Notificación TX recibida (API < 33) ──
            @Suppress("DEPRECATION")
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                @Suppress("DEPRECATION")
                handleNotification(gatt, characteristic.value)
            }
        }, BluetoothDevice.TRANSPORT_LE)
    }

    /** Procesa una notificación TX entrante (mensaje del server remoto). */
    private fun handleNotification(gatt: BluetoothGatt, value: ByteArray) {
        val raw = String(value, Charsets.UTF_8)
        val parts = raw.split(":", limit = 3)
        if (parts.size == 3) {
            val (rNodeId, rHandle, content) = parts
            // Si la conexión estaba pendiente, registrarla ahora con el nodeId autoritativo
            if (findConnection(gatt)?.ready != true) {
                registerConnection(gatt, rNodeId, rHandle)
            }
            scope.launch {
                _incomingMessages.emit(IncomingMessage(rNodeId, rHandle, content, gatt.device.address))
            }
        }
    }

    /** Registra una conexión como lista para enviar bajo su nodeId autoritativo. */
    private fun registerConnection(gatt: BluetoothGatt, nodeId: String, handle: String) {
        if (nodeId == localNodeId) {
            // Resultó ser nosotros mismos (eco): cerrar
            Log.w(TAG, "registerConnection: nodeId=$nodeId == localNodeId → cerrando eco")
            cleanupConnection(gatt)
            runCatching { gatt.close() }
            return
        }

        val mac = gatt.device.address
        val conn = pendingConnections.remove(mac) ?: findConnection(gatt)
        if (conn == null) {
            Log.e(TAG, "registerConnection: no se encontró conexión pendiente para ${gatt.device.address}")
            return
        }

        // Si la conexión ya estaba registrada bajo OTRO nodeId (hint del advertising),
        // remover la entrada vieja antes de re-indexar con el nodeId autoritativo.
        val oldEntry = connectedClients.entries.firstOrNull { it.value.gatt === gatt }
        if (oldEntry != null && oldEntry.key != nodeId) {
            Log.i(TAG, "Re-indexando conexión: ${oldEntry.key} → $nodeId (corrección via CHAR_INFO/mensaje)")
            connectedClients.remove(oldEntry.key)
        }

        conn.confirmIdentity(nodeId, handle)
        connectedClients[nodeId] = conn
        _connectedNodeIds.value = connectedClients.keys.toSet()

        val peers = _discoveredPeers.value.toMutableMap()
        peers[nodeId] = PeerInfo(nodeId, handle, conn.device, peers[nodeId]?.rssi ?: 0)
        _discoveredPeers.value = peers

        _state.value = TransportState.CONNECTED
        Log.i(TAG, "Peer registrado: handle=$handle nodeId=$nodeId mac=$mac — listo para enviar")
    }

    /** Limpia una conexión (pending o confirmada) asociada a este gatt. */
    private fun cleanupConnection(gatt: BluetoothGatt) {
        val mac = gatt.device.address
        connectingMacs.remove(mac)
        pendingConnections.remove(mac)?.let { return }

        val entry = connectedClients.entries.firstOrNull { it.value.gatt === gatt }
        if (entry != null) {
            connectedClients.remove(entry.key)
            _connectedNodeIds.value = connectedClients.keys.toSet()
            val peers = _discoveredPeers.value.toMutableMap()
            peers.remove(entry.key)
            _discoveredPeers.value = peers
        }
    }

    private fun findConnection(gatt: BluetoothGatt): GattConnection? {
        connectedClients.values.firstOrNull { it.gatt === gatt }?.let { return it }
        return pendingConnections.values.firstOrNull { it.gatt === gatt }
    }

    // ── Envío ─────────────────────────────────────────────────────────

    /**
     * Envía un mensaje a un contacto específico.
     * @param toNodeId nodeId del destinatario
     * @param content texto del mensaje
     * @return Result.success si se envió y se recibió ACK del GATT server remoto,
     *         failure si no hay conexión, servicios no listos, o el write falló.
     */
    suspend fun sendMessage(toNodeId: String, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.i(TAG, "sendMessage: to=$toNodeId content='${content.take(50)}' connectedClients=${connectedClients.keys}")
        val conn = connectedClients[toNodeId]
        if (conn == null) {
            Log.w(TAG, "sendMessage: Peer $toNodeId NO está en connectedClients. Pendings=${pendingConnections.keys}")
            return@withContext Result.failure(Exception("Peer $toNodeId no conectado"))
        }

        if (!conn.ready) {
            Log.w(TAG, "sendMessage: Peer $toNodeId conectado pero ready=false")
            return@withContext Result.failure(Exception("Peer $toNodeId conectado pero servicios no listos aún"))
        }

        val gatt = conn.gatt
        val service = gatt.getService(SERVICE_UUID)
        if (service == null) {
            Log.e(TAG, "sendMessage: Servicio Lock-Chat no encontrado en el gatt del peer $toNodeId")
            return@withContext Result.failure(Exception("Servicio BLE no encontrado (¿desconectado?)"))
        }
        val charRx = service.getCharacteristic(CHAR_RX_UUID)
        if (charRx == null) {
            Log.e(TAG, "sendMessage: CHAR_RX no encontrada en el peer $toNodeId")
            return@withContext Result.failure(Exception("Característica RX no encontrada"))
        }

        val packet = "$localNodeId:$localHandle:$content".toByteArray(Charsets.UTF_8)
        if (packet.size > MAX_PACKET_SIZE) {
            return@withContext Result.failure(
                Exception("Mensaje demasiado grande (${packet.size} > $MAX_PACKET_SIZE bytes)")
            )
        }

        Log.i(TAG, "sendMessage: encolando write de ${packet.size} bytes a $toNodeId")
        val ok = conn.enqueueWrite(charRx, packet)
        Log.i(TAG, "sendMessage: enqueueWrite → $ok")
        if (ok) Result.success(Unit)
        else Result.failure(Exception("writeCharacteristic falló o sin ACK"))
    }

    fun isConnectedTo(nodeId: String): Boolean = connectedClients.containsKey(nodeId)
    fun connectedNodeIds(): Set<String> = connectedClients.keys.toSet()

    // ── Conexión GATT con cola de escritura ───────────────────────────

    /**
     * Envoltorio de una conexión GATT client. Serializa los writes vía un Mutex
     * (BLE solo permite un write pendiente a la vez) y espera el ACK de
     * onCharacteristicWrite antes de liberar el siguiente.
     */
    private class GattConnection(
        val gatt: BluetoothGatt,
        val device: BluetoothDevice,
        initialNodeIdHint: String?,
        val handleHint: String?
    ) {
        var nodeId: String? = initialNodeIdHint
            private set
        var handle: String? = handleHint
            private set
        var ready: Boolean = false
            private set

        private val writeMutex = Mutex()
        private val ackLock = Any()
        private var pendingAck: CompletableDeferred<Boolean>? = null

        fun confirmIdentity(nodeId: String, handle: String) {
            this.nodeId = nodeId
            this.handle = handle
            this.ready = true
        }

        fun onWriteResult(success: Boolean) {
            synchronized(ackLock) {
                pendingAck?.complete(success)
                pendingAck = null
            }
        }

        suspend fun enqueueWrite(
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ): Boolean = writeMutex.withLock {
            val ack = CompletableDeferred<Boolean>()
            synchronized(ackLock) { pendingAck = ack }

            val initiated: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+: writeCharacteristic(char, value, writeType) devuelve Int (status)
                gatt.writeCharacteristic(
                    characteristic,
                    value,
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                ) == BluetoothGatt.GATT_SUCCESS
            } else {
                @Suppress("DEPRECATION")
                characteristic.value = value
                @Suppress("DEPRECATION")
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(characteristic)
            }

            if (!initiated) {
                synchronized(ackLock) { pendingAck = null }
                return@withLock false
            }

            // Esperar onCharacteristicWrite con timeout (BLE puede drops)
            val result = withTimeoutOrNull(WRITE_ACK_TIMEOUT_MS) { ack.await() }
            synchronized(ackLock) { pendingAck = null }
            result ?: false
        }
    }
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
