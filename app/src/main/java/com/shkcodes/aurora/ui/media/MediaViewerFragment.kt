package com.shkcodes.aurora.ui.media

import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import coil.ImageLoader
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.cache.entities.MediaEntity
import com.shkcodes.aurora.databinding.FragmentMediaViewerBinding
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent
import com.shkcodes.aurora.ui.media.MediaViewerContract.Intent.Init
import com.shkcodes.aurora.ui.media.MediaViewerContract.State
import com.shkcodes.aurora.util.AnimationConstants
import com.shkcodes.aurora.util.observePageChanges
import com.shkcodes.aurora.util.onDestroy
import com.shkcodes.aurora.util.viewBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaViewerFragment : BaseFragment<State, Intent>(), Player.EventListener {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val pagerAdapter = GroupAdapter<GroupieViewHolder>()
    private val args by navArgs<MediaViewerFragmentArgs>()

    override val viewModel by viewModels<MediaViewerViewModel>()

    override val binding by viewBinding(FragmentMediaViewerBinding::inflate)

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
        dispatchIntent(Init(args.index, args.tweetId))
        binding.pager.adapter = pagerAdapter
    }

    override fun renderState(state: State) {
        with(state) {
            if (media.isNotEmpty()) {
                if (media.first().isAnimatedMedia) {
                    showVideo(media.first())
                } else {
                    showImagesPager(state)
                }
            }
        }
    }

    private fun showVideo(media: MediaEntity) {
        startPostponedEnterTransition()
        with(binding) {
            val videoPlayer = instantiatePlayer(media.url)
            with(playerView) {
                player = videoPlayer
                isVisible = true
                showController()
            }
            viewLifecycleOwner.lifecycle.onDestroy { videoPlayer.removeListener(this@MediaViewerFragment) }
            progressBar.show()
        }
    }

    private fun showImagesPager(state: State) {
        with(state) {
            val items = media.map { ImageAdapterItem(it, imageLoader) }
            pagerAdapter.update(items)
            binding.pageIndicator.isVisible = true
            with(binding.pager) {
                isVisible = true
                setCurrentItem(initialIndex, false)
                observePageChanges(viewLifecycleOwner.lifecycle, ::updatePageIndicator)
                updatePageIndicator(initialIndex)
            }
        }
    }

    private fun updatePageIndicator(currentIndex: Int) {
        binding.pageIndicator.text =
            getString(R.string.media_page_indicator, currentIndex + 1, pagerAdapter.itemCount)
        startPostponedEnterTransition()
    }

    override fun onPlaybackStateChanged(state: Int) {
        binding.progressBar.isVisible = state == Player.STATE_BUFFERING
    }

    private fun instantiatePlayer(url: String): SimpleExoPlayer {
        return SimpleExoPlayer.Builder(requireContext()).build().apply {
            val dataSourceFactory =
                DefaultDataSourceFactory(
                    requireContext(),
                    Util.getUserAgent(requireContext(), requireContext().packageName)
                )

            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(url)))

            setMediaSource(source)
            prepare()
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
            addListener(this@MediaViewerFragment)
        }
    }
}
