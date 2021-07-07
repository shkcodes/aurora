package com.shkcodes.aurora.ui.profile.media

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import com.fueled.reclaim.ItemsViewAdapter
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.databinding.FragmentProfileMediaViewerBinding
import com.shkcodes.aurora.ui.media.ImageAdapterItem
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.Intent.PageChange
import com.shkcodes.aurora.ui.profile.media.ProfileMediaViewerContract.State
import com.shkcodes.aurora.util.observePageChanges
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileMediaViewerFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val args by navArgs<ProfileMediaViewerFragmentArgs>()
    private val pagerAdapter = ItemsViewAdapter()

    override val viewModel by viewModels<ProfileMediaViewerViewModel>()
    override val binding by viewBinding(FragmentProfileMediaViewerBinding::inflate)

    override fun setupView() {
        dispatchIntent(Init(args.userHandle, args.index))
        binding.pager.adapter = pagerAdapter
        binding.pager.observePageChanges(viewLifecycleOwner.lifecycle, ::updatePageIndicator)
    }

    override fun renderState(state: State) {
        with(state) {
            if (pagerAdapter.itemCount == 0) {
                val items = media.map { ImageAdapterItem(it.image, imageLoader) }
                pagerAdapter.replaceItems(items)
                binding.pager.setCurrentItem(currentIndex, false)
            }
            if (state.media.isNotEmpty()) {
                binding.retweets.text = "${media[currentIndex].retweets}"
                binding.likes.text = "${media[currentIndex].likes}"
            }
        }
    }

    private fun updatePageIndicator(currentIndex: Int) {
        dispatchIntent(PageChange(currentIndex))
    }
}
