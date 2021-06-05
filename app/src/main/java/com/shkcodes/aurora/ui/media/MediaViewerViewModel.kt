package com.shkcodes.aurora.ui.media

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewerViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                viewModelScope.launch {
                    val media = userService.getMediaForTweet(intent.tweetId)
                    currentState = currentState.copy(initialIndex = intent.index, media = media)
                }
            }
        }
    }
}
