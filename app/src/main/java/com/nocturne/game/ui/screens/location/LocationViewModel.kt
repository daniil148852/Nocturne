package com.nocturne.game.ui.screens.location

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.data.api.ChatMessage
import com.nocturne.game.data.api.ChatRequest
import com.nocturne.game.domain.model.Case
import com.nocturne.game.domain.model.Evidence
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.util.MistralEndpoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val api = container.api
    private val prompts = container.prompts
    private val settings = container.settings

    private val caseId: String = handle.get<String>("caseId") ?: error("missing caseId")
    private val evidenceId: String = handle.get<String>("locationId") ?: error("missing locationId")

    data class State(
        val theCase: Case? = null,
        val evidence: Evidence? = null,
        val narration: String = "",
        val streaming: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val c = cases.load(caseId)
            val ev = c?.evidence?.firstOrNull { it.id == evidenceId || it.location == evidenceId }
            _state.update { it.copy(theCase = c, evidence = ev) }
            // Mark as collected + start streaming inspection
            if (c != null && ev != null) {
                collect(c, ev)
            }
        }
    }

    private fun collect(c: Case, ev: Evidence) {
        val collected = ev.copy(collected = true)
        if (!ev.collected) {
            viewModelScope.launch {
                cases.updateEvidence(
                    c.id,
                    c.evidence.map { if (it.id == ev.id) collected else it }
                )
            }
        }
        runNarration(c, collected)
    }

    private fun runNarration(c: Case, ev: Evidence) {
        val key = settings.apiKey() ?: return
        if (_state.value.streaming) return
        _state.update { it.copy(streaming = true, error = null, narration = "") }
        val suspectHint = c.suspects.firstOrNull { it.id == ev.linkedSuspectId }?.name
        val req = ChatRequest(
            model = MistralEndpoints.DEFAULT_MODEL,
            messages = listOf(
                ChatMessage("system", prompts.inspectSystem()),
                ChatMessage("user", prompts.inspectUser(ev.name, ev.description, ev.location, suspectHint))
            ),
            maxTokens = MistralEndpoints.MAX_TOKENS_INSPECT
        )
        viewModelScope.launch {
            try {
                api.stream(key, req).collect { delta ->
                    _state.update { it.copy(narration = it.narration + delta) }
                }
            } catch (t: Throwable) {
                _state.update { it.copy(streaming = false, error = "Связь прервалась") }
                return@launch
            }
            _state.update { it.copy(streaming = false) }
        }
    }
}
