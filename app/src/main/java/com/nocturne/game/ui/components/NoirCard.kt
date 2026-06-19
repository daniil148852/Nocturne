package com.nocturne.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirFog
import com.nocturne.game.ui.theme.NoirPaperDim

/**
 * Card with a stained-paper aesthetic — dark charcoal body, thin amber border,
 * subtle vertical gradient, and small notch-corners that suggest a torn dossier.
 */
@Composable
fun NoirCard(
    modifier: Modifier = Modifier,
    accent: Color = NoirAmber,
    onClick: (() -> Unit)? = null,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val base = modifier
        .clip(RoundedCornerShape(4.dp))
        .background(
            Brush.verticalGradient(
                colors = listOf(NoirFog, NoirCharcoal)
            )
        )
        .border(width = 1.dp, color = accent.copy(alpha = 0.55f), shape = RoundedCornerShape(4.dp))
    val tappable = if (onClick != null) base.clickable { onClick() } else base
    Box(modifier = tappable.padding(padding)) { content() }
}
