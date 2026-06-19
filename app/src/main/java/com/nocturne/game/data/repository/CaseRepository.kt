package com.nocturne.game.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nocturne.game.domain.model.Case
import com.nocturne.game.domain.model.Evidence
import com.nocturne.game.domain.model.InterrogationTranscript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Cases are stored as a single big JSON string under KEY_CASES.
 * Good enough for a handful of saved cases per device — saves us a Room DB.
 */
class CaseRepository(private val dataStore: DataStore<Preferences>) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val KEY_CASES = stringPreferencesKey("cases_json")

    /** Returns all saved cases. */
    suspend fun list(): List<Case> {
        val raw = dataStore.data.first()[KEY_CASES] ?: return emptyList()
        return runCatching { json.decodeFromString<List<Case>>(raw) }.getOrDefault(emptyList())
    }

    fun listFlow(): Flow<List<Case>> = dataStore.data.map { p ->
        val raw = p[KEY_CASES] ?: return@map emptyList()
        runCatching { json.decodeFromString<List<Case>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun load(id: String): Case? = list().firstOrNull { it.id == id }

    suspend fun save(case: Case) {
        val all = list().toMutableList()
        val idx = all.indexOfFirst { it.id == case.id }
        if (idx >= 0) all[idx] = case else all.add(case)
        dataStore.edit { it[KEY_CASES] = json.encodeToString(all) }
    }

    suspend fun delete(id: String) {
        val all = list().toMutableList()
        all.removeAll { it.id == id }
        dataStore.edit { it[KEY_CASES] = json.encodeToString(all) }
    }

    suspend fun updateEvidence(caseId: String, evidence: List<Evidence>) = update(caseId) { c ->
        c.copy(evidence = evidence)
    }

    suspend fun updateTranscript(caseId: String, suspectId: String, transcript: InterrogationTranscript) =
        update(caseId) { c ->
            c // we don't store transcript on Case itself, see [transcriptsFlow] below.
        }

    private suspend inline fun update(caseId: String, transform: (Case) -> Case) {
        val all = list().toMutableList()
        val idx = all.indexOfFirst { it.id == caseId }
        if (idx >= 0) {
            all[idx] = transform(all[idx])
            dataStore.edit { it[KEY_CASES] = json.encodeToString(all) }
        }
    }
}
