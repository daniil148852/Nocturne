package com.nocturne.game.data.api

/**
 * Best-effort extractor for JSON objects inside Model responses.
 * Handles three common cases:
 *  1. Model wraps its answer in ```json ... ``` fences.
 *  2. Model prepends prose like "Вот ответ: ..." before the JSON body.
 *  3. Model appends closing commentary after the closing brace.
 *
 * Strategy: find the first '{' and the matching '}' (string-aware) — then return that slice.
 */
object JsonExtract {

    fun extractObject(raw: String): String {
        val trimmed = raw
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val start = trimmed.indexOf('{')
        if (start < 0) error("No JSON object found in response")

        var depth = 0
        var inString = false
        var escape = false
        var end = -1
        for (i in start until trimmed.length) {
            val c = trimmed[i]
            if (escape) { escape = false; continue }
            if (c == '\\') { escape = true; continue }
            if (c == '"') { inString = !inString; continue }
            if (inString) continue
            when (c) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) { end = i; break }
                }
            }
        }
        if (end < 0) error("Unbalanced braces in response")
        return trimmed.substring(start, end + 1)
    }
}
