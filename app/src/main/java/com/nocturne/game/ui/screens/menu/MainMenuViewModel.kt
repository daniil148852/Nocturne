package com.nocturne.game.ui.screens.menu

import androidx.lifecycle.SavedStateHandle
import com.nocturne.game.AppContainer
import com.nocturne.game.data.api.ChatMessage
import com.nocturne.game.data.api.ChatRequest
import com.nocturne.game.data.api.JsonExtract
import com.nocturne.game.data.api.ResponseFormat
import com.nocturne.game.data.repository.CaseRepository
import com.nocturne.game.domain.model.Case
import com.nocturne.game.ui.common.NocturneViewModel
import com.nocturne.game.util.MistralEndpoints
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class MainMenuViewModel(
    container: AppContainer,
    @Suppress("unused") handle: SavedStateHandle
) : NocturneViewModel() {

    private val api = container.api
    private val prompts = container.prompts
    private val cases = container.cases
    private val settings = container.settings

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    data class State(
        val generating: Boolean = false,
        val lastError: String? = null,
        val statsText: String = "— / —",
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        refreshStats()
    }

    private suspend fun refreshStatsImpl() {
        val total = settings.totalCasesFlow.first()
        val solved = settings.solvedCasesFlow.first()
        _state.update { it.copy(statsText = "$solved / $total") }
    }

    fun refreshStats() = viewModelScope.launch { refreshStatsImpl() }

    fun newCase(onReady: (String) -> Unit) {
        if (_state.value.generating) return
        val key = settings.apiKey()
        if (key.isNullOrBlank()) {
            _state.update { it.copy(lastError = "Ключ API не задан") }
            return
        }
        _state.update { it.copy(generating = true, lastError = null) }
        viewModelScope.launch {
            try {
                val case = generateCase(key)
                cases.save(case)
                settings.setActiveCase(case.id)
                settings.bumpStats(solved = false)
                refreshStats()
                _state.update { it.copy(generating = false) }
                onReady(case.id)
            } catch (t: Throwable) {
                _state.update { it.copy(generating = false, lastError = friendlyError(t)) }
            }
        }
    }

    fun continueCase(caseId: String) {
        viewModelScope.launch {
            settings.setActiveCase(caseId)
        }
    }

    private suspend fun generateCase(apiKey: String): Case {
        val seed = System.currentTimeMillis().toString()
        val req = ChatRequest(
            model = MistralEndpoints.DEFAULT_MODEL,
            messages = listOf(
                ChatMessage("system", prompts.caseSystem()),
                ChatMessage("user", prompts.caseUserPrompt(seed))
            ),
            temperature = MistralEndpoints.TEMPERATURE,
            topP = MistralEndpoints.TOP_P,
            maxTokens = MistralEndpoints.MAX_TOKENS_CASE,
            stream = false,
            responseFormat = ResponseFormat("json_object")
        )
        val raw = try {
            api.complete(apiKey, req)
        } catch (e: Throwable) {
            // Fallback to a smaller open model on failure
            api.complete(apiKey, req.copy(model = MistralEndpoints.FALLBACK_MODEL))
        }
        val body = JsonExtract.extractObject(raw)
        return json.decodeFromString<Case>(body)
    }

    private fun friendlyError(t: Throwable): String = when {
        t.message?.contains("401") == true -> "Ключ отклонён. Проверь его и попробуй снова."
        t.message?.contains("429") == true -> "Лимит запросов. Подожди минуту и попробуй снова."
        t.message?.contains("HTTP") == true && t.message!!.contains("4") == false ->
            "Не удалось выйти на связь. Проверь интернет."
        else -> "Что-то пошло не так. Модель вернула странный ответ."
    }
}
