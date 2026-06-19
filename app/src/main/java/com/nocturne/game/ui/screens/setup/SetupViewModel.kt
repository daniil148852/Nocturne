package com.nocturne.game.ui.screens.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nocturne.game.AppContainer
import com.nocturne.game.ui.common.NocturneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SetupViewModel(
    container: AppContainer,
    @Suppress("unused") handle: SavedStateHandle
) : NocturneViewModel() {

    private val settings = container.settings

    data class State(
        val keyInput: String = "",
        val savedKey: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        _state.update { it.copy(savedKey = !settings.apiKey().isNullOrBlank()) }
    }

    fun updateKey(input: String) {
        _state.update { it.copy(keyInput = input, error = null) }
    }

    fun save() {
        val trimmed = _state.value.keyInput.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(error = "Ключ не может быть пустым") }
            return
        }
        viewModelScope.launch {
            settings.saveApiKey(trimmed)
            _state.update { it.copy(savedKey = true, keyInput = "", error = null) }
        }
    }

    fun clear() {
        settings.clearApiKey()
        _state.update { State(savedKey = false) }
    }
}
