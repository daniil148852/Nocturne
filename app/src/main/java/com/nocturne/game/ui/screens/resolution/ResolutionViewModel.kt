package com.nocturne.game.ui.screens.resolution

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.data.api.ChatMessage
import com.nocturne.game.data.api.ChatRequest
import com.nocturne.game.data.api.JsonExtract
import com.nocturne.game.data.api.ResponseFormat
import com.nocturne.game.domain.model.Case
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.util.MistralEndpoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ResolutionViewModel(
    container: AppContainer,
    handle: SavedStateHandle
) : NocturneViewModel() {

    private val cases = container.cases
    private val settings = container.settings
    private val api = container.api
    private val prompts = container.prompts

    private val caseId: String = handle.get<String>("caseId") ?: error("missing caseId")
    private val wasCorrect: Boolean = handle.get<Boolean>("wasCorrect") ?: false

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    @Serializable
    data class Verdict(
        val verdict: String,
        @SerialName("whatHappened") val whatHappened: String,
        @SerialName("finalNote") val finalNote: String
    )

    data class State(
        val theCase: Case? = null,
        val verdict: String = "",
        val whatHappened: String = "",
        val finalNote: String = "",
        val streaming: Boolean = false,
        val error: String? = null
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val c = cases.load(caseId)
            _state.update { it.copy(theCase = c) }
            if (c != null) generateVerdict(c)
        }
    }

    private fun generateVerdict(c: Case) {
        val key = settings.apiKey() ?: return
        val suspect = c.suspects.firstOrNull { it.id == c.truth.killerId }
        if (suspect == null) {
            _state.update {
                it.copy(
                    verdict = "Дело не раскрыто.",
                    whatHappened = c.truth.method,
                    finalNote = c.epilogue
                )
            }
            return
        }
        _state.update { it.copy(streaming = true, error = null) }
        viewModelScope.launch {
            try {
                val req = ChatRequest(
                    model = MistralEndpoints.DEFAULT_MODEL,
                    messages = listOf(
                        ChatMessage("system", prompts.resolutionSystem()),
                        ChatMessage(
                            "user",
                            prompts.resolutionUserPrompt(
                                wasCorrect = wasCorrect,
                                killerName = suspect.name,
                                motive = c.truth.trueMotive,
                                method = c.truth.method,
                                twist = c.truth.twist
                            )
                        )
                    ),
                    maxTokens = MistralEndpoints.MAX_TOKENS_RESOLUTION,
                    responseFormat = ResponseFormat("json_object")
                )
                val raw = api.complete(key, req)
                val body = JsonExtract.extractObject(raw)
                val parsed = json.decodeFromString<Verdict>(body)
                _state.update {
                    it.copy(
                        streaming = false,
                        verdict = parsed.verdict,
                        whatHappened = parsed.whatHappened,
                        finalNote = parsed.finalNote
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(streaming = false, error = "Запись в деле прервалась") }
            }
        }
    }

    fun newCase(): Unit {
        // No-op on the VM side — caller asks parent to start a fresh case via MainMenuScreen
    }
}
