package com.shkcodes.aurora.ui.home

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.cache.entities.CachedTweets
import com.shkcodes.aurora.ui.home.HomeContract.State.Loading

class HomeContract {
    abstract class ViewModel : BaseViewModel<State, Intent>(Loading)

    sealed class State {
        object Loading : State()
        data class Content(val tweets: CachedTweets) :
            State()

        data class Error(val message: String) : State()
    }

    sealed class Intent {
        object Init : Intent()
        object Retry : Intent()
    }
}
