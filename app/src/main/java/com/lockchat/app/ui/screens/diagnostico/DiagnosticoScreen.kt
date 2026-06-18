package com.lockchat.app.ui.screens.diagnostico

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily

@Composable
fun DiagnosticoScreen(
    viewModel: DiagnosticoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LockChatTheme.colors.surface)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = LockChatTheme.colors.primary
                    )
                }
                Text(
                    text = "DIAGNOSTICO BLE",
                    style = MaterialTheme.typography.titleMedium,
                    color = LockChatTheme.colors.onBackground,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = LockChatTheme.colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Identidad local ──
            DiagnosticoSection("IDENTIDAD LOCAL") {
                DiagnosticoRow("handle", state.localHandle)
                DiagnosticoRow("nodeId", state.localNodeId)
            }

            // ── Estado del transporte ──
            DiagnosticoSection("TRANSPORTE") {
                DiagnosticoRow("activo", state.activeTransport.name)
                DiagnosticoRow("bleState", state.bleState.name)
                DiagnosticoRow(
                    "bluetoothOn",
                    if (state.bluetoothEnabled) "SI" else "NO"
                )
            }

            // ── Peers descubiertos ──
            DiagnosticoSection("PEERS DESCUBIERTOS (${state.discoveredPeers.size})") {
                if (state.discoveredPeers.isEmpty()) {
                    Text(
                        "Ninguno — el scanning no detectó dispositivos LC_*",
                        color = LockChatTheme.colors.error,
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp
                    )
                } else {
                    state.discoveredPeers.forEach { (nodeId, peer) ->
                        DiagnosticoRow(
                            "handle=${peer.handle}",
                            "nodeId=$nodeId rssi=${peer.rssi}"
                        )
                    }
                }
            }

            // ── Conexiones GATT activas (salientes / podemos enviar) ──
            DiagnosticoSection("CONEXIONES SALIENTES (${state.connectedNodeIds.size})") {
                if (state.connectedNodeIds.isEmpty()) {
                    Text(
                        "Ninguna — no hay GATT clients listos para enviar. El peer podría estar conectado a nosotros (entrante) pero nosotros no a él.",
                        color = LockChatTheme.colors.error,
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp
                    )
                } else {
                    state.connectedNodeIds.forEach { nodeId ->
                        val handle = state.connectedPeers[nodeId] ?: "?"
                        DiagnosticoRow(nodeId, "handle=$handle")
                    }
                }
            }

            // ── Log ──
            DiagnosticoSection("LOG (${state.logLines.size})") {
                if (state.logLines.isEmpty()) {
                    Text(
                        "(vacío)",
                        color = LockChatTheme.colors.outline,
                        fontFamily = TerminalFontFamily,
                        fontSize = 12.sp
                    )
                } else {
                    state.logLines.forEach { line ->
                        Text(
                            text = line,
                            color = LockChatTheme.colors.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DiagnosticoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = LockChatTheme.colors.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(
                color = LockChatTheme.colors.outline.copy(alpha = 0.2f),
                thickness = 0.5.dp
            )
            content()
        }
    }
}

@Composable
private fun DiagnosticoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = LockChatTheme.colors.outline,
            fontFamily = TerminalFontFamily,
            fontSize = 11.sp
        )
        Text(
            text = value,
            color = LockChatTheme.colors.onBackground,
            fontFamily = TerminalFontFamily,
            fontSize = 11.sp
        )
    }
}
