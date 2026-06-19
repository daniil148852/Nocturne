package com.nocturne.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

private val NoirColors = darkColorScheme(
    primary = NoirAmber,
    onPrimary = NoirBlack,
    primaryContainer = NoirAmberDim,
    onPrimaryContainer = NoirPaper,
    secondary = NoirPaper,
    onSecondary = NoirBlack,
    secondaryContainer = NoirFog,
    onSecondaryContainer = NoirPaper,
    tertiary = NoirBlood,
    onTertiary = NoirPaper,
    background = NoirBlack,
    onBackground = NoirPaper,
    surface = NoirCharcoal,
    onSurface = NoirPaper,
    surfaceVariant = NoirSmoke,
    onSurfaceVariant = NoirPaperDim,
    outline = NoirPaperDim,
    outlineVariant = NoirFog,
    error = NoirBlood,
    onError = NoirPaper,
    scrim = Color(0xCC000000)
)

val LocalFlicker = compositionLocalOf { 0.0f }

@Composable
fun NocturneTheme(content: @Composable () -> Unit) {
    // We force dark — the whole game is "midnight NYC, rain on the windowsill".
    MaterialTheme(
        colorScheme = NoirColors,
        typography = NoirTypography,
        content = content
    )
    // We piggyback the theme wrapper to expose the lantern flicker alpha
    // so screens can drive a global feel without each owning their own transition.
    val infinite = rememberInfiniteTransition(label = "lantern")
    val flicker by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lanternAlpha"
    )
    CompositionLocalProvider(LocalFlicker provides flicker) {
        content()
    }
}
