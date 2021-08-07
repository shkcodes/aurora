package com.shkcodes.aurora.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<S, I>(private val initialState: S) : ViewModel() {

    protected abstract val dispatcherProvider: DispatcherProvider

    private val viewStateFlow = MutableSharedFlow<S>(1).apply {
        tryEmit(initialState)
    }

    private val sideEffectsFlow = MutableSharedFlow<SideEffect>()

    var currentState = initialState
        set(value) {
            field = value
            viewStateFlow.tryEmit(value)
        }

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

    suspend fun <T> execute(block: () -> T): Result<T> {
        return runCatching { withContext(dispatcherProvider.io) { block() } }
    }
}
