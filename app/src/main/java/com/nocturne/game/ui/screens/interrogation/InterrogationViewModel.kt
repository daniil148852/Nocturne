package com.nocturne.game.ui.screens.interrogation

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.data.api.ChatMessage
import com.nocturne.game.data.api.ChatRequest
import com.nocturne.game.domain.model.Case
import com.nocturne.game.domain.model.Suspect
import com.nocturne.game.domain.model.Turn
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.util.MistralEndpoints
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InterrogationViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val settings = container.settings
    private val api = container.api
    private val prompts = container.prompts

    private val caseId: String = handle.get<String>("caseId") ?: error("missing caseId")
    private val suspectId: String = handle.get<String>("suspectId") ?: error("missing suspectId")

    data class State(
        val theCase: Case? = null,
        val suspect: Suspect? = null,
        val turns: List<Turn> = emptyList(),
        val drafting: String = "",
        val thinking: Boolean = false,
        val streamingReply: String = "",
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            val case = cases.load(caseId)
            val suspect = case?.suspect(suspectId)
            _state.update { it.copy(theCase = case, suspect = suspect) }
            if (case != null && suspect != null && _state.value.turns.isEmpty()) {
                sendOpening(case, suspect)
            }
        }
    }

    fun updateDraft(text: String) =
        _state.update { it.copy(drafting = text) }

    fun send() {
        val draft = _state.value.drafting.trim()
        if (draft.isEmpty() || _state.value.thinking) return
        val case = _state.value.theCase ?: return
        val suspect = _state.value.suspect ?: return
        val key = settings.apiKey() ?: run {
            _state.update { it.copy(error = "Ключ API не задан") }
            return
        }

        _state.update {
            it.copy(
                turns = it.turns + Turn("detective", draft),
                drafting = "",
                thinking = true,
                streamingReply = "",
                error = null
            )
        }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            try {
                val req = ChatRequest(
                    model = MistralEndpoints.DEFAULT_MODEL,
                    messages = listOf(
                        ChatMessage(
                            role = "system",
                            content = prompts.interrogationSystem(
                                suspect = suspect.name,
                                role = suspect.profession,
                                isKiller = suspect.isKiller
                            )
                        ),
                        ChatMessage(
                            role = "user",
                            content = prompts.interrogationUser(
                                question = draft,
                                history = _state.value.turns.map { it.speaker to it.text }
                            )
                        )
                    ),
                    maxTokens = MistralEndpoints.MAX_TOKENS_DIALOG,
                    temperature = 0.85
                )
                val builder = StringBuilder()
                api.stream(key, req).collect { delta ->
                    builder.append(delta)
                    _state.update { it.copy(streamingReply = builder.toString()) }
                }
                _state.update {
                    it.copy(
                        turns = it.turns + Turn("suspect", builder.toString()),
                        streamingReply = "",
                        thinking = false
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(thinking = false, error = "Связь прервалась") }
            }
        }
    }

    private fun sendOpening(case: Case, suspect: Suspect) {
        val key = settings.apiKey() ?: return
        if (_state.value.turns.isNotEmpty()) return
        _state.update { it.copy(thinking = true) }
        streamJob = viewModelScope.launch {
            try {
                val req = ChatRequest(
                    model = MistralEndpoints.DEFAULT_MODEL,
                    messages = listOf(
                        ChatMessage(
                            "system",
                            prompts.interrogationSystem(
                                suspect.name, suspect.profession, suspect.isKiller
                            )
                        ),
                        ChatMessage(
                            "user",
                            """Детектив вошёл в комнату допросов. Ты сидишь напротив.
                               Скажи одну короткую реплику — первую."""
                        )
                    ),
                    maxTokens = 200
                )
                val builder = StringBuilder()
                api.stream(key, req).collect { delta ->
                    builder.append(delta)
                    _state.update { it.copy(streamingReply = builder.toString(), thinking = true) }
                }
                _state.update {
                    it.copy(
                        turns = it.turns + Turn("suspect", builder.toString()),
                        streamingReply = "",
                        thinking = false
                    )
                }
            } catch (_: Throwable) {
                _state.update { it.copy(thinking = false) }
            }
        }
    }

    override fun onCleared() {
        streamJob?.cancel()
        super.onCleared()
    }
}
