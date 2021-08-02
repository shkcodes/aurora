package com.shkcodes.aurora.util

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.transition.Transition
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.applySharedAxisExitTransition(axis: Int = MaterialSharedAxis.Z, forward: Boolean = true) {
    exitTransition = MaterialSharedAxis(axis, forward).apply {
        duration = AnimationConstants.DEFAULT_DURATION
    }
    reenterTransition = MaterialSharedAxis(axis, !forward).apply {
        duration = AnimationConstants.DEFAULT_DURATION
    }
}

fun Fragment.applySharedAxisEnterTransition(axis: Int = MaterialSharedAxis.Z, forward: Boolean = true) {
    enterTransition = MaterialSharedAxis(axis, forward).apply {
        duration = AnimationConstants.DEFAULT_DURATION
    }
    returnTransition = MaterialSharedAxis(axis, !forward).apply {
        duration = AnimationConstants.DEFAULT_DURATION
    }
}

fun Fragment.reenterTransitionListener(
    onStart: () -> Unit = {},
    onEnd: () -> Unit = {},
    onCancel: () -> Unit = {},
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
) {
    val listener = object : Transition.TransitionListener {
        override fun onTransitionStart(transition: Transition) {
            onStart()
        }

        override fun onTransitionEnd(transition: Transition) {
            onEnd()
        }

        override fun onTransitionCancel(transition: Transition) {
            onCancel()
        }

        override fun onTransitionPause(transition: Transition) {
            onPause()
        }

        override fun onTransitionResume(transition: Transition) {
            onResume()
        }
    }
    (reenterTransition as Transition).addListener(listener)
    viewLifecycleOwner.lifecycle.onDestroy {
        (reenterTransition as Transition).removeListener(listener)
    }
}

val Context.fileProviderAuthority: String
    get() = "$packageName.provider"
