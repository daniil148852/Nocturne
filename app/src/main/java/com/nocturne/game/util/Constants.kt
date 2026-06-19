package com.nocturne.game.util

object MistralEndpoints {
    const val BASE = "https://api.mistral.ai/"
    const val CHAT_COMPLETIONS = "v1/chat/completions"

    // mistral-medium produces the best creative writing in Russian.
    // open-mixtral-8x7b is the cheaper / free-tier fallback.
    const val DEFAULT_MODEL = "mistral-medium"
    const val FALLBACK_MODEL = "open-mixtral-8x7b"

    const val MAX_TOKENS_CASE = 2200
    const val MAX_TOKENS_DIALOG = 350
    const val MAX_TOKENS_INSPECT = 300
    const val MAX_TOKENS_RESOLUTION = 600

    const val TEMPERATURE = 0.9
    const val TOP_P = 0.95
}
