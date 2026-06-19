package com.nocturne.game.ui.screens.briefing

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.NoirCard
import com.nocturne.game.ui.components.PrimaryButton
import com.nocturne.game.ui.components.SuspectAvatar
import com.nocturne.game.ui.components.TypewriterText
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirBlood
import com.nocturne.game.ui.theme.NoirBlack
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirFog
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim

@Composable
fun BriefingScreen(
    caseId: String,
    @Suppress("UNUSED_PARAMETER") container: com.nocturne.game.AppContainer,
    onBack: () -> Unit,
    onStart: () -> Unit,
) {
    val vm: BriefingViewModel = viewModel(
        factory = NocturneViewModel.factory { c, h -> BriefingViewModel(c, h) },
        key = caseId
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val labelStart = stringRes(ctx, com.nocturne.game.R.string.briefing_start_investigation)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirCharcoal)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = NoirPaperDim)
            }
            Text(
                text = "УЧАСТОК",
                color = NoirAmberDim,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
            )
        }

        when {
            state.loading -> {
                Spacer(Modifier.height(220.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "Подшиваем дело…", color = NoirPaperDim, fontSize = 14.sp)
                }
            }
            state.case == null -> {
                Spacer(Modifier.height(220.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(state.error ?: "Дело не найдено", color = NoirPaperDim, fontSize = 14.sp)
                }
            }
            else -> {
                val c = state.case!!
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringRes(ctx, com.nocturne.game.R.string.briefing_title, c.id.takeLast(4).uppercase()),
                    color = NoirAmber,
                    fontSize = 13.sp,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = c.city.uppercase() + " · " + c.date.uppercase(),
                    color = NoirPaperDim,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(32.dp))
                NoirCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    accent = NoirBlood,
                    padding = androidx.compose.foundation.layout.PaddingValues(20.dp)
                ) {
                    Column {
                        Text(
                            text = stringRes(ctx, com.nocturne.game.R.string.briefing_victim_label).uppercase(),
                            color = NoirAmber,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SuspectAvatar(
                                name = c.victim.name,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = c.victim.name,
                                    color = NoirPaper,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${c.victim.age} · ${c.victim.profession}",
                                    color = NoirPaperDim,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        TypewriterText(
                            target = c.victim.backstory,
                            style = androidx.compose.ui.text.TextStyle(
                                color = NoirPaper,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            ),
                            charDelayMillis = 20L
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                NoirCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    padding = androidx.compose.foundation.layout.PaddingValues(20.dp)
                ) {
                    Column {
                        Text(
                            text = stringRes(ctx, com.nocturne.game.R.string.briefing_location_label).uppercase(),
                            color = NoirAmber,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = c.crimeScene.address,
                            color = NoirPaper,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(10.dp))
                        TypewriterText(
                            target = c.crimeScene.description + " " + c.crimeScene.weather,
                            style = androidx.compose.ui.text.TextStyle(
                                color = NoirPaper,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            charDelayMillis = 22L
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringRes(ctx, com.nocturne.game.R.string.briefing_recipient),
                    color = NoirAmberDim,
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(32.dp))
                PrimaryButton(
                    label = labelStart.uppercase(),
                    onClick = onStart,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun stringRes(context: android.content.Context, @androidx.annotation.StringRes id: Int, vararg args: Any): String =
    context.getString(id, *args)
