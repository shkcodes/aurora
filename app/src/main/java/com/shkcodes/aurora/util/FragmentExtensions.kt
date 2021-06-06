package com.shkcodes.aurora.util

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis

fun Fragment.applySharedAxisExitTransition(axis: Int = MaterialSharedAxis.Z, forward: Boolean = true) {
    exitTransition = MaterialSharedAxis(axis, forward).apply {
        duration =
            AnimationConstants.DEFAULT_DURATION
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
