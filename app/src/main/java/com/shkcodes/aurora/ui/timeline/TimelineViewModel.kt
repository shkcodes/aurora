package com.shkcodes.aurora.ui.timeline

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Init
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.TimelineContract.Intent.Retry
import com.shkcodes.aurora.ui.timeline.TimelineContract.State.Content
import com.shkcodes.aurora.ui.timeline.TimelineContract.State.Error
import com.shkcodes.aurora.ui.timeline.TimelineContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                fetchTweets()
            }

            is Retry -> {
                emitState { Content(isLoading = true) }
                fetchTweets()
            }

            is LoadNextPage -> {
                with(intent.currentState) {
                    val afterId = items.last().tweetId
                    if (!isPaginatedLoading) {
                        emitState { copy(isPaginatedLoading = true) }
                        fetchTweets(items, afterId)
                    }
                }
            }

            is Refresh -> {
                emitState { intent.currentState.copy(isLoading = true) }
                fetchTweets(forceRefresh = true)
            }
        }
    }

    private fun fetchTweets(
        previousItems: TimelineItems = emptyList(),
        afterId: Long? = null,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            userService.fetchTimelineTweets(forceRefresh, afterId).evaluate({
                if (afterId == null) observeCachedTweets()
            }, {
                Timber.e(it)
                val errorState = if (afterId == null) {
                    Error(errorHandler.getErrorMessage(it))
                } else {
                    Content(
                        false,
                        previousItems,
                        isPaginatedLoading = false,
                        isPaginatedError = true
                    )
                }
                emitState { errorState }
            })
        }
    }

    private fun observeCachedTweets() {
        userService.getTimelineTweets().filter { it.isNotEmpty() }.onEach {
            emitState { Content(false, it, false) }
        }.launchIn(viewModelScope)
    }
}
