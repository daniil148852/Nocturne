package com.nocturne.game.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nocturne.game.AppContainer
import com.nocturne.game.R
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.ui.components.GhostButton
import com.nocturne.game.ui.components.PrimaryButton
import com.nocturne.game.ui.theme.NoirAmber
import com.nocturne.game.ui.theme.NoirAmberDim
import com.nocturne.game.ui.theme.NoirCharcoal
import com.nocturne.game.ui.theme.NoirFog
import com.nocturne.game.ui.theme.NoirPaper
import com.nocturne.game.ui.theme.NoirPaperDim
import kotlin.system.exitProcess

@Composable
fun SetupScreen(
    onSaved: () -> Unit,
    container: AppContainer,
) {
    val vm: SetupViewModel = viewModel(
        factory = NocturneViewModel.factory { c, _ -> SetupViewModel(c, SavedStateHandle()) }
    )
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(state.savedKey) {
        if (state.savedKey) onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NoirCharcoal)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = string(context, R.string.app_name).uppercase(),
            color = NoirAmber,
            fontSize = 13.sp,
            letterSpacing = 6.sp,
        )
        Spacer(Modifier.height(36.dp))
        Text(
            text = string(context, R.string.setup_title),
            color = NoirPaper,
            fontSize = 30.sp,
            lineHeight = 36.sp,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = string(context, R.string.setup_subtitle),
            color = NoirPaperDim,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(36.dp))
        OutlinedTextField(
            value = state.keyInput,
            onValueChange = vm::updateKey,
            label = { Text(string(context, R.string.setup_key_hint), color = NoirPaperDim) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NoirAmber,
                unfocusedBorderColor = NoirAmberDim,
                cursorColor = NoirAmber,
                focusedTextColor = NoirPaper,
                unfocusedTextColor = NoirPaperDim
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error!!, color = Color(0xFFCF6363), fontSize = 13.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = string(context, R.string.setup_key_help),
            color = NoirPaperDim,
            fontSize = 11.sp,
            lineHeight = 16.sp,
        )

        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            label = string(context, R.string.setup_save),
            onClick = vm::save,
            enabled = state.keyInput.isNotBlank()
        )
        Spacer(Modifier.height(12.dp))
        GhostButton(
            label = string(context, R.string.setup_open_console),
            onClick = { uriHandler.openUri("https://console.mistral.ai/") }
        )

        @Suppress("UNUSED_VARIABLE")
        val cannotExit = false // placeholder for future "skip dev key" feature
    }
}

@Composable
private fun string(context: android.content.Context, resId: Int): String =
    context.getString(resId)
