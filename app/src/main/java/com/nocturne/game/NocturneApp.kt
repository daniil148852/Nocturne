package com.nocturne.game

import android.app.Application

/**
 * Application entry point. Holds the AppContainer for manual DI.
 */
class NocturneApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
