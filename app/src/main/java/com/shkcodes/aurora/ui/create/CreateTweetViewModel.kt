package com.shkcodes.aurora.ui.create

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTweetViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is ContentChange -> {
                currentState = currentState.copy(content = intent.content)
            }

            is PostTweet -> {
                postTweet()
            }
        }
    }

    private fun postTweet() {
        viewModelScope.launch {
            currentState = currentState.copy(isLoading = true)
            userService.postTweet(currentState.content.orEmpty())
            onSideEffect(SideEffect.DisplayScreen(Previous))
        }
    }
}
