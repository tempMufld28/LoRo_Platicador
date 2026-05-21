package com.lockchat.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lockchat.app.ui.theme.LockChatTheme

/**
 * SignalWavesIcon — Un icono premium dibujado en Canvas que representa
 * una antena de red inalámbrica expandiendo ondas en 3 bandas.
 */
@Composable
fun SignalWavesIcon(
    modifier: Modifier = Modifier,
    color: Color = LockChatTheme.colors.primary,
    size: Dp = 64.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        
        // Centro emisor de la antena (punto central)
        val center = Offset(w / 2f, h * 0.75f)
        
        // Mástil/Soporte de la antena
        drawLine(
            color = color,
            start = Offset(w / 2f, h * 0.75f),
            end = Offset(w / 2f, h * 0.92f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // Punto emisor
        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = center
        )
        
        // 3 Ondas/Bandas concéntricas expandiéndose hacia arriba
        val strokeWidth = 2.5.dp.toPx()
        val sweepAngle = 110f
        val startAngle = 215f // Centrado en 270f (arriba)
        
        // Radio incremental para cada banda
        val step = w * 0.22f
        
        for (i in 1..3) {
            val radius = step * i
            val arcSize = radius * 2f
            drawArc(
                color = color.copy(alpha = 1f - ((i - 1) * 0.25f)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(arcSize, arcSize),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
