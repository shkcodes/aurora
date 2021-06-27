package com.shkcodes.aurora.ui.profile

import android.os.Bundle
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import coil.ImageLoader
import coil.load
import com.fueled.reclaim.ItemsViewAdapter
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.shkcodes.aurora.R
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentProfileBinding
import com.shkcodes.aurora.ui.profile.ProfileContract.Constants.BANNER_SCROLL_OFFSET
import com.shkcodes.aurora.ui.profile.ProfileContract.Constants.PROFILE_IMAGE_SCALE_LIMIT
import com.shkcodes.aurora.ui.profile.ProfileContract.Constants.USER_INFO_BACKGROUND_SCROLL_OFFSET
import com.shkcodes.aurora.ui.profile.ProfileContract.Constants.USER_INFO_SCROLL_OFFSET
import com.shkcodes.aurora.ui.profile.ProfileContract.Constants.tabIcons
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent
import com.shkcodes.aurora.ui.profile.ProfileContract.Intent.Init
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.OpenUrl
import com.shkcodes.aurora.ui.profile.ProfileContract.ProfileSideEffect.ScrollToBottom
import com.shkcodes.aurora.ui.profile.ProfileContract.Screen.UserProfile
import com.shkcodes.aurora.ui.profile.ProfileContract.State
import com.shkcodes.aurora.ui.profile.items.PagerTweetListItem
import com.shkcodes.aurora.ui.timeline.UrlMetadataHandler
import com.shkcodes.aurora.ui.tweetlist.TweetItems
import com.shkcodes.aurora.util.EmptyAdapterItem
import com.shkcodes.aurora.util.annotatedDescription
import com.shkcodes.aurora.util.annotatedLink
import com.shkcodes.aurora.util.applySharedAxisEnterTransition
import com.shkcodes.aurora.util.applySharedAxisExitTransition
import com.shkcodes.aurora.util.getDrawableCompat
import com.shkcodes.aurora.util.handleClickableSpans
import com.shkcodes.aurora.util.openUrl
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class ProfileFragment : BaseFragment<State, Intent>() {

    @Inject
    lateinit var imageLoader: ImageLoader
    private val pagerAdapter = ItemsViewAdapter()
    private val handler = ProfileTweetListHandler(this)
    private val urlMetadataHandler by lazy { UrlMetadataHandler(lifecycleScope, imageLoader) }
    private val args by navArgs<ProfileFragmentArgs>()

    override val viewModel by viewModels<ProfileViewModel>()

    override val binding by viewBinding(FragmentProfileBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySharedAxisEnterTransition()
    }

    override fun setupView() {
        binding.profilePager.adapter = pagerAdapter
        with(binding.root) {
            postponeEnterTransition()
            doOnPreDraw { startPostponedEnterTransition() }
        }
        dispatchIntent(Init(args.userHandle))
        with(binding.profileAppBar) {
            addOnOffsetChangedListener(OnOffsetChangedListener { _, verticalOffset ->
                with(binding) {
                    val offset = verticalOffset / totalScrollRange.toFloat()
                    val scrollOffset = offset * height
                    userInfoCard.translationY = scrollOffset * USER_INFO_BACKGROUND_SCROLL_OFFSET
                    name.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    description.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    handle.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    link.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    location.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    bannerImage.translationY = scrollOffset * BANNER_SCROLL_OFFSET
                    profileImage.translationY = scrollOffset * USER_INFO_SCROLL_OFFSET
                    profileImage.scaleX = maxOf(PROFILE_IMAGE_SCALE_LIMIT, 1 - abs(offset))
                    profileImage.scaleY = maxOf(PROFILE_IMAGE_SCALE_LIMIT, 1 - abs(offset))
                }
            })
        }
        TabLayoutMediator(binding.tabs, binding.profilePager) { tab, position ->
            tab.icon = requireContext().getDrawableCompat(tabIcons[position])
        }.attach()
    }

    override fun renderState(state: State) {
        with(binding) {
            setupDataStateAnimation()
            progressBar.isVisible = state.isLoading
            paginatedLoading.isVisible = state.isPaginatedLoading
            profileAppBar.isVisible = !state.isLoading
            profilePager.isVisible = !state.isLoading
            if (state.user != null) renderDataState(state)
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
            val items = listOf(
                tweetListItem(state.tweets, state.isPaginatedError),
                EmptyAdapterItem("Item 2"),
                EmptyAdapterItem("Item 3"),
                EmptyAdapterItem("Item 4")
            )
            pagerAdapter.replaceItems(items)
        }
    }

    private fun setupDataStateAnimation() {
        TransitionManager.beginDelayedTransition(
            binding.root, TransitionInflater.from(requireContext()).inflateTransition(
                R.transition.profile_data_state_enter
            )
        )
    }

    private fun tweetListItem(tweets: TweetItems, isPaginatedError: Boolean): PagerTweetListItem {
        return PagerTweetListItem(
            tweets,
            handler,
            urlMetadataHandler,
            imageLoader,
            isPaginatedError
        )
    }

    override fun handleAction(sideEffect: SideEffect.Action<*>) {
        when (val action = sideEffect.action) {
            is OpenUrl -> {
                requireContext().openUrl(action.url)
            }
            is ScrollToBottom -> {
                val items = listOf(
                    tweetListItem(action.tweets, true),
                    EmptyAdapterItem("Item 2"),
                    EmptyAdapterItem("Item 3"),
                    EmptyAdapterItem("Item 4")
                )
                handler.scrollToBottom = true
                pagerAdapter.replaceItems(items)
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
}
