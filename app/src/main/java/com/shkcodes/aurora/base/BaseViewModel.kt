package com.shkcodes.aurora.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, I>(initialState: S) : ViewModel() {

    protected abstract val dispatcherProvider: DispatcherProvider

    private val viewStateFlow = MutableStateFlow(initialState)

    private val sideEffectsFlow = MutableSharedFlow<SideEffect>()

    fun emitState(emitter: () -> S) {
        viewModelScope.launch { viewStateFlow.emit(emitter.invoke()) }
    }

    fun getState(): StateFlow<S> = viewStateFlow

    fun getSideEffects(): SharedFlow<SideEffect> = sideEffectsFlow

    open fun handleIntent(intent: I) {}

    protected fun onSideEffect(sideEffect: SideEffect) {
        viewModelScope.launch {
            sideEffectsFlow.emit(sideEffect)
        }
    }
}
