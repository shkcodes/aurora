package com.shkcodes.aurora.ui.create

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shkcodes.aurora.base.BaseFragment
import com.shkcodes.aurora.base.SideEffect
import com.shkcodes.aurora.databinding.FragmentCreateTweetBinding
import com.shkcodes.aurora.ui.Screen.Previous
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.ContentChange
import com.shkcodes.aurora.ui.create.CreateTweetContract.Intent.PostTweet
import com.shkcodes.aurora.ui.create.CreateTweetContract.State
import com.shkcodes.aurora.util.observeTextChanges
import com.shkcodes.aurora.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateTweetFragment : BaseFragment<State, Intent>() {

    override val viewModel by viewModels<CreateTweetViewModel>()

    override val binding by viewBinding(FragmentCreateTweetBinding::inflate)

    override fun setupView() {
        with(binding) {
            tweetContent.observeTextChanges(viewLifecycleOwner.lifecycle) {
                viewModel.handleIntent(ContentChange(it))
            }
            tweetContent.requestFocus()
            postTweet.setOnClickListener { viewModel.handleIntent(PostTweet) }
        }
    }

    override fun renderState(state: State) {
        with(binding) {
            tweetContent.isVisible = !state.isLoading
            postTweet.isVisible = !state.isLoading
            progressBar.isVisible = state.isLoading
        }
    }

    override fun handleNavigation(sideEffect: SideEffect.DisplayScreen<*>) {
        when (sideEffect.screen) {
            is Previous -> findNavController().popBackStack()
        }
    }
}
