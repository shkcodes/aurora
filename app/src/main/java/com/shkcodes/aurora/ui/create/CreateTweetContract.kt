package com.shkcodes.aurora.ui.create

import com.shkcodes.aurora.base.BaseViewModel

interface CreateTweetContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = false,
        val content: String? = null
    )

    sealed class Intent {
        object PostTweet : Intent()
        data class ContentChange(val content: String) : Intent()
    }
}
