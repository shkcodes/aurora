package com.shkcodes.aurora.ui.settings

import com.shkcodes.aurora.base.BaseViewModel

interface SettingsContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(val autoplayVideos: Boolean = false)

    sealed class Intent {
        object ToggleAutoplayVideos : Intent()
    }
}
