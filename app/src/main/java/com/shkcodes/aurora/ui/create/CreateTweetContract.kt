package com.shkcodes.aurora.ui.create

import android.net.Uri
import com.shkcodes.aurora.base.BaseViewModel

interface CreateTweetContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = false,
        val content: String? = null,
        val mediaAttachments: List<Uri> = emptyList()
    )

    sealed class Intent {
        object PostTweet : Intent()
        data class ContentChange(val content: String) : Intent()
        data class MediaSelected(val attachments: List<Uri>, val types: Set<String>) : Intent()
    }

    sealed class CreateTweetSideEffect {
        data class MediaSelectionError(val message: String) : CreateTweetSideEffect()
    }

    object Constants {
        const val MEDIA_ATTACHMENT_LIMIT = 4
        const val ATTACHMENT_TYPE_IMAGE = "image"
        const val ATTACHMENT_TYPE_VIDEO = "video"
        val VALID_ATTACHMENT_TYPES = listOf("image", "video")
    }
}
