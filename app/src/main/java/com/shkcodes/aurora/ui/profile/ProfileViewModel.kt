package com.shkcodes.aurora.ui.profile

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.api.zip
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.HandleAnnotationClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.MediaClick
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.UserProfile
import com.shkcodes.aurora.ui.profile.ProfileContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val errorHandler: ErrorHandler,
    private val preferenceService: PreferenceService
) : ViewModel() {

    private val autoplayVideos: Boolean
        get() = preferenceService.autoplayVideos

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                fetchData(intent.userHandle)
            }

            is Retry -> {
                currentState =
                    currentState.copy(isLoading = true, isTerminalError = false, errorMessage = "")
                fetchData(intent.userHandle)
            }

            LoadNextPage -> {
                val afterId = currentState.tweets.last().tweetId
                if (!currentState.isPaginatedLoading) {
                    currentState =
                        currentState.copy(isPaginatedLoading = true, isPaginatedError = false)
                    fetchTweets(afterId)
                }
            }

            is MediaClick -> {
                onSideEffect(SideEffect.DisplayScreen(MediaViewer(intent.index, intent.tweetId)))
            }

            is HandleAnnotationClick -> {
                when (intent.data.first()) {
                    'h' -> onSideEffect(SideEffect.Action(OpenUrl(intent.data)))
                    '@' -> onSideEffect(SideEffect.DisplayScreen(UserProfile(intent.data.substring(1))))
                }
            }
        }
    }

    private fun fetchData(userHandle: String) {
        viewModelScope.launch {
            zip(
                userService.fetchUserProfile(userHandle),
                userService.fetchUserTweets(userHandle)
            ).evaluate({
                currentState =
                    currentState.copy(
                        isLoading = false,
                        user = it.first,
                        tweets = it.second,
                        isPaginatedLoading = false,
                        autoplayVideos = autoplayVideos
                    )
            }, {
                Timber.e(it)
                currentState = currentState.copy(
                    isLoading = false,
                    isTerminalError = true,
                    errorMessage = errorHandler.getErrorMessage(it),
                    isPaginatedLoading = false
                )
            })
        }
    }

    private fun fetchTweets(afterId: Long) {
        viewModelScope.launch {
            userService.fetchUserTweets(currentState.user!!.screenName, afterId)
                .evaluate({
                    currentState = currentState.copy(tweets = it, isPaginatedLoading = false)
                }, {
                    Timber.e(it)
                    currentState =
                        currentState.copy(isPaginatedLoading = false, isPaginatedError = true)
                })
        }
    }
}
