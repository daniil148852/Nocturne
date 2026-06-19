package com.nocturne.game.ui.noir

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import com.nocturne.game.ui.theme.LocalFlicker
import com.nocturne.game.ui.theme.NoirAmber

/**
 * Ambient amber lantern light that breathes over the whole screen.
 * Reads the flicker alpha from the theme's CompositionLocal so all
 * screens pulse in unison.
 */
@Composable
fun LanternFlicker(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val alpha = LocalFlicker.current
    Box(modifier = modifier
        .fillMaxSize()
        .drawWithCache {
            val w = size.width
            val h = size.height
            val amber = Brush.radialGradient(
                colors = listOf(
                    NoirAmber.copy(alpha = 0.10f * alpha),
                    Color.Transparent
                ),
                center = Offset(w / 2f, h * 0.15f),
                radius = w
            )
            val vignette = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF000000).copy(alpha = 0.55f)
                ),
                center = Offset(w / 2f, h / 2f),
                radius = w * 0.95f
            )
            onDrawWithContent {
                drawIntoCanvas {
                    drawRect(amber)
                    drawRect(vignette)
                    drawContent()
                }
            }
        }
    ) { content() }
}
