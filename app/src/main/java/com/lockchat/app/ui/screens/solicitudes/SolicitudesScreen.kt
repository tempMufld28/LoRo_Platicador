package com.lockchat.app.ui.screens.solicitudes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lockchat.app.data.repository.Solicitud
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily

@Composable
fun SolicitudesScreen(
    viewModel: SolicitudesViewModel = hiltViewModel(),
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
                    text = "SOLICITUDES",
                    style = MaterialTheme.typography.titleMedium,
                    color = LockChatTheme.colors.onBackground,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = LockChatTheme.colors.background
    ) { innerPadding ->
        if (state.solicitudes.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sin solicitudes pendientes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LockChatTheme.colors.outline,
                    fontFamily = TerminalFontFamily
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(state.solicitudes, key = { it.nodeId }) { sol ->
                    SolicitudItem(
                        solicitud = sol,
                        onAccept = { viewModel.onAccept(sol) },
                        onReject = { viewModel.onReject(sol) }
                    )
                    HorizontalDivider(
                        color = LockChatTheme.colors.outline.copy(alpha = 0.15f),
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun SolicitudItem(
    solicitud: Solicitud,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LockChatTheme.colors.surfaceVariant)
                    .border(1.dp, LockChatTheme.colors.outline, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = solicitud.initials,
                    color = LockChatTheme.colors.primary,
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = solicitud.handle,
                        style = MaterialTheme.typography.titleSmall,
                        color = LockChatTheme.colors.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (solicitud.count > 1) {
                        Text(
                            text = "x${solicitud.count}",
                            color = LockChatTheme.colors.primary,
                            fontFamily = TerminalFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = solicitud.nodeIdFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = LockChatTheme.colors.outline
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = solicitud.lastContent,
            style = MaterialTheme.typography.bodySmall,
            color = LockChatTheme.colors.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LockChatTheme.colors.primary,
                    contentColor = LockChatTheme.colors.onPrimary
                )
            ) {
                Text(
                    "ACEPTAR",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LockChatTheme.colors.outline
                )
            ) {
                Text(
                    "RECHAZAR",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
