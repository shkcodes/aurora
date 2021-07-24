package com.shkcodes.aurora.ui.create

import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.base.StringId.MULTIPLE_TYPES
import com.shkcodes.aurora.base.StringId.TOO_MANY_IMAGES
import com.shkcodes.aurora.base.StringId.TOO_MANY_VIDEOS
import com.shkcodes.aurora.base.StringId.UNSUPPORTED_ATTACHMENT
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.ATTACHMENT_TYPE_IMAGE
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.ATTACHMENT_TYPE_VIDEO
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.MEDIA_ATTACHMENT_LIMIT
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.VALID_ATTACHMENT_TYPES
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.MediaSelectionError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveImage
import com.shkcodes.aurora.ui.create.CreateTweetContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTweetViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val stringProvider: StringProvider
) : ViewModel() {

    override fun handleIntent(intent: Intent) {
        when (intent) {
            is ContentChange -> {
                currentState = currentState.copy(content = intent.content)
            }

            is PostTweet -> {
                postTweet()
            }

            is MediaSelected -> {
                if (intent.attachments.isNotEmpty()) validateMediaSelection(intent)
            }

            is RemoveImage -> {
                val updatedAttachments =
                    currentState.mediaAttachments.toMutableList().apply { remove(intent.uri) }
                currentState = currentState.copy(mediaAttachments = updatedAttachments)
            }
        }
    }

    private fun validateMediaSelection(intent: MediaSelected) {
        val types = intent.types
        val attachments = intent.attachments
        if (attachments.isEmpty()) return
        val isSameTypeOfSelection = types.size == 1
        val isValidMedia = types.all { VALID_ATTACHMENT_TYPES.contains(it) }
        val isImageSelection = types.contains(ATTACHMENT_TYPE_IMAGE)
        val isVideoSelection = types.contains(ATTACHMENT_TYPE_VIDEO)
        val isValidVideoSelection = isVideoSelection && attachments.size == 1
        val isValidImageSelection = isImageSelection && attachments.size <= MEDIA_ATTACHMENT_LIMIT
        val isValidSelection =
            isSameTypeOfSelection && isValidMedia && (isValidImageSelection || isValidVideoSelection)

        if (isValidSelection) {
            currentState = currentState.copy(mediaAttachments = attachments, hasImageAttachments = isImageSelection)
        } else {
            val error = when {
                !isSameTypeOfSelection -> stringProvider.getString(MULTIPLE_TYPES)
                !isValidMedia -> stringProvider.getString(UNSUPPORTED_ATTACHMENT)
                isImageSelection -> stringProvider.getString(TOO_MANY_IMAGES)
                else -> stringProvider.getString(TOO_MANY_VIDEOS)
            }
            onSideEffect(SideEffect.Action(MediaSelectionError(error)))
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
