package com.shkcodes.aurora.ui.profile

import com.shkcodes.aurora.api.response.User
import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItems

interface ProfileContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = true,
        val user: User? = null,
        val tweets: TweetItems = emptyList(),
        val isPaginatedLoading: Boolean = false,
        val isPaginatedError: Boolean = false,
        val isTerminalError: Boolean = false,
        val autoplayVideos: Boolean = false,
        val errorMessage: String = ""
    )

    sealed class Intent {
        object LoadNextPage : Intent()
        data class Init(val userHandle: String) : Intent()
        data class Retry(val userHandle: String) : Intent()
        data class MediaClick(val index: Int, val tweetId: Long) : Intent()
        data class TweetContentClick(val text: String) : Intent()
    }

    sealed class ProfileSideEffect {
        data class OpenUrl(val url: String) : ProfileSideEffect()
    }

    sealed class Screen {
        data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
        data class UserProfile(val userHandle: String) : Screen()
    }
}
