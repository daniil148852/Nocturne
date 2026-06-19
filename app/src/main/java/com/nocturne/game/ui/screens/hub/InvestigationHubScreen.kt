package com.nocturne.game.ui.screens.hub

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
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
fun InvestigationHubScreen(
    caseId: String,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onBack: () -> Unit,
    onOpenLocation: (String) -> Unit,
    onOpenSuspect: (String) -> Unit,
    onAccuse: () -> Unit,
) {
    val vm: HubViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> HubViewModel(c, h) },
        key = caseId
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

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
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Text(
                    text = "ЗАПИСНАЯ КНИЖКА",
                    color = NoirAmberDim,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                )
                Text(
                    text = stringRes(ctx, com.nocturne.game.R.string.hub_title),
                    color = NoirPaper,
                    fontSize = 22.sp,
                )
            }
        }

        TabRow(state.tab) { vm.setTab(it) }

        val c = state.theCase
        if (c == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Дело не найдено", color = NoirPaperDim)
            }
            return@Column
        }

        Box(modifier = Modifier.weight(1f)) {
            when (state.tab) {
                HubViewModel.Tab.SUSPECTS -> SuspectList(c, onClick = { onOpenSuspect(it.id) })
                HubViewModel.Tab.EVIDENCE -> EvidenceList(c, state.collectedCount, onToggle = { vm.toggleCollected(it) })
                HubViewModel.Tab.PLACES -> PlacesList(c, onClick = { onOpenLocation(it.id) })
            }
        }

        if (state.collectedCount >= 2) {
            DangerButton(
                label = stringRes(ctx, com.nocturne.game.R.string.hub_action_accuse),
                onClick = onAccuse,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun TabRow(current: HubViewModel.Tab, onChange: (HubViewModel.Tab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf(
            HubViewModel.Tab.SUSPECTS to "ПОДОЗРЕВАЕМЫЕ",
            HubViewModel.Tab.EVIDENCE to "УЛИКИ",
            HubViewModel.Tab.PLACES to "МЕСТА"
        )
        tabs.forEach { (t, label) ->
            val active = current == t
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) NoirAmber else NoirFog)
                    .border(1.dp, if (active) NoirAmber else NoirAmberDim, RoundedCornerShape(2.dp))
                    .clickable { onChange(t) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    color = if (active) NoirCharcoal else NoirPaperDim,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SuspectList(case: com.nocturne.game.domain.model.Case, onClick: (com.nocturne.game.domain.model.Suspect) -> Unit) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(case.suspects, key = { it.id }) { suspect ->
            NoirCard(
                onClick = { onClick(suspect) },
                accent = NoirAmberDim
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SuspectAvatar(name = suspect.name, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(suspect.name, color = NoirPaper, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${suspect.age} · ${suspect.profession}",
                            color = NoirPaperDim,
                            fontSize = 12.sp,
                        )
                        Text(
                            "Связь: ${suspect.relation}",
                            color = NoirPaperDim,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceList(
    case: com.nocturne.game.domain.model.Case,
    collectedCount: Int,
    onToggle: (com.nocturne.game.domain.model.Evidence) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Собрано: $collectedCount / ${case.evidence.size}",
            color = NoirAmber,
            fontSize = 12.sp,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(case.evidence, key = { it.id }) { ev ->
                NoirCard(
                    onClick = { onToggle(ev) },
                    accent = if (ev.collected) NoirAmber else NoirAmberDim
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = if (ev.collected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (ev.collected) NoirAmber else NoirPaperDim,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                ev.name,
                                color = NoirPaper,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            ev.description,
                            color = NoirPaperDim,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Где: ${ev.location}",
                            color = NoirBlood,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlacesList(case: com.nocturne.game.domain.model.Case, onClick: (com.nocturne.game.domain.model.Evidence) -> Unit) {
    val places = remember(case.id) {
        case.evidence.map { ev ->
            ev.location to ev
        }.toSet().map { it.second }
    }
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(places, key = { it.id }) { ev ->
            NoirCard(onClick = { onClick(ev) }, accent = NoirAmberDim) {
                Column {
                    Text(ev.location, color = NoirPaper, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("Там найдено: ${ev.name}", color = NoirPaperDim, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int): String =
    context.getString(id)
