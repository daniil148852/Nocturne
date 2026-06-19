package com.nocturne.game.ui.navigation

/**
 * Every nav route in the app. Each route knows its URI template + a builder.
 */
sealed class Route(val route: String) {
    data object Setup : Route("setup")
    data object Menu : Route("menu")
    data object Briefing : Route("briefing/{caseId}") {
        fun build(caseId: String) = "briefing/$caseId"
        const val ARG = "caseId"
    }
    data object Hub : Route("hub/{caseId}") {
        fun build(caseId: String) = "hub/$caseId"
        const val ARG = "caseId"
    }
    data object Location : Route("location/{caseId}/{locationId}") {
        fun build(caseId: String, locationId: String) = "location/$caseId/$locationId"
        const val ARG_CASE = "caseId"
        const val ARG_LOC = "locationId"
    }
    data object Interrogation : Route("interrogation/{caseId}/{suspectId}") {
        fun build(caseId: String, suspectId: String) = "interrogation/$caseId/$suspectId"
        const val ARG_CASE = "caseId"
        const val ARG_SUSPECT = "suspectId"
    }
    data object Accusation : Route("accusation/{caseId}") {
        fun build(caseId: String) = "accusation/$caseId"
        const val ARG = "caseId"
    }
    data object Resolution : Route("resolution/{caseId}/{wasCorrect}") {
        fun build(caseId: String, wasCorrect: Boolean) = "resolution/$caseId/$wasCorrect"
        const val ARG = "caseId"
        const val ARG_OK = "wasCorrect"
    }
}
