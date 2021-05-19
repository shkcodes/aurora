package com.shkcodes.aurora.ui.home

import com.shkcodes.aurora.base.BaseViewModel

interface HomeContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(val isLoading: Boolean = false)

    sealed class Intent
}
