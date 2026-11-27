package com.example.service

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.ui.platform.ComposeView

class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    private var hasCreated = false
    private var hasDestroyed = false

    fun onCreate() {
        if (hasCreated || hasDestroyed) return
        hasCreated = true
        try {
            savedStateRegistryController.performRestore(Bundle())
        } catch (_: Throwable) {}
        try {
            if (lifecycleRegistry.currentState == Lifecycle.State.INITIALIZED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
            if (lifecycleRegistry.currentState == Lifecycle.State.CREATED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }
            if (lifecycleRegistry.currentState == Lifecycle.State.STARTED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        } catch (_: Throwable) {}
    }

    fun onDestroy() {
        if (hasDestroyed) return
        hasDestroyed = true
        try {
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
            if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.CREATED)) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            }
        } catch (_: Throwable) {}
        try {
            store.clear()
        } catch (_: Throwable) {}
    }

    fun attachTo(composeView: ComposeView) {
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
    }
}
