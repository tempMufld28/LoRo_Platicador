package com.lockchat.app.data.transport.lora

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.lockchat.app.domain.model.TransportState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LoRaUsbTransport — transporte serial USB para módulos LoRa (ESP32 + SX127x/SX126x).
 *
 * Estado actual: STUB funcional.
 * - Detecta si hay algún dispositivo USB serial conectado (CP2102, CH340, FTDI)
 * - Si hay → reporta disponibilidad via [isLoRaAvailable]
 * - Envío real: pendiente (se implementará cuando haya el hardware)
 *
 * Protocolo futuro (AT commands al ESP32):
 *   Enviar: AT+SEND=nodeId,len,data\r\n
 *   Recibir: +RCV=rssi,snr,nodeId,len,data\r\n
 */
@Singleton
class LoRaUsbTransport @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LoRaUsbTransport"
        const val BAUD_RATE = 9600
    }

    private val _state = MutableStateFlow(TransportState.DISCONNECTED)
    val state: StateFlow<TransportState> = _state.asStateFlow()

    private val _isLoRaAvailable = MutableStateFlow(false)
    val isLoRaAvailable: StateFlow<Boolean> = _isLoRaAvailable.asStateFlow()

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    /**
     * Intenta detectar un dispositivo USB serial compatible.
     * Llamado al inicio y cuando se conecta/desconecta un dispositivo USB.
     */
    fun detectDevice() {
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)

        if (availableDrivers.isEmpty()) {
            _isLoRaAvailable.value = false
            _state.value = TransportState.DISCONNECTED
            Log.i(TAG, "No se detectó dispositivo USB serial")
            return
        }

        val driver = availableDrivers.first()
        val device = driver.device
        Log.i(TAG, "Dispositivo USB detectado: ${device.deviceName} (${device.manufacturerName})")

        if (!usbManager.hasPermission(device)) {
            Log.w(TAG, "Sin permiso USB — esperar solicitud de usuario")
            _isLoRaAvailable.value = false
            return
        }

        // Dispositivo detectado y con permiso — reportar disponibilidad
        _isLoRaAvailable.value = true
        _state.value = TransportState.CONNECTING
        Log.i(TAG, "LoRa USB disponible: ${device.productName}")

        // TODO (fase LoRa): abrir puerto serial, handshake AT, iniciar lectura
        // val connection = usbManager.openDevice(device) ?: return
        // val port = driver.ports.first()
        // port.open(connection)
        // port.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        // startReadLoop(port)
    }

    /**
     * Envío de mensaje — NO implementado todavía.
     * Devuelve failure explicativo para que la UI muestre el mensaje correcto.
     */
    suspend fun sendMessage(toNodeId: String, content: String): Result<Unit> {
        return if (_isLoRaAvailable.value) {
            // TODO: enviar via puerto serial
            Result.failure(Exception("LoRa detectado pero envío no implementado aún"))
        } else {
            Result.failure(Exception("LoRa no disponible — conecta el módulo ESP32 via USB"))
        }
    }

    fun onDeviceDetached() {
        _isLoRaAvailable.value = false
        _state.value = TransportState.DISCONNECTED
        Log.i(TAG, "Dispositivo USB desconectado")
    }
}
