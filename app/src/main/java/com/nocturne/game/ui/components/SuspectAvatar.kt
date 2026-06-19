package com.nocturne.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirSmoke

/**
 * Procedurally drawn portrait silhouette for a suspect — no images, no network.
 * Hat style is deterministic from the suspect's name hash so the same suspect
 * always renders the same silhouette.
 */
@Composable
fun SuspectAvatar(name: String, modifier: Modifier = Modifier) {
    val hash = remember(name) { name.hashCode() }
    Canvas(modifier = modifier.clip(CircleShape)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w.coerceAtMost(h) / 2f

        // Background dish — smoked gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NoirSmoke, Color(0xFF050608)),
                center = Offset(cx, cy * 0.85f),
                radius = r
            ),
            center = Offset(cx, cy),
            radius = r
        )

        // Silhouette: head + shoulders
        val headR = r * 0.34f
        val headY = cy - r * 0.18f
        drawCircle(
            color = Color(0xFF1A1A22),
            radius = headR,
            center = Offset(cx, headY)
        )
        val shouldersPath = Path().apply {
            moveTo(cx - r * 0.75f, cy + r * 0.85f)
            quadraticBezierTo(cx - r * 0.55f, cy + r * 0.05f, cx - headR * 0.6f, headY + headR * 0.7f)
            lineTo(cx + headR * 0.6f, headY + headR * 0.7f)
            quadraticBezierTo(cx + r * 0.55f, cy + r * 0.05f, cx + r * 0.75f, cy + r * 0.85f)
            close()
        }
        drawPath(shouldersPath, color = Color(0xFF22232B))

        // Hat — fedora or wide brim from name hash
        val brimY = headY - headR * (if ((hash and 1) == 0) 0.95f else 0.6f)
        drawRect(
            color = Color(0xFF0E0F14),
            topLeft = Offset(cx - r * 0.78f, brimY - r * 0.02f),
            size = Size(r * 1.56f, r * 0.18f)
        )
        drawRect(
            color = Color(0xFF0E0F14),
            topLeft = Offset(
                cx - headR * (if ((hash and 1) == 0) 1.05f else 0.95f),
                brimY - r * (if ((hash and 1) == 0) 0.42f else 0.30f)
            ),
            size = Size(
                headR * 2.1f * (if ((hash and 1) == 0) 1f else 0.9f),
                r * (if ((hash and 1) == 0) 0.42f else 0.30f)
            )
        )

        // Coat lapels — small light triangle
        drawLine(
            color = Color(0xFF2E2E38),
            start = Offset(cx, headY + headR * 0.8f),
            end = Offset(cx - r * 0.35f, cy + r * 0.85f),
            strokeWidth = r * 0.06f
        )
        drawLine(
            color = Color(0xFF2E2E38),
            start = Offset(cx, headY + headR * 0.8f),
            end = Offset(cx + r * 0.35f, cy + r * 0.85f),
            strokeWidth = r * 0.06f
        )

        // Amber rim light
        drawCircle(
            color = NoirAmber.copy(alpha = 0.45f),
            center = Offset(cx, cy),
            radius = r - 2f,
            style = Stroke(width = 3f)
        )
        drawCircle(
            color = NoirAmberDim.copy(alpha = 0.4f),
            center = Offset(cx, cy),
            radius = r - 0.5f,
            style = Stroke(width = 1.5f)
        )

        // Initial monogram in lower-right
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.argb(220, 232, 163, 58)
                textSize = r * 0.42f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.SERIF,
                    android.graphics.Typeface.BOLD
                )
            }
            val initial = name.trim()
                .firstOrNull { it.isLetter() }
                ?.uppercaseChar()
                ?.toString()
                ?: "?"
            canvas.nativeCanvas.drawText(
                initial,
                cx + r * 0.62f,
                cy + r * 0.85f,
                paint
            )
        }
    }
}
