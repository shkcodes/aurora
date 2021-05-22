package com.shkcodes.aurora.ui.profile

import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.timeline.TimelineItems

interface ProfileContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = true,
        val user: User? = null,
        val items: TimelineItems = emptyList(),
        val isPaginatedError: Boolean = false,
        val isTerminalError: Boolean = false,
        val errorMessage: String = ""
    )

    sealed class Intent {
        data class Init(val userHandle: String) : Intent()
        data class Retry(val userHandle: String) : Intent()
    }
}
