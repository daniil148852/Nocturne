package com.nocturne.game.data.api

import com.nocturne.game.util.MistralEndpoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 * Thin wrapper around the Mistral Chat Completions endpoint.
 *
 * - Synchronous call (`complete`) — returns full assistant text.
 * - Streaming call (`stream`) — emits delta text chunks as they arrive.
 *
 * We use OkHttp directly (no Retrofit) to keep the dependency surface tiny
 * and to keep the request shape fully under our control.
 */
class MistralApi(private val httpClient: OkHttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    /**
     * Performs a non-streaming chat completion, returns the assistant text.
     */
    suspend fun complete(
        apiKey: String,
        request: ChatRequest,
        model: String = request.model
    ): String = withContext(Dispatchers.IO) {
        val payload = request.copy(model = model, stream = false)
        val body = json.encodeToString(ChatRequest.serializer(), payload)
            .toRequestBody(JSON)
        val req = Request.Builder()
            .url(MistralEndpoints.BASE + MistralEndpoints.CHAT_COMPLETIONS)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        httpClient.newCall(req).execute().use { response ->
            parseAssistantText(response)
        }
    }

    /**
     * Streams the assistant reply as a Flow<String> of delta chunks.
     */
    fun stream(
        apiKey: String,
        request: ChatRequest,
        model: String = request.model
    ): Flow<String> = callbackFlow {
        val payload = request.copy(model = model, stream = true)
        val body = json.encodeToString(ChatRequest.serializer(), payload)
            .toRequestBody(JSON)
        val req = Request.Builder()
            .url(MistralEndpoints.BASE + MistralEndpoints.CHAT_COMPLETIONS)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(body)
            .build()

        val call = httpClient.newCall(req)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use { r ->
                        if (!r.isSuccessful) {
                            val errBody = r.body?.string().orEmpty()
                            close(IOException("HTTP ${r.code}: $errBody"))
                            return
                        }
                        val source = r.body?.source()
                            ?: run { close(IOException("Empty body")); return }
                        while (!source.exhausted()) {
                            val line = source.readUtf8Line() ?: break
                            if (!line.startsWith("data:")) continue
                            val data = line.removePrefix("data:").trim()
                            if (data == "[DONE]") { close(); return }
                            if (data.isEmpty()) continue
                            val chunk = runCatching {
                                json.decodeFromString(ChatResponse.serializer(), data)
                            }.getOrNull() ?: continue
                            val delta = chunk.choices.firstOrNull()?.delta?.content
                                ?: chunk.choices.firstOrNull()?.message?.content
                                ?: continue
                            trySend(delta)
                        }
                        close()
                    }
                } catch (t: Throwable) {
                    close(t)
                }
            }
        })
        awaitClose { call.cancel() }
    }.flowOn(Dispatchers.IO)

    private fun parseAssistantText(response: Response): String {
        if (!response.isSuccessful) {
            val errBody = response.body?.string().orEmpty()
            throw IOException("HTTP ${response.code}: $errBody")
        }
        val text = response.body?.string()
            ?: throw IOException("Empty body from Mistral")
        val parsed = json.decodeFromString(ChatResponse.serializer(), text)
        val msg = parsed.choices.firstOrNull()?.message?.content
            ?: throw IOException("No message content in response")
        return msg
    }

    companion object {
        private val JSON = "application/json; charset=utf-8".toMediaType()
    }
}
