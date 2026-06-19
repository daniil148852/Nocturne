package com.nocturne.game.ui.screens.hub

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.domain.model.Case
import com.nocturne.game.domain.model.Evidence
import com.nocturne.game.ui.common.NocturneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HubViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val caseId: String = handle.get<String>("caseId") ?: error("missing caseId")

    enum class Tab { SUSPECTS, EVIDENCE, PLACES }

    data class State(
        val theCase: Case? = null,
        val tab: Tab = Tab.SUSPECTS,
        val collectedCount: Int = 0,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cases.listFlow().collect { list ->
                val c = list.firstOrNull { it.id == caseId }
                _state.update {
                    it.copy(
                        theCase = c,
                        collectedCount = c?.evidence?.count { e -> e.collected } ?: 0
                    )
                }
            }
        }
    }

    fun setTab(tab: Tab) {
        _state.update { it.copy(tab = tab) }
    }

    fun toggleCollected(evidence: Evidence) {
        val c = _state.value.theCase ?: return
        val updated = c.evidence.map {
            if (it.id == evidence.id) it.copy(collected = !it.collected) else it
        }
        viewModelScope.launch {
            cases.updateEvidence(c.id, updated)
        }
    }
}
