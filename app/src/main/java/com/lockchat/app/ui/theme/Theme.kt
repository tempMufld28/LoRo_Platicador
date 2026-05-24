package com.lockchat.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.lightColorScheme

// ─────────────────────────────────────────────────
// Material 3 color schemes (oscuro y claro)
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

private val LightColorScheme = lightColorScheme(
    primary              = Green600,
    onPrimary            = White,
    primaryContainer     = Color(0xFFE8F5E9),
    onPrimaryContainer   = Green900,
    secondary            = Green600,
    onSecondary          = White,
    secondaryContainer   = Color(0xFFF5F5F5),
    onSecondaryContainer = Black,
    tertiary             = Color(0xFFB8860B),
    onTertiary           = White,
    background           = White,
    onBackground         = Black,
    surface              = Color(0xFFF5F5F5),
    onSurface            = Black,
    surfaceVariant       = Color(0xFFE5E5E5),
    onSurfaceVariant     = Color(0xFF333333),
    error                = Red500,
    onError              = White,
    errorContainer       = Color(0xFFFFEBEE),
    onErrorContainer     = Red500,
    outline              = Color(0xFF7F7F7F),
    outlineVariant       = Color(0xFFE5E5E5),
    scrim                = Color(0xCC000000)
)

// ─────────────────────────────────────────────────
// CompositionLocal para los colores semánticos custom
// ─────────────────────────────────────────────────
private val LocalLockChatColors = staticCompositionLocalOf { LockChatDarkColors }
val LocalIsDarkTheme = staticCompositionLocalOf { true }

// ─────────────────────────────────────────────────
// Objeto de acceso global al tema
// Uso: LockChatTheme.colors.primary
// ─────────────────────────────────────────────────
object LockChatTheme {
    val colors: LockChatColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLockChatColors.current
        
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current
}

// ─────────────────────────────────────────────────
// Composable de tema principal
// ─────────────────────────────────────────────────
@Composable
fun LockChatTheme(
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkMode) LockChatDarkColors else LockChatLightColors
    val scheme = if (isDarkMode) TerminalColorScheme else LightColorScheme

    CompositionLocalProvider(
        LocalLockChatColors provides colors,
        LocalIsDarkTheme provides isDarkMode
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography  = LockChatTypography,
            content     = content
        )
    }
}
