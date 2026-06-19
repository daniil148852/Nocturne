package com.nocturne.game.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirPaper

/**
 * Renders [target] one character at a time, like a vintage typewriter.
 * - Animating a new [target] restarts the typewriter from the beginning.
 * - If [target] is empty (model is still loading), shows just the blinking cursor.
 */
@Composable
fun TypewriterText(
    target: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    charDelayMillis: Long = 22L,
    cursor: String = "▍",
) {
    var visibleLen by remember(target) { mutableStateOf(0) }
    LaunchedEffect(target) {
        visibleLen = 0
        if (target.isEmpty()) return@LaunchedEffect
        for (i in 1..target.length) {
            visibleLen = i
            kotlinx.coroutines.delay(charDelayMillis)
        }
    }

    val cursorAlpha by animateFloatAsState(
        targetValue = if (target.isEmpty()) 1f else if (visibleLen < target.length) 1f else 0.75f,
        animationSpec = tween(durationMillis = 360, easing = LinearEasing),
        label = "cursor"
    )

    val shown = target.substring(0, visibleLen.coerceAtMost(target.length))
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = NoirPaper)) { append(shown) }
        withStyle(SpanStyle(color = NoirAmber.copy(alpha = cursorAlpha))) {
            append(if (cursorAlpha > 0.0f) cursor else "")
        }
    }

    BasicText(
        text = text,
        modifier = modifier,
        style = style,
        overflow = TextOverflow.Clip,
        maxLines = Int.MAX_VALUE
    )
}
