package com.shkcodes.aurora.ui.login

import com.shkcodes.aurora.base.BaseViewModel

interface LoginContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    class State

    sealed class Intent {
        object ShowAuthScreen : Intent()
    }
}
