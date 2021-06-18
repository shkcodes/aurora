package com.shkcodes.aurora.ui.profile

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.ImageLoader
import coil.load
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentProfileBinding
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.LoadNextPage
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.AnimateDataState
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.ScrollToBottom
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.UserProfile
import com.shkcodes.aurora.ui.profile.ProfileContract.State
import com.shkcodes.aurora.ui.timeline.UrlMetadataHandler
import com.shkcodes.aurora.ui.timeline.items.PaginatedErrorItem
import com.shkcodes.aurora.ui.timeline.items.TweetAdapterItem
import com.shkcodes.aurora.util.PagedAdapter
import com.shkcodes.aurora.util.annotatedContent
import com.shkcodes.aurora.util.annotatedDescription
import com.shkcodes.aurora.util.annotatedLink
import com.shkcodes.aurora.util.applySharedAxisEnterTransition
import com.shkcodes.aurora.util.applySharedAxisExitTransition
import com.shkcodes.aurora.util.handleClickableSpans
import com.shkcodes.aurora.util.openUrl
import com.shkcodes.aurora.util.viewBinding
import com.xwray.groupie.viewbinding.BindableItem
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val timelineAdapter = PagedAdapter(::loadNextPage)
    private val handler = ProfileTweetListHandler(this)
    private val urlMetadataHandler by lazy { UrlMetadataHandler(lifecycleScope, imageLoader) }
    private val args by navArgs<ProfileFragmentArgs>()

    override val viewModel by viewModels<ProfileViewModel>()

    override val binding by viewBinding(FragmentProfileBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySharedAxisEnterTransition()
    }

    var currentState = -1
    var currentProgress = -1F

    override fun onPause() {
        currentState = binding.root.currentState
        currentProgress = binding.root.progress
        super.onPause()
    }


    override fun setupView() {
        binding.timeline.adapter = timelineAdapter
        with(binding.root) {
            postponeEnterTransition()
            doOnPreDraw { startPostponedEnterTransition() }
        }
        dispatchIntent(Init(args.userHandle))
    }

    override fun renderState(state: State) {
        if (state.user != null) {
            renderDataState(state)
        }
        if (currentState != -1) {
            binding.root.transitionToState(currentState)
        }
        if (currentProgress != -1F) {
            binding.root.progress = currentProgress
        }
    }

    private fun renderDataState(state: State) {
        with(binding) {
            val user = state.user!!
            profileImage.load(user.profileImageUrlLarge, imageLoader)
            bannerImage.load(user.profileBannerUrl, imageLoader)
            name.text = user.name
            handle.text = user.screenName
            description.text = user.annotatedDescription(requireContext()) { handler.onAnnotationClick(it) }
            description.handleClickableSpans()
            location.isVisible = user.location != null
            location.text = user.location
            link.isVisible = user.url != null
            link.text = user.annotatedLink(requireContext()) { handler.onAnnotationClick(it) }
            link.handleClickableSpans()
            val tweetItems = state.tweets.map { tweetItem ->
                val annotatedContent =
                    tweetItem.annotatedContent(requireContext()) { handler.onAnnotationClick(it) }
                TweetAdapterItem(annotatedContent, tweetItem, urlMetadataHandler, imageLoader, handler)
            }
            timelineAdapter.canLoadMore = !state.isPaginatedError && !state.isPaginatedLoading
            val items = mutableListOf<BindableItem<*>>().apply {
                addAll(tweetItems)
                if (state.isPaginatedError) {
                    add(PaginatedErrorItem(::loadNextPage))
                }
            }
            timelineAdapter.update(items)
        }
    }

    override fun handleAction(sideEffect: SideEffect.Action<*>) {
        when (val action = sideEffect.action) {
            is OpenUrl -> {
                requireContext().openUrl(action.url)
            }
            is ScrollToBottom -> {
                binding.timeline.scrollToPosition(action.lastIndex)
            }
            AnimateDataState -> {
                binding.root.transitionToState(R.id.profileData)
            }
        }
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (val screen = sideEffect.screen) {
            is UserProfile -> {
                applySharedAxisExitTransition()
                navigate(ProfileFragmentDirections.moveToProfile(screen.userHandle))
            }
        }
    }

    private fun loadNextPage() {
        dispatchIntent(LoadNextPage)
    }
}
