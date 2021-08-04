package com.shkcodes.aurora.ui.create

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.load
import com.fueled.reclaim.ItemsViewAdapter
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shkcodes.aurora.BuildConfig
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentCreateTweetBinding
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.AttachmentType.GIF
import com.shkcodes.aurora.ui.create.AttachmentType.IMAGE
import com.shkcodes.aurora.ui.create.AttachmentType.VIDEO
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.ERROR_DURATION
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.EXTENSION_CAPTURED_IMAGE
import com.shkcodes.aurora.ui.create.CreateTweetContract.Constants.GIPHY_DIALOG_TAG
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.AttachmentError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.GifSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveAttachment
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveImage
import com.shkcodes.aurora.ui.create.CreateTweetContract.State
import com.shkcodes.aurora.ui.create.items.ImageAttachmentAdapterItem
import com.shkcodes.aurora.util.fileProviderAuthority
import com.shkcodes.aurora.util.observeTextChanges
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.io.File
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class CreateTweetFragment : BaseFragment<State, Intent>(), LifecycleObserver, GifSelectionListener {

    @Inject
    lateinit var imageLoader: ImageLoader
    private var videoUri: Uri? = null
    private var imageUri: Uri? = null

    private val mediaSelectionRequest = registerForActivityResult(GetMultipleContents()) { uris: List<Uri> ->
        val contentResolver = requireContext().contentResolver
        val selectedTypes = uris.map {
            AttachmentType.from(contentResolver.getType(it).orEmpty())
        }.toSet()
        viewModel.handleIntent(MediaSelected(uris, selectedTypes))
    }
    private val captureImageRequest = registerForActivityResult(TakePicture()) {
        viewModel.handleIntent(MediaSelected(listOf(imageUri!!), setOf(IMAGE)))
    }
    private val imagesAdapter = ItemsViewAdapter()
    private val videoPlayer: SimpleExoPlayer by lazy {
        SimpleExoPlayer.Builder(requireContext()).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }
    private val dataSourceFactory by lazy {
        DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(requireContext(), requireContext().packageName)
        )
    }

    override val viewModel by viewModels<CreateTweetViewModel>()

    override val binding by viewBinding(FragmentCreateTweetBinding::inflate)

    override fun setupView() {
        with(binding) {
            imagesCarousel.adapter = imagesAdapter
            tweetContent.observeTextChanges(viewLifecycleOwner.lifecycle) {
                viewModel.handleIntent(ContentChange(it))
            }
            tweetContent.requestFocus()
            postTweet.setOnClickListener { viewModel.handleIntent(PostTweet) }
            media.setOnClickListener {
                mediaSelectionRequest.launch("*/*")
            }
            camera.setOnClickListener {
                createTempImageUri()
                captureImageRequest.launch(imageUri)
            }
            removeAnimatedMedia.setOnClickListener {
                viewModel.handleIntent(RemoveAttachment)
            }
            gif.setOnClickListener {
                val dialog = GiphyDialogFragment.newInstance(
                    GPHSettings(mediaTypeConfig = arrayOf(GPHContentType.gif), showSuggestionsBar = false),
                    BuildConfig.GIPHY_KEY
                )
                dialog.gifSelectionListener = this@CreateTweetFragment
                dialog.show(childFragmentManager, GIPHY_DIALOG_TAG)
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(this)
    }

    override fun renderState(state: State) {
        with(binding) {
            tweetContent.isVisible = !state.isLoading
            postTweet.isVisible = !state.isLoading
            bottomBar.isVisible = !state.isLoading
            progressBar.isVisible = state.isLoading
            tweetContent.isEnabled = !state.isLoading
            imagesCarousel.isVisible =
                state.attachmentType == IMAGE && state.mediaAttachments.isNotEmpty() && !state.isLoading
            renderImagesCarousel(state.mediaAttachments)
            val hasVideoAttachment =
                state.attachmentType == VIDEO && state.mediaAttachments.isNotEmpty()
            player.isVisible = hasVideoAttachment && !state.isLoading
            if (hasVideoAttachment && videoUri != state.mediaAttachments.first()) {
                videoUri = state.mediaAttachments.first()
                playVideo()
            }
            if (state.attachmentType == GIF && !gifPreview.isVisible) {
                gifPreview.load(state.mediaAttachments.first(), imageLoader)
            }
            gifPreview.isVisible = state.attachmentType == GIF && !state.isLoading && !state.isDownloadingGif
            if (state.mediaAttachments.isEmpty()) {
                videoUri = null
                videoPlayer.stop()
            }
            media.isEnabled = !state.hasMaxAttachments
            camera.isEnabled = !state.hasMaxAttachments
            videoPlayer.playWhenReady = !state.isLoading
            gif.isEnabled = !state.isDownloadingGif && !state.hasMaxAttachments
        }
    }

    private fun renderImagesCarousel(images: List<Uri>) {
        imagesAdapter.replaceItems(images.map {
            ImageAttachmentAdapterItem(it, imageLoader, ::removeImage)
        }, true)
    }

    private fun removeImage(uri: Uri) {
        viewModel.handleIntent(RemoveImage(uri))
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (sideEffect.screen) {
            is Previous -> findNavController().popBackStack()
        }
    }

    override fun handleAction(sideEffect: SideEffect.Action<*>) {
        when (val action = sideEffect.action) {
            is AttachmentError -> {
                with(binding) {
                    error.text = action.message
                    error.animate().translationY(0F).start()
                    attachmentOptions.animate().translationY(attachmentOptions.height * -1F).start()
                    viewLifecycleOwner.lifecycle.coroutineScope.launchWhenResumed {
                        delay(ERROR_DURATION)
                        error.animate().translationY(error.height.toFloat()).start()
                        attachmentOptions.animate().translationY(0F).start()
                    }
                }
            }
        }
    }

    private fun playVideo() {
        videoPlayer.apply {
            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(videoUri!!))
            setMediaSource(source)
            prepare()
        }
        with(binding.player) {
            player = videoPlayer
            showController()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun pausePlayback() {
        videoPlayer.playWhenReady = false
    }

    private fun createTempImageUri() {
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().fileProviderAuthority,
            File(requireContext().externalCacheDir, "${UUID.randomUUID()}$EXTENSION_CAPTURED_IMAGE")
        )
    }

    override fun gifSelected(media: Media) {
        with(media.images) {
            viewModel.handleIntent(GifSelected(downsizedMedium?.mediaId, downsizedMedium?.gifUrl))
        }
    }
}
