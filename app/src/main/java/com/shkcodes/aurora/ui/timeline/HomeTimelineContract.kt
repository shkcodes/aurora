package com.shkcodes.aurora.ui.timeline

import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItems

class HomeTimelineContract {
    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = true,
        val tweets: TweetItems = emptyList(),
        val isPaginatedLoading: Boolean = false,
        val isPaginatedError: Boolean = false,
        val autoplayVideos: Boolean = false,
        val isTerminalError: Boolean = false,
        val errorMessage: String = "",
        val newTweets: TweetItems = emptyList()
    )

    sealed class Intent {
        object Retry : Intent()
        object LoadNextPage : Intent()
        object Refresh : Intent()
        object MarkItemsAsSeen : Intent()
        data class MediaClick(val index: Int, val tweetId: Long) : Intent()
        data class ScrollIndexChange(val index: Int) : Intent()
        data class TweetContentClick(val text: String) : Intent()
    }

    sealed class TimelineSideEffect {
        object ScrollToTop : TimelineSideEffect()
        data class RetainScrollState(val newTweetsCount: Int) : TimelineSideEffect()
        data class OpenUrl(val url: String) : TimelineSideEffect()
    }

    sealed class Screen {
        data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
        data class UserProfile(val userHandle: String) : Screen()
    }
}
