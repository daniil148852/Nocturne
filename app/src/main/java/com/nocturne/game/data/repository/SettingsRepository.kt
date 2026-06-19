package com.nocturne.game.data.repository

import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nocturne.game.ui.navigation.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * API key + active-case pointer + a few cosmetic toggles.
 * - API key → encrypted prefs (MasterKey AES256_GCM).
 * - Active case id + score → plain DataStore (not sensitive).
 */
class SettingsRepository(
    private val securePrefs: SharedPreferences,
    private val dataStore: DataStore<Preferences>
) {

    // --- API KEY (secure) -----------------------------------------------

    fun apiKey(): String? = securePrefs.getString(KEY_API, null)?.takeIf { it.isNotBlank() }

    fun saveApiKey(value: String) {
        securePrefs.edit().putString(KEY_API, value.trim()).apply()
    }

    fun clearApiKey() {
        securePrefs.edit().remove(KEY_API).apply()
    }

    // --- ACTIVE CASE & APP NAV -----------------------------------------

    val activeCaseIdFlow: Flow<String?> = dataStore.data.map { it[KEY_ACTIVE_CASE] }
    suspend fun activeCaseId(): String? = activeCaseIdFlow.first()

    suspend fun setActiveCase(id: String?) {
        dataStore.edit { p ->
            if (id == null) p.remove(KEY_ACTIVE_CASE) else p[KEY_ACTIVE_CASE] = id
        }
    }

    val totalCasesFlow: Flow<Int> = dataStore.data.map { it[KEY_TOTAL]?.toIntOrNull() ?: 0 }
    val solvedCasesFlow: Flow<Int> = dataStore.data.map { it[KEY_SOLVED]?.toIntOrNull() ?: 0 }

    suspend fun bumpStats(solved: Boolean) {
        dataStore.edit { p ->
            p[KEY_TOTAL] = ((p[KEY_TOTAL]?.toIntOrNull() ?: 0) + 1).toString()
            if (solved) p[KEY_SOLVED] = ((p[KEY_SOLVED]?.toIntOrNull() ?: 0) + 1).toString()
        }
    }

    // --- INITIAL ROUTE -------------------------------------------------

    fun startRouteOrSetup(): String {
        // Synchronous lookup — only the API key presence matters here. We
        // re-evaluate the saved case-id once MainMenu mounts via the persisted repo.
        val key = apiKey()
        return if (key.isNullOrBlank()) Route.SETUP.route else Route.MENU.route
    }

    companion object {
        private const val KEY_API = "mixtral_api_key"
        private val KEY_ACTIVE_CASE = stringPreferencesKey("active_case_id")
        private val KEY_TOTAL = stringPreferencesKey("total_cases")
        private val KEY_SOLVED = stringPreferencesKey("solved_cases")
    }
}
