package com.nocturne.game.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nocturne.game.AppContainer
import com.nocturne.game.ui.components.RainBackground
import com.nocturne.game.ui.noir.LanternFlicker
import com.nocturne.game.ui.screens.accusation.AccusationScreen
import com.nocturne.game.ui.screens.briefing.BriefingScreen
import com.nocturne.game.ui.screens.hub.InvestigationHubScreen
import com.nocturne.game.ui.screens.interrogation.InterrogationScreen
import com.nocturne.game.ui.screens.location.LocationScreen
import com.nocturne.game.ui.screens.menu.MainMenuScreen
import com.nocturne.game.ui.screens.resolution.ResolutionScreen
import com.nocturne.game.ui.screens.setup.SetupScreen

@Composable
fun NocturneNav(container: AppContainer, startRoute: String) {
    val nav = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        RainBackground(intensity = 0.85f)
        LanternFlicker {
            NavHost(
                navController = nav,
                startDestination = startRoute
            ) {
                composable(Route.Setup.route) {
                    SetupScreen(
                        onSaved = {
                            nav.navigate(Route.Menu.route) {
                                popUpTo(Route.Setup.route) { inclusive = true }
                            }
                        },
                        container = container
                    )
                }
                composable(Route.Menu.route) {
                    MainMenuScreen(
                        container = container,
                        onNewCase = { nav.navigate(Route.Briefing.build(it)) },
                        onContinueCase = { nav.navigate(Route.Hub.build(it)) },
                        onOpenSetup = { nav.navigate(Route.Setup.route) }
                    )
                }
                composable(
                    Route.Briefing.route,
                    arguments = listOf(navArgument(Route.Briefing.ARG) { type = NavType.StringType })
                ) { entry ->
                    val caseId = entry.arguments?.getString(Route.Briefing.ARG).orEmpty()
                    BriefingScreen(
                        caseId = caseId,
                        container = container,
                        onBack = { nav.popBackStack() },
                        onStart = { nav.navigate(Route.Hub.build(caseId)) {
                            popUpTo(Route.Briefing.route) { inclusive = true }
                        } }
                    )
                }
                composable(
                    Route.Hub.route,
                    arguments = listOf(navArgument(Route.Hub.ARG) { type = NavType.StringType })
                ) { entry ->
                    val caseId = entry.arguments?.getString(Route.Hub.ARG).orEmpty()
                    InvestigationHubScreen(
                        caseId = caseId,
                        container = container,
                        onBack = { nav.popBackStack() },
                        onOpenLocation = { loc -> nav.navigate(Route.Location.build(caseId, loc)) },
                        onOpenSuspect = { sus -> nav.navigate(Route.Interrogation.build(caseId, sus)) },
                        onAccuse = { nav.navigate(Route.Accusation.build(caseId)) }
                    )
                }
                composable(
                    Route.Location.route,
                    arguments = listOf(
                        navArgument(Route.Location.ARG_CASE) { type = NavType.StringType },
                        navArgument(Route.Location.ARG_LOC) { type = NavType.StringType }
                    )
                ) { entry ->
                    val cid = entry.arguments?.getString(Route.Location.ARG_CASE).orEmpty()
                    val lid = entry.arguments?.getString(Route.Location.ARG_LOC).orEmpty()
                    LocationScreen(
                        caseId = cid,
                        locationId = lid,
                        container = container,
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(
                    Route.Interrogation.route,
                    arguments = listOf(
                        navArgument(Route.Interrogation.ARG_CASE) { type = NavType.StringType },
                        navArgument(Route.Interrogation.ARG_SUSPECT) { type = NavType.StringType }
                    )
                ) { entry ->
                    val cid = entry.arguments?.getString(Route.Interrogation.ARG_CASE).orEmpty()
                    val sid = entry.arguments?.getString(Route.Interrogation.ARG_SUSPECT).orEmpty()
                    InterrogationScreen(
                        caseId = cid,
                        suspectId = sid,
                        container = container,
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(
                    Route.Accusation.route,
                    arguments = listOf(navArgument(Route.Accusation.ARG) { type = NavType.StringType })
                ) { entry ->
                    val cid = entry.arguments?.getString(Route.Accusation.ARG).orEmpty()
                    AccusationScreen(
                        caseId = cid,
                        container = container,
                        onBack = { nav.popBackStack() },
                        onSubmitted = { ok, cid2 ->
                            nav.navigate(Route.Resolution.build(cid2, ok)) {
                                popUpTo(Route.Hub.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    Route.Resolution.route,
                    arguments = listOf(
                        navArgument(Route.Resolution.ARG) { type = NavType.StringType },
                        navArgument(Route.Resolution.ARG_OK) { type = NavType.BoolType }
                    )
                ) { entry ->
                    val cid = entry.arguments?.getString(Route.Resolution.ARG).orEmpty()
                    val ok = entry.arguments?.getBoolean(Route.Resolution.ARG_OK) ?: false
                    ResolutionScreen(
                        caseId = cid,
                        wasCorrect = ok,
                        container = container,
                        onNextCase = { newId ->
                            nav.navigate(Route.Menu.route) {
                                popUpTo(Route.Menu.route) { inclusive = true }
                            }
                            nav.navigate(Route.Briefing.build(newId))
                        },
                        onBackToMenu = {
                            nav.navigate(Route.Menu.route) {
                                popUpTo(Route.Menu.route) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
