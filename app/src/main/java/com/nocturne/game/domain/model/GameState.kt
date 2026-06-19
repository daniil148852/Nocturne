package com.nocturne.game.domain.model

import kotlinx.serialization.Serializable

/**
 * Full game transcript for a suspect interrogation.
 * Used to give the model enough context for coherent replies.
 */
@Serializable
data class InterrogationTranscript(
    val suspectId: String,
    val turns: List<Turn> = emptyList()
) {
    fun asPairs(): List<Pair<String, String>> = turns.map { it.speaker to it.text }
}

@Serializable
data class Turn(
    val speaker: String, // "detective" or "suspect"
    val text: String
)

/**
 * What we keep about the player's current case so when they close & reopen
 * the app, we can put them right back in the team room.
 */
@Serializable
data class PlayerProgress(
    val caseId: String,
    val collectedEvidence: Set<String> = emptySet(),
    val inspectedLocations: Set<String> = emptySet(),
    val transcripts: Map<String, InterrogationTranscript> = emptyMap(),
    val accuracyHistory: List<AccusationOutcome> = emptyList()
)

@Serializable
data class AccusationOutcome(
    val caseId: String,
    val wasCorrect: Boolean,
    val pickedSuspectId: String,
    val trueSuspectId: String,
    val verdictText: String = "",
    val epilogueText: String = ""
)
