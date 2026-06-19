package com.nocturne.game.ui.screens.interrogation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.domain.model.Turn
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.JazzPulse
import com.nocturne.game.ui.components.NoirCard
import com.nocturne.game.ui.components.SuspectAvatar
import com.nocturne.game.ui.components.TypewriterText
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirBlood
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirFog
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun InterrogationScreen(
    caseId: String,
    suspectId: String,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onBack: () -> Unit,
) {
    val vm: InterrogationViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> InterrogationViewModel(c, h) },
        key = "$caseId/$suspectId"
    )
    val state by vm.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirCharcoal)
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = NoirPaperDim)
            }
            Text(
                text = "ДОПРОС",
                color = NoirAmber,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
            )
        }

        val suspect = state.suspect
        if (suspect != null) {
            NoirCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                accent = NoirAmber,
                padding = androidx.compose.foundation.layout.PaddingValues(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuspectAvatar(name = suspect.name, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            suspect.name,
                            color = NoirPaper,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "${suspect.profession} · " + suspect.relation,
                            color = NoirPaperDim,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        val listState = rememberLazyListState()
        LaunchedEffect(state.turns.size, state.streamingReply) {
            if (state.turns.isNotEmpty()) {
                listState.animateScrollToItem(state.turns.size - 1 + (if (state.streamingReply.isNotEmpty()) 1 else 0))
            }
        }

        LazyColumn(
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.turns, key = { it.hashCode() }) { turn ->
                Bubble(turn)
            }
            if (state.streamingReply.isNotEmpty() || state.thinking) {
                item {
                    Column {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.suspect?.name?.uppercase() ?: "",
                            color = NoirAmber,
                            fontSize = 10.sp,
                            letterSpacing = 3.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        TypewriterText(
                            target = state.streamingReply,
                            style = TextStyle(color = NoirPaper, fontSize = 15.sp, lineHeight = 22.sp),
                            charDelayMillis = 14L
                        )
                    }
                }
            }
        }

        if (state.thinking && state.streamingReply.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                JazzPulse(modifier = Modifier.height(28.dp).width(120.dp))
            }
        }

        Spacer(Modifier.height(4.dp))
        InputBar(
            draft = state.drafting,
            onChange = vm::updateDraft,
            onSend = vm::send,
            enabled = !state.thinking && state.suspect != null,
        )
    }
}

@Composable
private fun Bubble(turn: Turn) {
    val fromDetective = turn.speaker == "detective"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromDetective) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(start = if (fromDetective) 64.dp else 0.dp, end = if (fromDetective) 0.dp else 64.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (fromDetective) NoirFog else NoirCharcoal)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = turn.text,
                color = NoirPaper,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun InputBar(draft: String, onChange: (String) -> Unit, onSend: () -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(NoirFog)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = draft,
            onValueChange = onChange,
            textStyle = TextStyle(color = NoirPaper, fontSize = 15.sp),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(NoirAmber),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp)
        )
        IconButton(onClick = onSend, enabled = enabled) {
            Icon(
                Icons.Filled.Send,
                contentDescription = null,
                tint = if (enabled) NoirAmber else NoirAmberDim
            )
        }
    }
}
