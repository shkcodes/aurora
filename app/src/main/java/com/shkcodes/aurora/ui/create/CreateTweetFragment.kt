package com.shkcodes.aurora.ui.create

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import com.fueled.reclaim.ItemsViewAdapter
import com.google.android.material.snackbar.Snackbar
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentCreateTweetBinding
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.CreateTweetSideEffect.MediaSelectionError
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.MediaSelected
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.RemoveImage
import com.shkcodes.aurora.ui.create.CreateTweetContract.State
import com.shkcodes.aurora.ui.create.items.ImageAttachmentAdapterItem
import com.shkcodes.aurora.util.observeTextChanges
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CreateTweetFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader

    private val mediaSelectionRequest = registerForActivityResult(GetMultipleContents()) { uris: List<Uri> ->
        val contentResolver = requireContext().contentResolver
        val selectedTypes = uris.map {
            val type = contentResolver.getType(it)
            type?.substring(0, type?.indexOf("/")).orEmpty()
        }.toSet()
        viewModel.handleIntent(MediaSelected(uris, selectedTypes))
    }

    private val imagesAdapter = ItemsViewAdapter()

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
        }
    }

    override fun renderState(state: State) {
        with(binding) {
            tweetContent.isVisible = !state.isLoading
            postTweet.isVisible = !state.isLoading
            bottomBar.isVisible = !state.isLoading
            progressBar.isVisible = state.isLoading
            tweetContent.isEnabled = !state.isLoading
            imagesCarousel.isVisible =
                state.hasImageAttachments && state.mediaAttachments.isNotEmpty() && !state.isLoading
            renderImagesCarousel(state.mediaAttachments)
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
            is MediaSelectionError -> {
                Snackbar.make(
                    binding.root,
                    action.message,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }
}
