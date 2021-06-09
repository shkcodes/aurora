package com.shkcodes.aurora.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

private class GenericLifecycleObserver(
    lifecycle: Lifecycle,
    private val destroyAction: () -> Unit
) : LifecycleObserver {

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        destroyAction()
    }
}

fun Lifecycle.onDestroy(action: () -> Unit) {
    GenericLifecycleObserver(this, action)
}
