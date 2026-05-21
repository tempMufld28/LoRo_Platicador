package com.lockchat.app.ui.screens.profile

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.lockchat.app.domain.model.Contact
import com.lockchat.app.ui.screens.chats.BottomNavBar
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateToAddContact: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LockChatTheme.colors.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // TopBar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LockChatTheme.colors.surface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás",
                        tint = LockChatTheme.colors.primary)
                }
                Text(
                    text = "Perfil",
                    style = MaterialTheme.typography.titleMedium,
                    color = LockChatTheme.colors.onBackground,
                    fontFamily = TerminalFontFamily
                )
            }
        }

        // Identidad
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(LockChatTheme.colors.surfaceVariant)
                        .border(2.dp, LockChatTheme.colors.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.initials,
                        color = LockChatTheme.colors.primary,
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Handle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.handle.ifBlank { "sin handle" },
                        style = MaterialTheme.typography.titleLarge,
                        color = LockChatTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick  = viewModel::onEditHandle,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar handle",
                            tint = LockChatTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // nodeId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable {
                        clipboard.setText(AnnotatedString(state.nodeIdFormatted))
                    }
                ) {
                    Text(
                        text = state.nodeIdFormatted.ifBlank { "—" },
                        style = MaterialTheme.typography.labelSmall,
                        color = LockChatTheme.colors.outline,
                        fontFamily = TerminalFontFamily
                    )
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copiar nodeId",
                        tint = LockChatTheme.colors.outline,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // QR Propio
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tu código QR",
                    style = MaterialTheme.typography.labelSmall,
                    color = LockChatTheme.colors.primary,
                    fontFamily = TerminalFontFamily,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (state.qrData.isNotBlank()) {
                    ProfileQrCode(data = state.qrData, size = 200)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Muéstraselo a quien quiera agregarte",
                        style = MaterialTheme.typography.bodySmall,
                        color = LockChatTheme.colors.outline,
                        textAlign = TextAlign.Center
                    )
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = LockChatTheme.colors.primary
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // Botón Agregar contacto
        item {
            Button(
                onClick  = onNavigateToAddContact,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape  = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LockChatTheme.colors.primary,
                    contentColor   = LockChatTheme.colors.onPrimary
                )
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "AGREGAR CONTACTO",
                    fontFamily = TerminalFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        // Sección Contactos
        item {
            Text(
                text = "CONTACTOS (${state.contacts.size})",
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }

        if (state.contacts.isEmpty()) {
            item {
                Text(
                    text = "Sin contactos aún. Agrega uno escaneando su QR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LockChatTheme.colors.outline,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        } else {
            items(state.contacts, key = { it.nodeId }) { contact ->
                ContactListItem(
                    contact  = contact,
                    onDelete = { viewModel.onDeleteContact(contact.nodeId) }
                )
                HorizontalDivider(
                    color     = LockChatTheme.colors.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp
                )
            }
        }
    }

    // Diálogo editar handle
    if (state.isEditingHandle) {
        EditHandleDialog(
            currentHandle = state.editHandleText,
            error         = state.editHandleError,
            onTextChange  = viewModel::onHandleTextChange,
            onConfirm     = viewModel::onHandleSave,
            onDismiss     = viewModel::onDismissEditHandle
        )
    }
}

// ── Componentes ───────────────────────────────────────────────────────

@Composable
private fun ProfileQrCode(data: String, size: Int) {
    val bitmap = remember(data) {
        runCatching {
            val bits = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
            }
            bmp
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap             = bitmap.asImageBitmap(),
            contentDescription = "Tu QR",
            modifier           = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color.White)
                .padding(8.dp)
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LockChatTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initials,
                color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.handle,
                style = MaterialTheme.typography.titleSmall,
                color = LockChatTheme.colors.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = contact.nodeIdFormatted,
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.outline,
                fontFamily = TerminalFontFamily
            )
        }

        // Online indicator
        if (contact.isOnline) {
            Text(
                text = "●",
                color = LockChatTheme.colors.primary,
                fontSize = 10.sp
            )
        }

        IconButton(
            onClick  = { showDeleteConfirm = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Eliminar contacto",
                tint = LockChatTheme.colors.error,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar contacto", fontFamily = TerminalFontFamily) },
            text  = { Text("¿Eliminar a ${contact.handle}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("ELIMINAR", color = LockChatTheme.colors.error, fontFamily = TerminalFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCELAR", color = LockChatTheme.colors.outline, fontFamily = TerminalFontFamily)
                }
            },
            containerColor = LockChatTheme.colors.surface
        )
    }
}

@Composable
private fun EditHandleDialog(
    currentHandle: String,
    error: String?,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = LockChatTheme.colors.surface
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Cambiar handle",
                    style = MaterialTheme.typography.titleMedium,
                    color = LockChatTheme.colors.onBackground,
                    fontFamily = TerminalFontFamily
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value         = currentHandle,
                    onValueChange = onTextChange,
                    isError       = error != null,
                    singleLine    = true,
                    shape         = RoundedCornerShape(4.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = LockChatTheme.colors.primary,
                        unfocusedBorderColor = LockChatTheme.colors.outline,
                        focusedTextColor     = LockChatTheme.colors.onBackground,
                        unfocusedTextColor   = LockChatTheme.colors.onBackground,
                        cursorColor          = LockChatTheme.colors.primary,
                        focusedContainerColor   = LockChatTheme.colors.background,
                        unfocusedContainerColor = LockChatTheme.colors.background
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = TerminalFontFamily),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(
                        text  = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = LockChatTheme.colors.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = LockChatTheme.colors.outline)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        shape   = RoundedCornerShape(4.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = LockChatTheme.colors.primary,
                            contentColor   = LockChatTheme.colors.onPrimary
                        )
                    ) {
                        Text("GUARDAR", fontFamily = TerminalFontFamily)
                    }
                }
            }
        }
    }
}
