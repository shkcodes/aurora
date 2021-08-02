package com.shkcodes.aurora.ui.create

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.shkcodes.aurora.base.DispatcherProvider
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.base.StringId.GIF_DOWNLOAD_ERROR
import com.shkcodes.aurora.base.StringId.MULTIPLE_TYPES
import com.shkcodes.aurora.base.StringId.TOO_MANY_IMAGES
import com.shkcodes.aurora.base.StringId.TOO_MANY_VIDEOS
import com.shkcodes.aurora.base.StringId.UNSUPPORTED_ATTACHMENT
import com.shkcodes.aurora.base.StringProvider
import com.shkcodes.aurora.service.FileService
import com.shkcodes.aurora.service.UserService
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.AttachmentType.GIF
import com.shkcodes.aurora.ui.create.AttachmentType.IMAGE
import com.shkcodes.aurora.ui.create.AttachmentType.OTHER
import com.shkcodes.aurora.ui.create.AttachmentType.VIDEO
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.IMAGE_ATTACHMENT_LIMIT
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.AttachmentError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.GifSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveImage
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveVideo
import com.shkcodes.aurora.ui.create.CreateTweetContract.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateTweetViewModel @Inject constructor(
    override val dispatcherProvider: DispatcherProvider,
    private val userService: UserService,
    private val fileService: FileService,
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

            is RemoveVideo -> {
                currentState = currentState.copy(mediaAttachments = emptyList())
            }

            is GifSelected -> {
                saveGif(intent)
            }
        }
    }

    private fun validateMediaSelection(intent: MediaSelected) {
        val types = intent.types
        val attachments = intent.attachments
        if (attachments.isEmpty()) return
        val isSameTypeOfSelection = types.size == 1
        val isValidMedia = types.none { it == OTHER }
        val isValidVideoSelection =
            (types.firstOrNull() == VIDEO || types.firstOrNull() == GIF) && attachments.size == 1
        val isValidImageSelection = types.firstOrNull() == IMAGE && attachments.size <= IMAGE_ATTACHMENT_LIMIT
        val isValidSelection =
            isSameTypeOfSelection && isValidMedia && (isValidImageSelection || isValidVideoSelection)

        if (isValidSelection) {
            val updatedAttachments = determineFinalAttachments(attachments, types.first())
            currentState = currentState.copy(
                mediaAttachments = updatedAttachments,
                attachmentType = types.first()
            )
        } else {
            val error = when {
                !isSameTypeOfSelection -> stringProvider.getString(MULTIPLE_TYPES)
                !isValidMedia -> stringProvider.getString(UNSUPPORTED_ATTACHMENT)
                types.first() == IMAGE -> stringProvider.getString(TOO_MANY_IMAGES)
                else -> stringProvider.getString(TOO_MANY_VIDEOS)
            }
            onSideEffect(SideEffect.Action(AttachmentError(error)))
        }
    }

    private fun determineFinalAttachments(
        newAttachments: List<Uri>,
        attachmentType: AttachmentType
    ): List<Uri> {
        val allAttachments = newAttachments + currentState.mediaAttachments
        return if (attachmentType == IMAGE && currentState.attachmentType == IMAGE) {
            if (allAttachments.size > IMAGE_ATTACHMENT_LIMIT) {
                allAttachments.subList(0, IMAGE_ATTACHMENT_LIMIT)
            } else {
                allAttachments
            }
        } else {
            newAttachments
        }
    }

    private fun postTweet() {
        viewModelScope.launch {
            currentState = currentState.copy(isLoading = true)
            runCatching {
                withContext(dispatcherProvider.io) {
                    userService.postTweet(
                        currentState.content.orEmpty(),
                        mediaFiles(), currentState.hasImageAttachments
                    )
                }
            }.onSuccess {
                onSideEffect(SideEffect.DisplayScreen(Previous))
            }.onFailure(Timber::e)
        }
    }

    private fun mediaFiles(): List<File> {
        return currentState.mediaAttachments.map { fileService.getFile(it) }
    }

    private fun saveGif(intent: GifSelected) {
        viewModelScope.launch {
            if (intent.id == null || intent.url == null) {
                onSideEffect(SideEffect.Action(AttachmentError(stringProvider.getString(GIF_DOWNLOAD_ERROR))))
            } else {
                currentState = currentState.copy(isDownloadingGif = true)
                val uri = withContext(dispatcherProvider.io) {
                    fileService.downloadGif(intent.id, intent.url)
                }
                currentState = currentState.copy(
                    isDownloadingGif = false,
                    mediaAttachments = listOf(uri),
                    attachmentType = GIF
                )
            }
        }
    }
}
