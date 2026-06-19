package com.nocturne.game.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Suspect(
    val id: String,
    val name: String,
    val age: Int,
    val profession: String,
    val appearance: String,
    val relation: String,
    val motive: String,
    val alibi: String,
    val secret: String,
    val isKiller: Boolean
)

/**
 * The hidden answer — only revealed by the model on accusation/resolution.
 * Kept in the Case so we can score the accusation locally without an extra API call.
 */
@Serializable
data class Truth(
    val killerId: String,
    val trueMotive: String,
    val method: String,
    val twist: String,
    val hiddenConnection: String = ""
)
