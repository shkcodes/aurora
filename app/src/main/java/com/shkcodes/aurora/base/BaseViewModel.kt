package com.shkcodes.aurora.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, I>(private val initialState: S) : ViewModel() {

    protected abstract val dispatcherProvider: DispatcherProvider

    private val viewStateFlow = MutableSharedFlow<S>(1).apply {
        tryEmit(initialState)
    }

    private val sideEffectsFlow = MutableSharedFlow<SideEffect>()

    var currentState = initialState
        set(value) {
            field = value
            viewStateFlow.tryEmit(field)
        }

    @Composable
    fun composableState() = getState().collectAsState(initial = currentState).value

    fun getState(): SharedFlow<S> = viewStateFlow

    fun getSideEffects(): SharedFlow<SideEffect> = sideEffectsFlow

    open fun handleIntent(intent: I) {}

    protected fun onSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch {
            sideEffectsFlow.emit(sideEffect)
        }
    }

    fun EventBus.emitEvent(event: Event) {
        viewModelScope.launch { sendEvent(event) }
    }
}
