package com.lockchat.app.ui.screens.ping

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lockchat.app.domain.model.LatencySample
import com.lockchat.app.domain.model.NodeUi
import com.lockchat.app.domain.model.PacketLogEntry
import com.lockchat.app.domain.model.PingStats
import com.lockchat.app.ui.theme.Green400
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.Red500
import com.lockchat.app.ui.theme.TerminalFontFamily
import com.lockchat.app.ui.components.SignalWavesIcon

@Composable
fun PingScreen(
    viewModel: PingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LockChatTheme.colors.background)
    ) {
        // TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LockChatTheme.colors.surface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = null,
                    tint = LockChatTheme.colors.primary)
            }
            Column {
                Text("PING DE NODO", style = MaterialTheme.typography.titleMedium,
                    color = LockChatTheme.colors.onBackground)
                Text("Prueba de latencia con contacto seleccionado",
                    style = MaterialTheme.typography.labelSmall,
                    color = LockChatTheme.colors.outline)
            }
        }

        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Node selector
            item {
                SectionLabel("NODO DESTINO")
                Spacer(Modifier.height(8.dp))
                NodeSelector(
                    selectedNode   = state.selectedNode,
                    availableNodes = state.availableNodes,
                    onNodeSelected = viewModel::onNodeSelected
                )
            }

            // Control button
            item {
                PingControlButton(
                    isRunning = state.isRunning,
                    onClick   = if (state.isRunning) viewModel::onStopPing else viewModel::onStartPing
                )
            }

            // Status row (solo si running)
            if (state.isRunning || state.stats.sent > 0) {
                item {
                    PingStatusRow(
                        currentLatencyMs = state.currentLatencyMs,
                        statusText       = if (state.isRunning) "Enviando paquetes..."
                        else "Ping completado"
                    )
                }
            }

            // Stats grid
            if (state.stats.sent > 0) {
                item {
                    SectionLabel("ESTADÍSTICAS")
                    Spacer(Modifier.height(8.dp))
                    PingStatsGrid(stats = state.stats)
                }
            }

            // Warning
            if (state.warning != null) {
                item { PingWarningBanner(message = state.warning!!) }
            }

            // Chart
            if (state.latencySamples.isNotEmpty()) {
                item {
                    SectionLabel("LATENCIA EN TIEMPO REAL")
                    Spacer(Modifier.height(8.dp))
                    LatencyChart(
                        samples = state.latencySamples,
                        avgMs   = state.stats.avgMs
                    )
                }
            }

            // Log
            if (state.packetLog.isNotEmpty()) {
                item { SectionLabel("LOG DE PAQUETES") }
                items(state.packetLog) { entry -> PacketLogRow(entry = entry) }
            }

            // Empty state
            if (!state.isRunning && state.stats.sent == 0) {
                item { PingEmptyState() }
            }
        }
    }
}

@Composable
private fun NodeSelector(
    selectedNode: NodeUi?,
    availableNodes: List<NodeUi>,
    onNodeSelected: (NodeUi) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            color    = LockChatTheme.colors.surface,
            border   = androidx.compose.foundation.BorderStroke(
                1.dp, LockChatTheme.colors.outline.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedNode != null) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                .background(LockChatTheme.colors.surfaceVariant)
                                .then(
                                    if (selectedNode.isOnline)
                                        Modifier.border(2.dp, LockChatTheme.colors.primary, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedNode.handle.take(2).uppercase(),
                                color = LockChatTheme.colors.primary,
                                fontFamily = TerminalFontFamily, fontSize = 12.sp)
                        }
                        Column {
                            Text(selectedNode.handle, style = MaterialTheme.typography.titleSmall,
                                color = LockChatTheme.colors.onBackground)
                            Text(selectedNode.nodeId, style = MaterialTheme.typography.labelSmall,
                                color = LockChatTheme.colors.outline)
                        }
                    }
                } else {
                    Text("Selecciona un nodo...", color = LockChatTheme.colors.outline,
                        fontFamily = TerminalFontFamily)
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null,
                        tint = LockChatTheme.colors.primary)
                }
            }
        }

        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableNodes.forEach { node ->
                DropdownMenuItem(
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape)
                                    .background(if (node.isOnline) LockChatTheme.colors.primary
                                    else LockChatTheme.colors.outline)
                            )
                            Column {
                                Text(node.handle, fontFamily = TerminalFontFamily)
                                Text(node.nodeId, style = MaterialTheme.typography.labelSmall,
                                    color = LockChatTheme.colors.outline)
                            }
                        }
                    },
                    onClick = { onNodeSelected(node); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun PingControlButton(isRunning: Boolean, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(4.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor = if (isRunning) LockChatTheme.colors.error
            else LockChatTheme.colors.primary,
            contentColor   = if (isRunning) Color.White else LockChatTheme.colors.onPrimary
        )
    ) {
        Text(
            text       = if (isRunning) "⬛  DETENER PING" else "▶  INICIAR PING",
            fontFamily = TerminalFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
    }
}

@Composable
private fun PingStatusRow(currentLatencyMs: Int?, statusText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                .background(LockChatTheme.colors.primary))
            Text(statusText, fontFamily = TerminalFontFamily,
                color = LockChatTheme.colors.onSurfaceVariant, fontSize = 13.sp)
        }
        if (currentLatencyMs != null) {
            Text("${currentLatencyMs}ms", fontFamily = TerminalFontFamily,
                color = LockChatTheme.colors.warning, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun PingStatsGrid(stats: PingStats) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("Enviados", "${stats.sent} pkts", Modifier.weight(1f))
            StatCell("Recibidos", "${stats.received} pkts", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("Mínimo", stats.minMs?.let { "${it} ms" } ?: "—", Modifier.weight(1f))
            StatCell("Máximo", stats.maxMs?.let { "${it} ms" } ?: "—", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCell("Promedio", stats.avgMs?.let { "${it} ms" } ?: "—", Modifier.weight(1f))
            StatCell("Pérdida", "${stats.lossPercent.toInt()} %",
                Modifier.weight(1f),
                valueColor = if (stats.lossPercent > 15f) LockChatTheme.colors.error
                else LockChatTheme.colors.onBackground
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = LockChatTheme.colors.onBackground
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(6.dp),
        color = LockChatTheme.colors.surface) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall,
                color = valueColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PingWarningBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(6.dp),
        color    = LockChatTheme.colors.warningContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠", fontSize = 16.sp)
            Text(message, style = MaterialTheme.typography.bodySmall,
                color = LockChatTheme.colors.warning)
        }
    }
}

@Composable
private fun LatencyChart(samples: List<LatencySample>, avgMs: Int?) {
    val green = Green400
    val red   = Red500
    val gridColor = Color(0xFF1A1A1A)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(LockChatTheme.colors.surface, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val maxY = 250f; val minY = 0f
            val range = maxY - minY
            val n = samples.size.coerceAtLeast(1)

            // Grid lines
            listOf(50f, 100f, 150f, 200f).forEach { ms ->
                val y = h - ((ms - minY) / range) * h
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }

            // Average line (dashed)
            if (avgMs != null) {
                val avgY = h - ((avgMs - minY) / range) * h
                drawLine(
                    color       = green.copy(alpha = 0.4f),
                    start       = Offset(0f, avgY),
                    end         = Offset(w, avgY),
                    strokeWidth = 1.5f,
                    pathEffect  = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }

            // Data points + connecting lines
            val validPoints = samples.mapIndexedNotNull { i, s ->
                if (s.latencyMs != null) {
                    val x = if (n == 1) w / 2 else (i.toFloat() / (n - 1)) * w
                    val y = h - ((s.latencyMs - minY) / range) * h
                    Pair(x, y)
                } else null
            }

            // Connect valid points with green line
            for (i in 1 until validPoints.size) {
                drawLine(green, validPoints[i - 1].let { Offset(it.first, it.second) },
                    validPoints[i].let { Offset(it.first, it.second) }, strokeWidth = 2f)
            }

            // Draw all points
            samples.forEachIndexed { i, sample ->
                val x = if (n == 1) w / 2 else (i.toFloat() / (n - 1)) * w
                if (sample.latencyMs != null) {
                    val y = h - ((sample.latencyMs - minY) / range) * h
                    drawCircle(green, radius = 5f, center = Offset(x, y))
                } else {
                    // TIMEOUT — punto rojo
                    drawCircle(red, radius = 6f, center = Offset(x, h * 0.9f))
                }
            }
        }

        // seq# label
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).padding(top = 2.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text("seq#", style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline, fontSize = 9.sp)
        }
    }
}

@Composable
private fun PacketLogRow(entry: PacketLogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = "${entry.timestamp}  #${entry.seq}  ${entry.nodeId} →",
            style = MaterialTheme.typography.labelSmall,
            color = LockChatTheme.colors.outline,
            fontFamily = TerminalFontFamily
        )
        Text(
            text  = if (entry.isTimeout) "TIMEOUT" else "${entry.latencyMs}ms",
            style = MaterialTheme.typography.labelSmall,
            color = if (entry.isTimeout) LockChatTheme.colors.error
            else LockChatTheme.colors.onBackground,
            fontFamily = TerminalFontFamily,
            fontWeight = if (entry.isTimeout) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun PingEmptyState() {
    Box(
        modifier = Modifier.fillMaxWidth().height(200.dp)
            .background(LockChatTheme.colors.surface, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SignalWavesIcon(
                color = LockChatTheme.colors.outline,
                size = 56.dp
            )
            Spacer(Modifier.height(12.dp))
            Text("Selecciona un contacto e inicia el ping",
                style = MaterialTheme.typography.bodySmall,
                color = LockChatTheme.colors.outline, textAlign = TextAlign.Center)
            Text("Prueba la latencia de tu canal LoRa/BT Mesh",
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        color = LockChatTheme.colors.primary.copy(alpha = 0.7f),
        fontFamily = TerminalFontFamily, letterSpacing = 1.5.sp)
}
