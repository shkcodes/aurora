package com.shkcodes.aurora.base

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface ViewModelOwner<S, I> {

    val viewModel: BaseViewModel<S, I>

    val screenLifecycle: Lifecycle

    fun renderState(state: S) {}

    fun handleAction(sideEffect: SideEffect.Action<*>) {}

    fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {}

    fun dispatchIntent(intent: I) {
        viewModel.handleIntent(intent)
    }

    fun observeViewState() {
        viewModel.getState().flowWithLifecycle(screenLifecycle, Lifecycle.State.RESUMED)
            .onEach(::renderState)
            .launchIn(screenLifecycle.coroutineScope)
    }

    fun observeSideEffects() {
        viewModel.getSideEffects().flowWithLifecycle(screenLifecycle, Lifecycle.State.RESUMED)
            .onEach(::handleSideEffects)
            .launchIn(screenLifecycle.coroutineScope)
    }

    private fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is SideEffect.Action<*> -> handleAction(sideEffect)
            is SideEffect.DisplayScreen<*> -> handleNavigation(sideEffect)
        }
    }
}
