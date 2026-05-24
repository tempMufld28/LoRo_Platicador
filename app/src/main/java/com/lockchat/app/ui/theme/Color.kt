package com.lockchat.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────
// Lock-Chat — Paleta de colores  (modo terminal)
// ─────────────────────────────────────────────────

/** Negro puro — fondo principal */
val Black         = Color(0xFF000000)

/** Verde terminal principal */
val Green400      = Color(0xFF00FF41)

/** Verde oscuro — superficies elevadas, chips activos */
val Green900      = Color(0xFF003B0F)

/** Verde medio — texto secundario */
val Green600      = Color(0xFF00CC33)

/** Verde tenue — labels de sección */
val Green200      = Color(0xFF80FF90)

/** Gris oscuro — superficies de cards / inputs */
val Gray900       = Color(0xFF0D0D0D)

/** Gris medio — burbujas entrantes, items */
val Gray800       = Color(0xFF1A1A1A)

/** Gris — texto deshabilitado / timestamps */
val Gray500       = Color(0xFF4D4D4D)

/** Gris claro — texto secundario */
val Gray300       = Color(0xFF9E9E9E)

/** Blanco — texto principal en dark mode */
val White         = Color(0xFFFFFFFF)

/** Rojo — errores, TIMEOUT en ping, mensajes FALLIDOS */
val Red500        = Color(0xFFFF3333)

/** Rojo oscuro — fondo del botón detener ping */
val Red900        = Color(0xFF3B0000)

/** Amarillo — advertencias (pérdida de paquetes) */
val Yellow400     = Color(0xFFFFD700)

/** Amarillo oscuro — fondo de banner de advertencia */
val Yellow900     = Color(0xFF3B2D00)

// ─────────────────────────────────────────────────
// Color tokens semánticos (referencias globales)
// ─────────────────────────────────────────────────
data class LockChatColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryDim: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val error: Color,
    val warning: Color,
    val warningContainer: Color,
    val online: Color,
    val outline: Color
)

val LockChatDarkColors = LockChatColors(
    background        = Black,
    surface           = Gray900,
    surfaceVariant    = Gray800,
    primary           = Green400,
    primaryDim        = Green600,
    onPrimary         = Black,
    onBackground      = White,
    onSurface         = White,
    onSurfaceVariant  = Gray300,
    error             = Red500,
    warning           = Yellow400,
    warningContainer  = Yellow900,
    online            = Green400,
    outline           = Gray500
)

val LockChatLightColors = LockChatColors(
    background        = White,
    surface           = Color(0xFFF5F5F5),
    surfaceVariant    = Color(0xFFE5E5E5),
    primary           = Green600, // Un verde más oscuro y legible sobre blanco
    primaryDim        = Green900,
    onPrimary         = White,
    onBackground      = Black,
    onSurface         = Black,
    onSurfaceVariant  = Color(0xFF333333),
    error             = Red500,
    warning           = Color(0xFFB8860B), // Dorado oscuro legible
    warningContainer  = Color(0xFFFFF9C4),
    online            = Green600,
    outline           = Color(0xFF7F7F7F)
)
