package com.nocturne.game.ui.screens.resolution

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.GhostButton
import com.nocturne.game.ui.components.JazzPulse
import com.nocturne.game.ui.components.NoirCard
import com.nocturne.game.ui.components.PrimaryButton
import com.nocturne.game.ui.components.TypewriterText
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirBlood
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun ResolutionScreen(
    caseId: String,
    wasCorrect: Boolean,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onNextCase: (String) -> Unit,
    onBackToMenu: () -> Unit,
) {
    val vm: ResolutionViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> ResolutionViewModel(c, h) },
        key = "$caseId/$wasCorrect"
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirCharcoal)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToMenu) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = NoirPaperDim)
            }
            Text(
                text = stringRes(ctx, com.nocturne.game.R.string.resolution_title).uppercase(),
                color = if (wasCorrect) NoirAmber else NoirPaperDim,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
            )
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (wasCorrect) "ДЕЛО РАСКРЫТО" else "УБИЙЦА УШЁЛ",
                color = if (wasCorrect) NoirAmber else NoirBlood,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 40.sp,
            )
        }

        Spacer(Modifier.height(16.dp))
        NoirCard(
            modifier = Modifier.padding(horizontal = 24.dp),
            accent = if (wasCorrect) NoirAmber else NoirBlood,
            padding = androidx.compose.foundation.layout.PaddingValues(20.dp)
        ) {
            Column {
                Text(
                    text = "ВЕРДИКТ",
                    color = NoirAmber,
                    fontSize = 10.sp,
                    letterSpacing = 4.sp,
                )
                Spacer(Modifier.height(10.dp))
                if (state.streaming && state.verdict.isEmpty()) {
                    JazzPulse(modifier = Modifier.height(36.dp))
                } else {
                    TypewriterText(
                        target = state.verdict,
                        style = TextStyle(color = NoirPaper, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
                        charDelayMillis = 20L
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        NoirCard(
            modifier = Modifier.padding(horizontal = 24.dp),
            accent = NoirAmberDim,
            padding = androidx.compose.foundation.layout.PaddingValues(20.dp)
        ) {
            Column {
                Text(
                    text = "ЧТО ПРОИЗОШЛО",
                    color = NoirAmber,
                    fontSize = 10.sp,
                    letterSpacing = 4.sp,
                )
                Spacer(Modifier.height(10.dp))
                TypewriterText(
                    target = state.whatHappened,
                    style = TextStyle(color = NoirPaper, fontSize = 16.sp, lineHeight = 24.sp),
                    charDelayMillis = 24L
                )
            }
        }

        if (state.finalNote.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "« " + state.finalNote + " »",
                color = NoirPaperDim,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
        }

        Spacer(Modifier.height(36.dp))
        PrimaryButton(
            label = "СЛЕДУЮЩЕЕ ДЕЛО",
            onClick = { onNextCase(caseId) /* effectively: parent will start a fresh case via menu */ },
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(10.dp))
        GhostButton(
            label = stringRes(ctx, com.nocturne.game.R.string.resolution_to_menu),
            onClick = onBackToMenu,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int): String =
    context.getString(id)
