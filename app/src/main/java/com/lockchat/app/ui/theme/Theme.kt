package com.lockchat.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────
// Material 3 color scheme (oscuro — único modo)
// ─────────────────────────────────────────────────
private val TerminalColorScheme = darkColorScheme(
    primary              = Green400,
    onPrimary            = Black,
    primaryContainer     = Green900,
    onPrimaryContainer   = Green200,
    secondary            = Green600,
    onSecondary          = Black,
    secondaryContainer   = Gray800,
    onSecondaryContainer = White,
    tertiary             = Yellow400,
    onTertiary           = Black,
    background           = Black,
    onBackground         = White,
    surface              = Gray900,
    onSurface            = White,
    surfaceVariant       = Gray800,
    onSurfaceVariant     = Gray300,
    error                = Red500,
    onError              = Black,
    errorContainer       = Red900,
    onErrorContainer     = Red500,
    outline              = Gray500,
    outlineVariant       = Gray800,
    scrim                = Color(0xCC000000)
)

// ─────────────────────────────────────────────────
// CompositionLocal para los colores semánticos custom
// ─────────────────────────────────────────────────
private val LocalLockChatColors = staticCompositionLocalOf { LockChatDarkColors }

// ─────────────────────────────────────────────────
// Objeto de acceso global al tema
// Uso: LockChatTheme.colors.primary
// ─────────────────────────────────────────────────
object LockChatTheme {
    val colors: LockChatColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLockChatColors.current
}

// ─────────────────────────────────────────────────
// Composable de tema principal
// ─────────────────────────────────────────────────
@Composable
fun LockChatTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLockChatColors provides LockChatDarkColors) {
        MaterialTheme(
            colorScheme = TerminalColorScheme,
            typography  = LockChatTypography,
            content     = content
        )
    }
}
