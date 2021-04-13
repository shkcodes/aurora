package com.shkcodes.aurora.ui.splash

import com.shkcodes.aurora.base.BaseViewModel

interface SplashContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    class State

    class Intent

    object Constants {
        const val SPLASH_TIMEOUT = 1500L
    }
}
