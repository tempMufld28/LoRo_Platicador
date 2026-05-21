package com.lockchat.app.domain.model

import kotlinx.coroutines.flow.StateFlow

// ─────────────────────────────────────────────────
// Transport — interfaz común a todos los transportes
//
// Implementaciones:
//   Fase 2: BleDirectTransport
//   Fase 3: LoRaViaUsbTransport
// ─────────────────────────────────────────────────

interface Transport {
    val state: StateFlow<TransportState>
    suspend fun send(packet: ByteArray): Result<Unit>
    fun setOnPacketReceived(listener: (ByteArray) -> Unit)
    suspend fun connect(): Result<Unit>
    suspend fun disconnect()
}

enum class TransportState {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    CONNECTED,
    ERROR
}

// Estado del TransportManager (UI-facing)
enum class TransportUiState {
    LORA_USB,        // ESP32 conectada vía USB
    BLE_DIRECT,      // Bluetooth directo entre dos Android
    SIN_TRANSPORTE   // Sin conexión activa
}

// ─────────────────────────────────────────────────
// Ping models
// ─────────────────────────────────────────────────

data class PingStats(
    val sent: Int          = 0,
    val received: Int      = 0,
    val minMs: Int?        = null,
    val maxMs: Int?        = null,
    val avgMs: Int?        = null,
    val lossPercent: Float = 0f
)

data class LatencySample(
    val seq: Int,
    val latencyMs: Int?    // null = TIMEOUT
)

data class PacketLogEntry(
    val timestamp: String,  // "11:51:18 a.m."
    val seq: Int,
    val nodeId: String,
    val latencyMs: Int?,    // null = TIMEOUT
    val isTimeout: Boolean
)
