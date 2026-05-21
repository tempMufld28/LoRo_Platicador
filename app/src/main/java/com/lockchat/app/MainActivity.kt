package com.lockchat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lockchat.app.ui.navigation.LockChatNavGraph
import com.lockchat.app.ui.theme.LockChatTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — único punto de entrada de la app.
 *
 * enableEdgeToEdge() + systemBarsPadding() en el NavGraph:
 *   - Surface ocupa toda la pantalla (fondo llega al borde)
 *   - El contenido navegable respeta status bar y navigation bar
 *   - Ninguna pantalla individual necesita gestionar insets
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockChatTheme {
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
    }
}
