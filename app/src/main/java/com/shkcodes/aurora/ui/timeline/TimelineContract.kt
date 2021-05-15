package com.shkcodes.aurora.ui.timeline

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.timeline.TimelineContract.State.Content

class TimelineContract {
    abstract class ViewModel : BaseViewModel<State, Intent>(Content(true))

    sealed class State {
        data class Content(
            val isLoading: Boolean = false,
            val items: TimelineItems = emptyList(),
            val isPaginatedLoading: Boolean = false,
            val isPaginatedError: Boolean = false,
            val autoplayVideos: Boolean = false
        ) : State()

        data class Error(val message: String) : State()
    }

    sealed class Intent {
        object Init : Intent()
        object Retry : Intent()
        data class LoadNextPage(val currentState: Content) : Intent()
        data class Refresh(val currentState: Content) : Intent()
        data class MediaClick(val index: Int, val tweetId: Long) : Intent()
    }

    sealed class Screen {
        data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
    }
}
