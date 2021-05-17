package com.shkcodes.aurora.ui.timeline

import com.shkcodes.aurora.base.BaseViewModel

class TimelineContract {
    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = true,
        val items: TimelineItems = emptyList(),
        val isPaginatedLoading: Boolean = false,
        val isPaginatedError: Boolean = false,
        val autoplayVideos: Boolean = false,
        val isTerminalError: Boolean = false,
        val errorMessage: String = "",
        val newItems: TimelineItems = emptyList()
    )

    sealed class Intent {
        object Retry : Intent()
        object LoadNextPage : Intent()
        object Refresh : Intent()
        data class MediaClick(val index: Int, val tweetId: Long) : Intent()
    }

    sealed class TimelineSideEffect {
        data class RetainScrollState(val newItemsCount: Int) : TimelineSideEffect()
    }

    sealed class Screen {
        data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
    }
}
