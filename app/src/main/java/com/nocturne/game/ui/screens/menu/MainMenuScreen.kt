package com.nocturne.game.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.AppContainer
import com.nocturne.game.R
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.GhostButton
import com.nocturne.game.ui.components.JazzPulse
import com.nocturne.game.ui.components.PrimaryButton
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun MainMenuScreen(
    container: AppContainer,
    onNewCase: (String) -> Unit,
    onContinueCase: (String) -> Unit,
    onOpenSetup: () -> Unit,
) {
    val vm: MainMenuViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> MainMenuViewModel(c, h) }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirCharcoal)
            .systemBarsPadding()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(Modifier.height(72.dp))
        Text(
            text = stringRes(context, R.string.menu_title).uppercase(),
            color = NoirAmber,
            fontSize = 14.sp,
            letterSpacing = 4.sp,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringRes(context, R.string.menu_title),
            color = NoirPaper,
            fontSize = 44.sp,
            lineHeight = 50.sp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringRes(context, R.string.menu_subtitle),
            color = NoirPaperDim,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(48.dp))
        PrimaryButton(
            label = if (state.generating) "Загружается дело…" else stringRes(context, R.string.menu_new_case),
            onClick = { vm.newCase { onNewCase(it) } },
            enabled = !state.generating
        )
        Spacer(Modifier.height(14.dp))
        // Continue is wired through container.cases so we keep the most-recent saved.
        ContinueButton(container = container, onContinue = onContinueCase)
        Spacer(Modifier.height(14.dp))
        GhostButton(
            label = stringRes(context, R.string.menu_change_key),
            onClick = onOpenSetup
        )

        if (state.generating) {
            Spacer(Modifier.height(28.dp))
            JazzPulse(label = null)
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringRes(context, R.string.rain_subtitle),
                color = NoirPaperDim,
                fontSize = 12.sp,
            )
        }
        if (state.lastError != null) {
            Spacer(Modifier.height(20.dp))
            Text(state.lastError!!, color = Color(0xFFCF6363), fontSize = 13.sp)
        }

        Spacer(Modifier.height(32.dp))
        val cases by container.cases.listFlow().collectAsStateWithLifecycle(initialValue = emptyList())
        if (cases.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "В ящике стола лежит ещё ${cases.size} ${pluralCases(cases.size)}.",
                color = NoirPaperDim,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ContinueButton(container: AppContainer, onContinue: (String) -> Unit) {
    val context = LocalContext.current
    val cases by container.cases.listFlow().collectAsStateWithLifecycle(initialValue = emptyList())
    val active = cases.lastOrNull()
    GhostButton(
        label = if (active != null) "Продолжить: ${active.victim.name}" else "Продолжить расследование",
        onClick = {
            if (active != null) {
                onContinue(active.id)
            }
        },
        enabled = active != null
    )
    @Suppress("UNUSED_VARIABLE") val _ctx = context
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int): String =
    context.getString(id)

internal fun pluralCases(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "дело"
    n % 10 in 2..4 -> "дела"
    else -> "дел"
}