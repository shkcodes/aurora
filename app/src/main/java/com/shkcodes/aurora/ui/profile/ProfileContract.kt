package com.shkcodes.aurora.ui.profile

import com.shkcodes.aurora.R
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
        data class Init(val userHandle: String) : Intent()
        data class Retry(val userHandle: String) : Intent()
        data class MediaClick(val index: Int, val tweetId: Long) : Intent()
        data class AnnotatedContentClick(val text: String) : Intent()
        data class LoadNextPage(val force: Boolean = false) : Intent()
    }

    sealed class ProfileSideEffect {
        data class OpenUrl(val url: String) : ProfileSideEffect()
        data class ScrollToBottom(val tweets: TweetItems) : ProfileSideEffect()
    }

    sealed class Screen {
        data class MediaViewer(val index: Int, val tweetId: Long) : Screen()
        data class UserProfile(val userHandle: String) : Screen()
    }

    object Constants {
        const val USER_INFO_BACKGROUND_SCROLL_OFFSET = 0.2F
        const val USER_INFO_SCROLL_OFFSET = 0.3F
        const val BANNER_SCROLL_OFFSET = 0.0001F
        const val PROFILE_IMAGE_SCALE_LIMIT = 0.8F
        val tabIcons =
            listOf(R.drawable.ic_tweet, R.drawable.ic_media, R.drawable.ic_favorite, R.drawable.ic_profile)
    }
}
