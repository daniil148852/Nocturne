package com.nocturne.game.ui.common

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nocturne.game.AppContainer
import com.nocturne.game.NocturneApp

/**
 * Manual ViewModel base used by every screen. Streams viewModelScope and a
 * factory helper that resolves AppContainer from the Application + a SavedStateHandle.
 */
open class NocturneViewModel : ViewModel() {

    companion object {
        inline fun <reified VM : NocturneViewModel> factory(
            crossinline builder: (AppContainer, SavedStateHandle) -> VM
        ) = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val container = (app as NocturneApp).container
                val handle = createSavedStateHandle()
                builder(container, handle)
            }
        }
    }
}
