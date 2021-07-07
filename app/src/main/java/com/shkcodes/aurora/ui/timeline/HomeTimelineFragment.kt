package com.shkcodes.aurora.ui.timeline

import android.animation.AnimatorInflater
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.fueled.reclaim.AdapterItem
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentHomeTimelineBinding
import com.shkcodes.aurora.ui.Screen.UserProfile
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.MarkItemsAsSeen
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.Refresh
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.ScrollIndexChange
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.State
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.ScrollToBottom
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.ScrollToTop
import com.shkcodes.aurora.ui.timeline.items.PaginatedErrorItem
import com.shkcodes.aurora.ui.timeline.items.TweetAdapterItem
import com.shkcodes.aurora.util.PagedAdapter
import com.shkcodes.aurora.util.annotatedContent
import com.shkcodes.aurora.util.applySharedAxisExitTransition
import com.shkcodes.aurora.util.linearLayoutManager
import com.shkcodes.aurora.util.observeScrolling
import com.shkcodes.aurora.util.openUrl
import com.shkcodes.aurora.util.reenterTransitionListener
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeTimelineFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val timelineAdapter = PagedAdapter(::loadNextPage)
    private val handler = HomeTweetListHandler(this)
    private val urlMetadataHandler by lazy { UrlMetadataHandler(lifecycleScope, imageLoader) }

    override val viewModel by viewModels<HomeTimelineViewModel>()

    override val binding by viewBinding(FragmentHomeTimelineBinding::inflate)

    override fun setupView() {
        with(binding) {
            timeline.adapter = timelineAdapter
            with(swipeRefresh) {
                setProgressBackgroundColorSchemeColor(requireContext().getColor(R.color.colorSurface))
                setColorSchemeColors(requireContext().getColor(R.color.colorSecondary))
                setOnRefreshListener { dispatchIntent(Refresh) }
            }
            newTweetsIndicator.setOnClickListener { dispatchIntent(MarkItemsAsSeen) }
            observeTimeline()
            with(binding.root) {
                postponeEnterTransition()
                doOnPreDraw { startPostponedEnterTransition() }
            }
        }
        if (reenterTransition == null) applySharedAxisExitTransition()
        reenterTransitionListener(onEnd = {
            applySharedAxisExitTransition()
        })
    }

    override fun renderState(state: State) {
        with(binding) {
            swipeRefresh.isRefreshing = state.isLoading
            if (!newTweetsIndicator.isVisible && state.newTweets.isNotEmpty()) {
                showNewTweetsIndicator()
            } else if (newTweetsIndicator.isVisible && state.newTweets.isEmpty()) {
                hideNewTweetsIndicator()
            }
            newTweetsIndicator.text = resources.getQuantityString(
                R.plurals.new_tweets_placeholder,
                state.newTweets.size,
                state.newTweets.size
            )
        }
        val tweetItems = state.tweets.map { tweetItem ->
            val annotatedContent =
                tweetItem.annotatedContent(requireContext()) { handler.onAnnotationClick(it) }
            TweetAdapterItem(annotatedContent, tweetItem, urlMetadataHandler, imageLoader, handler)
        }
        timelineAdapter.canLoadMore = !state.isPaginatedError
        val items = mutableListOf<AdapterItem<*>>().apply {
            addAll(tweetItems)
            if (state.isPaginatedError) {
                add(PaginatedErrorItem(::loadNextPage))
            }
        }
        timelineAdapter.replaceItems(items)
    }

    override fun handleAction(sideEffect: SideEffect.Action<*>) {
        when (val action = sideEffect.action) {
            is OpenUrl -> {
                requireContext().openUrl(action.url)
            }
            is ScrollToTop -> {
                binding.timeline.scrollToPosition(0)
            }
            is ScrollToBottom -> {
                binding.timeline.scrollToPosition(action.lastIndex)
            }
        }
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (val screen = sideEffect.screen) {
            is UserProfile -> {
                navigate(HomeTimelineFragmentDirections.moveToProfile(screen.userHandle))
            }
        }
    }

    private fun showNewTweetsIndicator() {
        AnimatorInflater.loadAnimator(requireContext(), R.animator.new_tweets_indicator_enter).apply {
            setTarget(binding.newTweetsIndicator)
            binding.newTweetsIndicator.isVisible = true
            start()
        }
    }

    private fun hideNewTweetsIndicator() {
        AnimatorInflater.loadAnimator(requireContext(), R.animator.new_tweets_indicator_exit).apply {
            setTarget(binding.newTweetsIndicator)
            binding.newTweetsIndicator.isVisible = false
            start()
        }
    }

    private fun observeTimeline() {
        with(binding.timeline) {
            observeScrolling(viewLifecycleOwner.lifecycle) {
                dispatchIntent(ScrollIndexChange(linearLayoutManager.findFirstVisibleItemPosition()))
            }
        }
    }

    private fun loadNextPage() {
        dispatchIntent(LoadNextPage)
    }
}
