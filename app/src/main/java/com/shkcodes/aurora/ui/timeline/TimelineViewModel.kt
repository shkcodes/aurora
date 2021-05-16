package com.shkcodes.aurora.ui.timeline

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.Event.AutoplayVideosToggled
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.PreferencesService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Init
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.Screen.MediaViewer
import com.shkcodes.aurora.ui.timeline.TimelineContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val errorHandler: ErrorHandler,
    private val preferencesService: PreferencesService,
    eventBus: EventBus,
) : ViewModel() {

    init {
        eventBus.getEvents().onEach(::handleEvent).launchIn(viewModelScope)
    }

    private val autoplayVideos: Boolean
        get() = preferencesService.autoplayVideos

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                fetchTweets()
            }

            is Retry -> {
                currentState =
                    currentState.copy(isLoading = true, isTerminalError = false, errorMessage = "")
                fetchTweets()
            }

            is LoadNextPage -> {
                val afterId = currentState.items.last().tweetId
                if (!currentState.isPaginatedLoading) {
                    currentState =
                        currentState.copy(isPaginatedLoading = true, isPaginatedError = false)
                    fetchTweets(afterId)
                }
            }

            is Refresh -> {
                currentState = currentState.copy(isLoading = true)
                fetchTweets(forceRefresh = true)
            }

            is MediaClick -> {
                onSideEffect(
                    SideEffect.DisplayScreen(
                        MediaViewer(
                            intent.index,
                            intent.tweetId
                        )
                    )
                )
            }
        }
    }

    private fun fetchTweets(
        afterId: Long? = null,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            userService.fetchTimelineTweets(forceRefresh, afterId).evaluate({
                currentState = currentState.copy(
                    isLoading = false, isPaginatedLoading = false, autoplayVideos = autoplayVideos,
                    items = it
                )
            }, {
                Timber.e(it)
                currentState = if (afterId == null) {
                    currentState.copy(
                        isLoading = false,
                        isTerminalError = true,
                        errorMessage = errorHandler.getErrorMessage(it)
                    )
                } else {
                    currentState.copy(isPaginatedError = true, isPaginatedLoading = false)
                }
            })
        }
    }

    private fun handleEvent(event: Event) {
        when (event) {
            is AutoplayVideosToggled -> {
                currentState = currentState.copy(autoplayVideos = autoplayVideos)
            }
        }
    }
}
