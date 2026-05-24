package com.lockchat.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lockchat.app.ui.theme.Green400
import com.lockchat.app.ui.theme.Green600
import com.lockchat.app.ui.theme.LockChatTheme

/**
 * SignalWavesIcon — Un icono pixel art premium de terminal dibujado en Canvas que
 * representa una antena/señal mesh con animación de escaneo cíclica.
 */
@Composable
fun SignalWavesIcon(
    modifier: Modifier = Modifier,
    color: Color = LockChatTheme.colors.primary,
    size: Dp = 64.dp,
    animate: Boolean = true
) {
    val isDark = LockChatTheme.isDark
    
    // Configuración de colores activos/inactivos según el modo
    val activeColor = if (isDark) Green400 else Color.White
    val inactiveColor = if (isDark) Color.White else Green600

    val infiniteTransition = rememberInfiniteTransition(label = "signal_scan")
    val frameIndex by if (animate) {
        infiniteTransition.animateValue(
            initialValue = 0,
            targetValue = 5,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "frame_index"
        )
    } else {
        remember { mutableStateOf(0) }
    }

    // Grid de pixel art de 13 de ancho por 10 de alto
    Canvas(modifier = modifier.size(size)) {
        val pixelSize = this.size.width / 13f
        
        // Definición de las partes de la señal
        val dotPixels = listOf(
            Offset(6f, 9f)
        )
        val band1Pixels = listOf(
            Offset(5f, 6f), Offset(6f, 6f), Offset(7f, 6f),
            Offset(4f, 7f), Offset(8f, 7f)
        )
        val band2Pixels = listOf(
            Offset(3f, 3f), Offset(4f, 3f), Offset(5f, 3f), Offset(6f, 3f),
            Offset(7f, 3f), Offset(8f, 3f), Offset(9f, 3f),
            Offset(2f, 4f), Offset(10f, 4f)
        )
        val band3Pixels = listOf(
            Offset(1f, 0f), Offset(2f, 0f), Offset(3f, 0f), Offset(4f, 0f),
            Offset(5f, 0f), Offset(6f, 0f), Offset(7f, 0f), Offset(8f, 0f),
            Offset(9f, 0f), Offset(10f, 0f), Offset(11f, 0f),
            Offset(0f, 1f), Offset(12f, 1f)
        )

        // Determinar qué bandas están activadas para cada frame
        // Frame 0: Todas activas (green)
        // Frame 1: Exterior blanca, interior verde
        // Frame 2: Exteriores blancas, punto central verde
        // Frame 3: Banda media verde, exteriores blancas
        // Frame 4: Todas inactivas/blancas
        val isDotActive = when (frameIndex) {
            4 -> false
            else -> true
        }
        val isBand1Active = when (frameIndex) {
            0, 1, 2 -> true
            else -> false
        }
        val isBand2Active = when (frameIndex) {
            0, 1 -> true
            3 -> true
            else -> false
        }
        val isBand3Active = when (frameIndex) {
            0 -> true
            else -> false
        }

        // Función helper para dibujar un set de píxeles
        fun drawPixelGroup(pixels: List<Offset>, isActive: Boolean) {
            val drawColor = if (isActive) activeColor else inactiveColor
            pixels.forEach { pixel ->
                drawRect(
                    color = drawColor,
                    topLeft = Offset(pixel.x * pixelSize, pixel.y * pixelSize),
                    size = Size(pixelSize * 0.9f, pixelSize * 0.9f) // Un pequeño espacio para efecto rejilla pixel
                )
            }
        }

        // Dibujar cada componente
        drawPixelGroup(dotPixels, isDotActive)
        drawPixelGroup(band1Pixels, isBand1Active)
        drawPixelGroup(band2Pixels, isBand2Active)
        drawPixelGroup(band3Pixels, isBand3Active)
    }
}
