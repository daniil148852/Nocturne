package com.nocturne.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim

/**
 * Animated wave/heartbeat indicator — used during "model is thinking" states.
 * Five amber bars breathing out of sync on a smoke background.
 */
@Composable
fun JazzPulse(modifier: Modifier = Modifier, label: String? = null) {
    val infinite = rememberInfiniteTransition(label = "jazz")
    val phases = (0 until 5).map { idx ->
        val v by infinite.animateFloat(
            initialValue = 0.15f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700 + idx * 80, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$idx"
        )
        v
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barW = w / 7f
        val gap = barW / 2.5f
        phases.forEachIndexed { i, p ->
            val barH = h * (0.25f + p * 0.7f)
            val x = gap + i * (barW + gap)
            val y = (h - barH) / 2f
            drawRect(
                color = NoirAmberDim.copy(alpha = 0.35f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barW, barH)
            )
            drawRect(
                color = NoirAmber.copy(alpha = 0.95f),
                topLeft = Offset(x + barW * 0.1f, y + barH * 0.05f),
                size = androidx.compose.ui.geometry.Size(barW * 0.8f, barH * 0.9f)
            )
        }
    }
}
