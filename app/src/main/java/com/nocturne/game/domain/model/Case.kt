package com.nocturne.game.domain.model

import kotlinx.serialization.Serializable

/**
 * A complete, AI-generated case. Persisted as JSON in DataStore.
 */
@Serializable
data class Case(
    val id: String,
    val city: String,
    val date: String,
    val victim: Victim,
    val crimeScene: CrimeScene,
    val suspects: List<Suspect>,
    val evidence: List<Evidence>,
    val redHerrings: List<String> = emptyList(),
    val truth: Truth,
    val epilogue: String = ""
) {
    val killerId: String get() = truth.killerId

    fun suspect(id: String): Suspect? = suspects.firstOrNull { it.id == id }
    fun evidence(id: String): Evidence? = evidence.firstOrNull { it.id == id }
}

@Serializable
data class Victim(
    val name: String,
    val age: Int,
    val profession: String,
    val backstory: String
)

@Serializable
data class CrimeScene(
    val address: String,
    val description: String,
    val weather: String
)
