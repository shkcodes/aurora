package com.shkcodes.aurora.ui.profile.media

import android.os.Bundle
import android.view.View
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import coil.ImageLoader
import com.fueled.reclaim.ItemsViewAdapter
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentProfileMediaViewerBinding
import com.shkcodes.aurora.service.SharedElementTransitionHelper
import com.shkcodes.aurora.ui.media.ImageAdapterItem
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.PageChange
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.State
import com.shkcodes.aurora.util.AnimationConstants
import com.shkcodes.aurora.util.observePageChanges
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.collections.set

@AndroidEntryPoint
class ProfileMediaViewerFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var transitionHelper: SharedElementTransitionHelper
    private val args by navArgs<ProfileMediaViewerFragmentArgs>()
    private val pagerAdapter = ItemsViewAdapter()
    private val sharedElementCallback = object : SharedElementCallback() {
        override fun onMapSharedElements(names: List<String>, sharedElements: MutableMap<String, View>) {
            transitionHelper.getPagerImageView(binding.pager)?.let {
                sharedElements[names[0]] = it
            }
        }
    }

    override val viewModel by viewModels<ProfileMediaViewerViewModel>()
    override val binding by viewBinding(FragmentProfileMediaViewerBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition()
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).apply {
                duration = AnimationConstants.DEFAULT_DURATION
            }
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move).apply {
                duration = AnimationConstants.DEFAULT_DURATION
            }
    }

    override fun setupView() {
        dispatchIntent(Init(args.userHandle, args.index))
        binding.pager.adapter = pagerAdapter
        binding.pager.observePageChanges(viewLifecycleOwner.lifecycle, ::onPageChange)
    }

    override fun renderState(state: State) {
        with(state) {
            if (pagerAdapter.itemCount == 0) {
                val items = media.map { ImageAdapterItem(it.image, imageLoader, true) }
                pagerAdapter.replaceItems(items)
                binding.pager.setCurrentItem(currentIndex, false)
            }
            if (state.media.isNotEmpty()) {
                binding.retweets.text = "${media[currentIndex].retweets}"
                binding.likes.text = "${media[currentIndex].likes}"
            }
        }
    }

    private fun onPageChange(currentIndex: Int) {
        startPostponedEnterTransition()
        dispatchIntent(PageChange(currentIndex))
        transitionHelper.mediaIndex = currentIndex
        setEnterSharedElementCallback(sharedElementCallback)
    }
}
