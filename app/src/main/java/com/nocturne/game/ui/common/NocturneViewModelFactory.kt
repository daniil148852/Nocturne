package com.nocturne.game.ui.common

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.nocturne.game.AppContainer
import com.nocturne.game.NocturneApp

/**
 * Base + factory for every screen ViewModel — they all need an [AppContainer] and a [SavedStateHandle].
 * Each ViewModel exposes a companion `Factory(container)` that returns a [ViewModelProvider.Factory].
 */
abstract class NocturneViewModel : ViewModel() {

    companion object {
        inline fun <reified VM : NocturneViewModel> factory(
            crossinline create: (AppContainer, SavedStateHandle) -> VM
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val handle = extras.createSavedStateHandle()
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application not provided in CreationExtras")
                val container = (app as NocturneApp).container
                @Suppress("UNCHECKED_CAST")
                return create(container, handle) as T
            }
        }
    }
}
