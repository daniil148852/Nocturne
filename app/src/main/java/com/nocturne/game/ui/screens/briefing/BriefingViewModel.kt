package com.nocturne.game.ui.screens.briefing

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.domain.model.Case
import com.nocturne.game.ui.common.NocturneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class BriefingViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val caseId: String = checkNotNull(handle["caseId"] ?: handle.get<String>("caseId"))

    data class State(
        val loading: Boolean = true,
        val case: Case? = null,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val c = runCatching { cases.load(caseId) }.getOrNull()
            _state.update {
                it.copy(loading = false, case = c, error = if (c == null) "Дело не найдено" else null)
            }
        }
    }
}
