package com.shkcodes.aurora.ui.timeline

import androidx.fragment.app.viewModels
import coil.ImageLoader
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentHomeTimelineBinding
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.Intent.TweetContentClick
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.State
import com.shkcodes.aurora.ui.timeline.HomeTimelineContract.TimelineSideEffect.OpenUrl
import com.shkcodes.aurora.ui.timeline.items.TweetAdapterItem
import com.shkcodes.aurora.util.formattedTweetContent
import com.shkcodes.aurora.util.openUrl
import com.shkcodes.aurora.util.viewBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeTimelineFragment : BaseFragment<State, Intent>(), TweetListHandler {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val timelineAdapter = GroupAdapter<GroupieViewHolder>()

    override val viewModel by viewModels<HomeTimelineViewModel>()

    override val binding by viewBinding(FragmentHomeTimelineBinding::inflate)

    override fun setupView() {
        with(binding) {
            timeline.adapter = timelineAdapter
            swipeRefresh.setProgressBackgroundColorSchemeColor(requireContext().getColor(R.color.colorSurface))
            swipeRefresh.setColorSchemeColors(requireContext().getColor(R.color.colorSecondary))
        }
    }

    override fun renderState(state: State) {
        with(binding) {
            swipeRefresh.isRefreshing = state.isLoading
        }
        val tweetItems = state.tweets.map { tweetItem ->
            val content =
                requireContext().formattedTweetContent(tweetItem.tweet, ::onTweetContentClick)
            val quoteTweetContent =
                tweetItem.quoteTweet?.let {
                    requireContext().formattedTweetContent(
                        it,
                        ::onTweetContentClick
                    )
                }
            TweetAdapterItem(content, quoteTweetContent, tweetItem, imageLoader)
        }
        timelineAdapter.update(tweetItems)
    }

    override fun onTweetContentClick(text: String) {
        dispatchIntent(TweetContentClick(text))
    }

    override fun handleAction(sideEffect: SideEffect.Action<*>) {
        when (val action = sideEffect.action) {
            is OpenUrl -> {
                requireContext().openUrl(action.url)
            }
        }
    }
}

interface TweetListHandler {
    fun onTweetContentClick(text: String)
}
