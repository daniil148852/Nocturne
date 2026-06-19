package com.nocturne.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nocturne.game.ui.navigation.NocturneNav
import com.nocturne.game.ui.theme.NocturneTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(0x00000000),
            navigationBarStyle = SystemBarStyle.dark(0x00000000)
        )
        val container = (application as NocturneApp).container
        setContent {
            NocturneTheme {
                val startRoute = remember { container.settings.startRouteOrSetup() }
                NocturneNav(container = container, startRoute = startRoute)
            }
        }
    }
}
