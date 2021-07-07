package com.shkcodes.aurora.ui.profile.media

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.PageChange
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileMediaViewerViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is Init -> {
                viewModelScope.launch {
                    val media =
                        userService.getCachedTweetsForUser(intent.userHandle).map { it.toProfileMediaDto() }
                            .flatten()
                    currentState = currentState.copy(media = media, currentIndex = intent.index)
                }
            }

            is PageChange -> {
                currentState = currentState.copy(currentIndex = intent.index)
            }
        }
    }
}
