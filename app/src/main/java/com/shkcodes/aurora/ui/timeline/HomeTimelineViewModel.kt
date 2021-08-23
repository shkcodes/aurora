package com.shkcodes.aurora.ui.timeline

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.base.Event
import com.shkcodes.aurora.base.Event.AutoplayVideosToggled
import com.shkcodes.aurora.base.Event.TogglePaginatedLoading
import com.shkcodes.aurora.base.EventBus
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.cache.entities.TweetEntity
import com.shkcodes.aurora.service.PreferenceService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.MediaViewer
import com.shkcodes.aurora.ui.Screen.UserProfile
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.AnnotatedContentClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MediaClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.RetainScrollState
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.ScrollToBottom
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.ScrollToTop
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeTimelineViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val errorHandler: ErrorHandler,
    private val preferenceService: PreferenceService,
    private val eventBus: EventBus
) : ViewModel() {

    init {
        fetchTweets()
        eventBus.getEvents().onEach(::handleEvent).launchIn(viewModelScope)
    }

    private val autoplayVideos: Boolean
        get() = preferenceService.autoplayVideos

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Retry -> {
                currentState =
                    currentState.copy(isLoading = true, isTerminalError = false, errorMessage = "")
                fetchTweets()
            }

            is LoadNextPage -> {
                val afterId = currentState.tweets.last().tweetId
                if (!currentState.isPaginatedLoading) {
                    eventBus.emitEvent(TogglePaginatedLoading(true))
                    currentState =
                        currentState.copy(isPaginatedLoading = true, isPaginatedError = false)
                    fetchTweets(afterId)
                }
            }

            is Refresh -> {
                currentState = currentState.copy(isLoading = true)
                val latestTweet = currentState.tweets.first().primaryTweet
                fetchTweets(newerThan = latestTweet)
            }

            is MediaClick -> {
                onSideEffect(SideEffect.DisplayScreen(MediaViewer(intent.index, intent.tweetId)))
            }

            is MarkItemsAsSeen -> {
                currentState = currentState.copy(newTweets = emptyList())
                onSideEffect(SideEffect.Action(ScrollToTop))
            }

            is ScrollIndexChange -> {
                if (currentState.newTweets.isNotEmpty() && intent.index < currentState.newTweets.size) {
                    currentState = currentState.copy(newTweets = currentState.newTweets.dropLast(1))
                }
            }

            is AnnotatedContentClick -> {
                when (intent.text.first()) {
                    'h' -> onSideEffect(SideEffect.Action(OpenUrl(intent.text)))
                    '@' -> onSideEffect(SideEffect.DisplayScreen(UserProfile(intent.text.substring(1))))
                }
            }
        }
    }

    private fun fetchTweets(
        afterId: Long? = null,
        newerThan: TweetEntity? = null
    ) {
        viewModelScope.launch {
            runCatching {
                withContext(dispatcherProvider.io) {
                    userService.fetchTimelineTweets(
                        newerThan,
                        afterId
                    )
                }
            }
                .onSuccess {
                    val newItems = if (newerThan != null) it else emptyList()
                    if (newItems.isNotEmpty()) {
                        onSideEffect(SideEffect.Action(RetainScrollState(newItems.size)))
                    }
                    eventBus.sendEvent(TogglePaginatedLoading(false))
                    currentState = currentState.copy(
                        isLoading = false, isPaginatedLoading = false, autoplayVideos = autoplayVideos,
                        tweets = if (newerThan != null) it + currentState.tweets else it,
                        newTweets = newItems
                    )
                }.onFailure {
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
                    if (afterId != null) {
                        onSideEffect(SideEffect.Action(ScrollToBottom(currentState.tweets.size)))
                    }
                    eventBus.sendEvent(TogglePaginatedLoading(false))
                }
        }
    }

    private fun handleEvent(event: Event) {
        if (event is AutoplayVideosToggled) {
            currentState = currentState.copy(autoplayVideos = autoplayVideos)
        }
    }
}
