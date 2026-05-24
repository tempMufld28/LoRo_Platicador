package com.lockchat.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lockchat.app.data.local.ThemePreferences
import com.lockchat.app.service.MeshForegroundService
import com.lockchat.app.ui.navigation.LockChatNavGraph
import com.lockchat.app.ui.theme.LockChatTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity — único punto de entrada de la app.
 *
 * Responsabilidades:
 *   1. Render del árbol Compose (tema, navegación)
 *   2. Arrancar MeshForegroundService para activar el transporte BLE/LoRa
 *   3. Verificar Bluetooth activo en cada onResume
 *
 * enableEdgeToEdge() + systemBarsPadding() en el NavGraph:
 *   - Surface ocupa toda la pantalla (fondo llega al borde)
 *   - El contenido navegable respeta status bar y navigation bar
 *   - Ninguna pantalla individual necesita gestionar insets
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var themePreferences: ThemePreferences

    /** Flag para evitar re-lanzar el servicio si ya fue arrancado en esta sesión */
    private var serviceStarted = false

    /**
     * Launcher para solicitar POST_NOTIFICATIONS en Android 13+.
     * Si se concede, arranca el servicio de transporte.
     */
    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                launchTransportService()
            } else {
                Log.w(TAG, "Permiso de notificaciones denegado — el servicio de transporte no se iniciará")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
            LockChatTheme(isDarkMode = isDarkMode) {
                // Surface cubre todo (color de fondo hasta los bordes)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LockChatTheme.colors.background
                ) {
                    // El NavGraph respeta los insets del sistema (status bar, nav bar)
                    LockChatNavGraph(
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            }
        }

        // Arrancar el servicio de transporte BLE/LoRa (con chequeo de permisos)
        startTransportService()
    }

    override fun onResume() {
        super.onResume()
        ensureBluetoothEnabled()
        // Re-arrancar servicio si por alguna razón se detuvo
        startTransportService()
    }

    /**
     * Verifica permisos y arranca el servicio de transporte.
     *
     * En Android 13+ (API 33): POST_NOTIFICATIONS es obligatorio para
     * que startForeground() funcione sin crashear. Si no lo tiene,
     * lo solicita al usuario.
     *
     * En Android 12 y anteriores: el permiso no existe, se inicia directamente.
     */
    private fun startTransportService() {
        if (serviceStarted) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requiere POST_NOTIFICATIONS para foreground services
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    launchTransportService()
                }
                else -> {
                    // Solicitar permiso — el resultado se maneja en notifPermissionLauncher
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 y anteriores: no necesita permiso de notificaciones
            launchTransportService()
        }
    }

    /**
     * Arranca MeshForegroundService como foreground service.
     * El servicio llama a TransportManager.start() que activa:
     *   - BLE GATT Server (recibe mensajes)
     *   - BLE Advertising (se anuncia a otros nodos)
     *   - BLE Scanning (descubre nodos cercanos)
     *   - Auto-conexión GATT Client a peers descubiertos
     */
    private fun launchTransportService() {
        if (serviceStarted) return
        try {
            val intent = Intent(this, MeshForegroundService::class.java)
            startForegroundService(intent)
            serviceStarted = true
            Log.i(TAG, "MeshForegroundService iniciado correctamente")
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo iniciar el servicio de transporte", e)
        }
    }

    /**
     * Verifica si el adaptador Bluetooth está habilitado.
     * Si no lo está, solicita al usuario que lo encienda mediante el diálogo del sistema.
     */
    private fun ensureBluetoothEnabled() {
        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                startActivity(enableBtIntent)
            } catch (e: SecurityException) {
                // Falta de permisos en tiempo de ejecución, se solicitarán en el flujo normal
            }
        }
    }
}

