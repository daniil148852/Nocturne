package com.nocturne.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.nocturne.game.ui.theme.NoirRain
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Title-screen & ambient-screen rain overlay — drops fall at slightly varied
 * speeds and angles, with the lightest hint of stagger between drops.
 */
@Composable
fun RainBackground(
    modifier: Modifier = Modifier,
    intensity: Float = 1.0f,
    accent: Color = NoirRain,
    seed: Long = 0L
) {
    val infinite = rememberInfiniteTransition(label = "rain")
    val t by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainAnim"
    )

    val rng = remember(seed) { Random(seed.takeIf { it != 0L } ?: System.nanoTime()) }
    val drops = remember(seed) {
        List(80) {
            Drop(
                xFrac = rng.nextFloat(),
                yFrac = rng.nextFloat() * 1.2f - 0.2f, // start above & below
                speed = 0.55f + rng.nextFloat() * 0.6f,
                lengthFrac = 0.012f + rng.nextFloat() * 0.022f,
                alpha = 0.25f + rng.nextFloat() * 0.55f,
            )
        }
    }

    val windAngle = 0.10f // radians, slight rightward slant

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val slant = sin(windAngle) * h * 0.04f
        drops.forEach { d ->
            val cycle = (t * d.speed + d.yFrac) % 1.4f
            val y = (cycle - 0.2f) * h
            val xBase = d.xFrac * w
            val x = xBase + slant + cos(windAngle) * (cycle - 0.2f) * h * 0.04f
            val len = d.lengthFrac * h * intensity
            drawLine(
                color = accent.copy(alpha = d.alpha * 0.85f),
                start = Offset(x, y),
                end = Offset(x + slant, y + len),
                strokeWidth = 1.2f,
                cap = StrokeCap.Round
            )
        }
    }
}

private data class Drop(
    val xFrac: Float,
    val yFrac: Float,
    val speed: Float,
    val lengthFrac: Float,
    val alpha: Float,
)

// import remember alias from runtime
@Composable
private fun <T> remember(key: Any?, calc: () -> T): T = androidx.compose.runtime.remember(key, calc)
