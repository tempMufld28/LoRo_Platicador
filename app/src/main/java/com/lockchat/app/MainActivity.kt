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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.lockchat.app.data.local.ThemePreferences
import com.lockchat.app.data.notification.MessageNotifier
import com.lockchat.app.service.MeshForegroundService
import com.lockchat.app.ui.navigation.LockChatNavGraph
import com.lockchat.app.ui.navigation.Routes
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
 * IMPORTANTE: En targetSdk 35, un foreground service con tipo connectedDevice
 * requiere que los permisos BLE de runtime (BLUETOOTH_SCAN, BLUETOOTH_CONNECT,
 * BLUETOOTH_ADVERTISE) estén concedidos ANTES de llamar startForeground().
 * Por eso, el servicio NO se arranca hasta que el onboarding conceda esos permisos.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var messageNotifier: MessageNotifier

    /** Flag para evitar re-lanzar el servicio si ya fue arrancado en esta sesión */
    private var serviceStarted = false

    /** nodeId del contacto que se debe abrir al arrancar (vía notificación) */
    private var pendingContactId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Capturar el contactId si la activity fue lanzada desde una notificación
        pendingContactId = intent?.getStringExtra(MessageNotifier.EXTRA_CONTACT_ID)
        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)
            LockChatTheme(isDarkMode = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = LockChatTheme.colors.background
                ) {
                    LockChatNavGraph(
                        modifier = Modifier.systemBarsPadding(),
                        pendingContactId = pendingContactId
                    )
                }
            }
        }

        // Intentar arrancar el servicio si ya tenemos permisos
        tryStartTransportService()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val contactId = intent.getStringExtra(MessageNotifier.EXTRA_CONTACT_ID)
        if (contactId != null) {
            pendingContactId = contactId
            // Forzar recomposición notificando el cambio
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        ensureBluetoothEnabled()
        // Re-intentar en cada onResume — al volver del onboarding con permisos
        // concedidos, esto arrancará el servicio automáticamente.
        tryStartTransportService()
    }

    /**
     * Intenta arrancar MeshForegroundService.
     *
     * Solo arranca si TODOS los permisos requeridos están concedidos:
     *   - BLUETOOTH_CONNECT (runtime, requerido por foregroundServiceType=connectedDevice)
     *   - POST_NOTIFICATIONS (Android 13+, requerido para mostrar la notificación)
     *
     * Si faltan permisos, simplemente no arranca. El onboarding se encarga de
     * solicitarlos, y al volver a onResume() se reintentará automáticamente.
     */
    private fun tryStartTransportService() {
        if (serviceStarted) return

        // 1. Permisos BLE runtime (API 31+). En API < 31 son permisos normal de instalación
        //    y se conceden al instalar, así que no requieren verificación en runtime.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blePermissions = listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
            val missing = blePermissions.firstOrNull {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing != null) {
                Log.d(TAG, "Sin permiso $missing — servicio diferido hasta onboarding")
                return
            }
        }

        // 2. Permiso de notificaciones (Android 13+, requerido para mostrar la notificación
        //    persistente del foreground service connectedDevice).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotifPermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotifPermission) {
                Log.d(TAG, "Sin permiso POST_NOTIFICATIONS — servicio diferido")
                return
            }
        }

        // 3. Todos los permisos concedidos → arrancar
        launchTransportService()
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
        // Solo solicitar si ya tenemos permiso BLUETOOTH_CONNECT
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
        if (adapter != null && !adapter.isEnabled) {
            val enableBtIntent = Intent(android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE)
            try {
                startActivity(enableBtIntent)
            } catch (_: SecurityException) {
                // Permiso revocado entre el check y la llamada — ignorar
            }
        }
    }
}
