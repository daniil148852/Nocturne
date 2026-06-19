package com.nocturne.game.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Evidence(
    val id: String,
    val name: String,
    val location: String,
    val description: String,
    val linkedSuspectId: String? = null,
    val weight: Int = 1,
    val collected: Boolean = false
)
