package com.nocturne.game.ui.screens.location

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
import androidx.compose.foundation.layout.width
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
import com.nocturne.game.ui.components.JazzPulse
import com.nocturne.game.ui.components.NoirCard
import com.nocturne.game.ui.components.TypewriterText
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirBlood
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun LocationScreen(
    caseId: String,
    locationId: String,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onBack: () -> Unit,
) {
    val vm: LocationViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> LocationViewModel(c, h) },
        key = "$caseId/$locationId"
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
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = NoirPaperDim)
            }
            Text(
                text = "МЕСТО ПРЕСТУПЛЕНИЯ",
                color = NoirAmber,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
            )
        }

        val ev = state.evidence
        if (ev == null) {
            Spacer(Modifier.height(80.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Место не найдено", color = NoirPaperDim)
            }
            return@Column
        }

        Spacer(Modifier.height(8.dp))
        NoirCard(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            accent = NoirBlood,
            padding = androidx.compose.foundation.layout.PaddingValues(18.dp)
        ) {
            Column {
                Text(
                    text = ev.location.uppercase(),
                    color = NoirAmber,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ev.name,
                    color = NoirPaper,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = ev.description,
                    color = NoirPaperDim,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "ВЕС УЛИКИ: " + "I".repeat(ev.weight.coerceIn(1, 3)),
                    color = NoirBlood,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        NoirCard(
            modifier = Modifier.padding(horizontal = 20.dp),
            accent = NoirAmberDim,
            padding = androidx.compose.foundation.layout.PaddingValues(20.dp)
        ) {
            Column {
                Text(
                    text = stringRes(ctx, com.nocturne.game.R.string.location_inspecting).uppercase(),
                    color = NoirAmberDim,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                )
                Spacer(Modifier.height(12.dp))
                TypewriterText(
                    target = state.narration.ifBlank { "" },
                    style = TextStyle(
                        color = NoirPaper,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    charDelayMillis = 18L
                )
                Spacer(Modifier.height(8.dp))
                if (state.streaming) JazzPulse(modifier = Modifier.height(36.dp).width(120.dp))
                if (state.error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(state.error!!, color = com.nocturne.game.ui.theme.NoirBlood, fontSize = 12.sp)
                }
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int): String =
    context.getString(id)
