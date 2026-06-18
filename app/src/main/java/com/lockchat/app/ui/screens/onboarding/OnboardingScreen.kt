package com.lockchat.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.MultiplePermissionsState
import com.lockchat.app.ui.components.SignalWavesIcon
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onIdentityCreated: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    val requiredPermissions = remember {
        buildList {
            add(android.Manifest.permission.CAMERA)
            add(android.Manifest.permission.BLUETOOTH_SCAN)
            add(android.Manifest.permission.BLUETOOTH_CONNECT)
            add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            // Android 13+: requerido para el foreground service (notificación persistente)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = requiredPermissions
    )

    var showPermissionsStep by remember { mutableStateOf(false) }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            if (multiplePermissionsState.allPermissionsGranted) {
                onIdentityCreated()
            } else {
                showPermissionsStep = true
            }
        }
    }

    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        if (state.isComplete && multiplePermissionsState.allPermissionsGranted) {
            onIdentityCreated()
        }
    }

    if (showPermissionsStep) {
        PermissionsRequestScreen(
            permissionsState = multiplePermissionsState
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LockChatTheme.colors.background)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo / ícono animado pixelado
            SignalWavesIcon(
                size = 80.dp,
                animate = true
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text       = "Lock-Chat",
                style      = MaterialTheme.typography.displaySmall,
                color      = LockChatTheme.colors.onBackground,
                textAlign  = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Text(
                text       = "BLE · LoRa Red",
                style      = MaterialTheme.typography.labelMedium,
                color      = LockChatTheme.colors.primary,
                textAlign  = TextAlign.Center,
                fontFamily = TerminalFontFamily
            )

            Spacer(Modifier.height(56.dp))

            // ── Handle ──────────────────────────────────────
            Text(
                text  = "Elige tu nombre de usuario",
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value         = state.handle,
                onValueChange = viewModel::onHandleChange,
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = {
                    Text(
                        "> ghost_77",
                        color      = LockChatTheme.colors.outline,
                        fontFamily = TerminalFontFamily,
                        fontSize   = 14.sp
                    )
                },
                isError    = state.handleError != null,
                singleLine = true,
                shape      = RoundedCornerShape(4.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboard?.hide()
                        viewModel.onCreateIdentity()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = LockChatTheme.colors.primary,
                    unfocusedBorderColor    = LockChatTheme.colors.outline,
                    errorBorderColor        = LockChatTheme.colors.error,
                    focusedTextColor        = LockChatTheme.colors.onBackground,
                    unfocusedTextColor      = LockChatTheme.colors.onBackground,
                    cursorColor             = LockChatTheme.colors.primary,
                    focusedContainerColor   = LockChatTheme.colors.surface,
                    unfocusedContainerColor = LockChatTheme.colors.surface
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = TerminalFontFamily)
            )

            if (state.handleError != null) {
                Text(
                    text     = state.handleError!!,
                    color    = LockChatTheme.colors.error,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Visible para todos en la red. Solo letras minúsculas, números y _",
                style = MaterialTheme.typography.bodySmall,
                color = LockChatTheme.colors.outline,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(40.dp))

            // ── Botón crear ──────────────────────────────────
            Button(
                onClick  = {
                    keyboard?.hide()
                    viewModel.onCreateIdentity()
                },
                enabled  = state.handle.isNotBlank() && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LockChatTheme.colors.primary,
                    contentColor   = LockChatTheme.colors.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = LockChatTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = "ENTRAR A LA RED",
                        fontFamily = TerminalFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsRequestScreen(
    permissionsState: MultiplePermissionsState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LockChatTheme.colors.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pixelated signal icon animates while asking for permissions
        SignalWavesIcon(
            size = 80.dp,
            animate = true
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text       = "PERMISOS REQUERIDOS",
            style      = MaterialTheme.typography.titleMedium,
            color      = LockChatTheme.colors.primary,
            fontFamily = TerminalFontFamily,
            textAlign  = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text       = "Para operar como un nodo en la Red descentralizada, Lock-Chat necesita acceder a:\n\n" +
                         "1. [CÁMARA]\n   Para escanear códigos QR de tus contactos.\n\n" +
                         "2. [BLUETOOTH]\n   Para descubrir y comunicarte con nodos cercanos sin internet.\n\n" +
                         "3. [NOTIFICACIONES]\n   Para mantener el servicio de red activo en segundo plano.",
            style      = MaterialTheme.typography.bodyMedium,
            color      = LockChatTheme.colors.onBackground,
            fontFamily = TerminalFontFamily
        )

        Spacer(Modifier.height(48.dp))

        Button(
            onClick  = { permissionsState.launchMultiplePermissionRequest() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(4.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = LockChatTheme.colors.primary,
                contentColor   = LockChatTheme.colors.onPrimary
            )
        ) {
            Text(
                text       = "CONCEDER PERMISOS",
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Componente de campo de texto reutilizable (terminal style)
// Exportado para uso en otras pantallas
// ─────────────────────────────────────────────────
@Composable
fun TerminalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    error: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = LockChatTheme.colors.primary,
            fontFamily = TerminalFontFamily
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            placeholder   = {
                Text(hint, color = LockChatTheme.colors.outline, fontFamily = TerminalFontFamily)
            },
            isError       = error != null,
            keyboardOptions = keyboardOptions,
            singleLine    = true,
            shape         = RoundedCornerShape(4.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = LockChatTheme.colors.primary,
                unfocusedBorderColor    = LockChatTheme.colors.outline,
                focusedTextColor        = LockChatTheme.colors.onBackground,
                unfocusedTextColor      = LockChatTheme.colors.onBackground,
                cursorColor             = LockChatTheme.colors.primary,
                errorBorderColor        = LockChatTheme.colors.error,
                focusedContainerColor   = LockChatTheme.colors.surface,
                unfocusedContainerColor = LockChatTheme.colors.surface
            ),
            textStyle = MaterialTheme.typography.bodyMedium
        )
        if (error != null) {
            Text(
                text  = error,
                style = MaterialTheme.typography.labelSmall,
                color = LockChatTheme.colors.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────
// Gate de permisos reutilizable
// Muestra la pantalla de solicitud de permisos si falta
// alguno, o el contenido normal si ya están concedidos.
// Usado por pantallas a las que llegan usuarios que ya
// tienen identidad (y por tanto saltan el onboarding).
// ─────────────────────────────────────────────────
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LockChatPermissionsGate(content: @Composable () -> Unit) {
    val requiredPermissions = remember {
        buildList {
            add(android.Manifest.permission.CAMERA)
            add(android.Manifest.permission.BLUETOOTH_SCAN)
            add(android.Manifest.permission.BLUETOOTH_CONNECT)
            add(android.Manifest.permission.BLUETOOTH_ADVERTISE)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    val permissionsState = rememberMultiplePermissionsState(permissions = requiredPermissions)

    if (permissionsState.allPermissionsGranted) {
        content()
    } else {
        PermissionsRequestScreen(permissionsState = permissionsState)
    }
}
