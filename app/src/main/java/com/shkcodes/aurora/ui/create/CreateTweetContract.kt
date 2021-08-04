package com.shkcodes.aurora.ui.create

import android.net.Uri
import com.shkcodes.aurora.base.BaseViewModel
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.IMAGE_ATTACHMENT_LIMIT
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.VIDEO_ATTACHMENT_LIMIT

interface CreateTweetContract {

    abstract class ViewModel : BaseViewModel<State, Intent>(State())

    data class State(
        val isLoading: Boolean = false,
        val content: String? = null,
        val mediaAttachments: List<Uri> = emptyList(),
        val attachmentType: AttachmentType? = null,
        val isDownloadingGif: Boolean = false
    ) {
        val hasMaxAttachments = when (attachmentType) {
            AttachmentType.IMAGE -> mediaAttachments.size == IMAGE_ATTACHMENT_LIMIT
            else -> mediaAttachments.size == VIDEO_ATTACHMENT_LIMIT
        }
        val hasImageAttachments =
            attachmentType == AttachmentType.IMAGE || attachmentType == AttachmentType.GIF
    }

    sealed class Intent {
        object PostTweet : Intent()
        object RemoveAttachment : Intent()
        data class ContentChange(val content: String) : Intent()
        data class MediaSelected(val attachments: List<Uri>, val types: Set<AttachmentType>) : Intent()
        data class RemoveImage(val uri: Uri) : Intent()
        data class GifSelected(val id: String?, val url: String?) : Intent()
    }

    sealed class CreateTweetSideEffect {
        data class AttachmentError(val message: String) : CreateTweetSideEffect()
    }

    object Constants {
        const val IMAGE_ATTACHMENT_LIMIT = 4
        const val VIDEO_ATTACHMENT_LIMIT = 1
        const val ERROR_DURATION = 2500L
        const val EXTENSION_CAPTURED_IMAGE = ".jpg"
        const val GIPHY_DIALOG_TAG = "aurora_giphy_dialog"
    }
}
