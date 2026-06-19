package com.nocturne.game

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nocturne.game.data.api.MistralApi
import com.nocturne.game.data.repository.CaseRepository
import com.nocturne.game.data.repository.SettingsRepository
import com.nocturne.game.util.Prompts
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Manual service-locator. No Hilt, no KSP — keeps the build simple and inspectable.
 * All long-lived dependencies live here for the app's lifetime.
 */
class AppContainer(@Suppress("unused") private val context: Context) {

    private val ctx: Context = context.applicationContext

    // 45s reads — Mixtral case-generation prompts can be slow.
    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val api: MistralApi by lazy { MistralApi(okHttp) }

    val prompts: Prompts by lazy { Prompts() }

    // Encrypted storage for the API key.
    private val securePrefs by lazy {
        val masterKey = MasterKey.Builder(ctx)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            ctx,
            "nocturne_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Plain DataStore for settings + active-case pointer.
    private val Context.settingsStore by preferencesDataStore(name = "nocturne_settings")
    val settings: SettingsRepository by lazy { SettingsRepository(securePrefs, ctx.settingsStore) }
    val cases: CaseRepository by lazy { CaseRepository(ctx.settingsStore) }
}

private const val SECURE_PREFS_FILE = "nocturne_secure_prefs"
