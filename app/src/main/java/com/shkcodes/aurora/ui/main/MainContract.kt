package com.shkcodes.aurora.ui.main

import com.shkcodes.aurora.base.BaseViewModel

interface MainContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(val isLoading: Boolean = false)

    sealed class Intent
}
