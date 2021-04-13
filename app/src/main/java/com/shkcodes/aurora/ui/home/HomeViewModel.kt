package com.shkcodes.aurora.ui.home

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.home.HomeContract.Intent
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Init
import com.shkcodes.aurora.ui.home.HomeContract.Intent.Retry
import com.shkcodes.aurora.ui.home.HomeContract.State.Content
import com.shkcodes.aurora.ui.home.HomeContract.State.Error
import com.shkcodes.aurora.ui.home.HomeContract.State.Loading
import com.shkcodes.aurora.ui.home.HomeContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
        }
    }

    private fun fetchTweets() {
        viewModelScope.launch {
            userService.getTimelineTweets().evaluate({
                emitState { Content(it) }
            }, {
                it.printStackTrace()
                emitState { Error(errorHandler.getErrorMessage(it)) }
            })
        }
    }
}
