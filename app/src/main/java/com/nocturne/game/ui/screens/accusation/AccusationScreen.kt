package com.nocturne.game.ui.screens.accusation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.DangerButton
import com.nocturne.game.ui.components.NoirCard
import com.nocturne.game.ui.components.SuspectAvatar
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirBlood
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirFog
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun AccusationScreen(
    caseId: String,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onBack: () -> Unit,
    onSubmitted: (wasCorrect: Boolean, caseId: String) -> Unit,
) {
    val vm: AccusationViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> AccusationViewModel(c, h) },
        key = caseId
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
                text = stringRes(ctx, com.nocturne.game.R.string.accusation_title).uppercase(),
                color = NoirBlood,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "УКАЖИ НА УБИЙЦУ",
            color = NoirPaper,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        state.theCase?.let { c ->
            Spacer(Modifier.height(20.dp))

            Text(
                text = stringRes(ctx, com.nocturne.game.R.string.accusation_pick_suspect).uppercase(),
                color = NoirAmber,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                c.suspects.forEach { suspect ->
                    ChoiceCard(
                        title = suspect.name,
                        sub = "${suspect.profession} · ${suspect.relation}",
                        selected = state.pickedSuspect == suspect.id,
                        leader = SuspectAvatar(name = suspect.name, modifier = Modifier.size(60.dp)),
                        onClick = { vm.pickSuspect(if (state.pickedSuspect == suspect.id) null else suspect.id) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringRes(ctx, com.nocturne.game.R.string.accusation_pick_motive).uppercase(),
                color = NoirAmber,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(2.dp))
                    .border(1.dp, NoirAmberDim, RoundedCornerShape(2.dp))
                    .background(NoirFog)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = state.motive,
                    onValueChange = vm::updateMotive,
                    textStyle = TextStyle(color = NoirPaper, fontSize = 15.sp),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(NoirAmber),
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    decorationBox = { inner ->
                        if (state.motive.isEmpty()) {
                            Text(
                                "Что двигало убийцей? Зачем?",
                                color = NoirPaperDim,
                                fontSize = 14.sp,
                            )
                        }
                        inner()
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = stringRes(ctx, com.nocturne.game.R.string.accusation_pick_evidence).uppercase(),
                color = NoirAmber,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.padding(start = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                c.evidence.filter { it.collected }.forEach { ev ->
                    ChoiceCard(
                        title = ev.name,
                        sub = ev.description,
                        selected = state.pickedEvidence == ev.id,
                        leader = Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(NoirCharcoal)
                                .border(1.dp, NoirBlood, RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("УЛ.", color = NoirBlood, fontSize = 10.sp, letterSpacing = 2.sp)
                        },
                        onClick = { vm.pickEvidence(if (state.pickedEvidence == ev.id) null else ev.id) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = NoirBlood,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            DangerButton(
                label = stringRes(ctx, com.nocturne.game.R.string.accusation_submit),
                onClick = { vm.submit { ok, cid -> onSubmitted(ok, cid) } },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    sub: String,
    selected: Boolean,
    leader: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) NoirFog else NoirCharcoal)
            .border(1.dp, if (selected) NoirAmber else NoirAmberDim, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leader()
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = NoirPaper,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                sub,
                color = NoirPaperDim,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2,
            )
        }
        if (selected) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(NoirAmber)
            )
        }
    }
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int): String =
    context.getString(id)
