package com.shkcodes.aurora.ui.home

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.home.HomeContract.Intent
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State.Content
import com.shkcodes.aurora.ui.home.HomeContract.State.Error
import com.shkcodes.aurora.ui.home.HomeContract.State.Loading
import com.shkcodes.aurora.ui.home.HomeContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
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
                emitState { Loading }
                fetchTweets()
            }

            is LoadNextPage -> {
                with(intent.currentState) {
                    val afterId = tweets.last().primaryTweet.id
                    if (!isLoadingNextPage) {
                        emitState { Content(tweets, true) }
                        fetchTweets(tweets, afterId)
                    }
                }
            }
        }
    }

    private fun fetchTweets(
        previousTweets: List<TimelineTweetItem> = emptyList(),
        afterId: Long? = null
    ) {
        viewModelScope.launch {
            userService.fetchTimelineTweets(afterId).evaluate({
                if (afterId == null) observeCachedTweets()
            }, {
                it.printStackTrace()
                val errorState = if (afterId == null) {
                    Error(errorHandler.getErrorMessage(it))
                } else {
                    Content(previousTweets, isLoadingNextPage = false, isPaginatedError = true)
                }
                emitState { errorState }
            })
        }
    }

    private fun observeCachedTweets() {
        userService.getTimelineTweets().filter { it.isNotEmpty() }.onEach {
            emitState { Content(it, false) }
        }.launchIn(viewModelScope)
    }
}
