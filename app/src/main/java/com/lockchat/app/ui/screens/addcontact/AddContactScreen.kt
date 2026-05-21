package com.lockchat.app.ui.screens.addcontact

import android.graphics.Bitmap
import android.graphics.Color
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.lockchat.app.ui.theme.LockChatTheme
import com.lockchat.app.ui.theme.TerminalFontFamily
import java.util.concurrent.Executors

@Composable
fun AddContactScreen(
    viewModel: AddContactViewModel = hiltViewModel(),
    onContactAdded: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onContactAdded()
    }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás",
                    tint = LockChatTheme.colors.primary)
            }
            Text(
                text = "Agregar Contacto",
                style = MaterialTheme.typography.titleMedium,
                color = LockChatTheme.colors.onBackground,
                fontFamily = TerminalFontFamily
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = state.activeTab,
            containerColor   = LockChatTheme.colors.surface,
            contentColor     = LockChatTheme.colors.primary
        ) {
            Tab(
                selected = state.activeTab == 0,
                onClick  = { viewModel.onTabSelected(0) },
                text     = {
                    Text(
                        "ESCANEAR QR",
                        fontFamily = TerminalFontFamily,
                        fontSize   = 12.sp,
                        color      = if (state.activeTab == 0) LockChatTheme.colors.primary
                                     else LockChatTheme.colors.outline
                    )
                }
            )
            Tab(
                selected = state.activeTab == 1,
                onClick  = { viewModel.onTabSelected(1) },
                text     = {
                    Text(
                        "MI QR",
                        fontFamily = TerminalFontFamily,
                        fontSize   = 12.sp,
                        color      = if (state.activeTab == 1) LockChatTheme.colors.primary
                                     else LockChatTheme.colors.outline
                    )
                }
            )
        }

        // Contenido por tab
        when (state.activeTab) {
            0 -> ScanQrTab(
                previewContact = state.previewContact,
                errorMessage   = state.errorMessage,
                isLoading      = state.isLoading,
                onQrScanned    = viewModel::onQrScanned,
                onConfirm      = viewModel::onConfirmContact,
                onCancel       = viewModel::onCancelPreview,
                onErrorDismiss = viewModel::onErrorDismissed
            )
            1 -> MyQrTab(qrData = state.myQrData)
        }
    }
}

// ── Tab: Escanear QR ──────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun ScanQrTab(
    previewContact: com.lockchat.app.domain.model.Contact?,
    errorMessage: String?,
    isLoading: Boolean,
    onQrScanned: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onErrorDismiss: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            if (previewContact == null) {
                // Cámara para escanear
                QrCameraPreview(onQrDetected = onQrScanned)

                // Overlay de guía
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .border(2.dp, LockChatTheme.colors.primary, RoundedCornerShape(12.dp))
                    )
                    Text(
                        text = "Apunta al QR del contacto",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontFamily = TerminalFontFamily,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 80.dp)
                            .background(
                                LockChatTheme.colors.background.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                // Preview del contacto a agregar
                ContactPreviewCard(
                    contact   = previewContact,
                    isLoading = isLoading,
                    onConfirm = onConfirm,
                    onCancel  = onCancel
                )
            }
        } else {
            // UI de Solicitud de Permiso estilo Terminal retro
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LockChatTheme.colors.background)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = LockChatTheme.colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LockChatTheme.colors.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "[ERROR: CAMERA_ACCESS_REQUIRED]",
                            color = LockChatTheme.colors.error,
                            fontFamily = TerminalFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Se requiere acceso a la cámara para escanear y decodificar códigos QR de tus contactos.",
                            color = LockChatTheme.colors.onSurfaceVariant,
                            fontFamily = TerminalFontFamily,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LockChatTheme.colors.primary,
                                contentColor = LockChatTheme.colors.onPrimary
                            )
                        ) {
                            Text(
                                text = "> CONCEDER ACCESO",
                                fontFamily = TerminalFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Error snackbar
        errorMessage?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = onErrorDismiss) {
                        Text("OK", color = LockChatTheme.colors.primary, fontFamily = TerminalFontFamily)
                    }
                },
                containerColor = LockChatTheme.colors.surfaceVariant
            ) {
                Text(msg, color = LockChatTheme.colors.error, fontFamily = TerminalFontFamily)
            }
        }
    }
}

@Composable
private fun QrCameraPreview(onQrDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var alreadyDetected by remember { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val scanner = BarcodeScanning.getClient()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { ia ->
                        ia.setAnalyzer(executor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null && !alreadyDetected) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage, imageProxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                                            ?.rawValue?.let { raw ->
                                                if (!alreadyDetected) {
                                                    alreadyDetected = true
                                                    onQrDetected(raw)
                                                }
                                            }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                runCatching {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, analysis
                    )
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}

@Composable
private fun ContactPreviewCard(
    contact: com.lockchat.app.domain.model.Contact,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "¿Agregar contacto?",
            style = MaterialTheme.typography.headlineSmall,
            color = LockChatTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(LockChatTheme.colors.surfaceVariant)
                .border(2.dp, LockChatTheme.colors.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.initials,
                color = LockChatTheme.colors.primary,
                fontFamily = TerminalFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = contact.handle,
            style = MaterialTheme.typography.titleLarge,
            color = LockChatTheme.colors.onBackground,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = contact.nodeIdFormatted,
            style = MaterialTheme.typography.labelSmall,
            color = LockChatTheme.colors.outline,
            fontFamily = TerminalFontFamily
        )

        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick  = onCancel,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(4.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = LockChatTheme.colors.error)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("CANCELAR", fontFamily = TerminalFontFamily)
            }

            Button(
                onClick  = onConfirm,
                enabled  = !isLoading,
                modifier = Modifier.weight(1f),
                shape    = RoundedCornerShape(4.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = LockChatTheme.colors.primary,
                    contentColor   = LockChatTheme.colors.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = LockChatTheme.colors.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AGREGAR", fontFamily = TerminalFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Tab: Mi QR ────────────────────────────────────────────────────────

@Composable
private fun MyQrTab(qrData: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tu código QR",
            style = MaterialTheme.typography.headlineSmall,
            color = LockChatTheme.colors.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Muéstraselo a quien quiera agregarte",
            style = MaterialTheme.typography.bodySmall,
            color = LockChatTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        if (qrData.isNotBlank()) {
            QrCodeImage(
                data    = qrData,
                size    = 280,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp)
            )
        } else {
            CircularProgressIndicator(color = LockChatTheme.colors.primary)
        }
    }
}

@Composable
private fun QrCodeImage(data: String, size: Int, modifier: Modifier = Modifier) {
    val bitmap = remember(data) {
        runCatching {
            val bits = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        }.getOrNull()
    }

    if (bitmap != null) {
        Image(
            bitmap      = bitmap.asImageBitmap(),
            contentDescription = "Tu código QR",
            modifier    = modifier.size(size.dp)
        )
    }
}
