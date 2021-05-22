package com.shkcodes.aurora.ui.profile

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.api.evaluate
import com.shkcodes.aurora.api.zip
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.ErrorHandler
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Retry
import com.shkcodes.aurora.ui.profile.ProfileContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val errorHandler: ErrorHandler
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                fetchTweets(intent.userHandle)
            }

            is Retry -> {
                currentState =
                    currentState.copy(isLoading = true, isTerminalError = false, errorMessage = "")
                fetchTweets(intent.userHandle)
            }
        }
    }

    private fun fetchTweets(userHandle: String) {
        viewModelScope.launch {
            zip(
                userService.fetchUserProfile(userHandle),
                userService.fetchUserTweets()
            ).evaluate({
                currentState =
                    currentState.copy(isLoading = false, user = it.first, items = it.second)
            }, {
                Timber.e(it)
                currentState = currentState.copy(
                    isLoading = false,
                    isTerminalError = true,
                    errorMessage = errorHandler.getErrorMessage(it)
                )
            })
        }
    }
}
