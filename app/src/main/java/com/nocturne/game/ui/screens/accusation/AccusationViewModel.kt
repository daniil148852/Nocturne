package com.nocturne.game.ui.screens.accusation

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.domain.model.Case
import com.nocturne.game.domain.model.Evidence
import com.nocturne.game.domain.model.Suspect
import com.nocturne.game.ui.common.NocturneViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccusationViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val settings = container.settings

    private val caseId: String = handle.get<String>("caseId") ?: error("missing caseId")

    data class State(
        val theCase: Case? = null,
        val pickedSuspect: String? = null,
        val pickedEvidence: String? = null,
        val motive: String = "",
        val submitting: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val c = cases.load(caseId)
            _state.update { it.copy(theCase = c) }
        }
    }

    fun pickSuspect(id: String?) = _state.update { it.copy(pickedSuspect = id) }
    fun pickEvidence(id: String?) = _state.update { it.copy(pickedEvidence = id) }
    fun updateMotive(text: String) = _state.update { it.copy(motive = text) }

    fun submit(onComplete: (wasCorrect: Boolean, caseId: String) -> Unit) {
        val s = _state.value
        val c = s.theCase ?: return
        if (s.pickedSuspect == null || s.pickedEvidence == null || s.motive.isBlank()) {
            _state.update { it.copy(error = "Заполни все поля") }
            return
        }
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            val wasCorrect = s.pickedSuspect == c.truth.killerId
            settings.bumpStats(solved = wasCorrect)
            _state.update { it.copy(submitting = false) }
            onComplete(wasCorrect, c.id)
        }
        @Suppress("UNUSED_VARIABLE") val suspect: Suspect? = c.suspect(s.pickedSuspect)
        @Suppress("UNUSED_VARIABLE") val evidence: Evidence? = c.evidence(s.pickedEvidence)
    }
}
