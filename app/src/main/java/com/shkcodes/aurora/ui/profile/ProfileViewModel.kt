package com.shkcodes.aurora.ui.profile

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.MediaViewer
import com.shkcodes.aurora.ui.Screen.UserProfile
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.MediaClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.ScrollToBottom
import com.shkcodes.aurora.ui.profile.ProfileContract.ViewModel
import com.shkcodes.aurora.ui.tweetlist.TweetItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userService: UserService,
    private val errorHandler: ErrorHandler,
    private val preferenceService: PreferenceService
) : ViewModel() {

    private val autoplayVideos: Boolean
        get() = preferenceService.autoplayVideos

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                if (currentState.user == null) {
                    fetchData(intent.userHandle)
                }
            }

            is Retry -> {
                currentState = currentState.copy(isLoading = true, isTerminalError = false, errorMessage = "")
                fetchData(intent.userHandle)
            }

            is LoadNextPage -> {
                if ((!currentState.isPaginatedLoading && !currentState.isPaginatedError) || intent.force) {
                    fetchNextPage()
                }
            }

            is MediaClick -> {
                onSideEffect(SideEffect.DisplayScreen(MediaViewer(intent.index, intent.tweetId)))
            }

            is AnnotatedContentClick -> {
                when (intent.text.first()) {
                    'h' -> onSideEffect(SideEffect.Action(OpenUrl(intent.text)))
                    '@' -> onSideEffect(SideEffect.DisplayScreen(UserProfile(intent.text.substring(1))))
                }
            }
        }
    }

    private fun fetchData(userHandle: String) {
        viewModelScope.launch {
            runCatching {
                val userProfile =
                    async { userService.fetchUserProfile(userHandle) }
                val userTweets =
                    async { userService.fetchUserTweets(userHandle) }
                val userFavorites = async { userService.fetchUserFavorites(userHandle) }
                Triple(userProfile.await(), userTweets.await(), userFavorites.await())
            }.onSuccess { data ->
                val media = data.second.map { it.tweetMedia }.flatten()
                currentState =
                    currentState.copy(
                        isLoading = false,
                        user = data.first,
                        tweets = data.second,
                        favorites = data.third,
                        autoplayVideos = autoplayVideos,
                        media = media
                    )
            }.onFailure {
                Timber.e(it)
                currentState = currentState.copy(
                    isLoading = false,
                    isTerminalError = true,
                    errorMessage = errorHandler.getErrorMessage(it)
                )
            }
        }
    }

    private fun fetchNextPage() {
        val lastTweetId = currentState.tweets.last().tweetId
        val lastFavoriteId = currentState.favorites.lastOrNull()?.tweetId
        currentState = currentState.copy(isPaginatedLoading = true, isPaginatedError = false)
        viewModelScope.launch {
            runCatching {
                val tweets =
                    async { userService.fetchUserTweets(currentState.user!!.screenName, lastTweetId) }
                val favorites =
                    async { userService.fetchUserFavorites(currentState.user!!.screenName, lastFavoriteId) }
                tweets.await() to favorites.await()
            }.onSuccess {
                val media = it.first.map(TweetItem::tweetMedia).flatten()
                currentState = currentState.copy(
                    tweets = it.first,
                    media = media,
                    favorites = it.second,
                    isPaginatedLoading = false
                )
            }.onFailure {
                Timber.e(it)
                currentState = currentState.copy(isPaginatedLoading = false, isPaginatedError = true)
                onSideEffect(SideEffect.Action(ScrollToBottom(currentState)))
            }
        }
    }
}
